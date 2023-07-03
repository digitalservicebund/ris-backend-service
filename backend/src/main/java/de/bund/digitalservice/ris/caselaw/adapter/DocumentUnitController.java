package de.bund.digitalservice.ris.caselaw.adapter;

import de.bund.digitalservice.ris.caselaw.domain.DocumentUnit;
import de.bund.digitalservice.ris.caselaw.domain.DocumentUnitListEntry;
import de.bund.digitalservice.ris.caselaw.domain.DocumentUnitService;
import de.bund.digitalservice.ris.caselaw.domain.LinkedDocumentationUnit;
import de.bund.digitalservice.ris.caselaw.domain.MailResponse;
import de.bund.digitalservice.ris.caselaw.domain.PublicationEntry;
import de.bund.digitalservice.ris.caselaw.domain.SingleNormValidationInfo;
import de.bund.digitalservice.ris.caselaw.domain.UserService;
import de.bund.digitalservice.ris.caselaw.domain.docx.Docx2Html;
import jakarta.validation.Valid;
import java.nio.ByteBuffer;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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
  private final DocxConverterService docxConverterService;

  public DocumentUnitController(
      DocumentUnitService service,
      UserService userService,
      DocxConverterService docxConverterService) {
    this.service = service;
    this.userService = userService;
    this.docxConverterService = docxConverterService;
  }

  @GetMapping(value = "new")
  @PreAuthorize("isAuthenticated()")
  public Mono<ResponseEntity<DocumentUnit>> generateNewDocumentUnit(
      @AuthenticationPrincipal OidcUser oidcUser) {

    return userService
        .getDocumentationOffice(oidcUser)
        .flatMap(service::generateNewDocumentUnit)
        .map(documentUnit -> ResponseEntity.status(HttpStatus.CREATED).body(documentUnit))
        .onErrorReturn(ResponseEntity.internalServerError().body(DocumentUnit.builder().build()));
  }

  @PutMapping(value = "/{uuid}/file")
  @PreAuthorize("@userHasWriteAccessByDocumentUnitUuid.apply(#uuid)")
  public Mono<ResponseEntity<DocumentUnit>> attachFileToDocumentUnit(
      @PathVariable UUID uuid,
      @RequestBody ByteBuffer byteBuffer,
      @RequestHeader HttpHeaders httpHeaders) {

    return service
        .attachFileToDocumentUnit(uuid, byteBuffer, httpHeaders)
        .map(documentUnit -> ResponseEntity.status(HttpStatus.CREATED).body(documentUnit))
        .onErrorReturn(ResponseEntity.internalServerError().body(DocumentUnit.builder().build()));
  }

  @DeleteMapping(value = "/{uuid}/file")
  @PreAuthorize("@userHasWriteAccessByDocumentUnitUuid.apply(#uuid)")
  public Mono<ResponseEntity<DocumentUnit>> removeFileFromDocumentUnit(@PathVariable UUID uuid) {

    return service
        .removeFileFromDocumentUnit(uuid)
        .map(documentUnit -> ResponseEntity.status(HttpStatus.OK).body(documentUnit))
        .onErrorReturn(ResponseEntity.internalServerError().body(DocumentUnit.builder().build()));
  }

  @GetMapping(value = "")
  @PreAuthorize("isAuthenticated()")
  // Access rights are being enforced through SQL filtering
  public Mono<Page<DocumentUnitListEntry>> getAll(
      @RequestParam("pg") int page,
      @RequestParam("sz") int size,
      @AuthenticationPrincipal OidcUser oidcUser) {

    return userService
        .getDocumentationOffice(oidcUser)
        .flatMap(user -> service.getAll(PageRequest.of(page, size), user));
  }

  @GetMapping(value = "/{documentNumber}")
  @PreAuthorize("@userHasReadAccessByDocumentNumber.apply(#documentNumber)")
  public Mono<ResponseEntity<DocumentUnit>> getByDocumentNumber(
      @NonNull @PathVariable String documentNumber) {

    if (documentNumber.length() != 13 && documentNumber.length() != 14) {
      return Mono.just(ResponseEntity.unprocessableEntity().body(DocumentUnit.builder().build()));
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

    return userService
        .getDocumentationOffice(oidcUser)
        .flatMap(
            documentationOffice -> service.updateDocumentUnit(documentUnit, documentationOffice))
        .map(du -> ResponseEntity.status(HttpStatus.OK).body(du))
        .onErrorReturn(ResponseEntity.internalServerError().body(DocumentUnit.builder().build()));
  }

  @PutMapping(value = "/{uuid}/publish", produces = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("@userHasWriteAccessByDocumentUnitUuid.apply(#uuid)")
  public Mono<ResponseEntity<MailResponse>> publishDocumentUnitAsEmail(
      @PathVariable UUID uuid, @AuthenticationPrincipal OidcUser oidcUser) {

    return service
        .publishAsEmail(uuid, userService.getEmail(oidcUser))
        .map(ResponseEntity::ok)
        .doOnError(ex -> ResponseEntity.internalServerError().build());
  }

  @GetMapping(value = "/{uuid}/publish", produces = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("@userHasReadAccessByDocumentUnitUuid.apply(#uuid)")
  public Flux<PublicationEntry> getPublicationHistory(@PathVariable UUID uuid) {
    return service.getPublicationLog(uuid);
  }

  @PutMapping(value = "/search")
  @PreAuthorize("isAuthenticated()")
  public Mono<Page<LinkedDocumentationUnit>> searchByLinkedDocumentationUnit(
      @RequestParam("pg") int page,
      @RequestParam("sz") int size,
      @RequestBody LinkedDocumentationUnit linkedDocumentationUnit) {

    return service.searchByLinkedDocumentationUnit(
        linkedDocumentationUnit, PageRequest.of(page, size));
  }

  @GetMapping(value = "/{uuid}/docx", produces = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("@userHasReadAccessByDocumentUnitUuid.apply(#uuid)")
  public Mono<ResponseEntity<Docx2Html>> html(@PathVariable UUID uuid) {
    return service
        .getByUuid(uuid)
        .map(DocumentUnit::s3path)
        .flatMap(docxConverterService::getConvertedObject)
        .map(ResponseEntity::ok)
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
