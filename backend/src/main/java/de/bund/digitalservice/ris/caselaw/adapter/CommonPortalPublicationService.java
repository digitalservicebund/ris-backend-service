package de.bund.digitalservice.ris.caselaw.adapter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.CaseLawLdml;
import de.bund.digitalservice.ris.caselaw.adapter.exception.BucketException;
import de.bund.digitalservice.ris.caselaw.adapter.exception.LdmlTransformationException;
import de.bund.digitalservice.ris.caselaw.adapter.exception.PublishException;
import de.bund.digitalservice.ris.caselaw.domain.Decision;
import de.bund.digitalservice.ris.caselaw.domain.Documentable;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnitRepository;
import de.bund.digitalservice.ris.caselaw.domain.exception.DocumentationUnitNotExistsException;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class CommonPortalPublicationService implements PortalPublicationService {

  private final DocumentationUnitRepository documentationUnitRepository;
  private final S3Bucket portalBucket;
  private final ObjectMapper objectMapper;
  private final XmlUtilService xmlUtilService;
  private final PortalTransformer ldmlTransformer;

  protected CommonPortalPublicationService(
      DocumentationUnitRepository documentationUnitRepository,
      XmlUtilService xmlUtilService,
      S3Bucket portalBucket,
      ObjectMapper objectMapper,
      PortalTransformer portalTransformer) {

    this.documentationUnitRepository = documentationUnitRepository;
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
    Documentable documentable = documentationUnitRepository.findByUuid(documentationUnitId);
    publishToBucket(documentable);
    try {
      uploadChangelog(List.of(documentable.documentNumber() + ".xml"), null);
    } catch (Exception e) {
      log.error("Could not upload changelog file.");
      deleteDocumentationUnit(documentable.documentNumber());
      throw new PublishException("Could not save changelog to bucket.", e);
    }
  }

  /**
   * Publish the documentation unit by transforming it to valid LDML and putting the resulting XML
   * file into a bucket together, specifying which documentation unit has been added or updated.
   *
   * @param documentNumber the documentation unit that should be published
   * @throws DocumentationUnitNotExistsException if the documentation unit with the given document
   *     number could not be found in the database
   * @throws LdmlTransformationException if the documentation unit could not be transformed to valid
   *     LDML
   * @throws PublishException if the LDML file could not be saved in the bucket
   */
  public void publishDocumentationUnit(String documentNumber)
      throws DocumentationUnitNotExistsException {
    Documentable documentable = documentationUnitRepository.findByDocumentNumber(documentNumber);
    publishToBucket(documentable);
  }

  private void publishToBucket(Documentable documentable) {
    if (!(documentable instanceof Decision decision)) {
      // for now pending proceedings can not be transformed to LDML, so they are ignored.
      return;
    }
    CaseLawLdml ldml = ldmlTransformer.transformToLdml(decision);
    Optional<String> fileContent = xmlUtilService.ldmlToString(ldml);
    if (fileContent.isEmpty()) {
      throw new LdmlTransformationException("Could not parse transformed LDML as string.", null);
    }

    try {
      portalBucket.save(ldml.getFileName(), fileContent.get());
    } catch (BucketException e) {
      throw new PublishException("Could not save LDML to bucket.", e);
    }
  }

  /**
   * Delete the documentation unit with the given documentNumber from the portal bucket.
   *
   * @param documentNumber the document number of the documentation unit to be deleted.
   */
  public void deleteDocumentationUnit(String documentNumber) {
    try {
      portalBucket.delete(documentNumber + ".xml");
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
}
