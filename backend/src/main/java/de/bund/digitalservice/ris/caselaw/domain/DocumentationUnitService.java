package de.bund.digitalservice.ris.caselaw.domain;

import static de.bund.digitalservice.ris.caselaw.domain.StringUtils.normalizeSpace;

import com.gravity9.jsonpatch.JsonPatch;
import com.gravity9.jsonpatch.JsonPatchOperation;
import de.bund.digitalservice.ris.caselaw.adapter.FmxService;
import de.bund.digitalservice.ris.caselaw.domain.exception.DocumentationUnitDeletionException;
import de.bund.digitalservice.ris.caselaw.domain.exception.DocumentationUnitException;
import de.bund.digitalservice.ris.caselaw.domain.exception.DocumentationUnitNotExistsException;
import de.bund.digitalservice.ris.caselaw.domain.exception.DocumentationUnitPatchException;
import de.bund.digitalservice.ris.caselaw.domain.mapper.PatchMapperService;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@SuppressWarnings("java:S6539") // Too many dependencies warning
public class DocumentationUnitService {

  private final DocumentationUnitRepository repository;
  private final DocumentNumberService documentNumberService;
  private final DocumentationUnitStatusService statusService;
  private final AttachmentService attachmentService;
  private final FmxService fmxService;
  private final DocumentNumberRecyclingService documentNumberRecyclingService;
  private final PatchMapperService patchMapperService;
  private final AuthService authService;
  private final UserService userService;
  private final Validator validator;
  private final DuplicateCheckService duplicateCheckService;
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

  public DocumentationUnitService(
      DocumentationUnitRepository repository,
      DocumentNumberService documentNumberService,
      DocumentationUnitStatusService statusService,
      DocumentNumberRecyclingService documentNumberRecyclingService,
      UserService userService,
      Validator validator,
      AttachmentService attachmentService,
      FmxService fmxService,
      @Lazy AuthService authService,
      PatchMapperService patchMapperService,
      DuplicateCheckService duplicateCheckService) {

    this.repository = repository;
    this.documentNumberService = documentNumberService;
    this.documentNumberRecyclingService = documentNumberRecyclingService;
    this.userService = userService;
    this.validator = validator;
    this.attachmentService = attachmentService;
    this.fmxService = fmxService;
    this.patchMapperService = patchMapperService;
    this.statusService = statusService;
    this.authService = authService;
    this.duplicateCheckService = duplicateCheckService;
  }

  @Transactional(transactionManager = "jpaTransactionManager")
  public DocumentationUnit generateNewDocumentationUnit(
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

    boolean isExternalHandover =
        params.documentationOffice() != null
            && userDocOffice != null
            && !userDocOffice.uuid().equals(params.documentationOffice().uuid());

    DocumentationUnit docUnit =
        DocumentationUnit.builder()
            .version(0L)
            .documentNumber(generateDocumentNumber(params.documentationOffice()))
            .coreData(
                CoreData.builder()
                    .documentationOffice(params.documentationOffice())
                    .fileNumbers(params.fileNumber() == null ? null : List.of(params.fileNumber()))
                    .documentType(params.documentType())
                    .decisionDate(params.decisionDate())
                    .court(params.court())
                    .creatingDocOffice(isExternalHandover ? userDocOffice : null)
                    .legalEffect(
                        LegalEffect.deriveFrom(params.court(), true)
                            .orElse(LegalEffect.NOT_SPECIFIED)
                            .getLabel())
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

    var newDocumentationUnit =
        repository.createNewDocumentationUnit(docUnit, status, params.reference(), user);

    //    fmxService.getDataFromEurlex(
    //        "67474f27-bded-11ef-91ed-01aa75ed71a1.0001.03", newDocumentationUnit);
    if (params.celexNumber() != null && !params.celexNumber().isBlank()) {}
    duplicateCheckService.checkDuplicates(docUnit.documentNumber());
    return newDocumentationUnit;
  }

  private String generateDocumentNumber(DocumentationOffice documentationOffice) {
    try {
      return documentNumberService.generateDocumentNumber(documentationOffice.abbreviation());
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
      Optional<InboxStatus> inboxStatus) {

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
            .build();

    Slice<DocumentationUnitListItem> documentationUnitListItems =
        repository.searchByDocumentationUnitSearchInput(
            PageRequest.of(pageable.getPageNumber(), pageable.getPageSize()),
            oidcUser,
            searchInput);

    return documentationUnitListItems.map(item -> addPermissions(oidcUser, item));
  }

  public DocumentationUnitListItem takeOverDocumentationUnit(
      String documentNumber, OidcUser oidcUser) throws DocumentationUnitNotExistsException {

    Status status =
        Status.builder().publicationStatus(PublicationStatus.UNPUBLISHED).withError(false).build();
    statusService.update(documentNumber, status, userService.getUser(oidcUser));

    return addPermissions(
        oidcUser, repository.findDocumentationUnitListItemByDocumentNumber(documentNumber));
  }

  public void setPublicationDateTime(UUID uuid) {
    repository.saveLastPublicationDateTime(uuid);
  }

  private DocumentationUnitListItem addPermissions(
      OidcUser oidcUser, DocumentationUnitListItem listItem) {

    boolean hasWriteAccess =
        !(listItem.documentType() != null
                && listItem
                    .documentType()
                    .jurisShortcut()
                    .equals("Anh")) // pending proceedings are not to be edited or deleted yet
            && authService.userHasWriteAccess(
                oidcUser,
                listItem.creatingDocumentationOffice(),
                listItem.documentationOffice(),
                listItem.status());
    boolean isInternalUser = authService.userIsInternal().apply(oidcUser);

    return listItem.toBuilder()
        .isDeletable(hasWriteAccess && isInternalUser)
        .isEditable(
            (hasWriteAccess
                && (isInternalUser || authService.isAssignedViaProcedure().apply(listItem.uuid()))))
        .build();
  }

  public Documentable getByDocumentNumber(String documentNumber)
      throws DocumentationUnitNotExistsException {
    return repository.findByDocumentNumber(documentNumber);
  }

  public Documentable getByDocumentNumberWithUser(String documentNumber, OidcUser oidcUser)
      throws DocumentationUnitNotExistsException {
    var documentable =
        repository.findByDocumentNumber(documentNumber, userService.getUser(oidcUser));
    if (documentable instanceof DocumentationUnit documentationUnit) {
      return documentationUnit.toBuilder()
          .isEditable(
              authService.userHasWriteAccess(
                  oidcUser,
                  documentationUnit.coreData().creatingDocOffice(),
                  documentationUnit.coreData().documentationOffice(),
                  documentationUnit.status()))
          .build();
    } else {
      log.info("Documentable type not supported: {}", documentable.getClass().getName());
      return documentable;
    }
  }

  public Documentable getByUuid(UUID documentationUnitId)
      throws DocumentationUnitNotExistsException {
    return repository.findByUuid(documentationUnitId, null);
  }

  public Documentable getByUuid(UUID documentationUnitId, User user)
      throws DocumentationUnitNotExistsException {
    return repository.findByUuid(documentationUnitId, user);
  }

  @Transactional(transactionManager = "jpaTransactionManager")
  public String deleteByUuid(UUID documentationUnitId) throws DocumentationUnitNotExistsException {

    Documentable docUnit = getByUuid(documentationUnitId);
    Map<RelatedDocumentationType, Long> relatedEntities =
        repository.getAllRelatedDocumentationUnitsByDocumentNumber(docUnit.documentNumber());

    if (!(relatedEntities == null
        || relatedEntities.isEmpty()
        || relatedEntities.values().stream().mapToLong(Long::longValue).sum() == 0)) {

      log.debug(
          "Could not delete document unit {} cause of related entities: {}",
          documentationUnitId,
          relatedEntities);

      throw new DocumentationUnitDeletionException(
          "Die Dokumentationseinheit konnte nicht gelöscht werden, da", relatedEntities);
    }

    log.debug("Deleting DocumentationUnitDTO " + documentationUnitId);

    if (docUnit instanceof DocumentationUnit decision
        && decision.attachments() != null
        && !decision.attachments().isEmpty())
      attachmentService.deleteAllObjectsFromBucketForDocumentationUnit(documentationUnitId);

    saveForRecycling(docUnit);
    try {
      repository.delete(docUnit);
    } catch (Exception e) {
      log.error("Could not delete documentation unit from database {}", documentationUnitId, e);
      throw new DocumentationUnitDeletionException(
          "Could not delete documentation unit from database");
    }

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
      throws DocumentationUnitNotExistsException, DocumentationUnitPatchException {

    /*
     next iteration:
       * handle add operation with null values (remove of values from two users at the same time)
       * handle unique following operation (sometimes by add and remove operations at the same time)
    */

    Documentable documentable = getByUuid(documentationUnitId, user);

    if (!(documentable instanceof DocumentationUnit existingDocumentationUnit)) {
      throw new UnsupportedOperationException(
          "Update not supported for Documentable type: " + documentable.getClass());
    }

    long newVersion = 1L;
    if (existingDocumentationUnit.version() != null) {
      newVersion = existingDocumentationUnit.version() + 1;
    }

    JsonPatch newPatch =
        patchMapperService.calculatePatch(
            existingDocumentationUnit.uuid(), patch.documentationUnitVersion());

    if (!patch.patch().getOperations().isEmpty() || !newPatch.getOperations().isEmpty()) {
      log.debug(
          "documentation unit '{}' with patch '{}' for version '{}'",
          documentationUnitId,
          patch.documentationUnitVersion(),
          patch.patch());
      log.debug("new version is {}", newVersion);
      log.debug("version {} - patch in database: {}", patch.documentationUnitVersion(), newPatch);
    }

    JsonPatch toFrontendJsonPatch = new JsonPatch(Collections.emptyList());
    RisJsonPatch toFrontend;
    if (!patch.patch().getOperations().isEmpty()
        && !PatchMapperService.containsOnlyVersionInPatch(patch.patch())) {
      JsonPatch toUpdate = patchMapperService.removePatchForSamePath(patch.patch(), newPatch);

      log.debug("version {} - update patch: {}", patch.documentationUnitVersion(), toUpdate);

      if (!toUpdate.getOperations().isEmpty()) {
        toUpdate = patchMapperService.removeTextCheckTags(toUpdate);

        DocumentationUnit patchedDocumentationUnit =
            patchMapperService.applyPatchToEntity(toUpdate, existingDocumentationUnit);
        patchedDocumentationUnit = patchedDocumentationUnit.toBuilder().version(newVersion).build();

        DuplicateCheckStatus duplicateCheckStatus = getDuplicateCheckStatus(patch);

        DocumentationUnit updatedDocumentationUnit =
            updateDocumentationUnit(patchedDocumentationUnit, duplicateCheckStatus, user);

        toFrontendJsonPatch =
            patchMapperService.getDiffPatch(patchedDocumentationUnit, updatedDocumentationUnit);

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

      toFrontend =
          patchMapperService.handlePatchForSamePath(
              existingDocumentationUnit, toFrontendJsonPatch, patch.patch(), newPatch);

      log.debug(
          "version {} - cleaned to frontend patch: {}",
          patch.documentationUnitVersion(),
          toFrontend);

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
    } else {
      if (newPatch == null) {
        newPatch = new JsonPatch(Collections.emptyList());
      }
      toFrontend =
          new RisJsonPatch(existingDocumentationUnit.version(), newPatch, Collections.emptyList());
    }

    return toFrontend;
  }

  public DocumentationUnit updateDocumentationUnit(DocumentationUnit documentationUnit)
      throws DocumentationUnitNotExistsException {
    return this.updateDocumentationUnit(documentationUnit, DuplicateCheckStatus.DISABLED, null);
  }

  public DocumentationUnit updateDocumentationUnit(
      DocumentationUnit documentationUnit, DuplicateCheckStatus duplicateCheckStatus, User user)
      throws DocumentationUnitNotExistsException {
    repository.saveKeywords(documentationUnit);
    repository.saveFieldsOfLaw(documentationUnit);
    repository.saveProcedures(documentationUnit, user);

    repository.save(documentationUnit, user);

    if (duplicateCheckStatus == DuplicateCheckStatus.ENABLED) {
      try {
        duplicateCheckService.checkDuplicates(documentationUnit.documentNumber());
      } catch (Exception e) {
        // Errors in duplicate check should not affect saving, logging in service
      }
    }

    return (DocumentationUnit) repository.findByUuid(documentationUnit.uuid(), user);
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

  private void saveForRecycling(Documentable documentationUnit) {
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
