package de.bund.digitalservice.ris.caselaw.adapter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.CaseLawLdml;
import de.bund.digitalservice.ris.caselaw.adapter.exception.BucketException;
import de.bund.digitalservice.ris.caselaw.adapter.exception.LdmlTransformationException;
import de.bund.digitalservice.ris.caselaw.adapter.exception.PublishException;
import de.bund.digitalservice.ris.caselaw.adapter.transformer.PublicPortalTransformer;
import de.bund.digitalservice.ris.caselaw.domain.Documentable;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnit;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnitRepository;
import de.bund.digitalservice.ris.caselaw.domain.exception.DocumentationUnitNotExistsException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.xml.parsers.DocumentBuilderFactory;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class PublicPortalPublicationService {

  private final DocumentationUnitRepository documentationUnitRepository;
  private final PublicPortalBucket publicPortalBucket;
  private final ObjectMapper objectMapper;
  private final XmlUtilService xmlUtilService;
  private final PublicPortalTransformer ldmlTransformer;
  private final RiiService riiService;

  @Autowired
  public PublicPortalPublicationService(
      DocumentationUnitRepository documentationUnitRepository,
      XmlUtilService xmlUtilService,
      DocumentBuilderFactory documentBuilderFactory,
      PublicPortalBucket publicPortalBucket,
      ObjectMapper objectMapper,
      RiiService riiService) {

    this.documentationUnitRepository = documentationUnitRepository;
    this.publicPortalBucket = publicPortalBucket;
    this.objectMapper = objectMapper;
    this.xmlUtilService = xmlUtilService;
    this.ldmlTransformer = new PublicPortalTransformer(documentBuilderFactory);
    this.riiService = riiService;
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
      publicPortalBucket.save(ldml.getFileName(), fileContent.get());
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
      publicPortalBucket.delete(documentNumber + ".xml");
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
    publicPortalBucket.save(changelog.createFileName(), changelogString);
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

  //                        ↓ day of month (1-31)
  //                      ↓ hour (0-23)
  //                    ↓ minute (0-59)
  //                 ↓ second (0-59)
  // Default:        0 30 3 * * * (After migration: CET: 5:30)
  @Scheduled(cron = "0 30 3 * * *")
  @SchedulerLock(name = "portal-publication-diff-job", lockAtMostFor = "PT15M")
  public void logPortalPublicationSanityCheck() {
    List<String> portalBucketDocumentNumbers =
        publicPortalBucket.getAllFilenames().stream()
            .filter(fileName -> fileName.contains(".xml"))
            .map(fileName -> fileName.substring(0, fileName.lastIndexOf('.')))
            .toList();

    logDatabaseToBucketDiff(portalBucketDocumentNumbers);
    logBucketToRechtsprechungImInternetDiff(portalBucketDocumentNumbers);
  }

  private void logBucketToRechtsprechungImInternetDiff(List<String> portalBucketDocumentNumbers) {
    log.info(
        "Checking for discrepancies between published doc units and Rechtsprechung im Internet...");

    var riiDocumentNumbers = riiService.fetchRiiDocumentNumbers();
    log.info("Number of documents in Rechtsprechung im Internet: {}", riiDocumentNumbers.size());

    if (!riiDocumentNumbers.isEmpty()) {
      List<String> inRiiNotInPortal =
          riiDocumentNumbers.stream()
              .filter(documentNumber -> !portalBucketDocumentNumbers.contains(documentNumber))
              .toList();

      List<String> inPortalNotInRii =
          portalBucketDocumentNumbers.stream()
              .filter(documentNumber -> !riiDocumentNumbers.contains(documentNumber))
              .toList();

      log.info(
          "Found {} doc units in Portal but not in Rechtsprechung im Internet.",
          inPortalNotInRii.size());
      log.info(
          "Found {} doc units in Rechtsprechung im Internet but not in Portal.",
          inRiiNotInPortal.size());
      if (!inRiiNotInPortal.isEmpty()) {
        log.info(
            "Document numbers found in Rechtsprechung im Internet but not in Portal: {}",
            inRiiNotInPortal.stream().map(Object::toString).collect(Collectors.joining(", ")));
      }
      if (!inPortalNotInRii.isEmpty()) {
        log.info(
            "Document numbers found in Portal but not in Rechtsprechung im Internet: {}",
            inPortalNotInRii.stream().map(Object::toString).collect(Collectors.joining(", ")));
        log.info("Deleting documents not in Rechtsprechung im Internet...");
        try {
          inPortalNotInRii.forEach(this::deleteDocumentationUnit);
          uploadChangelog(
              null,
              inPortalNotInRii.stream().map(documentNumber -> documentNumber + ".xml").toList());

        } catch (JsonProcessingException | PublishException e) {
          log.error(
              "Deleting documents not in Rechtsprechung im Internet failed with exception: {}",
              e.getMessage());
        }
      }
    }
  }

  private void logDatabaseToBucketDiff(List<String> portalBucketDocumentNumbers) {
    List<String> databaseDocumentNumbers =
        documentationUnitRepository.findAllDocumentNumbersByMatchingPublishCriteria();
    List<String> inBucketNotInDatabase =
        portalBucketDocumentNumbers.stream()
            .filter(documentNumber -> !databaseDocumentNumbers.contains(documentNumber))
            .toList();

    List<String> inDatabaseNotInBucket =
        databaseDocumentNumbers.stream()
            .filter(documentNumber -> !portalBucketDocumentNumbers.contains(documentNumber))
            .toList();

    log.info("Number of LDML files in bucket: {}", portalBucketDocumentNumbers.size());
    log.info(
        "Found {} publishable doc units by database query but not in bucket.",
        inDatabaseNotInBucket.size());
    if (!inDatabaseNotInBucket.isEmpty()) {
      log.info(
          "Document numbers found in database but not in bucket: {}",
          inDatabaseNotInBucket.stream().map(Object::toString).collect(Collectors.joining(", ")));
    }
    log.info(
        "Found {} doc units in bucket but not by database query.", inBucketNotInDatabase.size());
    if (!inBucketNotInDatabase.isEmpty()) {
      log.info(
          "Document numbers found in bucket but not in database: {}",
          inBucketNotInDatabase.stream().map(Object::toString).collect(Collectors.joining(", ")));
    }
  }
}
