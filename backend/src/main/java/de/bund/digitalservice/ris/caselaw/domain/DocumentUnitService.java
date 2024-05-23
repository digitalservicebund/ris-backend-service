package de.bund.digitalservice.ris.caselaw.domain;

import de.bund.digitalservice.ris.caselaw.domain.docx.Docx2Html;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.ListUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class DocumentUnitService {

  private final DocumentUnitRepository repository;
  private final PublicationReportRepository publicationReportRepository;
  private final DocumentNumberService documentNumberService;
  private final EmailPublishService publicationService;
  private final DocumentUnitStatusService documentUnitStatusService;
  private final AttachmentService attachmentService;
  private final DocumentNumberRecyclingService documentNumberRecyclingService;
  private final Validator validator;

  @Value("${otc.obs.bucket-name}")
  private String bucketName;

  @Value("${mail.exporter.recipientAddress:neuris@example.com}")
  private String recipientAddress;

  public DocumentUnitService(
      DocumentUnitRepository repository,
      DocumentNumberService documentNumberService,
      EmailPublishService publicationService,
      DocumentUnitStatusService documentUnitStatusService,
      PublicationReportRepository publicationReportRepository,
      DocumentNumberRecyclingService documentNumberRecyclingService,
      Validator validator,
      AttachmentService attachmentService) {

    this.repository = repository;
    this.documentNumberService = documentNumberService;
    this.publicationService = publicationService;
    this.documentUnitStatusService = documentUnitStatusService;
    this.publicationReportRepository = publicationReportRepository;
    this.documentNumberRecyclingService = documentNumberRecyclingService;
    this.validator = validator;
    this.attachmentService = attachmentService;
  }

  public DocumentUnit generateNewDocumentUnit(DocumentationOffice documentationOffice)
      throws DocumentationUnitNotExistsException, DocumentationUnitException {

    var documentNumber = generateDocumentNumber(documentationOffice);
    var documentUnit = repository.createNewDocumentUnit(documentNumber, documentationOffice);
    return documentUnitStatusService.setInitialStatus(documentUnit);
  }

  private String generateDocumentNumber(DocumentationOffice documentationOffice) {
    try {
      return documentNumberService.generateDocumentNumber(documentationOffice.abbreviation(), 5);
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
            .documentNumber(documentNumber.orElse(null))
            .fileNumber(fileNumber.orElse(null))
            .courtType(courtType.orElse(null))
            .courtLocation(courtLocation.orElse(null))
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

    return repository
        .searchByDocumentationUnitSearchInput(
            PageRequest.of(pageable.getPageNumber(), pageable.getPageSize()),
            documentationOffice,
            searchInput)
        .map(
            listItem ->
                listItem.toBuilder()
                    .isEditableByCurrentUser(
                        listItem.documentationOffice() != null
                            && listItem.documentationOffice().equals(documentationOffice))
                    .build());
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

  public Publication publishAsEmail(UUID documentUnitUuid, String issuerAddress)
      throws DocumentationUnitNotExistsException {

    DocumentUnit documentUnit =
        repository
            .findByUuid(documentUnitUuid)
            .orElseThrow(() -> new DocumentationUnitNotExistsException(documentUnitUuid));

    XmlPublication mailResponse = publicationService.publish(documentUnit, recipientAddress);
    if (mailResponse.getStatusCode().equals(String.valueOf(HttpStatus.OK.value()))) {
      documentUnitStatusService.setToPublishing(
          documentUnit, mailResponse.getPublishDate(), issuerAddress);
      return mailResponse;
    } else {
      return mailResponse;
    }
  }

  public XmlResultObject previewPublication(UUID documentUuid)
      throws DocumentationUnitNotExistsException {
    DocumentUnit documentUnit =
        repository
            .findByUuid(documentUuid)
            .orElseThrow(() -> new DocumentationUnitNotExistsException(documentUuid));
    return publicationService.getPublicationPreview(documentUnit);
  }

  public List<PublicationHistoryRecord> getPublicationHistory(UUID documentUuid) {
    List<PublicationHistoryRecord> list =
        ListUtils.union(
            publicationService.getPublications(documentUuid),
            publicationReportRepository.getAllByDocumentUnitUuid(documentUuid));
    list.sort(Comparator.comparing(PublicationHistoryRecord::getDate).reversed());
    return list;
  }

  public Slice<RelatedDocumentationUnit> searchLinkableDocumentationUnits(
      RelatedDocumentationUnit relatedDocumentationUnit,
      DocumentationOffice documentationOffice,
      String documentNumberToExclude,
      Pageable pageable) {

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

  public void updateECLI(UUID uuid, Docx2Html docx2html) {
    if (docx2html.ecliList().size() == 1) {
      repository.updateECLI(uuid, docx2html.ecliList().get(0));
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
