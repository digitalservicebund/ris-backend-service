package de.bund.digitalservice.ris.caselaw.adapter;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.AttachmentRepository;
import de.bund.digitalservice.ris.caselaw.adapter.eurlex.EurLexSOAPSearchService;
import de.bund.digitalservice.ris.caselaw.adapter.exception.LdmlTransformationException;
import de.bund.digitalservice.ris.caselaw.adapter.publication.ManualPortalPublicationResult;
import de.bund.digitalservice.ris.caselaw.adapter.publication.PortalPublicationService;
import de.bund.digitalservice.ris.caselaw.adapter.transformer.DocumentationUnitTransformerException;
import de.bund.digitalservice.ris.caselaw.domain.Attachment;
import de.bund.digitalservice.ris.caselaw.domain.Attachment2Html;
import de.bund.digitalservice.ris.caselaw.domain.AttachmentService;
import de.bund.digitalservice.ris.caselaw.domain.AttachmentType;
import de.bund.digitalservice.ris.caselaw.domain.BulkAssignProcedureRequest;
import de.bund.digitalservice.ris.caselaw.domain.BulkAssignProcessStepRequest;
import de.bund.digitalservice.ris.caselaw.domain.BulkDocumentationUnitService;
import de.bund.digitalservice.ris.caselaw.domain.ConverterService;
import de.bund.digitalservice.ris.caselaw.domain.Decision;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnit;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnitCreationParameters;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnitDocxMetadataInitializationService;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnitHistoryLogService;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnitListItem;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnitService;
import de.bund.digitalservice.ris.caselaw.domain.DuplicateCheckService;
import de.bund.digitalservice.ris.caselaw.domain.DuplicateRelationStatusRequest;
import de.bund.digitalservice.ris.caselaw.domain.EurlexCreationParameters;
import de.bund.digitalservice.ris.caselaw.domain.EventRecord;
import de.bund.digitalservice.ris.caselaw.domain.HandoverEntityType;
import de.bund.digitalservice.ris.caselaw.domain.HandoverException;
import de.bund.digitalservice.ris.caselaw.domain.HandoverMail;
import de.bund.digitalservice.ris.caselaw.domain.HandoverNotAllowedException;
import de.bund.digitalservice.ris.caselaw.domain.HandoverService;
import de.bund.digitalservice.ris.caselaw.domain.HistoryLogEventType;
import de.bund.digitalservice.ris.caselaw.domain.Image;
import de.bund.digitalservice.ris.caselaw.domain.InboxStatus;
import de.bund.digitalservice.ris.caselaw.domain.Kind;
import de.bund.digitalservice.ris.caselaw.domain.LdmlTransformationResult;
import de.bund.digitalservice.ris.caselaw.domain.RelatedDocumentationUnit;
import de.bund.digitalservice.ris.caselaw.domain.RisJsonPatch;
import de.bund.digitalservice.ris.caselaw.domain.SingleNormValidationInfo;
import de.bund.digitalservice.ris.caselaw.domain.User;
import de.bund.digitalservice.ris.caselaw.domain.UserService;
import de.bund.digitalservice.ris.caselaw.domain.XmlTransformationResult;
import de.bund.digitalservice.ris.caselaw.domain.exception.DocumentationOfficeNotExistsException;
import de.bund.digitalservice.ris.caselaw.domain.exception.DocumentationUnitDeletionException;
import de.bund.digitalservice.ris.caselaw.domain.exception.DocumentationUnitException;
import de.bund.digitalservice.ris.caselaw.domain.exception.DocumentationUnitNotExistsException;
import de.bund.digitalservice.ris.caselaw.domain.exception.ImageNotExistsException;
import de.bund.digitalservice.ris.caselaw.domain.exception.ProcessStepNotFoundException;
import de.bund.digitalservice.ris.domain.export.juris.response.StatusImporterException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.BadRequestException;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.mapping.MappingException;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

@RestController
@RequestMapping("api/v1/caselaw/documentunits")
@Slf4j
public class DocumentationUnitController {
  public static final String X_FILENAME = "X-Filename";
  private final DocumentationUnitService service;
  private final BulkDocumentationUnitService abstractService;
  private final UserService userService;
  private final AttachmentService attachmentService;
  private final ConverterService converterService;
  private final HandoverService handoverService;
  private final PortalPublicationService portalPublicationService;
  private final DocumentationUnitHistoryLogService historyLogService;
  private final DocumentationUnitDocxMetadataInitializationService
      documentationUnitDocxMetadataInitializationService;
  private final DuplicateCheckService duplicateCheckService;
  private final EurLexSOAPSearchService eurLexSOAPSearchService;
  private final AttachmentRepository attachmentRepository;

  public DocumentationUnitController(
      DocumentationUnitService service,
      BulkDocumentationUnitService abstractService,
      UserService userService,
      AttachmentService attachmentService,
      ConverterService converterService,
      HandoverService handoverService,
      PortalPublicationService portalPublicationService,
      DocumentationUnitDocxMetadataInitializationService
          documentationUnitDocxMetadataInitializationService,
      DuplicateCheckService duplicateCheckService,
      EurLexSOAPSearchService eurLexSOAPSearchService,
      AttachmentRepository attachmentRepository,
      DocumentationUnitHistoryLogService historyLogService) {
    this.service = service;
    this.userService = userService;
    this.attachmentService = attachmentService;
    this.converterService = converterService;
    this.handoverService = handoverService;
    this.portalPublicationService = portalPublicationService;
    this.documentationUnitDocxMetadataInitializationService =
        documentationUnitDocxMetadataInitializationService;
    this.duplicateCheckService = duplicateCheckService;
    this.eurLexSOAPSearchService = eurLexSOAPSearchService;
    this.abstractService = abstractService;
    this.attachmentRepository = attachmentRepository;
    this.historyLogService = historyLogService;
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
      @RequestBody(required = false) Optional<DocumentationUnitCreationParameters> parameters,
      @RequestParam(value = "kind", defaultValue = "DECISION") Kind kind) {
    try {
      var documentationUnit =
          service.generateNewDocumentationUnit(userService.getUser(oidcUser), parameters, kind);
      return ResponseEntity.status(HttpStatus.CREATED).body(documentationUnit);
    } catch (DocumentationUnitException e) {
      log.error("error in generate new documentation unit", e);
      return ResponseEntity.internalServerError().body(Decision.builder().build());
    }
  }

  /**
   * Generate a new documentation unit out of eurlex decision.
   *
   * @param oidcUser the logged-in user
   * @param parameters the parameters for the new documentation unit (optional)
   * @return the new documentation unit or an empty response with status code 500 if the creation
   *     failed
   */
  @PutMapping(value = "new/eurlex", produces = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("isAuthenticated() and @userIsInternal.apply(#oidcUser)")
  public ResponseEntity<List<String>> generateNewDecisionFromEurlex(
      @AuthenticationPrincipal OidcUser oidcUser,
      @RequestBody(required = false) Optional<EurlexCreationParameters> parameters) {
    try {
      if (parameters.isPresent()) {
        List<String> documentationNumbers =
            service.generateNewDecisionFromEurlex(userService.getUser(oidcUser), parameters);
        eurLexSOAPSearchService.updateResultStatus(parameters.get().celexNumbers());
        return ResponseEntity.status(HttpStatus.CREATED).body(documentationNumbers);
      }
      log.error("Missing eurlex creation parameters.");
      return ResponseEntity.badRequest().body(List.of("Missing eurlex creation parameters."));
    } catch (DocumentationUnitException e) {
      log.error("error in generate new documentation unit", e);
      return ResponseEntity.internalServerError().body(Collections.emptyList());
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

  @GetMapping(value = "/{uuid}/file/{fileId}")
  @PreAuthorize("@userIsInternal.apply(#oidcUser) and @userHasWriteAccess.apply(#uuid)")
  public ResponseEntity<StreamingResponseBody> downloadFile(
      @AuthenticationPrincipal OidcUser oidcUser,
      @PathVariable UUID uuid,
      @PathVariable UUID fileId) {
    var fileResponse = attachmentService.getFileStream(uuid, fileId);

    return ResponseEntity.ok()
        .header(
            HttpHeaders.CONTENT_DISPOSITION,
            "attachment; filename=\"" + fileResponse.filename() + "\"")
        .contentType(MediaType.parseMediaType(fileResponse.response().contentType()))
        .contentLength(fileResponse.response().contentLength())
        .body(fileResponse.body());
  }

  /**
   * Attach a content file (docx) to the documentation unit. This file is used to fill the
   * categories of the documentation unit.
   *
   * <p>Do a conversion into html and parse the footer for ECLI information.
   *
   * @param uuid UUID of the documentation unit
   * @param file the file to be uploaded
   * @param filename http headers with the X-Filename information
   * @return the into html converted content of the file with some additional metadata (ECLI)
   */
  @PutMapping(
      value = {"/{uuid}/original-file"},
      produces = MediaType.APPLICATION_JSON_VALUE,
      consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @PreAuthorize("@userIsInternal.apply(#oidcUser) and @userHasWriteAccess.apply(#uuid)")
  public ResponseEntity<Attachment2Html> attachOriginalFileToDocumentationUnit(
      @AuthenticationPrincipal OidcUser oidcUser,
      @PathVariable UUID uuid,
      @RequestPart("file") MultipartFile file,
      @RequestHeader(X_FILENAME) String filename) {

    if (file == null || file.isEmpty()) {
      return ResponseEntity.badRequest().build();
    }

    Attachment attachment;
    try (InputStream inputStream = file.getInputStream()) {
      User user = userService.getUser(oidcUser);
      attachment =
          attachmentService.streamFileToDocumentationUnit(
              uuid, inputStream, filename, user, AttachmentType.ORIGINAL);
    } catch (IOException e) {
      log.atError()
          .setCause(e)
          .setMessage("Error reading uploaded file for documentation unit")
          .addKeyValue("id", uuid)
          .log();
      return ResponseEntity.unprocessableContent().build();
    } catch (Exception e) {
      log.atError()
          .setCause(e)
          .setMessage("Error by attaching file to documentation unit")
          .addKeyValue("id", uuid)
          .log();
      return ResponseEntity.internalServerError().build();
    }

    try {
      var attachment2Html = converterService.getConvertedObject(attachment.s3path());
      initializeCoreDataAndCheckDuplicates(uuid, attachment2Html, userService.getUser(oidcUser));
      return ResponseEntity.status(HttpStatus.OK).body(attachment2Html);
    } catch (Exception e) {
      attachmentService.deleteByFileId(attachment.id(), uuid, userService.getUser(oidcUser));
      log.atError()
          .setCause(e)
          .setMessage("Error by converting uploaded file to html")
          .addKeyValue("id", uuid)
          .log();
      return ResponseEntity.unprocessableContent().build();
    }
  }

  /**
   * Attaches an additional file to an existing documentation unit identified by the provided UUID.
   * The file is uploaded as part of a multipart form request.
   *
   * @param oidcUser the authenticated user making the request, obtained automatically from the
   *     security context
   * @param uuid the unique identifier of the documentation unit to which the file will be attached
   * @param file the file to be attached to the documentation unit; must be a non-empty multipart
   *     file
   * @return a {@link org.springframework.http.ResponseEntity} representing the result of the
   *     operation; returns 201 (Created) on success, 400 (Bad Request) if the file is invalid or
   *     missing, and 500 (Internal Server Error) for any unexpected errors
   */
  @PutMapping(value = "/{uuid}/other-file", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @PreAuthorize("@userIsInternal.apply(#oidcUser) and @userHasWriteAccess.apply(#uuid)")
  public ResponseEntity<Void> attachOtherFileToDocumentationUnit(
      @AuthenticationPrincipal OidcUser oidcUser,
      @PathVariable UUID uuid,
      @RequestPart("file") MultipartFile file,
      @RequestHeader(X_FILENAME) String filename) {

    if (file == null || file.isEmpty()) {
      return ResponseEntity.badRequest().build();
    }

    try (InputStream inputStream = file.getInputStream()) {
      User user = userService.getUser(oidcUser);
      attachmentService.streamFileToDocumentationUnit(
          uuid, inputStream, filename, user, AttachmentType.OTHER);
      return ResponseEntity.status(HttpStatus.CREATED).build();
    } catch (IOException e) {
      log.atError()
          .setCause(e)
          .setMessage("IO Error reading file stream")
          .addKeyValue("id", uuid)
          .log();

      return ResponseEntity.badRequest().build();
    } catch (Exception e) {
      log.atError()
          .setCause(e)
          .setMessage("Error by attaching file to documentation unit")
          .addKeyValue("id", uuid)
          .log();
      return ResponseEntity.internalServerError().build();
    }
  }

  private void initializeCoreDataAndCheckDuplicates(
      UUID uuid, Attachment2Html attachment2Html, User user) {
    try {
      DocumentationUnit documentationUnit = service.getByUuid(uuid);
      if (documentationUnit instanceof Decision docUnit) {
        documentationUnitDocxMetadataInitializationService.initializeCoreData(
            docUnit, attachment2Html, user);
        checkDuplicates(docUnit.documentNumber());
      } else {
        log.info("Documentable type not supported: {}", documentationUnit.getClass().getName());
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

  @DeleteMapping(value = "/{uuid}/file/{fileId}")
  @PreAuthorize("@userIsInternal.apply(#oidcUser) and @userHasWriteAccess.apply(#uuid)")
  public ResponseEntity<Object> removeAttachmentFromDocumentationUnit(
      @AuthenticationPrincipal OidcUser oidcUser,
      @PathVariable UUID uuid,
      @PathVariable UUID fileId) {

    try {
      attachmentService.deleteByFileId(fileId, uuid, userService.getUser(oidcUser));
      return ResponseEntity.noContent().build();
    } catch (Exception e) {
      log.error(
          "Error by deleting attachment with '{}' for documentation unit {}", fileId, uuid, e);
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
      @RequestParam(value = "resolutionDate") Optional<LocalDate> resolutionDate,
      @RequestParam(value = "resolutionDateEnd") Optional<LocalDate> resolutionDateEnd,
      @RequestParam(value = "isResolved") Optional<Boolean> isResolved,
      @RequestParam(value = "inboxStatus") Optional<InboxStatus> inboxStatus,
      @RequestParam(value = "kind") Optional<Kind> kind,
      @RequestParam(value = "processStep") Optional<String> processStep,
      @RequestParam(value = "assignedToMe") Optional<Boolean> assignedToMe,
      @RequestParam(value = "unassigned") Optional<Boolean> unassigned,
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
        resolutionDate,
        resolutionDateEnd,
        isResolved,
        inboxStatus,
        kind,
        processStep,
        assignedToMe,
        unassigned);
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
      DocumentationUnit documentationUnit =
          service.getByDocumentNumberWithUser(documentNumber, oidcUser);
      return ResponseEntity.ok(documentationUnit);

    } catch (DocumentationUnitNotExistsException e) {
      log.error("Documentation unit '{}' doesn't exist", documentNumber);
      return ResponseEntity.notFound().build();
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
      return ResponseEntity.badRequest().body(ex.getMessage());
    }
  }

  @PutMapping(
      value = "/{uuid}",
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("@userHasWriteAccess.apply(#uuid)")
  public ResponseEntity<Decision> updateByUuid(
      @PathVariable UUID uuid,
      @Valid @RequestBody Decision decision,
      @AuthenticationPrincipal OidcUser oidcUser) {

    if (!uuid.equals(decision.uuid())) {
      return ResponseEntity.unprocessableContent().body(Decision.builder().build());
    }
    try {
      var du = service.updateDocumentationUnit(decision);
      return ResponseEntity.status(HttpStatus.OK).body(du);
    } catch (DocumentationUnitNotExistsException
        | DocumentationUnitException
        | DocumentationUnitTransformerException e) {
      log.error("Error by updating documentation unit '{}'", decision.documentNumber(), e);
      return ResponseEntity.internalServerError().body(Decision.builder().build());
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
    } catch (ProcessStepNotFoundException e) {
      return ResponseEntity.notFound().build();
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
        return ResponseEntity.unprocessableContent().body(handoverMail);
      }
      service.saveSuccessfulHandover(uuid);
      return ResponseEntity.ok(handoverMail);
    } catch (DocumentationUnitNotExistsException | HandoverException e) {
      log.error("Error handing over documentation unit '{}' as email", uuid, e);
      return ResponseEntity.internalServerError().build();
    } catch (HandoverNotAllowedException e) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN)
          .body(
              HandoverMail.builder()
                  .success(false)
                  .statusMessages(List.of(e.getMessage()))
                  .build());
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
  public XmlTransformationResult getXmlPreview(
      @PathVariable UUID uuid,
      @RequestParam(value = "prettify", defaultValue = "true") boolean prettify) {
    try {
      return handoverService.createPreviewXml(uuid, prettify);
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
      @RequestParam(value = "onlyPendingProceedings") Optional<Boolean> onlyPendingProceedings,
      @RequestBody RelatedDocumentationUnit relatedDocumentationUnit,
      @AuthenticationPrincipal OidcUser oidcUser) {

    var documentationOffice = userService.getDocumentationOffice(oidcUser);
    return service.searchLinkableDocumentationUnits(
        relatedDocumentationUnit,
        documentationOffice,
        documentNumberToExclude,
        onlyPendingProceedings.orElse(false),
        PageRequest.of(page, size));
  }

  @GetMapping(
      value = "/{documentationUnitId}/file/{attachmentId}/html",
      produces = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("@userHasReadAccessByDocumentationUnitId.apply(#documentationUnitId)")
  public ResponseEntity<Attachment2Html> getDocxHtml(
      @PathVariable UUID documentationUnitId, @PathVariable UUID attachmentId) {

    try {
      service.getByUuid(documentationUnitId);
    } catch (DocumentationUnitNotExistsException ex) {
      return ResponseEntity.notFound().build();
    }

    try {
      var attachment = attachmentRepository.findById(attachmentId);
      if (attachment.isEmpty()) {
        return ResponseEntity.notFound().build();
      }
      var attach = attachment.get();
      var attachment2Html =
          converterService.getConvertedObject(
              attach.getFormat(), attach.getS3ObjectPath(), documentationUnitId);
      return ResponseEntity.ok()
          .cacheControl(CacheControl.maxAge(Duration.ofDays(1))) // Set cache duration
          .body(attachment2Html);
    } catch (Exception ex) {
      log.error("Error by getting docx for documentation unit {}", documentationUnitId, ex);
      return ResponseEntity.internalServerError().build();
    }
  }

  @GetMapping(
      value = "/{documentNumber}/image/{imageName}",
      produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
  @PreAuthorize("@userHasReadAccessByDocumentNumber.apply(#documentNumber)")
  public ResponseEntity<byte[]> getImage(
      @PathVariable String documentNumber, @PathVariable String imageName) {

    try {
      Image imageResult = service.getImageBytes(documentNumber, imageName);

      if (imageResult == null || imageResult.getContent() == null) {
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
      }

      byte[] imageBytes = imageResult.getContent();
      String contentType = imageResult.getContentType();

      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(getMediaType(contentType));
      headers.setContentLength(imageBytes.length);
      headers.setCacheControl(CacheControl.maxAge(Duration.ofDays(1)));

      return new ResponseEntity<>(imageBytes, headers, HttpStatus.OK);
    } catch (DocumentationUnitNotExistsException e) {
      log.warn("Documentation unit not found: {}", documentNumber, e);
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    } catch (ImageNotExistsException e) {
      log.warn("Image {} not found for documentation unit {}", imageName, documentNumber, e);
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
  }

  /**
   * Validate and parse the given content type
   *
   * @param contentType the content type string to parse
   * @return the MediaType object representing the content type, or a default type if parsing fails
   */
  @NotNull
  private static MediaType getMediaType(String contentType) {
    try {
      return MediaType.parseMediaType("image/" + contentType);
    } catch (IllegalArgumentException e) {
      // defaulting to application/octet-stream if parsing fails
      return MediaType.APPLICATION_OCTET_STREAM;
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
   * Get the LDML preview of a documentation unit.
   *
   * @param uuid UUID of the documentation unit
   * @return the LDML preview result (including potential error messages)
   */
  @GetMapping(value = "/{uuid}/preview-ldml", produces = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("@userHasWriteAccess.apply(#uuid)")
  public ResponseEntity<LdmlTransformationResult> getLdmlPreview(@PathVariable UUID uuid) {
    try {
      var result = portalPublicationService.createLdmlPreview(uuid);
      return ResponseEntity.ok(result);
    } catch (DocumentationUnitNotExistsException e) {
      return ResponseEntity.notFound().build();
    } catch (LdmlTransformationException | MappingException e) {
      var result =
          LdmlTransformationResult.builder()
              .success(false)
              .statusMessages(List.of(e.getMessage()))
              .build();
      return ResponseEntity.unprocessableContent().body(result);
    } catch (Exception e) {
      var result =
          LdmlTransformationResult.builder()
              .success(false)
              .statusMessages(List.of(e.getMessage()))
              .build();
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
    }
  }

  /**
   * Transforms the documentation unit to LDML and hands it over to the portal to be published.
   *
   * @param uuid UUID of the documentation unit
   */
  @PutMapping(value = "/{uuid}/publish")
  @PreAuthorize("@userHasWriteAccess.apply(#uuid)")
  public ResponseEntity<ManualPortalPublicationResult> publishDocumentationUnit(
      @PathVariable UUID uuid, @AuthenticationPrincipal OidcUser oidcUser) {

    User user = userService.getUser(oidcUser);

    try {
      var result = portalPublicationService.publishDocumentationUnitWithChangelog(uuid, user);
      return ResponseEntity.ok(result);

    } catch (DocumentationUnitNotExistsException e) {
      log.atError()
          .setMessage("Could not find documentation unit to publish to portal")
          .setCause(e)
          .addKeyValue("id", uuid)
          .log();
      return ResponseEntity.notFound().build();

    } catch (Exception e) {
      historyLogService.saveHistoryLog(
          uuid,
          user,
          HistoryLogEventType.PORTAL_PUBLICATION,
          "Dokeinheit konnte nicht im Portal veröffentlicht werden");
      throw e;
    }
  }

  /**
   * Withdraws the documentation unit from the portal.
   *
   * @param uuid UUID of the documentation unit
   */
  @PutMapping(value = "/{uuid}/withdraw")
  @PreAuthorize("@userHasWriteAccess.apply(#uuid)")
  public ResponseEntity<Void> withdrawDocumentationUnit(
      @PathVariable UUID uuid, @AuthenticationPrincipal OidcUser oidcUser) {
    User user = userService.getUser(oidcUser);
    try {
      portalPublicationService.withdrawDocumentationUnitWithChangelog(uuid, user);
      return ResponseEntity.ok().build();
    } catch (DocumentationUnitNotExistsException e) {
      log.atError()
          .setMessage("Could not find documentation unit to withdraw from portal")
          .setCause(e)
          .addKeyValue("id", uuid)
          .log();
      return ResponseEntity.notFound().build();
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

  @PatchMapping(value = "/bulk-assign-process-step")
  @PreAuthorize("@userHasBulkWriteAccess.apply(#body.getDocumentationUnitIds())")
  public ResponseEntity<Void> bulkAssignProcessStep(
      @AuthenticationPrincipal OidcUser oidcUser,
      @RequestBody @Valid BulkAssignProcessStepRequest body) {
    try {
      User user = userService.getUser(oidcUser);
      abstractService.bulkAssignProcessStep(
          body.getDocumentationUnitIds(), body.getDocumentationUnitProcessStep(), user);
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
   * @param documentationUnitId UUID of the documentation unit
   * @param documentationOfficeId UUID of documentation Office to assign to
   * @return HTTP 200 with result string for success, 404/400 for client errors, 500 for server
   *     errors.
   */
  @PutMapping(value = "/{documentationUnitId}/assign/{documentationOfficeId}")
  @PreAuthorize(
      "isAuthenticated() and @userIsInternal.apply(#oidcUser) and @userHasWriteAccess.apply(#documentationUnitId)")
  public ResponseEntity<String> assignDocumentationOffice(
      @AuthenticationPrincipal OidcUser oidcUser,
      @PathVariable @NonNull UUID documentationUnitId,
      @PathVariable @NonNull UUID documentationOfficeId) {
    try {
      var result =
          service.assignDocumentationOffice(
              documentationUnitId, documentationOfficeId, userService.getUser(oidcUser));
      return ResponseEntity.ok().body(result);
    } catch (DocumentationUnitNotExistsException e) {
      log.warn("Documentation unit not found: {}", documentationUnitId, e);
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Documentation unit not found");
    } catch (DocumentationOfficeNotExistsException e) {
      log.warn("Documentation office not found: {}", documentationOfficeId, e);
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Documentation office not found");
    } catch (DocumentationUnitException e) {
      log.error(
          "Error assigning documentation office {} to {}",
          documentationOfficeId,
          documentationUnitId,
          e);
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
    }
  }
}
