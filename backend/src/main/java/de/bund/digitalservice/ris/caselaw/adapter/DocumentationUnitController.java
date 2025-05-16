package de.bund.digitalservice.ris.caselaw.adapter;

import de.bund.digitalservice.ris.caselaw.adapter.transformer.DocumentationUnitTransformerException;
import de.bund.digitalservice.ris.caselaw.domain.AssignDocumentationOfficeRequest;
import de.bund.digitalservice.ris.caselaw.domain.AttachmentService;
import de.bund.digitalservice.ris.caselaw.domain.BulkAssignProcedureRequest;
import de.bund.digitalservice.ris.caselaw.domain.ConverterService;
import de.bund.digitalservice.ris.caselaw.domain.Documentable;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnit;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnitCreationParameters;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnitDocxMetadataInitializationService;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnitListItem;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnitService;
import de.bund.digitalservice.ris.caselaw.domain.DuplicateCheckService;
import de.bund.digitalservice.ris.caselaw.domain.DuplicateRelationStatusRequest;
import de.bund.digitalservice.ris.caselaw.domain.EventRecord;
import de.bund.digitalservice.ris.caselaw.domain.HandoverEntityType;
import de.bund.digitalservice.ris.caselaw.domain.HandoverException;
import de.bund.digitalservice.ris.caselaw.domain.HandoverMail;
import de.bund.digitalservice.ris.caselaw.domain.HandoverService;
import de.bund.digitalservice.ris.caselaw.domain.InboxStatus;
import de.bund.digitalservice.ris.caselaw.domain.RelatedDocumentationUnit;
import de.bund.digitalservice.ris.caselaw.domain.RisJsonPatch;
import de.bund.digitalservice.ris.caselaw.domain.SingleNormValidationInfo;
import de.bund.digitalservice.ris.caselaw.domain.User;
import de.bund.digitalservice.ris.caselaw.domain.UserService;
import de.bund.digitalservice.ris.caselaw.domain.XmlTransformationResult;
import de.bund.digitalservice.ris.caselaw.domain.docx.Docx2Html;
import de.bund.digitalservice.ris.caselaw.domain.exception.DocumentationUnitDeletionException;
import de.bund.digitalservice.ris.caselaw.domain.exception.DocumentationUnitException;
import de.bund.digitalservice.ris.caselaw.domain.exception.DocumentationUnitNotExistsException;
import de.bund.digitalservice.ris.domain.export.juris.response.StatusImporterException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.BadRequestException;
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
public class DocumentationUnitController {
  private final DocumentationUnitService service;
  private final UserService userService;
  private final AttachmentService attachmentService;
  private final ConverterService converterService;
  private final HandoverService handoverService;
  private final PortalPublicationService portalPublicationService;
  private final DocumentationUnitDocxMetadataInitializationService
      documentationUnitDocxMetadataInitializationService;
  private final DuplicateCheckService duplicateCheckService;

  public DocumentationUnitController(
      DocumentationUnitService service,
      UserService userService,
      AttachmentService attachmentService,
      ConverterService converterService,
      HandoverService handoverService,
      PortalPublicationService portalPublicationService,
      DocumentationUnitDocxMetadataInitializationService
          documentationUnitDocxMetadataInitializationService,
      DuplicateCheckService duplicateCheckService) {
    this.service = service;
    this.userService = userService;
    this.attachmentService = attachmentService;
    this.converterService = converterService;
    this.handoverService = handoverService;
    this.portalPublicationService = portalPublicationService;
    this.documentationUnitDocxMetadataInitializationService =
        documentationUnitDocxMetadataInitializationService;
    this.duplicateCheckService = duplicateCheckService;
  }

  /**
   * Generate a new documentation unit with optional parameters.
   *
   * @param oidcUser the logged-in user
   * @param parameters the parameters for the new documentation unit (optional)
   * @return the new documentation unit or an empty response with status code 500 if the creation
   *     failed
   */
  @PutMapping(value = "new", produces = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("isAuthenticated() and @userIsInternal.apply(#oidcUser)")
  public ResponseEntity<DocumentationUnit> generateNewDocumentationUnit(
      @AuthenticationPrincipal OidcUser oidcUser,
      @RequestBody(required = false) Optional<DocumentationUnitCreationParameters> parameters) {
    try {
      var documentationUnit =
          service.generateNewDocumentationUnit(userService.getUser(oidcUser), parameters);
      return ResponseEntity.status(HttpStatus.CREATED).body(documentationUnit);
    } catch (DocumentationUnitException e) {
      log.error("error in generate new documentation unit", e);
      return ResponseEntity.internalServerError().body(DocumentationUnit.builder().build());
    }
  }

  @PutMapping(value = "/{documentNumber}/takeover", produces = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize(
      "@userIsInternal.apply(#oidcUser) and @userHasSameDocOfficeAsDocument.apply(#documentNumber)")
  public ResponseEntity<DocumentationUnitListItem> takeOverDocumentationUnit(
      @AuthenticationPrincipal OidcUser oidcUser, @PathVariable String documentNumber) {
    try {
      var updatedDocumentationUnit = service.takeOverDocumentationUnit(documentNumber, oidcUser);

      return ResponseEntity.ok(updatedDocumentationUnit);
    } catch (Exception e) {
      throw new StatusImporterException("Could not update publicationStatus", e);
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
  @PreAuthorize("@userIsInternal.apply(#oidcUser) and @userHasWriteAccess.apply(#uuid)")
  public ResponseEntity<Docx2Html> attachFileToDocumentationUnit(
      @AuthenticationPrincipal OidcUser oidcUser,
      @PathVariable UUID uuid,
      @RequestBody byte[] bytes,
      @RequestHeader HttpHeaders httpHeaders) {

    var attachmentPath =
        attachmentService
            .attachFileToDocumentationUnit(
                uuid, ByteBuffer.wrap(bytes), httpHeaders, userService.getUser(oidcUser))
            .s3path();
    try {
      var docx2html = converterService.getConvertedObject(attachmentPath);
      initializeCoreDataAndCheckDuplicates(uuid, docx2html, userService.getUser(oidcUser));
      return ResponseEntity.status(HttpStatus.OK).body(docx2html);

    } catch (Exception e) {
      attachmentService.deleteByS3Path(attachmentPath, uuid, userService.getUser(oidcUser));
      return ResponseEntity.unprocessableEntity().build();
    }
  }

  private void initializeCoreDataAndCheckDuplicates(UUID uuid, Docx2Html docx2html, User user) {
    try {
      Documentable documentable = service.getByUuid(uuid);
      if (documentable instanceof DocumentationUnit docUnit) {
        documentationUnitDocxMetadataInitializationService.initializeCoreData(
            docUnit, docx2html, user);
        checkDuplicates(docUnit.documentNumber());
      } else {
        log.info("Documentable type not supported: {}", documentable.getClass().getName());
      }
    } catch (DocumentationUnitNotExistsException ex) {
      // file upload should not fail because of core data initialization or dup check
      log.error(
          "Initialize core data failed, because documentation unit '{}' doesn't exist!", uuid);
    }
  }

  private void checkDuplicates(String documentNumber) {
    try {
      duplicateCheckService.checkDuplicates(documentNumber);
    } catch (Exception e) {
      // Error in duplicate check should not affect program flow, logging in service
    }
  }

  @DeleteMapping(value = "/{uuid}/file/{s3Path}")
  @PreAuthorize("@userIsInternal.apply(#oidcUser) and @userHasWriteAccess.apply(#uuid)")
  public ResponseEntity<Object> removeAttachmentFromDocumentationUnit(
      @AuthenticationPrincipal OidcUser oidcUser,
      @PathVariable UUID uuid,
      @PathVariable String s3Path) {

    try {
      attachmentService.deleteByS3Path(s3Path, uuid, userService.getUser(oidcUser));
      return ResponseEntity.noContent().build();
    } catch (Exception e) {
      log.error("Error by deleting attachment '{}' for documentation unit {}", s3Path, uuid, e);
      return ResponseEntity.internalServerError().build();
    }
  }

  @GetMapping(value = "/search", produces = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("isAuthenticated()")
  // Access rights are being enforced through SQL filtering
  public Slice<DocumentationUnitListItem> searchByDocumentationUnitListEntry(
      @RequestParam("pg") int page,
      @RequestParam("sz") int size,
      @RequestParam(value = "documentNumber") Optional<String> documentNumber,
      @RequestParam(value = "fileNumber") Optional<String> fileNumber,
      @RequestParam(value = "courtType") Optional<String> courtType,
      @RequestParam(value = "courtLocation") Optional<String> courtLocation,
      @RequestParam(value = "decisionDate") Optional<LocalDate> decisionDate,
      @RequestParam(value = "decisionDateEnd") Optional<LocalDate> decisionDateEnd,
      @RequestParam(value = "publicationDate") Optional<LocalDate> publicationDate,
      @RequestParam(value = "scheduledOnly") Optional<Boolean> scheduledOnly,
      @RequestParam(value = "publicationStatus") Optional<String> publicationStatus,
      @RequestParam(value = "withError") Optional<Boolean> withError,
      @RequestParam(value = "myDocOfficeOnly") Optional<Boolean> myDocOfficeOnly,
      @RequestParam(value = "withDuplicateWarning") Optional<Boolean> withDuplicateWarning,
      @RequestParam(value = "inboxStatus") Optional<InboxStatus> inboxStatus,
      @AuthenticationPrincipal OidcUser oidcUser) {

    return service.searchByDocumentationUnitSearchInput(
        PageRequest.of(page, size),
        oidcUser,
        documentNumber,
        fileNumber,
        courtType,
        courtLocation,
        decisionDate,
        decisionDateEnd,
        publicationDate,
        scheduledOnly,
        publicationStatus,
        withError,
        myDocOfficeOnly,
        withDuplicateWarning,
        inboxStatus);
  }

  @GetMapping(value = "/{documentNumber}", produces = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("@userHasReadAccessByDocumentNumber.apply(#documentNumber)")
  public ResponseEntity<DocumentationUnit> getByDocumentNumber(
      @AuthenticationPrincipal OidcUser oidcUser, @NonNull @PathVariable String documentNumber) {

    if (documentNumber.length() != 13 && documentNumber.length() != 14) {
      throw new DocumentationUnitException("Die Dokumentennummer unterstützt nur 13-14 Zeichen");
    }

    try {
      // Duplicate check must happen before getting the doc unit, otherwise new ones won't be shown
      checkDuplicates(documentNumber);
      Documentable documentable = service.getByDocumentNumberWithUser(documentNumber, oidcUser);
      return ResponseEntity.ok((DocumentationUnit) documentable);

    } catch (DocumentationUnitNotExistsException e) {
      log.error("Documentation unit '{}' doesn't exist", documentNumber);
      return ResponseEntity.ok(DocumentationUnit.builder().build());
    }
  }

  @DeleteMapping(value = "/{uuid}")
  @PreAuthorize("@userIsInternal.apply(#oidcUser) and @userHasWriteAccess.apply(#uuid)")
  public ResponseEntity<String> deleteByUuid(
      @AuthenticationPrincipal OidcUser oidcUser, @PathVariable UUID uuid) {

    try {
      var str = service.deleteByUuid(uuid);
      return ResponseEntity.status(HttpStatus.OK).body(str);
    } catch (DocumentationUnitNotExistsException | DocumentationUnitDeletionException ex) {
      return ResponseEntity.internalServerError().body(ex.getMessage());
    }
  }

  @PutMapping(
      value = "/{uuid}",
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("@userHasWriteAccess.apply(#uuid)")
  public ResponseEntity<DocumentationUnit> updateByUuid(
      @PathVariable UUID uuid,
      @Valid @RequestBody DocumentationUnit documentationUnit,
      @AuthenticationPrincipal OidcUser oidcUser) {

    if (!uuid.equals(documentationUnit.uuid())) {
      return ResponseEntity.unprocessableEntity().body(DocumentationUnit.builder().build());
    }
    try {
      var du = service.updateDocumentationUnit(documentationUnit);
      return ResponseEntity.status(HttpStatus.OK).body(du);
    } catch (DocumentationUnitNotExistsException
        | DocumentationUnitException
        | DocumentationUnitTransformerException e) {
      log.error("Error by updating documentation unit '{}'", documentationUnit.documentNumber(), e);
      return ResponseEntity.internalServerError().body(DocumentationUnit.builder().build());
    }
  }

  /**
   * Update a documentation unit with a {@link com.gravity9.jsonpatch.JsonPatch} object.
   *
   * @param uuid id of the documentation unit
   * @param patch patch with the change operations
   * @return updated and saved documentation unit
   */
  @PatchMapping(
      value = "/{uuid}",
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize(
      "@userHasWriteAccess.apply(#uuid) and (@userIsInternal.apply(#oidcUser) or (@isAssignedViaProcedure.apply(#uuid) and @isPatchAllowedForExternalUsers.apply(#patch)))")
  public ResponseEntity<RisJsonPatch> partialUpdateByUuid(
      @AuthenticationPrincipal OidcUser oidcUser,
      @PathVariable UUID uuid,
      @RequestBody RisJsonPatch patch) {

    String documentNumber = "unknown";

    try {
      if (patch == null) {
        return ResponseEntity.internalServerError().build();
      }

      var documentationUnit = service.getByUuid(uuid);
      if (documentationUnit != null) {
        documentNumber = documentationUnit.documentNumber();
      }
      User user = userService.getUser(oidcUser);
      var newPatch = service.updateDocumentationUnit(uuid, patch, user);

      return ResponseEntity.ok().body(newPatch);
    } catch (DocumentationUnitNotExistsException e) {
      return ResponseEntity.internalServerError().build();
    } catch (Exception e) {
      log.error("Error by updating documentation unit '{}/{}'", uuid, documentNumber, e);
      return ResponseEntity.internalServerError().build();
    }
  }

  /**
   * Hands over the documentation unit to jDV as XML via email.
   *
   * @param uuid UUID of the documentation unit
   * @param oidcUser the logged-in user, used to forward the response email
   * @return the email sent containing the XML or an empty response with status code 400 * if the
   *     user is not authorized
   */
  @PutMapping(value = "/{uuid}/handover", produces = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("@userHasWriteAccess.apply(#uuid)")
  public ResponseEntity<HandoverMail> handoverDocumentationUnitAsMail(
      @PathVariable UUID uuid, @AuthenticationPrincipal OidcUser oidcUser) {

    try {
      HandoverMail handoverMail =
          handoverService.handoverDocumentationUnitAsMail(
              uuid, userService.getEmail(oidcUser), userService.getUser(oidcUser));
      if (handoverMail == null || !handoverMail.isSuccess()) {
        log.warn("Failed to send mail for documentation unit {}", uuid);
        return ResponseEntity.unprocessableEntity().body(handoverMail);
      }
      service.setPublicationDateTime(uuid);
      return ResponseEntity.ok(handoverMail);
    } catch (DocumentationUnitNotExistsException | HandoverException e) {
      log.error("Error handing over documentation unit '{}' as email", uuid, e);
      return ResponseEntity.internalServerError().build();
    }
  }

  /**
   * Get all events of a documentation unit (can be handover events, received handover reports,
   * import/migration events)
   *
   * @param uuid UUID of the documentation unit
   * @return ordered list of event records (newest first) or an empty response with status code 400
   *     if the user is not authorized
   */
  @GetMapping(value = "/{uuid}/handover", produces = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("@userHasWriteAccess.apply(#uuid)")
  public List<EventRecord> getEventLog(@PathVariable UUID uuid) {
    return handoverService.getEventLog(uuid, HandoverEntityType.DOCUMENTATION_UNIT);
  }

  /**
   * Get the XML preview of a documentation unit.
   *
   * @param uuid UUID of the documentation unit
   * @return the XML preview or an empty response with status code 400 if the user is not authorized
   *     or an empty response if the documentation unit does not exist
   */
  @GetMapping(value = "/{uuid}/preview-xml", produces = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("@userHasReadAccessByDocumentationUnitId.apply(#uuid)")
  public XmlTransformationResult getXmlPreview(@PathVariable UUID uuid) {
    try {
      return handoverService.createPreviewXml(uuid);
    } catch (DocumentationUnitNotExistsException e) {
      return null;
    }
  }

  @PutMapping(
      value = "/search-linkable-documentation-units",
      produces = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("isAuthenticated()")
  public Slice<RelatedDocumentationUnit> searchLinkableDocumentationUnits(
      @RequestParam("pg") int page,
      @RequestParam("sz") int size,
      @RequestParam(value = "documentNumber") Optional<String> documentNumberToExclude,
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
  @PreAuthorize("@userHasReadAccessByDocumentationUnitId.apply(#uuid)")
  public ResponseEntity<Docx2Html> getHtml(@PathVariable UUID uuid, @PathVariable String s3Path) {

    try {
      service.getByUuid(uuid);
    } catch (DocumentationUnitNotExistsException ex) {
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

  /**
   * Transforms the documentation unit to LDML and hands it over to the portal to be published.
   *
   * @param uuid UUID of the documentation unit
   */
  @PutMapping(value = "/{uuid}/publish")
  @PreAuthorize("@userHasWriteAccess.apply(#uuid)")
  public ResponseEntity<Void> publishDocumentationUnit(@PathVariable UUID uuid) {

    try {
      portalPublicationService.publishDocumentationUnitWithChangelog(uuid);
      return ResponseEntity.ok().build();
    } catch (DocumentationUnitNotExistsException e) {
      log.error("Error handing over documentation unit '{}' to portal", uuid, e);
      return ResponseEntity.internalServerError().build();
    }
  }

  /**
   * Assigns a procedure to a list of documentation units. Also removes the documentation units from
   * the inbox (e.g. EU inbox or Fremdanlage inbox)
   */
  @PatchMapping(value = "/bulk-assign-procedure")
  @PreAuthorize("@userHasBulkWriteAccess.apply(#body.getDocumentationUnitIds())")
  public ResponseEntity<Void> bulkAssignProcedure(
      @AuthenticationPrincipal OidcUser oidcUser,
      @RequestBody @Valid BulkAssignProcedureRequest body) {
    try {
      User user = userService.getUser(oidcUser);
      service.bulkAssignProcedure(body.getDocumentationUnitIds(), body.getProcedureLabel(), user);
      return ResponseEntity.ok().build();
    } catch (DocumentationUnitNotExistsException e) {
      return ResponseEntity.notFound().build();
    } catch (BadRequestException e) {
      return ResponseEntity.badRequest().build();
    }
  }

  /**
   * Update the duplicate status of a duplicate of a documentation unit (ignored vs. pending)
   *
   * @param documentNumberOrigin documentNumber of the original documentation unit
   * @param documentNumberDuplicate documentNumber of the duplicate
   * @return a String response or empty response with status code 4xx if invalid auth or input
   */
  @PutMapping(
      value = "/{documentNumberOrigin}/duplicate-status/{documentNumberDuplicate}",
      produces = MediaType.APPLICATION_JSON_VALUE,
      consumes = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize(
      "@userIsInternal.apply(#oidcUser) and @userHasSameDocOfficeAsDocument.apply(#documentNumberOrigin)")
  public ResponseEntity<String> updateDuplicateStatus(
      @AuthenticationPrincipal OidcUser oidcUser,
      @PathVariable String documentNumberOrigin,
      @PathVariable String documentNumberDuplicate,
      @RequestBody @Valid DuplicateRelationStatusRequest duplicateRelationStatusRequest) {
    try {
      var result =
          duplicateCheckService.updateDuplicateStatus(
              documentNumberOrigin,
              documentNumberDuplicate,
              duplicateRelationStatusRequest.getStatus());
      return ResponseEntity.status(HttpStatus.OK).body(result);
    } catch (DocumentationUnitNotExistsException | EntityNotFoundException ex) {
      return ResponseEntity.notFound().build();
    }
  }

  /**
   * Assign an existing document to another documentation office
   *
   * @param oidcUser the logged-in user
   * @param uuid UUID of the documentation unit
   * @param assignDocumentationOfficeRequest Request containing documentation Office to assign to
   * @return HTTP 200 with result string for success, 404/400 for client errors, 500 for server
   *     errors.
   */
  @PutMapping(value = "/{uuid}/assign")
  @PreAuthorize(
      "isAuthenticated() and @userIsInternal.apply(#oidcUser) and @userHasWriteAccess.apply(#uuid)")
  public ResponseEntity<String> assignDocumentationOffice(
      @AuthenticationPrincipal OidcUser oidcUser,
      @PathVariable @NonNull UUID uuid,
      @RequestBody @Valid AssignDocumentationOfficeRequest assignDocumentationOfficeRequest) {
    try {
      var result =
          service.assignDocumentationOffice(
              uuid,
              assignDocumentationOfficeRequest.getDocumentationOffice(),
              userService.getUser(oidcUser));
      return ResponseEntity.ok().body(result);
    } catch (DocumentationUnitNotExistsException e) {
      log.warn("Documentation unit not found: {}", uuid, e);
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Documentation unit not found");
    } catch (DocumentationUnitException e) {
      log.error(
          "Error assigning documentation office {} to {}",
          assignDocumentationOfficeRequest.getDocumentationOffice().abbreviation(),
          uuid,
          e);
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
    }
  }
}
