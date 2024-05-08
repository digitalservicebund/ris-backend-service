package de.bund.digitalservice.ris.caselaw.adapter;

import de.bund.digitalservice.ris.caselaw.domain.AttachmentService;
import de.bund.digitalservice.ris.caselaw.domain.ConverterService;
import de.bund.digitalservice.ris.caselaw.domain.DocumentUnit;
import de.bund.digitalservice.ris.caselaw.domain.DocumentUnitService;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnitException;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnitListItem;
import de.bund.digitalservice.ris.caselaw.domain.Publication;
import de.bund.digitalservice.ris.caselaw.domain.PublicationHistoryRecord;
import de.bund.digitalservice.ris.caselaw.domain.RelatedDocumentationUnit;
import de.bund.digitalservice.ris.caselaw.domain.SingleNormValidationInfo;
import de.bund.digitalservice.ris.caselaw.domain.UserService;
import de.bund.digitalservice.ris.caselaw.domain.XmlResultObject;
import de.bund.digitalservice.ris.caselaw.domain.docx.Docx2Html;
import jakarta.validation.Valid;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("api/v1/caselaw/documentunits")
@Slf4j
public class DocumentUnitController {
  private final DocumentUnitService service;
  private final UserService userService;
  private final AttachmentService attachmentService;
  private final ConverterService converterService;

  public DocumentUnitController(
      DocumentUnitService service,
      UserService userService,
      AttachmentService attachmentService,
      ConverterService converterService) {
    this.service = service;
    this.userService = userService;
    this.attachmentService = attachmentService;
    this.converterService = converterService;
  }

  @GetMapping(value = "new")
  @PreAuthorize("isAuthenticated()")
  public Mono<ResponseEntity<DocumentUnit>> generateNewDocumentUnit(
      @AuthenticationPrincipal OidcUser oidcUser) {

    return userService
        .getDocumentationOffice(oidcUser)
        .flatMap(service::generateNewDocumentUnit)
        .map(documentUnit -> ResponseEntity.status(HttpStatus.CREATED).body(documentUnit))
        .doOnError(ex -> log.error("error in generate new documentation unit", ex))
        .onErrorReturn(ResponseEntity.internalServerError().body(DocumentUnit.builder().build()));
  }

  /**
   * Attach a content file (docx) to the documentation unit. This file is used to fill the
   * categories of the documentation unit.
   *
   * <p>Do a conversion into html and parse the footer for ECLI information.
   *
   * @param uuid UUID of the documentation unit
   * @param byteBuffer bytes of the content file
   * @param httpHeaders http headers with the X-Filename information
   * @return the into html converted content of the file with some additional metadata (ECLI)
   */
  @PutMapping(value = "/{uuid}/file")
  @PreAuthorize("@userHasWriteAccessByDocumentUnitUuid.apply(#uuid)")
  public Mono<ResponseEntity<Docx2Html>> attachFileToDocumentUnit(
      @PathVariable UUID uuid,
      @RequestBody ByteBuffer byteBuffer,
      @RequestHeader HttpHeaders httpHeaders) {

    return Mono.just(
            converterService.getConvertedObject(
                attachmentService
                    .attachFileToDocumentationUnit(uuid, byteBuffer, httpHeaders)
                    .s3path()))
        .doOnNext(docx2html -> service.updateECLI(uuid, docx2html))
        .map(docx2Html -> ResponseEntity.status(HttpStatus.OK).body(docx2Html))
        .onErrorReturn(ResponseEntity.unprocessableEntity().build());
  }

  @DeleteMapping(value = "/{uuid}/file/{s3Path}")
  @PreAuthorize("@userHasWriteAccessByDocumentUnitUuid.apply(#uuid)")
  public Mono<ResponseEntity<Object>> removeAttachmentFromDocumentationUnit(
      @PathVariable UUID uuid, @PathVariable String s3Path) {

    try {
      attachmentService.deleteByS3Path(s3Path);
      return Mono.just(ResponseEntity.noContent().build());
    } catch (Exception e) {
      return Mono.error(e);
    }
  }

  @GetMapping(value = "/search")
  @PreAuthorize("isAuthenticated()")
  // Access rights are being enforced through SQL filtering
  public Mono<Slice<DocumentationUnitListItem>> searchByDocumentUnitListEntry(
      @RequestParam("pg") int page,
      @RequestParam("sz") int size,
      @RequestParam(value = "documentNumber") Optional<String> documentNumber,
      @RequestParam(value = "fileNumber") Optional<String> fileNumber,
      @RequestParam(value = "courtType") Optional<String> courtType,
      @RequestParam(value = "courtLocation") Optional<String> courtLocation,
      @RequestParam(value = "decisionDate") Optional<LocalDate> decisionDate,
      @RequestParam(value = "decisionDateEnd") Optional<LocalDate> decisionDateEnd,
      @RequestParam(value = "publicationStatus") Optional<String> publicationStatus,
      @RequestParam(value = "withError") Optional<Boolean> withError,
      @RequestParam(value = "myDocOfficeOnly") Optional<Boolean> myDocOfficeOnly,
      @AuthenticationPrincipal OidcUser oidcUser) {

    return userService
        .getDocumentationOffice(oidcUser)
        .flatMap(
            documentationOffice ->
                Mono.just(
                    service.searchByDocumentationUnitSearchInput(
                        PageRequest.of(page, size),
                        documentationOffice,
                        documentNumber,
                        fileNumber,
                        courtType,
                        courtLocation,
                        decisionDate,
                        decisionDateEnd,
                        publicationStatus,
                        withError,
                        myDocOfficeOnly)));
  }

  @GetMapping(value = "/{documentNumber}")
  @PreAuthorize("@userHasReadAccessByDocumentNumber.apply(#documentNumber)")
  public Mono<ResponseEntity<DocumentUnit>> getByDocumentNumber(
      @NonNull @PathVariable String documentNumber) {

    if (documentNumber.length() != 13 && documentNumber.length() != 14) {
      return Mono.error(
          new DocumentationUnitException("Die Dokumentennummer unterstützt nur 13-14 Zeichen"));
    }

    return service.getByDocumentNumber(documentNumber).map(ResponseEntity::ok);
  }

  @DeleteMapping(value = "/{uuid}")
  @PreAuthorize("@userHasWriteAccessByDocumentUnitUuid.apply(#uuid)")
  public Mono<ResponseEntity<String>> deleteByUuid(@PathVariable UUID uuid) {

    return service
        .deleteByUuid(uuid)
        .map(str -> ResponseEntity.status(HttpStatus.OK).body(str))
        .onErrorResume(ex -> Mono.just(ResponseEntity.internalServerError().body(ex.getMessage())));
  }

  @PutMapping(value = "/{uuid}", consumes = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("@userHasWriteAccessByDocumentUnitUuid.apply(#uuid)")
  public Mono<ResponseEntity<DocumentUnit>> updateByUuid(
      @PathVariable UUID uuid,
      @Valid @RequestBody DocumentUnit documentUnit,
      @AuthenticationPrincipal OidcUser oidcUser) {

    if (!uuid.equals(documentUnit.uuid())) {
      return Mono.just(ResponseEntity.unprocessableEntity().body(DocumentUnit.builder().build()));
    }

    return service
        .updateDocumentUnit(documentUnit)
        .map(du -> ResponseEntity.status(HttpStatus.OK).body(du))
        .onErrorReturn(ResponseEntity.internalServerError().body(DocumentUnit.builder().build()));
  }

  @PutMapping(value = "/{uuid}/publish", produces = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("@userHasWriteAccessByDocumentUnitUuid.apply(#uuid)")
  public Mono<ResponseEntity<Publication>> publishDocumentUnitAsEmail(
      @PathVariable UUID uuid, @AuthenticationPrincipal OidcUser oidcUser) {

    return service
        .publishAsEmail(uuid, userService.getEmail(oidcUser))
        .map(ResponseEntity::ok)
        .doOnError(ex -> ResponseEntity.internalServerError().build());
  }

  @GetMapping(value = "/{uuid}/publish", produces = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("@userHasReadAccessByDocumentUnitUuid.apply(#uuid)")
  public Flux<PublicationHistoryRecord> getPublicationHistory(@PathVariable UUID uuid) {
    return service.getPublicationHistory(uuid);
  }

  @GetMapping(
      value = "/{uuid}/preview-publication-xml",
      produces = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("@userHasReadAccessByDocumentUnitUuid.apply(#uuid)")
  public Mono<XmlResultObject> getPublicationPreview(@PathVariable UUID uuid) {
    return service.previewPublication(uuid);
  }

  @PutMapping(value = "/{documentNumberToExclude}/search-linkable-documentation-units")
  @PreAuthorize("isAuthenticated()")
  public Mono<Slice<RelatedDocumentationUnit>> searchLinkableDocumentationUnits(
      @PathVariable("documentNumberToExclude") String documentNumberToExclude,
      @RequestParam("pg") int page,
      @RequestParam("sz") int size,
      @RequestBody RelatedDocumentationUnit relatedDocumentationUnit,
      @AuthenticationPrincipal OidcUser oidcUser) {

    return userService
        .getDocumentationOffice(oidcUser)
        .flatMap(
            documentationOffice ->
                Mono.just(
                    service.searchLinkableDocumentationUnits(
                        relatedDocumentationUnit,
                        documentationOffice,
                        documentNumberToExclude,
                        PageRequest.of(page, size))));
  }

  @GetMapping(value = "/{uuid}/docx/{s3Path}", produces = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("@userHasReadAccessByDocumentUnitUuid.apply(#uuid)")
  public Mono<ResponseEntity<Docx2Html>> getHtml(
      @PathVariable UUID uuid, @PathVariable String s3Path) {

    return service
        .getByUuid(uuid)
        .flatMap(documentUnit -> Mono.justOrEmpty(converterService.getConvertedObject(s3Path)))
        .map(
            docx2Html ->
                ResponseEntity.ok()
                    .cacheControl(CacheControl.maxAge(Duration.ofDays(1))) // Set cache duration
                    .body(docx2Html))
        .onErrorReturn(ResponseEntity.internalServerError().build());
  }

  @PostMapping(value = "/validateSingleNorm")
  @PreAuthorize("isAuthenticated()")
  public Mono<ResponseEntity<String>> validateSingleNorm(
      @RequestBody SingleNormValidationInfo singleNormValidationInfo) {
    return service
        .validateSingleNorm(singleNormValidationInfo)
        .map(ResponseEntity::ok)
        .onErrorReturn(ResponseEntity.internalServerError().build());
  }
}
