package de.bund.digitalservice.ris.caselaw.adapter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.fge.jsonpatch.JsonPatch;
import com.github.fge.jsonpatch.JsonPatchException;
import de.bund.digitalservice.ris.caselaw.adapter.mapper.DocumentUnitPatchMapper;
import de.bund.digitalservice.ris.caselaw.adapter.transformer.DocumentUnitTransformerException;
import de.bund.digitalservice.ris.caselaw.domain.AttachmentService;
import de.bund.digitalservice.ris.caselaw.domain.ConverterService;
import de.bund.digitalservice.ris.caselaw.domain.DocumentUnit;
import de.bund.digitalservice.ris.caselaw.domain.DocumentUnitPublishException;
import de.bund.digitalservice.ris.caselaw.domain.DocumentUnitService;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnitException;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnitListItem;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnitNotExistsException;
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
import java.util.List;
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
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/caselaw/documentunits")
@Slf4j
public class DocumentUnitController {
  private final DocumentUnitService service;
  private final UserService userService;
  private final AttachmentService attachmentService;
  private final ConverterService converterService;
  private final DocumentUnitPatchMapper documentUnitPatchMapper;

  public DocumentUnitController(
      DocumentUnitService service,
      UserService userService,
      AttachmentService attachmentService,
      ConverterService converterService,
      DocumentUnitPatchMapper documentUnitPatchMapper) {
    this.service = service;
    this.userService = userService;
    this.attachmentService = attachmentService;
    this.converterService = converterService;
    this.documentUnitPatchMapper = documentUnitPatchMapper;
  }

  @GetMapping(value = "new", produces = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<DocumentUnit> generateNewDocumentUnit(
      @AuthenticationPrincipal OidcUser oidcUser) {
    var docOffice = userService.getDocumentationOffice(oidcUser);
    try {
      var documentUnit = service.generateNewDocumentUnit(docOffice);
      return ResponseEntity.status(HttpStatus.CREATED).body(documentUnit);
    } catch (DocumentationUnitNotExistsException | DocumentationUnitException e) {
      log.error("error in generate new documentation unit", e);
      return ResponseEntity.internalServerError().body(DocumentUnit.builder().build());
    }
  }

  /**
   * Attach a content file (docx) to the documentation unit. This file is used to fill the
   * categories of the documentation unit.
   *
   * <p>Do a conversion into html and parse the footer for ECLI information.
   *
   * @param uuid UUID of the documentation unit
   * @param bytes bytes of the content file
   * @param httpHeaders http headers with the X-Filename information
   * @return the into html converted content of the file with some additional metadata (ECLI)
   */
  @PutMapping(
      value = "/{uuid}/file",
      produces = MediaType.APPLICATION_JSON_VALUE,
      consumes = "application/vnd.openxmlformats-officedocument.wordprocessingml.document")
  @PreAuthorize("@userHasWriteAccessByDocumentUnitUuid.apply(#uuid)")
  public ResponseEntity<Docx2Html> attachFileToDocumentUnit(
      @PathVariable UUID uuid, @RequestBody byte[] bytes, @RequestHeader HttpHeaders httpHeaders) {
    var docx2html =
        converterService.getConvertedObject(
            attachmentService
                .attachFileToDocumentationUnit(uuid, ByteBuffer.wrap(bytes), httpHeaders)
                .s3path());
    service.updateECLI(uuid, docx2html);
    if (docx2html == null) {
      return ResponseEntity.unprocessableEntity().build();
    }
    return ResponseEntity.status(HttpStatus.OK).body(docx2html);
  }

  @DeleteMapping(value = "/{uuid}/file/{s3Path}")
  @PreAuthorize("@userHasWriteAccessByDocumentUnitUuid.apply(#uuid)")
  public ResponseEntity<Object> removeAttachmentFromDocumentationUnit(
      @PathVariable UUID uuid, @PathVariable String s3Path) {

    try {
      attachmentService.deleteByS3Path(s3Path);
      return ResponseEntity.noContent().build();
    } catch (Exception e) {
      log.error("Error by deleting attachment '{}' for documentation unit {}", s3Path, uuid, e);
      return ResponseEntity.internalServerError().build();
    }
  }

  @GetMapping(value = "/search", produces = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("isAuthenticated()")
  // Access rights are being enforced through SQL filtering
  public Slice<DocumentationUnitListItem> searchByDocumentUnitListEntry(
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

    var documentationOffice = userService.getDocumentationOffice(oidcUser);
    return service.searchByDocumentationUnitSearchInput(
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
        myDocOfficeOnly);
  }

  @GetMapping(value = "/{documentNumber}", produces = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("@userHasReadAccessByDocumentNumber.apply(#documentNumber)")
  public ResponseEntity<DocumentUnit> getByDocumentNumber(
      @NonNull @PathVariable String documentNumber) {

    if (documentNumber.length() != 13 && documentNumber.length() != 14) {
      throw new DocumentationUnitException("Die Dokumentennummer unterstützt nur 13-14 Zeichen");
    }

    return ResponseEntity.ok(service.getByDocumentNumber(documentNumber));
  }

  @DeleteMapping(value = "/{uuid}")
  @PreAuthorize("@userHasWriteAccessByDocumentUnitUuid.apply(#uuid)")
  public ResponseEntity<String> deleteByUuid(@PathVariable UUID uuid) {

    try {
      var str = service.deleteByUuid(uuid);
      return ResponseEntity.status(HttpStatus.OK).body(str);
    } catch (DocumentationUnitNotExistsException ex) {
      return ResponseEntity.internalServerError().body(ex.getMessage());
    }
  }

  @PutMapping(
      value = "/{uuid}",
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("@userHasWriteAccessByDocumentUnitUuid.apply(#uuid)")
  public ResponseEntity<DocumentUnit> updateByUuid(
      @PathVariable UUID uuid,
      @Valid @RequestBody DocumentUnit documentUnit,
      @AuthenticationPrincipal OidcUser oidcUser) {

    if (!uuid.equals(documentUnit.uuid())) {
      return ResponseEntity.unprocessableEntity().body(DocumentUnit.builder().build());
    }
    try {
      var du = service.updateDocumentUnit(documentUnit);
      return ResponseEntity.status(HttpStatus.OK).body(du);
    } catch (DocumentationUnitNotExistsException
        | DocumentationUnitException
        | DocumentUnitTransformerException e) {
      log.error("Error by updating documentation unit '{}'", documentUnit.documentNumber(), e);
      return ResponseEntity.internalServerError().body(DocumentUnit.builder().build());
    }
  }

  @PatchMapping(
      value = "/{uuid}",
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("@userHasWriteAccessByDocumentUnitUuid.apply(#uuid)")
  public ResponseEntity<DocumentUnit> updateCustomer(
      @PathVariable UUID uuid, @RequestBody JsonPatch patch) {
    try {
      DocumentUnit documentUnit = service.getByUuid(uuid);
      DocumentUnit customerPatched =
          documentUnitPatchMapper.applyPatchToDocumentUnit(patch, documentUnit);
      return ResponseEntity.ok(customerPatched);
    } catch (JsonPatchException | JsonProcessingException e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }

  @PutMapping(value = "/{uuid}/publish", produces = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("@userHasWriteAccessByDocumentUnitUuid.apply(#uuid)")
  public ResponseEntity<Publication> publishDocumentUnitAsEmail(
      @PathVariable UUID uuid, @AuthenticationPrincipal OidcUser oidcUser) {

    try {
      var publication = service.publishAsEmail(uuid, userService.getEmail(oidcUser));
      return ResponseEntity.ok(publication);
    } catch (DocumentationUnitNotExistsException | DocumentUnitPublishException e) {
      log.error("Error by publishing the documentation unit '{}' as email", uuid, e);
      return ResponseEntity.internalServerError().build();
    }
  }

  @GetMapping(value = "/{uuid}/publish", produces = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("@userHasReadAccessByDocumentUnitUuid.apply(#uuid)")
  public List<PublicationHistoryRecord> getPublicationHistory(@PathVariable UUID uuid) {
    return service.getPublicationHistory(uuid);
  }

  @GetMapping(
      value = "/{uuid}/preview-publication-xml",
      produces = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("@userHasReadAccessByDocumentUnitUuid.apply(#uuid)")
  public XmlResultObject getPublicationPreview(@PathVariable UUID uuid) {
    try {
      return service.previewPublication(uuid);
    } catch (DocumentationUnitNotExistsException e) {
      return null;
    }
  }

  @PutMapping(
      value = "/{documentNumberToExclude}/search-linkable-documentation-units",
      produces = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("isAuthenticated()")
  public Slice<RelatedDocumentationUnit> searchLinkableDocumentationUnits(
      @PathVariable("documentNumberToExclude") String documentNumberToExclude,
      @RequestParam("pg") int page,
      @RequestParam("sz") int size,
      @RequestBody RelatedDocumentationUnit relatedDocumentationUnit,
      @AuthenticationPrincipal OidcUser oidcUser) {

    var documentationOffice = userService.getDocumentationOffice(oidcUser);
    return service.searchLinkableDocumentationUnits(
        relatedDocumentationUnit,
        documentationOffice,
        documentNumberToExclude,
        PageRequest.of(page, size));
  }

  @GetMapping(value = "/{uuid}/docx/{s3Path}", produces = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("@userHasReadAccessByDocumentUnitUuid.apply(#uuid)")
  public ResponseEntity<Docx2Html> getHtml(@PathVariable UUID uuid, @PathVariable String s3Path) {
    if (service.getByUuid(uuid) == null) {
      return ResponseEntity.notFound().build();
    }

    try {
      var docx2Html = converterService.getConvertedObject(s3Path);
      return ResponseEntity.ok()
          .cacheControl(CacheControl.maxAge(Duration.ofDays(1))) // Set cache duration
          .body(docx2Html);
    } catch (Exception ex) {
      log.error("Error by getting docx for documentation unit {}", uuid, ex);
      return ResponseEntity.internalServerError().build();
    }
  }

  @PostMapping(value = "/validateSingleNorm")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<String> validateSingleNorm(
      @RequestBody SingleNormValidationInfo singleNormValidationInfo) {
    try {
      return ResponseEntity.ok(service.validateSingleNorm(singleNormValidationInfo));
    } catch (Exception ex) {
      log.error(
          "Error by validation of single norm '{} - {}'",
          singleNormValidationInfo.normAbbreviation(),
          singleNormValidationInfo.singleNorm(),
          ex);
      return ResponseEntity.internalServerError().build();
    }
  }
}
