package de.bund.digitalservice.ris.caselaw.domain;

import de.bund.digitalservice.ris.caselaw.domain.docx.Docx2Html;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import java.time.Duration;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

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

  public Mono<DocumentUnit> generateNewDocumentUnit(DocumentationOffice documentationOffice) {
    return Mono.just(documentationOffice)
        .flatMap(this::generateDocumentNumber)
        .flatMap(
            documentNumber -> repository.createNewDocumentUnit(documentNumber, documentationOffice))
        .flatMap(documentUnitStatusService::setInitialStatus)
        .retryWhen(Retry.backoff(5, Duration.ofSeconds(2)).jitter(0.75))
        .doOnError(ex -> log.error("Couldn't create empty doc unit", ex));
  }

  private Mono<String> generateDocumentNumber(DocumentationOffice documentationOffice) {
    try {
      return Mono.just(
          documentNumberService.generateDocumentNumber(documentationOffice.abbreviation(), 5));
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

    return repository.searchByDocumentationUnitSearchInput(
        PageRequest.of(pageable.getPageNumber(), pageable.getPageSize()),
        documentationOffice,
        searchInput);
  }

  public Mono<DocumentUnit> getByDocumentNumber(String documentNumber) {
    try {
      var optionalDocumentUnit = repository.findByDocumentNumber(documentNumber);
      return Mono.just(optionalDocumentUnit.orElseThrow());
    } catch (Exception e) {
      return Mono.empty();
    }
  }

  public Mono<DocumentUnit> getByUuid(UUID documentUnitUuid) {
    return Mono.just(repository.findByUuid(documentUnitUuid).orElseThrow());
  }

  public Mono<String> deleteByUuid(UUID documentUnitUuid) {
    try {

      Map<RelatedDocumentationType, Long> relatedEntities =
          repository.getAllDocumentationUnitWhichLink(documentUnitUuid);

      if (!(relatedEntities == null
          || relatedEntities.isEmpty()
          || relatedEntities.values().stream().mapToLong(Long::longValue).sum() == 0)) {

        log.debug(
            "Could not delete document unit {} cause of related entities: {}",
            documentUnitUuid,
            relatedEntities);

        return Mono.error(
            new DocumentUnitDeletionException(
                "Die Dokumentationseinheit konnte nicht gelöscht werden, da", relatedEntities));
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
      return Mono.just("Dokumentationseinheit gelöscht: " + documentUnitUuid);
    } catch (Exception e) {
      return Mono.error(e);
    }
  }

  public Mono<DocumentUnit> updateDocumentUnit(DocumentUnit documentUnit) {
    repository.saveKeywords(documentUnit);
    repository.saveFieldsOfLaw(documentUnit);
    repository.saveProcedures(documentUnit);

    repository.save(documentUnit);

    try {
      return Mono.just(
          repository
              .findByUuid(documentUnit.uuid())
              .orElseThrow(
                  () -> new DocumentationUnitNotExistsException(documentUnit.documentNumber())));
    } catch (DocumentationUnitNotExistsException e) {
      return Mono.error(e);
    }
  }

  public Mono<Publication> publishAsEmail(UUID documentUnitUuid, String issuerAddress) {
    try {
      DocumentUnit documentUnit =
          repository
              .findByUuid(documentUnitUuid)
              .orElseThrow(() -> new DocumentationUnitNotExistsException(documentUnitUuid));

      return publicationService
          .publish(documentUnit, recipientAddress)
          .flatMap(
              mailResponse -> {
                if (mailResponse.getStatusCode().equals(String.valueOf(HttpStatus.OK.value()))) {
                  return documentUnitStatusService
                      .setToPublishing(documentUnit, mailResponse.getPublishDate(), issuerAddress)
                      .thenReturn(mailResponse);
                } else {
                  return Mono.just(mailResponse);
                }
              });
    } catch (Exception e) {
      return Mono.error(e);
    }
  }

  public Mono<XmlResultObject> previewPublication(UUID documentUuid) {
    try {
      DocumentUnit documentUnit =
          repository
              .findByUuid(documentUuid)
              .orElseThrow(() -> new DocumentationUnitNotExistsException(documentUuid));
      return publicationService.getPublicationPreview(documentUnit);
    } catch (Exception e) {
      return Mono.error(e);
    }
  }

  public Flux<PublicationHistoryRecord> getPublicationHistory(UUID documentUuid) {
    return Flux.concat(
            publicationService.getPublications(documentUuid),
            Flux.fromIterable(publicationReportRepository.getAllByDocumentUnitUuid(documentUuid)))
        .sort(Comparator.comparing(PublicationHistoryRecord::getDate).reversed());
  }

  public Slice<RelatedDocumentationUnit> searchLinkableDocumentationUnits(
      RelatedDocumentationUnit relatedDocumentationUnit,
      DocumentationOffice documentationOffice,
      String documentNumberToExclude,
      Pageable pageable) {

    return repository.searchLinkableDocumentationUnits(
        relatedDocumentationUnit, documentationOffice, documentNumberToExclude, pageable);
  }

  public Mono<String> validateSingleNorm(SingleNormValidationInfo singleNormValidationInfo) {
    Set<ConstraintViolation<SingleNormValidationInfo>> violations =
        validator.validate(singleNormValidationInfo);

    if (violations.isEmpty()) {
      return Mono.just("Ok");
    }
    return Mono.just("Validation error");
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
