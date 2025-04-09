package de.bund.digitalservice.ris.caselaw.adapter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.CaseLawLdml;
import de.bund.digitalservice.ris.caselaw.adapter.exception.BucketException;
import de.bund.digitalservice.ris.caselaw.adapter.exception.LdmlTransformationException;
import de.bund.digitalservice.ris.caselaw.adapter.exception.PublishException;
import de.bund.digitalservice.ris.caselaw.adapter.transformer.InternalPortalTransformer;
import de.bund.digitalservice.ris.caselaw.domain.Documentable;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnit;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnitRepository;
import de.bund.digitalservice.ris.caselaw.domain.exception.DocumentationUnitNotExistsException;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import javax.xml.parsers.DocumentBuilderFactory;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PortalPublicationService {

  private final DocumentationUnitRepository documentationUnitRepository;
  private final S3Bucket portalBucket;
  private final ObjectMapper objectMapper;
  private final XmlUtilService xmlUtilService;
  private final InternalPortalTransformer ldmlTransformer;

  public PortalPublicationService(
      DocumentationUnitRepository documentationUnitRepository,
      XmlUtilService xmlUtilService,
      DocumentBuilderFactory documentBuilderFactory,
      S3Bucket portalBucket,
      ObjectMapper objectMapper) {

    this.documentationUnitRepository = documentationUnitRepository;
    this.portalBucket = portalBucket;
    this.objectMapper = objectMapper;
    this.xmlUtilService = xmlUtilService;
    this.ldmlTransformer = new InternalPortalTransformer(documentBuilderFactory);
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

    if (!(documentable instanceof DocumentationUnit documentationUnit)) {
      throw new UnsupportedOperationException(
          "Publish not supported for Documentable type: " + documentable.getClass());
    }

    CaseLawLdml ldml = ldmlTransformer.transformToLdml(documentationUnit);

    Optional<String> fileContent = xmlUtilService.ldmlToString(ldml);
    if (fileContent.isEmpty()) {
      throw new LdmlTransformationException(
          "Could not transform documentation unit to valid LDML.", null);
    }

    Changelog changelog = new Changelog(List.of(ldml.getFileName()), null, null);
    String changelogJson;
    try {
      changelogJson = objectMapper.writeValueAsString(changelog);
    } catch (IOException e) {
      log.error("Could not write changelog file. {}", e.getMessage());
      throw new PublishException(
          "Could not publish documentation unit to portal, because changelog file could not be created.",
          null);
    }

    try {
      portalBucket.save(changelog.createFileName(), changelogJson);
    } catch (BucketException e) {
      log.error("Could not save changelog to bucket", e);
      throw new PublishException("Could not save changelog to bucket.", e);
    }

    try {
      portalBucket.save(ldml.getFileName(), fileContent.get());
      log.info("LDML for documentation unit {} successfully published.", ldml.getUniqueId());
    } catch (BucketException e) {
      log.error("Could not save LDML to bucket", e);
      throw new PublishException("Could not save LDML to bucket.", e);
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
    if (!(documentable instanceof DocumentationUnit documentationUnit)) {
      // for now pending proceedings can not be transformed to LDML, so they are ignored.
      return;
    }
    CaseLawLdml ldml = ldmlTransformer.transformToLdml(documentationUnit);

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
   * Delete the documentation unit with the given documentNumber from the public portal bucket.
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

  private void uploadChangelog(
      List<String> publishedDocumentNumbers, List<String> deletedDocumentNumbers, Boolean changeAll)
      throws JsonProcessingException {
    Changelog changelog =
        new Changelog(publishedDocumentNumbers, deletedDocumentNumbers, changeAll);

    String changelogString = objectMapper.writeValueAsString(changelog);
    portalBucket.save(changelog.createFileName(), changelogString);
  }

  /**
   * Generates a changelog file with the given parameters and saves it to the public portal bucket.
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
    uploadChangelog(publishedDocumentNumbers, deletedDocumentNumbers, null);
  }

  /**
   * Generates a changelog file to trigger a full re-indexing of all documents in the bucket
   *
   * @throws JsonProcessingException if the changelog cannot be generated.
   */
  public void uploadFullReindexChangelog() throws JsonProcessingException {
    uploadChangelog(null, null, true);
  }
}
