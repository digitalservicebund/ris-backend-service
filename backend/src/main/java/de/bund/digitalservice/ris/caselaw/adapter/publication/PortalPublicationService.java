package de.bund.digitalservice.ris.caselaw.adapter.publication;

import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;

import de.bund.digitalservice.ris.caselaw.adapter.S3Bucket;
import de.bund.digitalservice.ris.caselaw.adapter.XmlUtilService;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.CaseLawLdml;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.AttachmentInlineDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.AttachmentInlineRepository;
import de.bund.digitalservice.ris.caselaw.adapter.exception.BucketException;
import de.bund.digitalservice.ris.caselaw.adapter.exception.ChangelogException;
import de.bund.digitalservice.ris.caselaw.adapter.exception.LdmlTransformationException;
import de.bund.digitalservice.ris.caselaw.adapter.exception.PublishException;
import de.bund.digitalservice.ris.caselaw.adapter.publication.ManualPortalPublicationResult.RelatedPendingProceedingPublicationResult;
import de.bund.digitalservice.ris.caselaw.adapter.transformer.ldml.PortalTransformer;
import de.bund.digitalservice.ris.caselaw.domain.Decision;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnit;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnitHistoryLogService;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnitRepository;
import de.bund.digitalservice.ris.caselaw.domain.FeatureToggleService;
import de.bund.digitalservice.ris.caselaw.domain.HistoryLogEventType;
import de.bund.digitalservice.ris.caselaw.domain.LdmlTransformationResult;
import de.bund.digitalservice.ris.caselaw.domain.LoggingKeys;
import de.bund.digitalservice.ris.caselaw.domain.PendingProceeding;
import de.bund.digitalservice.ris.caselaw.domain.PortalPublicationStatus;
import de.bund.digitalservice.ris.caselaw.domain.RelatedPendingProceeding;
import de.bund.digitalservice.ris.caselaw.domain.User;
import de.bund.digitalservice.ris.caselaw.domain.exception.DocumentationUnitNotExistsException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mapping.MappingException;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

@Service
@Slf4j
public class PortalPublicationService {

  private final DocumentationUnitRepository documentationUnitRepository;
  private final S3Bucket portalBucket;
  private final ObjectMapper objectMapper;
  private final XmlUtilService xmlUtilService;
  private final PortalTransformer ldmlTransformer;
  private final FeatureToggleService featureToggleService;
  private final DocumentationUnitHistoryLogService historyLogService;

  private static final String PUBLICATION_FEATURE_FLAG = "neuris.portal-publication";
  private static final String CHANGELOG_FEATURE_FLAG = "neuris.regular-changelogs";
  private final AttachmentInlineRepository attachmentInlineRepository;

  public PortalPublicationService(
      DocumentationUnitRepository documentationUnitRepository,
      XmlUtilService xmlUtilService,
      S3Bucket portalBucket,
      ObjectMapper objectMapper,
      PortalTransformer portalTransformer,
      FeatureToggleService featureToggleService,
      DocumentationUnitHistoryLogService historyLogService,
      AttachmentInlineRepository attachmentInlineRepository) {

    this.documentationUnitRepository = documentationUnitRepository;
    this.portalBucket = portalBucket;
    this.objectMapper = objectMapper;
    this.xmlUtilService = xmlUtilService;
    this.ldmlTransformer = portalTransformer;
    this.featureToggleService = featureToggleService;
    this.historyLogService = historyLogService;
    this.attachmentInlineRepository = attachmentInlineRepository;
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
      DocumentationUnit documentationUnit =
          documentationUnitRepository.findByUuid(documentationUnitId);
      var result = publishToBucket(documentationUnit);
      uploadChangelogWithdrawOnFailure(documentationUnit, result);
      updatePortalPublicationStatus(documentationUnit, PortalPublicationStatus.PUBLISHED, user);

      var relatedPendingProceedingUpdateResult =
          RelatedPendingProceedingPublicationResult.NO_ACTION;
      if (documentationUnit instanceof Decision decision) {
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
    DocumentationUnit documentationUnit =
        documentationUnitRepository.findByDocumentNumber(documentNumber);
    var publicationResult = publishToBucket(documentationUnit);
    updatePortalPublicationStatus(documentationUnit, PortalPublicationStatus.PUBLISHED, null);
    if (documentationUnit instanceof Decision decision)
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
      DocumentationUnit documentationUnit =
          documentationUnitRepository.findByDocumentNumber(documentNumber);
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
      var documentationUnit = documentationUnitRepository.findByUuid(documentationUnitId);
      var result = withdraw(documentationUnit.documentNumber());
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

  private PortalPublicationResult publishToBucket(DocumentationUnit documentationUnit) {
    List<AttachmentInlineDTO> inlineImages =
        attachmentInlineRepository.findAllByDocumentationUnitId(documentationUnit.uuid());
    CaseLawLdml ldml = ldmlTransformer.transformToLdml(documentationUnit);
    Optional<String> fileContent = xmlUtilService.ldmlToString(ldml);
    if (fileContent.isEmpty()) {
      throw new LdmlTransformationException("Could not parse transformed LDML as string.", null);
    }

    var result =
        saveToBucket(ldml.getUniqueId() + "/", ldml.getFileName(), fileContent.get(), inlineImages);

    log.atInfo()
        .setMessage("Doc unit published to portal bucket.")
        .addKeyValue(LoggingKeys.DOCUMENT_NUMBER, documentationUnit.documentNumber())
        .log();

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
      DocumentationUnit documentationUnit, PortalPublicationResult result) {
    try {
      uploadChangelog(result.changedPaths(), result.deletedPaths());
    } catch (Exception e) {
      log.error("Could not upload changelog file.");
      withdraw(documentationUnit.documentNumber());
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
      DocumentationUnit documentationUnit, PortalPublicationStatus newStatus, User user) {

    boolean statusUnchanged = newStatus.equals(documentationUnit.portalPublicationStatus());
    boolean isPublishAction = newStatus.equals(PortalPublicationStatus.PUBLISHED);

    if (isPublishAction) {
      documentationUnitRepository.savePublicationDateTime(documentationUnit.uuid());
    }
    if (!statusUnchanged) {
      documentationUnitRepository.updatePortalPublicationStatus(
          documentationUnit.uuid(), newStatus);
    }

    addHistoryLog(documentationUnit, newStatus, user);
  }

  private RelatedPendingProceedingPublicationResult
      publishResolutionNoteOfRelatedPendingProceedings(Decision decision, User user) {
    if (decision.contentRelatedIndexing() == null
        || decision.contentRelatedIndexing().relatedPendingProceedings() == null) {
      return RelatedPendingProceedingPublicationResult.NO_ACTION;
    }

    var pendingProceedings = decision.contentRelatedIndexing().relatedPendingProceedings();
    Set<RelatedPendingProceedingPublicationResult> results = new HashSet<>();
    for (RelatedPendingProceeding relatedPendingProceeding : pendingProceedings) {
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
      DocumentationUnit documentationUnit, PendingProceeding pendingProceeding, User user)
      throws DocumentationUnitNotExistsException {
    if (pendingProceeding.coreData() != null && pendingProceeding.coreData().isResolved()) {
      log.atInfo()
          .addKeyValue(LoggingKeys.DOCUMENT_NUMBER, documentationUnit.documentNumber())
          .addKeyValue("id", documentationUnit.uuid())
          .setMessage(
              String.format(
                  "Do not mark pending proceeding %s as resolved. It already is resolved. A Documentation unit (%s) was published that contained it as a related pending proceeding",
                  pendingProceeding.documentNumber(), documentationUnit.documentNumber()))
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
            .orElse("Erledigt durch " + documentationUnit.documentNumber());

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
        .addKeyValue(LoggingKeys.DOCUMENT_NUMBER, documentationUnit.documentNumber())
        .addKeyValue("id", documentationUnit.uuid())
        .log(
            "Published pending proceeding {} as resolved. A Documentation unit ({}) was published that contained it as a related pending proceeding",
            pendingProceeding.documentNumber(),
            documentationUnit.documentNumber());
    return RelatedPendingProceedingPublicationResult.SUCCESS;
  }

  private void addHistoryLog(
      DocumentationUnit documentationUnit, PortalPublicationStatus newStatus, User user) {
    String historyLogMessage;
    if (PortalPublicationStatus.PUBLISHED.equals(newStatus)) {
      historyLogMessage = "Dokeinheit im Portal veröffentlicht";
    } else {
      historyLogMessage = "Dokeinheit wurde aus dem Portal zurückgezogen";
    }
    historyLogService.saveHistoryLog(
        documentationUnit.uuid(), user, HistoryLogEventType.PORTAL_PUBLICATION, historyLogMessage);
  }
}
