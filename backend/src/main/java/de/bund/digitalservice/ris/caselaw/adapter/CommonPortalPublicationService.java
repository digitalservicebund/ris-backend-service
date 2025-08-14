package de.bund.digitalservice.ris.caselaw.adapter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.CaseLawLdml;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.AttachmentDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.AttachmentRepository;
import de.bund.digitalservice.ris.caselaw.adapter.exception.BucketException;
import de.bund.digitalservice.ris.caselaw.adapter.exception.LdmlTransformationException;
import de.bund.digitalservice.ris.caselaw.adapter.exception.PublishException;
import de.bund.digitalservice.ris.caselaw.domain.Decision;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnit;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnitRepository;
import de.bund.digitalservice.ris.caselaw.domain.PendingProceeding;
import de.bund.digitalservice.ris.caselaw.domain.PortalPublicationStatus;
import de.bund.digitalservice.ris.caselaw.domain.exception.DocumentationUnitNotExistsException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class CommonPortalPublicationService implements PortalPublicationService {

  private final DocumentationUnitRepository documentationUnitRepository;
  private final AttachmentRepository attachmentRepository;
  private final S3Bucket portalBucket;
  private final ObjectMapper objectMapper;
  private final XmlUtilService xmlUtilService;
  private final PortalTransformer ldmlTransformer;

  protected CommonPortalPublicationService(
      DocumentationUnitRepository documentationUnitRepository,
      AttachmentRepository attachmentRepository,
      XmlUtilService xmlUtilService,
      S3Bucket portalBucket,
      ObjectMapper objectMapper,
      PortalTransformer portalTransformer) {

    this.documentationUnitRepository = documentationUnitRepository;
    this.attachmentRepository = attachmentRepository;
    this.portalBucket = portalBucket;
    this.objectMapper = objectMapper;
    this.xmlUtilService = xmlUtilService;
    this.ldmlTransformer = portalTransformer;
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
   * @throws PublishException if the changelog file could not be created or either of the files
   *     could not be saved in the bucket
   */
  public void publishDocumentationUnitWithChangelog(UUID documentationUnitId)
      throws DocumentationUnitNotExistsException {
    DocumentationUnit documentationUnit =
        documentationUnitRepository.findByUuid(documentationUnitId);
    var result = publishToBucket(documentationUnit);
    try {
      uploadChangelog(result.changedPaths(), result.deletedPaths());
    } catch (Exception e) {
      log.error("Could not upload changelog file.");
      deleteDocumentationUnit(documentationUnit.documentNumber());
      throw new PublishException("Could not save changelog to bucket.", e);
    }
    updatePortalPublicationStatus(documentationUnit);
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
    updatePortalPublicationStatus(documentationUnit);
    return publicationResult;
  }

  protected PortalPublicationResult publishToBucket(DocumentationUnit documentationUnit) {
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

  protected PortalPublicationResult saveToBucket(
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

  /**
   * Delete the documentation unit with the given documentNumber, including all attachments, from
   * the portal bucket.
   *
   * @param documentNumber the document number of the documentation unit to be deleted.
   */
  @Override
  public PortalPublicationResult deleteDocumentationUnit(String documentNumber) {
    try {
      var deletableFiles = portalBucket.getAllFilenamesByPath(documentNumber + "/");
      deletableFiles.forEach(portalBucket::delete);
      return new PortalPublicationResult(List.of(), deletableFiles);
    } catch (BucketException e) {
      throw new PublishException("Could not delete LDML from bucket.", e);
    }
  }

  /**
   * Generates a changelog file to trigger a full re-indexing of all documents in the bucket
   *
   * @throws JsonProcessingException if the changelog cannot be generated.
   */
  public void uploadFullReindexChangelog() throws JsonProcessingException {
    uploadChangelog(null, null, true);
  }

  /**
   * Generates a changelog file with the given parameters and saves it to the portal bucket.
   *
   * @param publishedDocumentNumbers the document numbers of the documentation units which have been
   *     changed or added.
   * @param deletedDocumentNumbers the document numbers of the documentation units which have been
   *     deleted.
   * @throws JsonProcessingException if the changelog cannot be generated.
   */
  public void uploadChangelog(
      List<String> publishedDocumentNumbers, List<String> deletedDocumentNumbers)
      throws JsonProcessingException {
    uploadChangelog(publishedDocumentNumbers, deletedDocumentNumbers, false);
  }

  /**
   * Generates a changelog file with the given parameters and saves it to the portal bucket.
   *
   * @param deletedDocumentNumbers the document numbers of the documentation units which have been
   *     deleted.
   * @throws JsonProcessingException if the changelog cannot be generated.
   */
  protected void uploadDeletionChangelog(List<String> deletedDocumentNumbers)
      throws JsonProcessingException {
    uploadChangelog(null, deletedDocumentNumbers, false);
  }

  private void uploadChangelog(
      List<String> publishedDocumentNumbers, List<String> deletedDocumentNumbers, boolean changeAll)
      throws JsonProcessingException {
    Changelog changelog;
    if (changeAll) {
      changelog = new ChangelogChangeAll(true);
    } else {
      changelog = new ChangelogUpdateDelete(publishedDocumentNumbers, deletedDocumentNumbers);
    }

    String changelogString = objectMapper.writeValueAsString(changelog);
    portalBucket.save(createChangelogFileName(), changelogString);
  }

  private String createChangelogFileName() {
    return "changelogs/" + Instant.now().toString() + "-caselaw.json";
  }

  private void updatePortalPublicationStatus(DocumentationUnit documentationUnit) {
    if (documentationUnit instanceof Decision decision) {
      documentationUnit =
          decision.toBuilder().portalPublicationStatus(PortalPublicationStatus.PUBLISHED).build();
    } else if (documentationUnit instanceof PendingProceeding pendingProceeding) {
      documentationUnit =
          pendingProceeding.toBuilder()
              .portalPublicationStatus(PortalPublicationStatus.PUBLISHED)
              .build();
    }
    documentationUnitRepository.save(documentationUnit);
  }
}
