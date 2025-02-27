package de.bund.digitalservice.ris.caselaw.adapter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.CaseLawLdml;
import de.bund.digitalservice.ris.caselaw.adapter.exception.BucketException;
import de.bund.digitalservice.ris.caselaw.adapter.exception.LdmlTransformationException;
import de.bund.digitalservice.ris.caselaw.adapter.exception.PublishException;
import de.bund.digitalservice.ris.caselaw.adapter.transformer.DocumentationUnitToLdmlTransformer;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnit;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnitRepository;
import de.bund.digitalservice.ris.caselaw.domain.exception.DocumentationUnitNotExistsException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import javax.xml.parsers.DocumentBuilderFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class PublicPortalPublicationService {

  private final DocumentationUnitRepository documentationUnitRepository;
  private final DocumentBuilderFactory documentBuilderFactory;
  private final PublicPortalBucket publicPortalBucket;
  private final ObjectMapper objectMapper;
  private final XmlUtilService xmlUtilService;

  @Autowired
  public PublicPortalPublicationService(
      DocumentationUnitRepository documentationUnitRepository,
      XmlUtilService xmlUtilService,
      DocumentBuilderFactory documentBuilderFactory,
      PublicPortalBucket publicPortalBucket,
      ObjectMapper objectMapper) {

    this.documentationUnitRepository = documentationUnitRepository;
    this.documentBuilderFactory = documentBuilderFactory;
    this.publicPortalBucket = publicPortalBucket;
    this.objectMapper = objectMapper;
    this.xmlUtilService = xmlUtilService;
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
    DocumentationUnit documentationUnit =
        documentationUnitRepository.findByDocumentNumber(documentNumber);
    Optional<CaseLawLdml> ldml =
        DocumentationUnitToLdmlTransformer.transformToLdml(
            documentationUnit, documentBuilderFactory);

    if (ldml.isEmpty()) {
      throw new LdmlTransformationException(
          "Could not transform documentation unit to LDML.", null);
    }

    Optional<String> fileContent = xmlUtilService.ldmlToString(ldml.get());
    if (fileContent.isEmpty()) {
      throw new LdmlTransformationException(
          "Could not transform documentation unit to valid LDML.", null);
    }

    try {
      publicPortalBucket.save(ldml.get().getUniqueId() + ".xml", fileContent.get());
      log.info("LDML for documentation unit {} successfully published.", ldml.get().getUniqueId());
    } catch (BucketException e) {
      log.error("Could not save LDML to bucket", e);
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
      publicPortalBucket.delete(documentNumber + ".xml");
    } catch (BucketException e) {
      log.error("Could not delete LDML from bucket, docNumber: {}", documentNumber, e);
    }
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
    Changelog changelog = new Changelog(publishedDocumentNumbers, deletedDocumentNumbers);

    String changelogString = objectMapper.writeValueAsString(changelog);
    publicPortalBucket.save(
        "changelogs/" + DateUtils.toDateTimeString(LocalDateTime.now()) + ".json", changelogString);
  }
}
