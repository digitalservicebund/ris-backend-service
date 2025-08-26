package de.bund.digitalservice.ris.caselaw.adapter;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.CaseLawLdml;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.AttachmentDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.AttachmentRepository;
import de.bund.digitalservice.ris.caselaw.adapter.exception.BucketException;
import de.bund.digitalservice.ris.caselaw.adapter.exception.ChangelogException;
import de.bund.digitalservice.ris.caselaw.adapter.exception.LdmlTransformationException;
import de.bund.digitalservice.ris.caselaw.adapter.exception.PublishException;
import de.bund.digitalservice.ris.caselaw.domain.Decision;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnit;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnitHistoryLogService;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnitRepository;
import de.bund.digitalservice.ris.caselaw.domain.FeatureToggleService;
import de.bund.digitalservice.ris.caselaw.domain.HistoryLogEventType;
import de.bund.digitalservice.ris.caselaw.domain.LdmlTransformationResult;
import de.bund.digitalservice.ris.caselaw.domain.PortalPublicationStatus;
import de.bund.digitalservice.ris.caselaw.domain.User;
import de.bund.digitalservice.ris.caselaw.domain.exception.DocumentationUnitException;
import de.bund.digitalservice.ris.caselaw.domain.exception.DocumentationUnitNotExistsException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mapping.MappingException;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class PortalPublicationService {

  private final DocumentationUnitRepository documentationUnitRepository;
  private final AttachmentRepository attachmentRepository;
  private final S3Bucket portalBucket;
  private final ObjectMapper objectMapper;
  private final XmlUtilService xmlUtilService;
  private final PortalTransformer ldmlTransformer;
  private final FeatureToggleService featureToggleService;
  private final DocumentationUnitHistoryLogService historyLogService;

  private static final String PUBLICATION_FEATURE_FLAG = "neuris.portal-publication";
  private static final String CHANGELOG_FEATURE_FLAG = "neuris.regular-changelogs";

  public PortalPublicationService(
      DocumentationUnitRepository documentationUnitRepository,
      AttachmentRepository attachmentRepository,
      XmlUtilService xmlUtilService,
      S3Bucket portalBucket,
      ObjectMapper objectMapper,
      PortalTransformer portalTransformer,
      FeatureToggleService featureToggleService,
      DocumentationUnitHistoryLogService historyLogService) {

    this.documentationUnitRepository = documentationUnitRepository;
    this.attachmentRepository = attachmentRepository;
    this.portalBucket = portalBucket;
    this.objectMapper = objectMapper;
    this.xmlUtilService = xmlUtilService;
    this.ldmlTransformer = portalTransformer;
    this.featureToggleService = featureToggleService;
    this.historyLogService = historyLogService;
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
  public void publishDocumentationUnitWithChangelog(UUID documentationUnitId, User user)
      throws DocumentationUnitNotExistsException {
    if (!featureToggleService.isEnabled(PUBLICATION_FEATURE_FLAG)) {
      return;
    }
    try {
      DocumentationUnit documentationUnit =
          documentationUnitRepository.findByUuid(documentationUnitId);
      var result = publishToBucket(documentationUnit);
      uploadChangelogWithdrawOnFailure(documentationUnit, result);
      updatePortalPublicationStatus(documentationUnit, PortalPublicationStatus.PUBLISHED, user);
    } catch (Exception exception) {
      historyLogService.saveHistoryLog(
          documentationUnitId,
          user,
          HistoryLogEventType.PORTAL_PUBLICATION,
          "Dokeinheit konnte nicht veröffentlicht werden");
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
    return publicationResult;
  }

  /**
   * Deletes the documentation unit and its attachments with the given documentNumber from the
   * portal bucket.
   *
   * @param documentNumber the document number of the documentation unit to be withdrawn.
   */
  public PortalPublicationResult withdrawDocumentationUnit(String documentNumber) {
    try {
      var deletableFiles = portalBucket.getAllFilenamesByPath(documentNumber + "/");
      deletableFiles.forEach(portalBucket::delete);
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
      var result = withdrawDocumentationUnit(documentationUnit.documentNumber());
      uploadDeletionChangelog(result.deletedPaths());
      updatePortalPublicationStatus(documentationUnit, PortalPublicationStatus.WITHDRAWN, user);
    } catch (Exception e) {
      historyLogService.saveHistoryLog(
          documentationUnitId,
          user,
          HistoryLogEventType.PORTAL_PUBLICATION,
          "Dokeinheit konnte nicht zurückgezogen werden");
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
    if (documentationUnit instanceof Decision) {
      CaseLawLdml ldml = ldmlTransformer.transformToLdml(documentationUnit);
      Optional<String> fileContent = xmlUtilService.ldmlToString(ldml);
      if (fileContent.isEmpty()) {
        throw new LdmlTransformationException("Could not parse transformed LDML as string.", null);
      }
      return LdmlTransformationResult.builder().success(true).ldml(fileContent.get()).build();
    } else {
      var message =
          String.format(
              "Document type %s is not supported.", documentationUnit.getClass().getSimpleName());
      throw new DocumentationUnitException(message);
    }
  }

  private PortalPublicationResult publishToBucket(DocumentationUnit documentationUnit) {
    if (!(documentationUnit instanceof Decision)) {
      // for now pending proceedings can not be processed by the portal, so they are ignored.
      return null;
    }
    List<AttachmentDTO> attachments =
        attachmentRepository.findAllByDocumentationUnitId(documentationUnit.uuid());
    CaseLawLdml ldml = ldmlTransformer.transformToLdml(documentationUnit);
    Optional<String> fileContent = xmlUtilService.ldmlToString(ldml);
    if (fileContent.isEmpty()) {
      throw new LdmlTransformationException("Could not parse transformed LDML as string.", null);
    }

    return saveToBucket(
        ldml.getUniqueId() + "/", ldml.getFileName(), fileContent.get(), attachments);
  }

  private PortalPublicationResult saveToBucket(
      String path, String fileName, String fileContent, List<AttachmentDTO> attachments) {
    try {
      List<String> existingFiles = portalBucket.getAllFilenamesByPath(path);
      List<String> addedFiles = new ArrayList<>();

      portalBucket.save(path + fileName, fileContent);
      addedFiles.add(path + fileName);

      if (!attachments.isEmpty()) {
        attachments.stream()
            .filter(
                attachment ->
                    !attachment.getFormat().equals("docx") && !attachment.getFormat().equals("fmx"))
            .forEach(
                attachment -> {
                  portalBucket.saveBytes(path + attachment.getFilename(), attachment.getContent());
                  addedFiles.add(path + attachment.getFilename());
                });
      }

      // Check for files that are not part of this update and remove them (e.g. removed images)
      existingFiles.removeAll(addedFiles);
      existingFiles.forEach(portalBucket::delete);

      return new PortalPublicationResult(addedFiles, existingFiles);
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
      withdrawDocumentationUnit(documentationUnit.documentNumber());
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
    var oldStatus = documentationUnit.portalPublicationStatus();
    if (newStatus.equals(oldStatus)) {
      return;
    }
    documentationUnitRepository.updatePortalPublicationStatus(documentationUnit.uuid(), newStatus);
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
