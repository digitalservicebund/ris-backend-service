package de.bund.digitalservice.ris.caselaw.adapter.publication;

import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;

import de.bund.digitalservice.ris.caselaw.adapter.S3Bucket;
import de.bund.digitalservice.ris.caselaw.adapter.XmlUtilService;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.CaseLawLdml;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.AttachmentInlineDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseAttachmentInlineRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDocumentationUnitRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DecisionDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentationUnitDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.PendingProceedingDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.PublishedDocumentationSnapshotEntity;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.PublishedDocumentationSnapshotRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.RelatedPendingProceedingDTO;
import de.bund.digitalservice.ris.caselaw.adapter.exception.BucketException;
import de.bund.digitalservice.ris.caselaw.adapter.exception.ChangelogException;
import de.bund.digitalservice.ris.caselaw.adapter.exception.LdmlTransformationException;
import de.bund.digitalservice.ris.caselaw.adapter.exception.PublishException;
import de.bund.digitalservice.ris.caselaw.adapter.publication.ManualPortalPublicationResult.RelatedPendingProceedingPublicationResult;
import de.bund.digitalservice.ris.caselaw.adapter.transformer.DecisionTransformer;
import de.bund.digitalservice.ris.caselaw.adapter.transformer.PendingProceedingTransformer;
import de.bund.digitalservice.ris.caselaw.adapter.transformer.ldml.PortalTransformer;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnit;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnitHistoryLogService;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnitRepository;
import de.bund.digitalservice.ris.caselaw.domain.FeatureToggleService;
import de.bund.digitalservice.ris.caselaw.domain.HistoryLogEventType;
import de.bund.digitalservice.ris.caselaw.domain.LdmlTransformationResult;
import de.bund.digitalservice.ris.caselaw.domain.LoggingKeys;
import de.bund.digitalservice.ris.caselaw.domain.PendingProceeding;
import de.bund.digitalservice.ris.caselaw.domain.PortalPublicationStatus;
import de.bund.digitalservice.ris.caselaw.domain.PublicationStatus;
import de.bund.digitalservice.ris.caselaw.domain.User;
import de.bund.digitalservice.ris.caselaw.domain.exception.DocumentationUnitNotExistsException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Nullable;
import org.springframework.data.mapping.MappingException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

@Service
@Slf4j
public class PortalPublicationService {

  private final DocumentationUnitRepository documentationUnitRepository;
  private final DatabaseDocumentationUnitRepository databaseDocumentationUnitRepository;
  private final S3Bucket portalBucket;
  private final ObjectMapper objectMapper;
  private final XmlUtilService xmlUtilService;
  private final PortalTransformer ldmlTransformer;
  private final FeatureToggleService featureToggleService;
  private final DocumentationUnitHistoryLogService historyLogService;
  private final PublishedDocumentationSnapshotRepository snapshotRepository;
  private final CaselawCitationSyncService caselawCitationSyncService;
  private final CaselawCitationPublishService caselawCitationPublishService;

  private static final String PUBLICATION_FEATURE_FLAG = "neuris.portal-publication";
  private static final String CHANGELOG_FEATURE_FLAG = "neuris.regular-changelogs";
  private final DatabaseAttachmentInlineRepository attachmentInlineRepository;

  public PortalPublicationService(
      DocumentationUnitRepository documentationUnitRepository,
      DatabaseDocumentationUnitRepository databaseDocumentationUnitRepository,
      XmlUtilService xmlUtilService,
      S3Bucket portalBucket,
      ObjectMapper objectMapper,
      PortalTransformer portalTransformer,
      FeatureToggleService featureToggleService,
      DocumentationUnitHistoryLogService historyLogService,
      DatabaseAttachmentInlineRepository attachmentInlineRepository,
      PublishedDocumentationSnapshotRepository snapshotRepository,
      CaselawCitationSyncService caselawCitationSyncService,
      CaselawCitationPublishService caselawCitationPublishService) {

    this.documentationUnitRepository = documentationUnitRepository;
    this.databaseDocumentationUnitRepository = databaseDocumentationUnitRepository;
    this.portalBucket = portalBucket;
    this.objectMapper = objectMapper;
    this.xmlUtilService = xmlUtilService;
    this.ldmlTransformer = portalTransformer;
    this.featureToggleService = featureToggleService;
    this.historyLogService = historyLogService;
    this.attachmentInlineRepository = attachmentInlineRepository;
    this.snapshotRepository = snapshotRepository;
    this.caselawCitationSyncService = caselawCitationSyncService;
    this.caselawCitationPublishService = caselawCitationPublishService;
  }

  /**
   * Publish the documentation unit by transforming it to valid LDML and putting the resulting XML
   * file into a bucket together with a changelog file, specifying which documentation unit has been
   * added or updated.
   *
   * @param documentationUnitId the id of the documentation unit that should be published
   * @throws DocumentationUnitNotExistsException if the documentation unit with the given id could
   *     not be found in the database
   * @throws LdmlTransformationException if the documentation unit could not be transformed to valid
   *     LDML
   * @throws PublishException if the LDML could not be saved in the bucket
   * @throws ChangelogException if the changelog cannot be generated or saved
   */
  public ManualPortalPublicationResult publishDocumentationUnitWithChangelog(
      UUID documentationUnitId, User user) throws DocumentationUnitNotExistsException {
    if (!featureToggleService.isEnabled(PUBLICATION_FEATURE_FLAG)) {
      return new ManualPortalPublicationResult(RelatedPendingProceedingPublicationResult.NO_ACTION);
    }
    try {
      DocumentationUnitDTO documentationUnit =
          documentationUnitRepository.loadDocumentationUnitDTO(documentationUnitId);
      var result = publishToBucket(documentationUnit);
      uploadChangelogWithdrawOnFailure(documentationUnit, result);
      updatePortalPublicationStatus(documentationUnit, PortalPublicationStatus.PUBLISHED, user);

      var relatedPendingProceedingUpdateResult =
          RelatedPendingProceedingPublicationResult.NO_ACTION;
      if (documentationUnit instanceof DecisionDTO decision) {
        relatedPendingProceedingUpdateResult =
            publishResolutionNoteOfRelatedPendingProceedings(decision, user);
      }
      return new ManualPortalPublicationResult(relatedPendingProceedingUpdateResult);
    } catch (Exception exception) {
      historyLogService.saveHistoryLog(
          documentationUnitId,
          user,
          HistoryLogEventType.PORTAL_PUBLICATION,
          "Dokeinheit konnte nicht im Portal veröffentlicht werden");
      throw exception;
    }
  }

  @Async
  public void publishSnapshots(int page, int size) {
    List<UUID> documentationUnitIds =
        documentationUnitRepository.findAllByCurrentStatus(PublicationStatus.PUBLISHED, page, size);

    log.info("save {} documentation units as snapshots", documentationUnitIds.size());

    AtomicInteger counter = new AtomicInteger(0);
    AtomicInteger decisionCounter = new AtomicInteger(0);
    AtomicInteger batchCounter = new AtomicInteger(0);
    documentationUnitIds.forEach(
        documentationUnitId -> {
          DocumentationUnitDTO documentationUnit = null;
          try {
            documentationUnit =
                documentationUnitRepository.loadDocumentationUnitDTO(documentationUnitId);
          } catch (Exception e) {
            return;
          }

          counter.incrementAndGet();
          if (documentationUnit instanceof DecisionDTO decision) {
            decisionCounter.incrementAndGet();
            saveSnapshot(decision);
          }

          if (counter.get() > 1000) {
            log.info(
                "save {} decisions of {} documentation units in batch {}",
                decisionCounter.get(),
                counter.get(),
                batchCounter.get());
            counter.set(0);
            decisionCounter.set(0);
            batchCounter.incrementAndGet();
          }
        });

    if (counter.get() > 0) {
      log.info(
          "save {} decisions of {} documentation units in batch {}",
          decisionCounter.get(),
          counter.get(),
          batchCounter.get());
      if (!documentationUnitIds.isEmpty()) {
        log.info("last documentation units: {}", documentationUnitIds.getLast());
      }
    }
  }

  private void saveSnapshot(DocumentationUnitDTO documentationUnit) {
    Optional<PublishedDocumentationSnapshotEntity> snapshot =
        snapshotRepository.findByDocumentationUnitId(documentationUnit.getId());
    PublishedDocumentationSnapshotEntity entity;
    if (snapshot.isPresent()) {
      entity =
          snapshot.get().toBuilder()
              .json(toDomain(documentationUnit))
              .publishedAt(LocalDateTime.now())
              .build();
    } else {
      entity =
          PublishedDocumentationSnapshotEntity.builder()
              .documentationUnitId(documentationUnit.getId())
              .json(toDomain(documentationUnit))
              .publishedAt(LocalDateTime.now())
              .build();
    }

    snapshotRepository.save(entity);
  }

  /**
   * Publish the documentation unit by transforming it to LDML and writing the resulting XML file
   * together with any to a bucket.
   *
   * @param documentNumber the documentation unit that should be published
   * @throws DocumentationUnitNotExistsException if the documentation unit with the given document
   *     number could not be found in the database
   * @throws LdmlTransformationException if the documentation unit could not be transformed to valid
   *     LDML
   * @throws PublishException if the LDML file could not be saved in the bucket
   */
  public PortalPublicationResult publishDocumentationUnit(String documentNumber)
      throws DocumentationUnitNotExistsException {
    DocumentationUnitDTO documentationUnit =
        documentationUnitRepository.loadDocumentationUnitDTO(documentNumber);
    var publicationResult = publishToBucket(documentationUnit);
    updatePortalPublicationStatus(documentationUnit, PortalPublicationStatus.PUBLISHED, null);
    if (documentationUnit instanceof DecisionDTO decision)
      publishResolutionNoteOfRelatedPendingProceedings(decision, null);
    return publicationResult;
  }

  /**
   * Deletes the documentation unit and its attachments with the given documentNumber from the
   * portal bucket.
   *
   * @param documentNumber the document number of the documentation unit to be withdrawn.
   */
  public PortalPublicationResult withdrawDocumentationUnit(String documentNumber)
      throws DocumentationUnitNotExistsException {
    var result = withdraw(documentNumber);
    try {
      DocumentationUnitDTO documentationUnit =
          databaseDocumentationUnitRepository
              .findByDocumentNumber(documentNumber)
              .orElseThrow(() -> new DocumentationUnitNotExistsException(documentNumber));
      updatePortalPublicationStatus(documentationUnit, PortalPublicationStatus.WITHDRAWN, null);
    } catch (DocumentationUnitNotExistsException e) {
      log.atInfo()
          .setMessage(
              "Withdrawn documentation unit cannot be found in database. Likely, it was deleted by the migration or manually in the database. Portal publication status update skipped.")
          .addKeyValue(LoggingKeys.DOCUMENT_NUMBER, documentNumber)
          .log();
    }
    return result;
  }

  private PortalPublicationResult withdraw(String documentNumber) {
    try {
      var deletableFiles = portalBucket.getAllFilenamesByPath(documentNumber + "/");
      deletableFiles.forEach(portalBucket::delete);

      log.atInfo()
          .setMessage("Documentation unit withdrawn from portal bucket.")
          .addKeyValue(LoggingKeys.DOCUMENT_NUMBER, documentNumber)
          .log();

      return new PortalPublicationResult(List.of(), deletableFiles);
    } catch (BucketException e) {
      throw new PublishException("Could not delete LDML from bucket.", e);
    }
  }

  /**
   * Deletes the documentation unit and its attachments with the given documentNumber from the
   * portal bucket and writes a changelog file to the bucket, informing about the withdrawal.
   *
   * @param documentationUnitId the id of the documentation unit to be withdrawn.
   * @param user the user that initiated the withdrawal.
   * @throws DocumentationUnitNotExistsException if the documentation unit with the given id could
   *     not be found in the database.
   * @throws ChangelogException if the deletion changelog cannot be generated or saved.
   * @throws PublishException if the files could not be deleted from the bucket.
   */
  public void withdrawDocumentationUnitWithChangelog(UUID documentationUnitId, User user)
      throws DocumentationUnitNotExistsException {
    try {
      var documentationUnit =
          databaseDocumentationUnitRepository
              .findById(documentationUnitId)
              .orElseThrow(() -> new DocumentationUnitNotExistsException(documentationUnitId));
      var result = withdraw(documentationUnit.getDocumentNumber());
      uploadDeletionChangelog(result.deletedPaths());
      updatePortalPublicationStatus(documentationUnit, PortalPublicationStatus.WITHDRAWN, user);
    } catch (Exception e) {
      historyLogService.saveHistoryLog(
          documentationUnitId,
          user,
          HistoryLogEventType.PORTAL_PUBLICATION,
          "Dokeinheit konnte nicht aus dem Portal zurückgezogen werden");
      log.atError()
          .setMessage("Could not withdraw documentation unit from portal.")
          .addKeyValue("id", documentationUnitId)
          .setCause(e)
          .log();
      throw e;
    }
  }

  /**
   * Generates a changelog file with the given parameters and saves it to the portal bucket.
   *
   * @param publishedDocumentNumbers the document numbers of the documentation units which have been
   *     changed or added.
   * @param deletedDocumentNumbers the document numbers of the documentation units which have been
   *     deleted.
   * @throws ChangelogException if the changelog cannot be generated or saved.
   */
  public void uploadChangelog(
      List<String> publishedDocumentNumbers, List<String> deletedDocumentNumbers) {
    if (featureToggleService.isEnabled(CHANGELOG_FEATURE_FLAG)) {
      uploadChangelog(publishedDocumentNumbers, deletedDocumentNumbers, false);
    }
  }

  /**
   * Generates a changelog file to trigger a full re-indexing of all documents in the bucket
   *
   * @throws ChangelogException if the changelog cannot be generated or saved.
   */
  public void uploadFullReindexChangelog() {
    if (featureToggleService.isEnabled(CHANGELOG_FEATURE_FLAG)) {
      return; // regular changelogs are enabled, no nightly re-indexing needed
    }
    uploadChangelog(null, null, true);
  }

  /**
   * Generates a changelog file with the given parameters and saves it to the portal bucket.
   *
   * @param deletedDocumentNumbers the document numbers of the documentation units which have been
   *     deleted.
   * @throws ChangelogException if the changelog cannot be generated or saved.
   */
  public void uploadDeletionChangelog(List<String> deletedDocumentNumbers) {
    uploadChangelog(null, deletedDocumentNumbers, false);
  }

  /**
   * Create a LDML preview for a documentation unit.
   *
   * @param documentUuid the UUID of the documentation unit
   * @return the export result, containing the LDML
   */
  public LdmlTransformationResult createLdmlPreview(UUID documentUuid)
      throws DocumentationUnitNotExistsException, LdmlTransformationException, MappingException {
    DocumentationUnit documentationUnit = documentationUnitRepository.findByUuid(documentUuid);
    CaseLawLdml ldml = ldmlTransformer.transformToLdml(documentationUnit);
    Optional<String> fileContent = xmlUtilService.ldmlToString(ldml);
    if (fileContent.isEmpty()) {
      throw new LdmlTransformationException("Could not parse transformed LDML as string.", null);
    }
    return LdmlTransformationResult.builder().success(true).ldml(fileContent.get()).build();
  }

  private PortalPublicationResult publishToBucket(DocumentationUnitDTO documentationUnit) {
    List<AttachmentInlineDTO> inlineImages =
        attachmentInlineRepository.findAllByDocumentationUnitId(documentationUnit.getId());

    if (documentationUnit instanceof DecisionDTO decision) {
      validateAndEnrichCaselawCitations(decision);
    }

    CaseLawLdml ldml = ldmlTransformer.transformToLdml(toDomain(documentationUnit));
    Optional<String> fileContent = xmlUtilService.ldmlToString(ldml);
    if (fileContent.isEmpty()) {
      throw new LdmlTransformationException("Could not parse transformed LDML as string.", null);
    }

    var result =
        saveToBucket(ldml.getUniqueId() + "/", ldml.getFileName(), fileContent.get(), inlineImages);

    log.atInfo()
        .setMessage("Doc unit published to portal bucket.")
        .addKeyValue(LoggingKeys.DOCUMENT_NUMBER, documentationUnit.getDocumentNumber())
        .log();

    var documentsToRepublish = caselawCitationSyncService.syncCitations(documentationUnit);

    documentsToRepublish.forEach(
        documentNumber -> {
          try {
            log.atInfo()
                .addKeyValue(
                    "originalPublishedDocumentationUnit", documentationUnit.getDocumentNumber())
                .addKeyValue("documentationUnit", documentNumber)
                .setMessage(
                    "Publish documentation unit changed during the publishing of another documentation unit.")
                .log();
            publishDocumentationUnit(documentNumber);
          } catch (DocumentationUnitNotExistsException e) {
            throw new PublishException(
                "Couldn't find changed documentation unit that should be published as well", e);
          }
        });

    return result;
  }

  private PortalPublicationResult saveToBucket(
      String path, String fileName, String fileContent, List<AttachmentInlineDTO> inlineImages) {
    try {
      List<String> existingFiles = portalBucket.getAllFilenamesByPath(path);
      List<String> addedInlineImages = new ArrayList<>();

      portalBucket.save(path + fileName, fileContent);
      addedInlineImages.add(path + fileName);

      if (!inlineImages.isEmpty()) {
        inlineImages.forEach(
            inlineImage -> {
              portalBucket.saveBytes(path + inlineImage.getFilename(), inlineImage.getContent());
              addedInlineImages.add(path + inlineImage.getFilename());
            });
      }

      // Check for files that are not part of this update and remove them (e.g. removed images)
      existingFiles.removeAll(addedInlineImages);
      existingFiles.forEach(portalBucket::delete);

      return new PortalPublicationResult(addedInlineImages, existingFiles);
    } catch (BucketException e) {
      throw new PublishException("Could not save LDML to bucket.", e);
    }
  }

  private void uploadChangelogWithdrawOnFailure(
      DocumentationUnitDTO documentationUnit, PortalPublicationResult result) {
    try {
      uploadChangelog(result.changedPaths(), result.deletedPaths());
    } catch (Exception e) {
      log.error("Could not upload changelog file.");
      withdraw(documentationUnit.getDocumentNumber());
      throw new PublishException("Could not save changelog to bucket.", e);
    }
  }

  private void uploadChangelog(
      List<String> publishedDocumentNumbers,
      List<String> deletedDocumentNumbers,
      boolean changeAll) {
    Changelog changelog;
    if (changeAll) {
      changelog = new ChangelogChangeAll(true);
    } else {
      changelog = new ChangelogUpdateDelete(publishedDocumentNumbers, deletedDocumentNumbers);
    }

    try {
      String changelogString = objectMapper.writeValueAsString(changelog);
      portalBucket.save(createChangelogFileName(), changelogString);
    } catch (Exception e) {
      throw new ChangelogException("Could not create changelog file", e);
    }
  }

  private String createChangelogFileName() {
    return "changelogs/" + Instant.now().toString() + "-caselaw.json";
  }

  private void updatePortalPublicationStatus(
      DocumentationUnitDTO documentationUnit, PortalPublicationStatus newStatus, User user) {

    boolean statusUnchanged = newStatus.equals(documentationUnit.getPortalPublicationStatus());
    boolean isPublishAction = newStatus.equals(PortalPublicationStatus.PUBLISHED);

    if (isPublishAction) {
      documentationUnitRepository.savePublicationDateTime(documentationUnit.getId());
    }
    if (!statusUnchanged) {
      documentationUnitRepository.updatePortalPublicationStatus(
          documentationUnit.getId(), newStatus);
    }

    addHistoryLog(documentationUnit, newStatus, user);
  }

  private RelatedPendingProceedingPublicationResult
      publishResolutionNoteOfRelatedPendingProceedings(DecisionDTO decision, User user) {
    if (decision.getRelatedPendingProceedings() == null) {
      return RelatedPendingProceedingPublicationResult.NO_ACTION;
    }

    var pendingProceedings = decision.getRelatedPendingProceedings();
    Set<RelatedPendingProceedingPublicationResult> results = new HashSet<>();
    for (RelatedPendingProceedingDTO relatedPendingProceeding : pendingProceedings) {
      try {
        var docUnit =
            documentationUnitRepository.findByDocumentNumber(
                relatedPendingProceeding.getDocumentNumber());

        if (docUnit instanceof PendingProceeding pendingProceeding) {
          var result = updateResolutionNoteOfPendingProceeding(decision, pendingProceeding, user);
          results.add(result);
        }
      } catch (Exception e) {
        results.add(RelatedPendingProceedingPublicationResult.ERROR);
        log.atError()
            .addKeyValue(LoggingKeys.DOCUMENT_NUMBER, relatedPendingProceeding.getDocumentNumber())
            .log(
                "Could not resolve and publish pending proceeding {}",
                relatedPendingProceeding.getDocumentNumber(),
                e);
      }
    }
    if (results.contains(RelatedPendingProceedingPublicationResult.ERROR)) {
      return RelatedPendingProceedingPublicationResult.ERROR;
    } else if (results.contains(RelatedPendingProceedingPublicationResult.SUCCESS)) {
      return RelatedPendingProceedingPublicationResult.SUCCESS;
    } else {
      return RelatedPendingProceedingPublicationResult.NO_ACTION;
    }
  }

  private RelatedPendingProceedingPublicationResult updateResolutionNoteOfPendingProceeding(
      DocumentationUnitDTO documentationUnit, PendingProceeding pendingProceeding, User user)
      throws DocumentationUnitNotExistsException {
    if (pendingProceeding.coreData() != null && pendingProceeding.coreData().isResolved()) {
      log.atInfo()
          .addKeyValue(LoggingKeys.DOCUMENT_NUMBER, documentationUnit.getDocumentNumber())
          .addKeyValue("id", documentationUnit.getId())
          .setMessage(
              String.format(
                  "Do not mark pending proceeding %s as resolved. It already is resolved. A Documentation unit (%s) was published that contained it as a related pending proceeding",
                  pendingProceeding.documentNumber(), documentationUnit.getDocumentNumber()))
          .log();
      return RelatedPendingProceedingPublicationResult.NO_ACTION;
    }

    // We won't automatically publish unpublished pending proceedings.
    // Normally, they should already be in the portal as unresolved.
    boolean isUnpublished =
        pendingProceeding.portalPublicationStatus() != PortalPublicationStatus.PUBLISHED;

    // Pflichtfelder
    boolean hasRequiredData =
        pendingProceeding.coreData() != null
            && isNotEmpty(pendingProceeding.coreData().fileNumbers())
            && pendingProceeding.coreData().decisionDate() != null
            && pendingProceeding.coreData().court() != null
            && pendingProceeding.shortTexts() != null
            && pendingProceeding.shortTexts().legalIssue() != null;

    if (isUnpublished || !hasRequiredData) {
      log.atInfo()
          .addKeyValue(LoggingKeys.DOCUMENT_NUMBER, pendingProceeding.documentNumber())
          .addKeyValue("status", pendingProceeding.portalPublicationStatus())
          .addKeyValue("hasRequiredData", hasRequiredData)
          .log("Unresolved linked pending proceeding cannot be published automatically.");
      return RelatedPendingProceedingPublicationResult.ERROR;
    }

    var today = LocalDate.ofInstant(Instant.now(), ZoneId.of("Europe/Berlin"));
    var resolutionDate =
        Optional.ofNullable(pendingProceeding.coreData().resolutionDate()).orElse(today);
    var resolutionNote =
        Optional.ofNullable(pendingProceeding.shortTexts().resolutionNote())
            .orElse("Erledigt durch " + documentationUnit.getDocumentNumber());

    PendingProceeding updatedPendingProceeding =
        pendingProceeding.toBuilder()
            .coreData(
                pendingProceeding.coreData().toBuilder()
                    .isResolved(true)
                    // Only set today if empty
                    .resolutionDate(resolutionDate)
                    .build())
            .shortTexts(
                pendingProceeding.shortTexts().toBuilder().resolutionNote(resolutionNote).build())
            .build();
    documentationUnitRepository.save(updatedPendingProceeding, user);

    publishDocumentationUnitWithChangelog(pendingProceeding.uuid(), user);

    log.atInfo()
        .addKeyValue(LoggingKeys.DOCUMENT_NUMBER, documentationUnit.getDocumentNumber())
        .addKeyValue("id", documentationUnit.getId())
        .log(
            "Published pending proceeding {} as resolved. A Documentation unit ({}) was published that contained it as a related pending proceeding",
            pendingProceeding.documentNumber(),
            documentationUnit.getDocumentNumber());
    return RelatedPendingProceedingPublicationResult.SUCCESS;
  }

  private void addHistoryLog(
      DocumentationUnitDTO documentationUnit, PortalPublicationStatus newStatus, User user) {
    String historyLogMessage;
    if (PortalPublicationStatus.PUBLISHED.equals(newStatus)) {
      historyLogMessage = "Dokeinheit im Portal veröffentlicht";
    } else {
      historyLogMessage = "Dokeinheit wurde aus dem Portal zurückgezogen";
    }
    historyLogService.saveHistoryLog(
        documentationUnit.getId(), user, HistoryLogEventType.PORTAL_PUBLICATION, historyLogMessage);
  }

  private void validateAndEnrichCaselawCitations(DecisionDTO decision) {
    decision.setActiveCaselawCitations(
        decision.getActiveCaselawCitations().stream()
            .map(caselawCitationPublishService::updateActiveCitationTargetWithInformationFromTarget)
            .toList());
    decision.setPassiveCaselawCitations(
        decision.getPassiveCaselawCitations().stream()
            .map(
                caselawCitationPublishService::updatePassiveCitationSourceWithInformationFromSource)
            .flatMap(Optional::stream)
            .toList());
  }

  @Nullable
  private DocumentationUnit toDomain(DocumentationUnitDTO documentationUnit) {
    if (documentationUnit instanceof DecisionDTO decisionDTO) {
      return DecisionTransformer.transformToDomain(decisionDTO, null);
    }
    if (documentationUnit instanceof PendingProceedingDTO pendingProceedingDTO) {
      return PendingProceedingTransformer.transformToDomain(pendingProceedingDTO, null);
    }
    return null;
  }
}
