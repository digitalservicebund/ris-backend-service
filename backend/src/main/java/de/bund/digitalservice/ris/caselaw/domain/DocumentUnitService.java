package de.bund.digitalservice.ris.caselaw.domain;

import static de.bund.digitalservice.ris.caselaw.domain.StringUtils.normalizeSpace;

import com.gravity9.jsonpatch.JsonPatch;
import de.bund.digitalservice.ris.caselaw.domain.court.Court;
import de.bund.digitalservice.ris.caselaw.domain.court.CourtRepository;
import de.bund.digitalservice.ris.caselaw.domain.docx.DocXPropertyField;
import de.bund.digitalservice.ris.caselaw.domain.docx.Docx2Html;
import de.bund.digitalservice.ris.caselaw.domain.exception.DocumentUnitDeletionException;
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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class DocumentUnitService {

  private final DocumentUnitRepository repository;
  private final DocumentNumberService documentNumberService;
  private final AttachmentService attachmentService;
  private final DocumentNumberRecyclingService documentNumberRecyclingService;
  private final PatchMapperService patchMapperService;
  private final CourtRepository courtRepository;
  private final Validator validator;

  public DocumentUnitService(
      DocumentUnitRepository repository,
      DocumentNumberService documentNumberService,
      DocumentNumberRecyclingService documentNumberRecyclingService,
      Validator validator,
      AttachmentService attachmentService,
      PatchMapperService patchMapperService,
      CourtRepository courtRepository) {

    this.repository = repository;
    this.documentNumberService = documentNumberService;
    this.documentNumberRecyclingService = documentNumberRecyclingService;
    this.validator = validator;
    this.attachmentService = attachmentService;
    this.patchMapperService = patchMapperService;
    this.courtRepository = courtRepository;
  }

  @Transactional(transactionManager = "jpaTransactionManager")
  public DocumentUnit generateNewDocumentUnit(DocumentationOffice documentationOffice)
      throws DocumentationUnitException {
    var documentNumber = generateDocumentNumber(documentationOffice);
    return repository.createNewDocumentUnit(documentNumber, documentationOffice);
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
      DocumentationOffice documentationOffice,
      Optional<String> documentNumber,
      Optional<String> fileNumber,
      Optional<String> courtType,
      Optional<String> courtLocation,
      Optional<LocalDate> decisionDate,
      Optional<LocalDate> decisionDateEnd,
      Optional<String> publicationStatus,
      Optional<Boolean> withError,
      Optional<Boolean> myDocOfficeOnly) {

    DocumentationUnitSearchInput searchInput =
        DocumentationUnitSearchInput.builder()
            .documentNumber(normalizeSpace(documentNumber.orElse(null)))
            .fileNumber(normalizeSpace(fileNumber.orElse(null)))
            .courtType(normalizeSpace(courtType.orElse(null)))
            .courtLocation(normalizeSpace(courtLocation.orElse(null)))
            .decisionDate(decisionDate.orElse(null))
            .decisionDateEnd(decisionDateEnd.orElse(null))
            .status(
                (publicationStatus.isPresent() || withError.isPresent())
                    ? Status.builder()
                        .publicationStatus(
                            publicationStatus.map(PublicationStatus::valueOf).orElse(null))
                        .withError(withError.orElse(false))
                        .build()
                    : null)
            .myDocOfficeOnly(myDocOfficeOnly.orElse(false))
            .build();

    return repository.searchByDocumentationUnitSearchInput(
        PageRequest.of(pageable.getPageNumber(), pageable.getPageSize()),
        documentationOffice,
        searchInput);
  }

  public DocumentUnit getByDocumentNumber(String documentNumber) {
    try {
      var optionalDocumentUnit = repository.findByDocumentNumber(documentNumber);
      return optionalDocumentUnit.orElseThrow();
    } catch (Exception e) {
      return null;
    }
  }

  public DocumentUnit getByUuid(UUID documentUnitUuid) {
    return repository.findByUuid(documentUnitUuid).orElseThrow();
  }

  @Transactional(transactionManager = "jpaTransactionManager")
  public String deleteByUuid(UUID documentUnitUuid) throws DocumentationUnitNotExistsException {

    Map<RelatedDocumentationType, Long> relatedEntities =
        repository.getAllDocumentationUnitWhichLink(documentUnitUuid);

    if (!(relatedEntities == null
        || relatedEntities.isEmpty()
        || relatedEntities.values().stream().mapToLong(Long::longValue).sum() == 0)) {

      log.debug(
          "Could not delete document unit {} cause of related entities: {}",
          documentUnitUuid,
          relatedEntities);

      throw new DocumentUnitDeletionException(
          "Die Dokumentationseinheit konnte nicht gelöscht werden, da", relatedEntities);
    }

    DocumentUnit documentUnit =
        repository
            .findByUuid(documentUnitUuid)
            .orElseThrow(() -> new DocumentationUnitNotExistsException(documentUnitUuid));

    log.debug("Deleting DocumentUnitDTO " + documentUnitUuid);

    if (documentUnit.attachments() != null && !documentUnit.attachments().isEmpty())
      attachmentService.deleteAllObjectsFromBucketForDocumentationUnit(documentUnitUuid);

    saveForRecycling(documentUnit);
    repository.delete(documentUnit);
    return "Dokumentationseinheit gelöscht: " + documentUnitUuid;
  }

  /**
   * Update a documenation unit with a {@link RisJsonPatch}.
   *
   * @param documentationUnitId id of the documentation unit
   * @param patch patch to update the documentation unit
   * @return a patch with changes the client not know yet (automatically set fields, fields update
   *     by other user)
   * @throws DocumentationUnitNotExistsException if the documentation unit not exist
   * @throws DocumentationUnitPatchException if the documentation unit couldn't updated
   */
  public RisJsonPatch updateDocumentUnit(UUID documentationUnitId, RisJsonPatch patch)
      throws DocumentationUnitNotExistsException, DocumentationUnitPatchException {

    log.debug(
        "documentation unit '{}' with patch '{}' for version '{}'",
        documentationUnitId,
        patch.documentationUnitVersion(),
        patch.patch());

    DocumentUnit existingDocumentationUnit = getByUuid(documentationUnitId);

    long newVersion = 1L;
    if (existingDocumentationUnit.version() != null) {
      newVersion = existingDocumentationUnit.version() + 1;
    }

    log.debug("new version is {}", newVersion);

    JsonPatch newPatch =
        patchMapperService.calculatePatch(
            existingDocumentationUnit.uuid(), patch.documentationUnitVersion());

    log.debug("version {} - patch in database: {}", patch.documentationUnitVersion(), newPatch);

    RisJsonPatch toFrontend;
    if (!patch.patch().getOperations().isEmpty()) {
      JsonPatch toUpdate = patchMapperService.removePatchForSamePath(patch.patch(), newPatch);

      log.debug("version {} - update patch: {}", patch.documentationUnitVersion(), toUpdate);

      DocumentUnit patchedDocumentationUnit =
          patchMapperService.applyPatchToEntity(toUpdate, existingDocumentationUnit);
      patchedDocumentationUnit = patchedDocumentationUnit.toBuilder().version(newVersion).build();
      DocumentUnit updatedDocumentUnit = updateDocumentUnit(patchedDocumentationUnit);

      JsonPatch toFrontendJsonPatch =
          patchMapperService.getDiffPatch(patchedDocumentationUnit, updatedDocumentUnit);

      log.debug(
          "version {} - raw to frontend patch: {}",
          patch.documentationUnitVersion(),
          toFrontendJsonPatch);

      JsonPatch toSaveJsonPatch = patchMapperService.addUpdatePatch(toUpdate, toFrontendJsonPatch);

      log.debug(
          "version {} - to save patch: {}", patch.documentationUnitVersion(), toSaveJsonPatch);

      patchMapperService.savePatch(
          toSaveJsonPatch, existingDocumentationUnit.uuid(), existingDocumentationUnit.version());

      toFrontend =
          patchMapperService.handlePatchForSamePath(
              existingDocumentationUnit, toFrontendJsonPatch, patch.patch(), newPatch);

      log.debug(
          "version {} - cleaned to frontend patch: {}",
          patch.documentationUnitVersion(),
          toFrontend);

      // TODO: check logs. possible not needed
      toFrontend = patchMapperService.removeExistPatches(toFrontend, patch.patch());
      toFrontend = toFrontend.toBuilder().documentationUnitVersion(newVersion).build();

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

  public DocumentUnit updateDocumentUnit(DocumentUnit documentUnit)
      throws DocumentationUnitNotExistsException {
    repository.saveKeywords(documentUnit);
    repository.saveFieldsOfLaw(documentUnit);
    repository.saveProcedures(documentUnit);

    repository.save(documentUnit);

    return repository
        .findByUuid(documentUnit.uuid())
        .orElseThrow(() -> new DocumentationUnitNotExistsException(documentUnit.documentNumber()));
  }

  public Slice<RelatedDocumentationUnit> searchLinkableDocumentationUnits(
      RelatedDocumentationUnit relatedDocumentationUnit,
      DocumentationOffice documentationOffice,
      String documentNumberToExclude,
      Pageable pageable) {

    if (relatedDocumentationUnit.getFileNumber() != null) {
      relatedDocumentationUnit.setFileNumber(
          normalizeSpace(relatedDocumentationUnit.getFileNumber()));
    }
    return repository.searchLinkableDocumentationUnits(
        relatedDocumentationUnit, documentationOffice, documentNumberToExclude, pageable);
  }

  public String validateSingleNorm(SingleNormValidationInfo singleNormValidationInfo) {
    Set<ConstraintViolation<SingleNormValidationInfo>> violations =
        validator.validate(singleNormValidationInfo);

    if (violations.isEmpty()) {
      return "Ok";
    }
    return "Validation error";
  }

  public void initializeCoreData(UUID uuid, Docx2Html docx2html) {
    Optional<DocumentUnit> documentationUnitDTOOptional = repository.findByUuid(uuid);
    if (documentationUnitDTOOptional.isEmpty()) {
      return;
    }
    DocumentUnit documentUnit = documentationUnitDTOOptional.get();
    CoreData.CoreDataBuilder builder = documentationUnitDTOOptional.get().coreData().toBuilder();

    initializeEcli(docx2html.ecliList(), documentUnit, builder);
    initializeFieldsFromProperties(docx2html.properties(), documentUnit, builder);

    repository.save(documentUnit.toBuilder().coreData(builder.build()).build());
  }

  @SuppressWarnings("java:S3776")
  private void initializeFieldsFromProperties(
      Map<DocXPropertyField, String> properties,
      DocumentUnit documentUnit,
      CoreData.CoreDataBuilder builder) {
    for (Map.Entry<DocXPropertyField, String> entry : properties.entrySet()) {
      switch (entry.getKey()) {
        case COURT_TYPE -> {
          if (documentUnit.coreData().court() == null) {
            List<Court> courts = courtRepository.findBySearchStr(entry.getValue());
            if (courts.size() == 1) {
              builder.court(courts.get(0));
            }
          }
        }
        case FILE_NUMBER -> {
          if (documentUnit.coreData().fileNumbers().isEmpty()) {
            builder.fileNumbers(Collections.singletonList(entry.getValue()));
          }
        }
        case LEGAL_EFFECT -> {
          if (documentUnit.coreData().legalEffect() == null
              || documentUnit
                  .coreData()
                  .legalEffect()
                  .equals(LegalEffect.NOT_SPECIFIED.getLabel())) {
            builder.legalEffect(entry.getValue());
          }
        }
        case APPRAISAL_BODY -> {
          if (documentUnit.coreData().appraisalBody() == null) {
            builder.appraisalBody(entry.getValue());
          }
        }
        default -> {}
      }
    }
  }

  private void initializeEcli(
      List<String> ecliList, DocumentUnit documentUnit, CoreData.CoreDataBuilder builder) {
    if (ecliList.size() == 1 && documentUnit.coreData().ecli() == null) {
      builder.ecli(ecliList.get(0));
    }
  }

  private void saveForRecycling(DocumentUnit documentUnit) {
    try {
      documentNumberRecyclingService.addForRecycling(
          documentUnit.uuid(),
          documentUnit.documentNumber(),
          documentUnit.coreData().documentationOffice().abbreviation());

    } catch (Exception e) {
      log.info("Did not save for recycling", e);
    }
  }
}
