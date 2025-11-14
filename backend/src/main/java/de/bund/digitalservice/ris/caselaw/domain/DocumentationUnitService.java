package de.bund.digitalservice.ris.caselaw.domain;

import static de.bund.digitalservice.ris.caselaw.domain.StringUtils.normalizeSpace;

import com.gravity9.jsonpatch.JsonPatch;
import com.gravity9.jsonpatch.JsonPatchOperation;
import de.bund.digitalservice.ris.caselaw.domain.exception.DocumentationOfficeNotExistsException;
import de.bund.digitalservice.ris.caselaw.domain.exception.DocumentationUnitDeletionException;
import de.bund.digitalservice.ris.caselaw.domain.exception.DocumentationUnitException;
import de.bund.digitalservice.ris.caselaw.domain.exception.DocumentationUnitNotExistsException;
import de.bund.digitalservice.ris.caselaw.domain.exception.DocumentationUnitPatchException;
import de.bund.digitalservice.ris.caselaw.domain.exception.ImageNotExistsException;
import de.bund.digitalservice.ris.caselaw.domain.exception.ProcessStepNotFoundException;
import de.bund.digitalservice.ris.caselaw.domain.mapper.PatchMapperService;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.BooleanSupplier;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.BadRequestException;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.orm.jpa.JpaObjectRetrievalFailureException;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@SuppressWarnings("java:S6539") // Too many dependencies warning
public class DocumentationUnitService {

  public static final String DOCUMENT_NUMBER = "documentNumber";
  private final DocumentationUnitRepository repository;
  private final DocumentNumberService documentNumberService;
  private final DocumentTypeService documentTypeService;
  private final DocumentationUnitStatusService statusService;
  private final AttachmentService attachmentService;
  private final TransformationService transformationService;
  private final DocumentNumberRecyclingService documentNumberRecyclingService;
  private final PatchMapperService patchMapperService;
  private final AuthService authService;
  private final UserService userService;
  private final Validator validator;
  private final DuplicateCheckService duplicateCheckService;
  private final DocumentationOfficeService documentationOfficeService;
  private final ProcessStepService processStepService;
  private static final List<String> pathsForDuplicateCheck =
      List.of(
          "/coreData/ecli",
          "/coreData/deviatingEclis",
          "/coreData/fileNumbers",
          "/coreData/deviatingFileNumbers",
          "/coreData/court",
          "/coreData/deviatingCourts",
          "/coreData/decisionDate",
          "/coreData/deviatingDecisionDates",
          "/coreData/documentType");
  private final DocumentationUnitHistoryLogService historyLogService;
  private final DocumentationUnitSearchRepository docUnitSearchRepository;

  protected DocumentationUnitService(
      DocumentationUnitRepository repository,
      DocumentNumberService documentNumberService,
      DocumentTypeService documentTypeService,
      DocumentationUnitStatusService statusService,
      DocumentNumberRecyclingService documentNumberRecyclingService,
      UserService userService,
      Validator validator,
      AttachmentService attachmentService,
      TransformationService transformationService,
      @Lazy AuthService authService,
      PatchMapperService patchMapperService,
      DuplicateCheckService duplicateCheckService,
      DocumentationOfficeService documentationOfficeService,
      DocumentationUnitHistoryLogService historyLogService,
      DocumentationUnitSearchRepository docUnitSearchRepository,
      ProcessStepService processStepService) {

    this.repository = repository;
    this.documentNumberService = documentNumberService;
    this.documentTypeService = documentTypeService;
    this.documentNumberRecyclingService = documentNumberRecyclingService;
    this.userService = userService;
    this.validator = validator;
    this.attachmentService = attachmentService;
    this.transformationService = transformationService;
    this.patchMapperService = patchMapperService;
    this.statusService = statusService;
    this.authService = authService;
    this.duplicateCheckService = duplicateCheckService;
    this.documentationOfficeService = documentationOfficeService;
    this.historyLogService = historyLogService;
    this.docUnitSearchRepository = docUnitSearchRepository;
    this.processStepService = processStepService;
  }

  @Transactional(transactionManager = "jpaTransactionManager")
  public DocumentationUnit generateNewDocumentationUnit(
      User user, Optional<DocumentationUnitCreationParameters> parameters, Kind kind) {
    DocumentationUnit newDocUnit;
    if (kind.equals(Kind.DECISION)) {
      newDocUnit = generateNewDecision(user, parameters);
    } else if (kind.equals(Kind.PENDING_PROCEEDING)) {
      newDocUnit = generateNewPendingProceeding(user, parameters);
    } else {
      throw new DocumentationUnitException(
          "DocumentationUnit is neither decision nor pending proceeding.");
    }
    log.atInfo()
        .addKeyValue("documentationNumber", newDocUnit.documentNumber())
        .addKeyValue("id", newDocUnit.uuid())
        .setMessage("New documentation unit created.")
        .log();
    return newDocUnit;
  }

  public Decision generateNewDecision(
      User user, Optional<DocumentationUnitCreationParameters> parameters)
      throws DocumentationUnitException {

    return generateNewDecision(user, parameters, null);
  }

  public PendingProceeding generateNewPendingProceeding(
      User user, Optional<DocumentationUnitCreationParameters> parameters)
      throws DocumentationUnitException {

    var userDocOffice = user.documentationOffice();
    // default office is user office
    DocumentationUnitCreationParameters params =
        parameters.orElse(
            DocumentationUnitCreationParameters.builder()
                .documentationOffice(userDocOffice)
                .build());
    if (params.documentationOffice() == null) {
      params = params.toBuilder().documentationOffice(userDocOffice).build();
    }

    var documentType = documentTypeService.getPendingProceedingType();
    PendingProceeding docUnit =
        PendingProceeding.builder()
            .version(0L)
            .documentNumber(
                generateDocumentNumber(params.documentationOffice().abbreviation() + "-Anh"))
            .coreData(
                CoreData.builder()
                    .documentationOffice(params.documentationOffice())
                    .documentType(documentType)
                    .decisionDate(params.decisionDate())
                    .court(params.court())
                    .legalEffect(
                        LegalEffect.deriveFrom(params.court(), true)
                            .orElse(LegalEffect.NOT_SPECIFIED)
                            .getLabel())
                    .build())
            .build();

    Status status =
        Status.builder().publicationStatus(PublicationStatus.UNPUBLISHED).withError(false).build();

    ProcessStep initialProcessStep = getInitialProcessStep(null, false);
    // create new DocumentationUnitProcessStep with first process step for dooffice
    var initialDocUnitProcessStep =
        DocumentationUnitProcessStep.builder()
            .createdAt(LocalDateTime.now())
            .processStep(initialProcessStep)
            .user(user)
            .build();

    return (PendingProceeding)
        repository.createNewDocumentationUnit(
            docUnit,
            status,
            params.reference(),
            params.fileNumber(),
            user,
            initialDocUnitProcessStep);
  }

  @Transactional(transactionManager = "jpaTransactionManager")
  public List<String> generateNewDecisionFromEurlex(
      User user, Optional<EurlexCreationParameters> parameters) throws DocumentationUnitException {

    List<String> documentNumbers = new ArrayList<>();

    if (parameters.isPresent() && !parameters.get().celexNumbers().isEmpty()) {
      for (String celexNumber : parameters.get().celexNumbers()) {
        var inputTypes = new ArrayList<String>();
        inputTypes.add("EUR-LEX-Schnittstelle");
        var newDecision =
            generateNewDecision(
                user,
                parameters.map(
                    params ->
                        DocumentationUnitCreationParameters.builder()
                            .documentationOffice(params.documentationOffice())
                            .inputTypes(inputTypes)
                            .build()),
                celexNumber);
        documentNumbers.add(newDecision.documentNumber());
      }
    }

    return documentNumbers;
  }

  Decision generateNewDecision(
      User user, Optional<DocumentationUnitCreationParameters> parameters, String celexNumber) {
    var userDocOffice = user.documentationOffice();
    // default office is user office
    DocumentationUnitCreationParameters params =
        parameters.orElse(
            DocumentationUnitCreationParameters.builder()
                .documentationOffice(userDocOffice)
                .build());
    if (params.documentationOffice() == null) {
      params = params.toBuilder().documentationOffice(userDocOffice).build();
    }

    boolean isExternalHandover =
        params.documentationOffice() != null
            && userDocOffice != null
            && !userDocOffice.id().equals(params.documentationOffice().id())
            && celexNumber == null;

    Decision docUnit =
        Decision.builder()
            .version(0L)
            .documentNumber(generateDocumentNumber(params.documentationOffice().abbreviation()))
            .coreData(
                CoreData.builder()
                    .documentationOffice(params.documentationOffice())
                    .documentType(params.documentType())
                    .decisionDate(params.decisionDate())
                    .court(params.court())
                    .creatingDocOffice(isExternalHandover ? userDocOffice : null)
                    .legalEffect(
                        LegalEffect.deriveFrom(params.court(), true)
                            .orElse(LegalEffect.NOT_SPECIFIED)
                            .getLabel())
                    .inputTypes(params.inputTypes())
                    .sources(params.sources())
                    .build())
            .inboxStatus(isExternalHandover ? InboxStatus.EXTERNAL_HANDOVER : null)
            .build();

    Status status =
        Status.builder()
            .publicationStatus(
                isExternalHandover
                    ? PublicationStatus.EXTERNAL_HANDOVER_PENDING
                    : PublicationStatus.UNPUBLISHED)
            .withError(false)
            .build();

    ProcessStep initialProcessStep = getInitialProcessStep(celexNumber, isExternalHandover);

    User initialUser = null;
    if (celexNumber == null && !isExternalHandover) {
      initialUser = user;
    }

    // create new DocumentationUnitProcessStep with first process step for dooffice
    var initialDocUnitProcessStep =
        DocumentationUnitProcessStep.builder()
            .createdAt(LocalDateTime.now())
            .processStep(initialProcessStep)
            .user(initialUser)
            .build();

    var newDocumentationUnit =
        repository.createNewDocumentationUnit(
            docUnit,
            status,
            params.reference(),
            params.fileNumber(),
            user,
            initialDocUnitProcessStep);

    if (isExternalHandover) {
      String description =
          "Fremdanalage angelegt für " + params.documentationOffice().abbreviation();
      historyLogService.saveHistoryLog(
          newDocumentationUnit.uuid(), user, HistoryLogEventType.EXTERNAL_HANDOVER, description);
    }

    if (celexNumber != null) {
      transformationService.getDataFromEurlex(celexNumber, (Decision) newDocumentationUnit, user);
    }
    duplicateCheckService.checkDuplicates(docUnit.documentNumber());

    return (Decision) newDocumentationUnit;
  }

  private ProcessStep getInitialProcessStep(String celexNumber, boolean isExternalHandover) {
    // if decision origin is Eurlex or external handover, the initial status is 'Neu'
    if (celexNumber != null || isExternalHandover) {
      return processStepService
          .getProcessStepForName("Neu")
          .orElseThrow(
              () ->
                  new ProcessStepNotFoundException("Could not find Process Step with name 'Neu'"));
    }
    // otherwise the initial status is 'Ersterfassung'
    return processStepService
        .getProcessStepForName("Ersterfassung")
        .orElseThrow(
            () ->
                new ProcessStepNotFoundException(
                    "Could not find Process Step with name 'Ersterfassung'"));
  }

  private String generateDocumentNumber(String documentationOfficeAbbreviation) {
    try {
      return documentNumberService.generateDocumentNumber(documentationOfficeAbbreviation);
    } catch (Exception e) {
      throw new DocumentationUnitException("Could not generate document number", e);
    }
  }

  public Slice<DocumentationUnitListItem> searchByDocumentationUnitSearchInput(
      Pageable pageable,
      OidcUser oidcUser,
      Optional<String> documentNumber,
      Optional<String> fileNumber,
      Optional<String> courtType,
      Optional<String> courtLocation,
      Optional<LocalDate> decisionDate,
      Optional<LocalDate> decisionDateEnd,
      Optional<LocalDate> publicationDate,
      Optional<Boolean> scheduledOnly,
      Optional<String> publicationStatus,
      Optional<Boolean> withError,
      Optional<Boolean> myDocOfficeOnly,
      Optional<Boolean> withDuplicateWarning,
      Optional<LocalDate> resolutionDate,
      Optional<LocalDate> resolutionDateEnd,
      Optional<Boolean> isResolved,
      Optional<InboxStatus> inboxStatus,
      Optional<Kind> kind,
      Optional<String> processStep,
      Optional<Boolean> assignedToMe,
      Optional<Boolean> unassigned) {

    DocumentationUnitSearchInput searchInput =
        DocumentationUnitSearchInput.builder()
            .documentNumber(normalizeSpace(documentNumber.orElse(null)))
            .fileNumber(normalizeSpace(fileNumber.orElse(null)))
            .courtType(normalizeSpace(courtType.orElse(null)))
            .courtLocation(normalizeSpace(courtLocation.orElse(null)))
            .decisionDate(decisionDate.orElse(null))
            .decisionDateEnd(decisionDateEnd.orElse(null))
            .publicationDate(publicationDate.orElse(null))
            .scheduledOnly(scheduledOnly.orElse(false))
            .status(
                (publicationStatus.isPresent() || withError.isPresent())
                    ? Status.builder()
                        .publicationStatus(
                            publicationStatus.map(PublicationStatus::valueOf).orElse(null))
                        .withError(withError.orElse(false))
                        .build()
                    : null)
            .myDocOfficeOnly(myDocOfficeOnly.orElse(false))
            .withDuplicateWarning(withDuplicateWarning.orElse(false))
            .inboxStatus(inboxStatus.orElse(null))
            .resolutionDate(resolutionDate.orElse(null))
            .resolutionDateEnd(resolutionDateEnd.orElse(null))
            .isResolved(isResolved.orElse(false))
            .kind(kind.orElse(null))
            .processStep(processStep.orElse(null))
            .assignedToMe(assignedToMe.orElse(false))
            .unassigned(unassigned.orElse(false))
            .build();

    Slice<DocumentationUnitListItem> documentationUnitListItems;
    try {
      documentationUnitListItems =
          docUnitSearchRepository.searchByDocumentationUnitSearchInput(
              searchInput,
              PageRequest.of(pageable.getPageNumber(), pageable.getPageSize()),
              oidcUser);
    } catch (JpaObjectRetrievalFailureException e) {
      // This exception can occur if a doc unit is deleted while the search is running.
      // 1. Search runs and fetches list of doc unit entities incl. doc unit A123, request ongoing
      // 2. Other user/requests deletes doc unit A123
      // 3. Search tries to fetch lazy relationships of doc unit A123 while transforming to domain
      //    object and fails because the status with referenced id does not exist anymore.

      // This regularly happens during e2e tests running in parallel.
      log.info("Retrying search after JpaObjectRetrievalFailureException", e);
      documentationUnitListItems =
          docUnitSearchRepository.searchByDocumentationUnitSearchInput(
              searchInput,
              PageRequest.of(pageable.getPageNumber(), pageable.getPageSize()),
              oidcUser);
    }

    return retrieveCurrentProcessStepUser(
        documentationUnitListItems.map(item -> addPermissions(oidcUser, item)), oidcUser);
  }

  public DocumentationUnitListItem takeOverDocumentationUnit(
      String documentNumber, OidcUser oidcUser) throws DocumentationUnitNotExistsException {

    Status status =
        Status.builder().publicationStatus(PublicationStatus.UNPUBLISHED).withError(false).build();
    statusService.update(documentNumber, status, userService.getUser(oidcUser));

    return addPermissions(
        oidcUser, repository.findDocumentationUnitListItemByDocumentNumber(documentNumber));
  }

  public void saveSuccessfulHandover(UUID uuid) {
    repository.saveSuccessfulHandover(uuid);
  }

  private DocumentationUnitListItem addPermissions(
      OidcUser oidcUser, DocumentationUnitListItem listItem) {

    boolean hasWriteAccess =
        authService.userHasWriteAccess(
            oidcUser,
            listItem.creatingDocumentationOffice(),
            listItem.documentationOffice(),
            listItem.status());
    boolean isInternalUser = authService.userIsInternal().apply(oidcUser);
    BooleanSupplier isAssignedViaGroup =
        () ->
            listItem.assignedUserGroup() != null
                && listItem
                    .assignedUserGroup()
                    .id()
                    .equals(userService.getUserGroup(oidcUser).map(UserGroup::id).orElse(null));
    return listItem.toBuilder()
        .isDeletable(hasWriteAccess && isInternalUser)
        .isEditable((hasWriteAccess && (isInternalUser || isAssignedViaGroup.getAsBoolean())))
        .build();
  }

  public DocumentationUnit getByDocumentNumber(String documentNumber)
      throws DocumentationUnitNotExistsException {
    return repository.findByDocumentNumber(documentNumber);
  }

  public DocumentationUnit getByDocumentNumberWithUser(String documentNumber, OidcUser oidcUser)
      throws DocumentationUnitNotExistsException {
    User user = userService.getUser(oidcUser);

    var documentable = repository.findByDocumentNumber(documentNumber, user);

    documentable = filterProcessStepsOfOtherDocumentationOffices(documentable, user);

    switch (documentable) {
      case Decision decision -> {
        return decision.toBuilder()
            .isEditable(
                authService.userHasWriteAccess(
                    oidcUser,
                    decision.coreData().creatingDocOffice(),
                    decision.coreData().documentationOffice(),
                    decision.status()))
            .build();
      }
      case PendingProceeding pendingProceeding -> {
        return pendingProceeding.toBuilder()
            .isEditable(
                authService.userHasWriteAccess(
                    oidcUser,
                    pendingProceeding.coreData().creatingDocOffice(),
                    pendingProceeding.coreData().documentationOffice(),
                    pendingProceeding.status()))
            .build();
      }
      default -> {
        log.info("Documentable type not supported: {}", documentable.getClass().getName());
        return documentable;
      }
    }
  }

  private Slice<DocumentationUnitListItem> retrieveCurrentProcessStepUser(
      Slice<DocumentationUnitListItem> documentationUnitListItems, OidcUser oidcUser) {

    // In case user is in two groups take the first
    Map<UUID, User> userIdMap =
        userService.getUsersInSameDocOffice(oidcUser).stream()
            .collect(
                Collectors.toMap(
                    User::id, Function.identity(), (user, sameReferencedUser) -> user));

    return documentationUnitListItems.map(
        item -> {
          Optional.ofNullable(item.currentDocumentationUnitProcessStep())
              .map(DocumentationUnitProcessStep::getUser)
              .map(user -> userIdMap.get(user.id()))
              .ifPresent(user -> item.currentDocumentationUnitProcessStep().setUser(user));
          return item;
        });
  }

  private DocumentationUnit filterProcessStepsOfOtherDocumentationOffices(
      DocumentationUnit documentable, User user) {

    if (user == null || user.documentationOffice() == null) {
      return addProcessSteps(documentable, Collections.emptyList());
    }

    List<DocumentationUnitProcessStep> processStepsOfMyDocumentationOffice = new ArrayList<>();
    if (documentable.processSteps() != null) {
      processStepsOfMyDocumentationOffice =
          documentable.processSteps().stream()
              .filter(
                  processStep -> {
                    if (processStep.getUser() == null) {
                      return true;
                    }

                    if (processStep.getUser().id() == null) {
                      return false;
                    }

                    if (processStep.getUser().documentationOffice() == null) {
                      return false;
                    }

                    return processStep
                        .getUser()
                        .documentationOffice()
                        .id()
                        .equals(user.documentationOffice().id());
                  })
              .toList();
    }

    return addProcessSteps(documentable, processStepsOfMyDocumentationOffice);
  }

  private DocumentationUnit addProcessSteps(
      DocumentationUnit documentable, List<DocumentationUnitProcessStep> processSteps) {
    if (documentable instanceof Decision decision) {
      return decision.toBuilder().processSteps(processSteps).build();
    } else if (documentable instanceof PendingProceeding pendingProceeding) {
      return pendingProceeding.toBuilder().processSteps(processSteps).build();
    }

    return documentable;
  }

  public DocumentationUnit getByUuid(UUID documentationUnitId)
      throws DocumentationUnitNotExistsException {
    return repository.findByUuid(documentationUnitId, null);
  }

  public DocumentationUnit getByUuid(UUID documentationUnitId, User user)
      throws DocumentationUnitNotExistsException {
    return repository.findByUuid(documentationUnitId, user);
  }

  @Transactional(transactionManager = "jpaTransactionManager")
  public String deleteByUuid(UUID documentationUnitId) throws DocumentationUnitNotExistsException {

    DocumentationUnit docUnit = getByUuid(documentationUnitId);

    Map<RelatedDocumentationType, Long> relatedEntities =
        repository.getAllRelatedDocumentationUnitsByDocumentNumber(docUnit.documentNumber());

    if (!(relatedEntities == null
        || relatedEntities.isEmpty()
        || relatedEntities.values().stream().mapToLong(Long::longValue).sum() == 0)) {

      log.atInfo()
          .setMessage("Could not delete document unit because of related entities")
          .addKeyValue(DOCUMENT_NUMBER, docUnit.documentNumber())
          .addKeyValue("id", documentationUnitId)
          .addKeyValue("relatedEntities", relatedEntities)
          .log();

      throw new DocumentationUnitDeletionException(
          "Die Dokumentationseinheit konnte nicht gelöscht werden, da", relatedEntities);
    }

    if (PortalPublicationStatus.PUBLISHED.equals(docUnit.portalPublicationStatus())) {
      log.atInfo()
          .setMessage("Doc unit deletion was prohibited because it is published in the portal.")
          .addKeyValue(DOCUMENT_NUMBER, docUnit.documentNumber())
          .addKeyValue("id", documentationUnitId)
          .log();
      throw new DocumentationUnitDeletionException(
          "Die Dokumentationseinheit konnte nicht gelöscht werden, da Sie im Portal veröffentlicht ist.\nSie muss zuerst zurückgezogen werden.");
    }

    if (docUnit instanceof Decision decision
        && decision.attachments() != null
        && !decision.attachments().isEmpty()) {
      attachmentService.deleteAllObjectsFromBucketForDocumentationUnit(documentationUnitId);
    }

    if (docUnit instanceof Decision decision) {
      saveForRecycling(decision);
    }

    try {
      repository.delete(docUnit);
    } catch (Exception e) {
      log.error("Could not delete documentation unit from database {}", documentationUnitId, e);
      throw new DocumentationUnitDeletionException(
          "Could not delete documentation unit from database");
    }

    log.atInfo()
        .setMessage("Documentation unit deleted.")
        .addKeyValue(DOCUMENT_NUMBER, docUnit.documentNumber())
        .addKeyValue("id", documentationUnitId)
        .log();

    return "Dokumentationseinheit gelöscht: " + documentationUnitId;
  }

  /**
   * Update a documenation unit with a {@link RisJsonPatch}.
   *
   * @param documentationUnitId id of the documentation unit
   * @param patch patch to update the documentation unit
   * @param user current logged-in user
   * @return a patch with changes the client not know yet (automatically set fields, fields update
   *     by other user)
   * @throws DocumentationUnitNotExistsException if the documentation unit not exist
   * @throws DocumentationUnitPatchException if the documentation unit couldn't updated
   */
  public RisJsonPatch updateDocumentationUnit(
      UUID documentationUnitId, RisJsonPatch patch, User user)
      throws DocumentationUnitNotExistsException,
          DocumentationUnitPatchException,
          ProcessStepNotFoundException {

    /*
     next iteration:
       * handle add operation with null values (remove of values from two users at the same time)
       * handle unique following operation (sometimes by add and remove operations at the same time)
    */

    var existingDocumentationUnit = getByUuid(documentationUnitId, user);

    DocumentationUnit patchedDocumentationUnitWithBase64Images;

    long newVersion =
        existingDocumentationUnit.version() != null ? existingDocumentationUnit.version() + 1 : 1L;

    var newPatch =
        patchMapperService.calculatePatch(
            existingDocumentationUnit.uuid(), patch.documentationUnitVersion());

    logPatches(patch, newPatch, newVersion, documentationUnitId);

    var toFrontendJsonPatch = new JsonPatch(Collections.emptyList());
    RisJsonPatch toFrontend;

    if (patch.patch().getOperations().isEmpty()
        || PatchMapperService.containsOnlyVersionInPatch(patch.patch())) {
      return defaultNoUpdateResponse(newPatch, existingDocumentationUnit);
    }

    var toUpdate = patchMapperService.removePatchForSamePath(patch.patch(), newPatch);

    log.debug("version {} - update patch: {}", patch.documentationUnitVersion(), toUpdate);

    if (!toUpdate.getOperations().isEmpty()) {
      toUpdate = patchMapperService.removeTextCheckTags(toUpdate);

      toUpdate =
          patchMapperService.removeOpsWhereContentNotChanged(toUpdate, existingDocumentationUnit);

      if (!toUpdate.getOperations().isEmpty()) {
        // Saving a return patch including the base64 images as src attributes with an api path.
        patchedDocumentationUnitWithBase64Images =
            cloneDocumentationUnitWithNewVersion(
                patchMapperService.applyPatchToEntity(toUpdate, existingDocumentationUnit),
                newVersion);

        toUpdate =
            patchMapperService.extractAndStoreBase64Images(
                toUpdate, existingDocumentationUnit, null);

        DocumentationUnit patchedDocumentationUnit =
            patchMapperService.applyPatchToEntity(toUpdate, existingDocumentationUnit);

        patchedDocumentationUnit =
            cloneDocumentationUnitWithNewVersion(patchedDocumentationUnit, newVersion);

        DuplicateCheckStatus duplicateCheckStatus = getDuplicateCheckStatus(patch);

        DocumentationUnit updatedDocumentationUnit =
            updateDocumentationUnit(user, patchedDocumentationUnit, duplicateCheckStatus);

        toFrontendJsonPatch =
            patchMapperService.getDiffPatch(
                patchedDocumentationUnitWithBase64Images, updatedDocumentationUnit);

        log.debug(
            "version {} - raw to frontend patch: {}",
            patch.documentationUnitVersion(),
            toFrontendJsonPatch);

        JsonPatch toSaveJsonPatch =
            patchMapperService.addUpdatePatch(toUpdate, toFrontendJsonPatch);

        log.debug(
            "version {} - to save patch: {}", patch.documentationUnitVersion(), toSaveJsonPatch);

        patchMapperService.savePatch(
            toSaveJsonPatch, existingDocumentationUnit.uuid(), existingDocumentationUnit.version());
      }
    }

    toFrontend =
        patchMapperService.handlePatchForSamePath(
            existingDocumentationUnit, toFrontendJsonPatch, patch.patch(), newPatch);

    log.debug(
        "version {} - cleaned to frontend patch: {}", patch.documentationUnitVersion(), toFrontend);

    if (toFrontend.errorPaths().isEmpty()) {
      toFrontend = toFrontend.toBuilder().documentationUnitVersion(newVersion).build();
    } else {
      toFrontend =
          toFrontend.toBuilder()
              .documentationUnitVersion(existingDocumentationUnit.version())
              .build();
    }

    log.debug(
        "version {} - second cleaned to frontend patch: {}",
        patch.documentationUnitVersion(),
        toFrontend);

    return toFrontend;
  }

  private RisJsonPatch defaultNoUpdateResponse(
      JsonPatch newPatch, DocumentationUnit existingDocumentationUnit) {
    if (newPatch == null) {
      newPatch = new JsonPatch(Collections.emptyList());
    }
    return new RisJsonPatch(existingDocumentationUnit.version(), newPatch, Collections.emptyList());
  }

  private void logPatches(
      RisJsonPatch patch, JsonPatch newPatch, long newVersion, UUID documentationUnitId) {
    if (patch.patch().getOperations().isEmpty() || newPatch.getOperations().isEmpty()) {
      return;
    }

    log.debug(
        "documentation unit '{}' with patch '{}' for version '{}'",
        documentationUnitId,
        patch.documentationUnitVersion(),
        patch.patch());
    log.debug("new version is {}", newVersion);
    log.debug("version {} - patch in database: {}", patch.documentationUnitVersion(), newPatch);
  }

  /** Return a documentation unit with a new version */
  private static DocumentationUnit cloneDocumentationUnitWithNewVersion(
      DocumentationUnit documentationUnit, long newVersion) {
    if (documentationUnit instanceof Decision docUnit) {
      return docUnit.toBuilder().version(newVersion).build();
    } else if (documentationUnit instanceof PendingProceeding pendingProceeding) {
      return pendingProceeding.toBuilder().version(newVersion).build();
    }
    throw new DocumentationUnitException(
        "Update not supported for Documentable type: " + documentationUnit.getClass());
  }

  private DocumentationUnit updateDocumentationUnit(
      User user,
      DocumentationUnit patchedDocumentationUnit,
      DuplicateCheckStatus duplicateCheckStatus)
      throws DocumentationUnitNotExistsException, ProcessStepNotFoundException {
    DocumentationUnit updatedDocumentationUnit;
    if (patchedDocumentationUnit instanceof Decision docUnit) {
      updatedDocumentationUnit = updateDocumentationUnit(docUnit, duplicateCheckStatus, user);
    } else if (patchedDocumentationUnit instanceof PendingProceeding pendingProceeding) {
      updatedDocumentationUnit = updatePendingProceeding(pendingProceeding, user);
    } else {
      throw new DocumentationUnitException(
          "Update not supported for Documentable type: " + patchedDocumentationUnit.getClass());
    }
    return updatedDocumentationUnit;
  }

  public Decision updateDocumentationUnit(Decision decision)
      throws DocumentationUnitNotExistsException {
    return this.updateDocumentationUnit(decision, DuplicateCheckStatus.DISABLED, null);
  }

  public Decision updateDocumentationUnit(
      Decision decision, DuplicateCheckStatus duplicateCheckStatus, User user)
      throws DocumentationUnitNotExistsException, ProcessStepNotFoundException {
    repository.saveKeywords(decision);
    repository.saveFieldsOfLaw(decision);
    repository.saveProcedures(decision, user);
    var processStepChanged = repository.saveProcessSteps(decision, user);

    repository.save(decision, user, null, processStepChanged);

    if (duplicateCheckStatus == DuplicateCheckStatus.ENABLED) {
      try {
        duplicateCheckService.checkDuplicates(decision.documentNumber());
      } catch (Exception e) {
        // Errors in duplicate check should not affect saving, logging in service
      }
    }

    return (Decision) repository.findByUuid(decision.uuid(), user);
  }

  public PendingProceeding updatePendingProceeding(PendingProceeding pendingProceeding, User user)
      throws DocumentationUnitNotExistsException {
    repository.saveKeywords(pendingProceeding);
    repository.saveFieldsOfLaw(pendingProceeding);
    repository.saveProcessSteps(pendingProceeding, user);

    repository.save(pendingProceeding, user);

    return (PendingProceeding) repository.findByUuid(pendingProceeding.uuid(), user);
  }

  public Slice<RelatedDocumentationUnit> searchLinkableDocumentationUnits(
      RelatedDocumentationUnit relatedDocumentationUnit,
      DocumentationOffice documentationOffice,
      Optional<String> documentNumberToExclude,
      Pageable pageable) {

    if (relatedDocumentationUnit.getFileNumber() != null) {
      relatedDocumentationUnit.setFileNumber(
          normalizeSpace(relatedDocumentationUnit.getFileNumber()));
    }
    return repository.searchLinkableDocumentationUnits(
        relatedDocumentationUnit,
        documentationOffice,
        documentNumberToExclude.orElse(null),
        pageable);
  }

  public String validateSingleNorm(SingleNormValidationInfo singleNormValidationInfo) {
    Set<ConstraintViolation<SingleNormValidationInfo>> violations =
        validator.validate(singleNormValidationInfo);

    if (violations.isEmpty()) {
      return "Ok";
    }
    return "Validation error";
  }

  public String assignDocumentationOffice(
      UUID documentationUnitId, UUID documentationOfficeId, User user)
      throws DocumentationUnitNotExistsException, DocumentationOfficeNotExistsException {
    DocumentationUnit documentationUnit = repository.findByUuid(documentationUnitId, user);
    var documentationOffice = documentationOfficeService.findByUuid(documentationOfficeId);
    if (documentationUnit instanceof Decision decision) {
      // Procedures need to be unassigned as they are linked to the previous documentation Office
      repository.unassignProcedures(decision.uuid());
      repository.saveDocumentationOffice(documentationUnitId, documentationOffice, user);

      decision =
          decision.toBuilder()
              .currentDocumentationUnitProcessStep(
                  DocumentationUnitProcessStep.builder()
                      .id(UUID.randomUUID())
                      .createdAt(LocalDateTime.now())
                      .processStep(
                          processStepService
                              .getProcessStepForName("Neu")
                              .orElseThrow(
                                  () ->
                                      new ProcessStepNotFoundException(
                                          "Process Step 'Neu' not found")))
                      .build())
              .processSteps(List.of())
              .build();
      repository.saveProcessSteps(decision, user);
      return "The documentation office [%s] has been successfully assigned."
          .formatted(documentationOffice.abbreviation());
    }
    throw new DocumentationUnitException(
        "The documentation office could not be reassigned: Document is not a decision.");
  }

  private void saveForRecycling(DocumentationUnit documentationUnit) {
    try {
      documentNumberRecyclingService.addForRecycling(
          documentationUnit.uuid(),
          documentationUnit.documentNumber(),
          documentationUnit.coreData().documentationOffice().abbreviation());

    } catch (Exception e) {
      log.info(
          "Won't recycle document number {}: {}",
          documentationUnit.documentNumber(),
          e.getMessage());
    }
  }

  @Transactional(rollbackFor = BadRequestException.class)
  public void bulkAssignProcedure(
      @NotNull List<UUID> documentationUnitIds, String procedureLabel, User user)
      throws DocumentationUnitNotExistsException, BadRequestException {
    Procedure procedure = Procedure.builder().label(procedureLabel).build();
    for (UUID documentationUnitId : documentationUnitIds) {
      DocumentationUnit documentationUnit = repository.findByUuid(documentationUnitId, user);
      if (documentationUnit instanceof Decision docUnit) {
        Decision updatedDocUnit =
            docUnit.toBuilder()
                .coreData(docUnit.coreData().toBuilder().procedure(procedure).build())
                // When a procedure is assigned, the doc unit is removed from the inbox
                // This might be replaced by explicit workflow management later
                .inboxStatus(null)
                .build();
        // Calling updateDocumentationUnit throws a JPA exception, unclear why.
        repository.saveProcedures(updatedDocUnit, user);
        repository.save(updatedDocUnit, user);
      } else {
        throw new BadRequestException("Can only assign procedures to decisions.");
      }
    }
  }

  public Image getImageBytes(String documentNumber, String imageName)
      throws ImageNotExistsException, DocumentationUnitNotExistsException {
    var docUnitId = repository.findIdForDocumentNumber(documentNumber);
    return attachmentService
        .findByDocumentationUnitIdAndFileName(docUnitId, imageName)
        .orElseThrow(
            () ->
                new ImageNotExistsException(
                    "Image not found for documentation unit: "
                        + docUnitId
                        + " and image name: "
                        + imageName));
  }

  public enum DuplicateCheckStatus {
    ENABLED,
    DISABLED
  }

  private static DuplicateCheckStatus getDuplicateCheckStatus(RisJsonPatch patch) {
    boolean hasPathRelevantForDuplicateCheck =
        patch.patch().getOperations().stream()
            .map(JsonPatchOperation::getPath)
            .anyMatch(path -> pathsForDuplicateCheck.stream().anyMatch(path::contains));
    if (hasPathRelevantForDuplicateCheck) {
      return DuplicateCheckStatus.ENABLED;
    } else {
      return DuplicateCheckStatus.DISABLED;
    }
  }
}
