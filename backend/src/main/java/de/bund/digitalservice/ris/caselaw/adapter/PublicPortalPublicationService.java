package de.bund.digitalservice.ris.caselaw.adapter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.CaseLawLdml;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.PortalPublicationJobRepository;
import de.bund.digitalservice.ris.caselaw.adapter.exception.BucketException;
import de.bund.digitalservice.ris.caselaw.adapter.exception.LdmlTransformationException;
import de.bund.digitalservice.ris.caselaw.adapter.exception.PublishException;
import de.bund.digitalservice.ris.caselaw.adapter.transformer.PublicPortalTransformer;
import de.bund.digitalservice.ris.caselaw.domain.Documentable;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnit;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnitRepository;
import de.bund.digitalservice.ris.caselaw.domain.exception.DocumentationUnitNotExistsException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.xml.parsers.DocumentBuilderFactory;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class PublicPortalPublicationService {

  private final DocumentationUnitRepository documentationUnitRepository;
  private final PortalPublicationJobRepository portalPublicationJobRepository;
  private final PublicPortalBucket publicPortalBucket;
  private final ObjectMapper objectMapper;
  private final XmlUtilService xmlUtilService;
  private final PublicPortalTransformer ldmlTransformer;

  @Autowired
  public PublicPortalPublicationService(
      DocumentationUnitRepository documentationUnitRepository,
      XmlUtilService xmlUtilService,
      DocumentBuilderFactory documentBuilderFactory,
      PublicPortalBucket publicPortalBucket,
      ObjectMapper objectMapper,
      PortalPublicationJobRepository portalPublicationJobRepository) {

    this.documentationUnitRepository = documentationUnitRepository;
    this.publicPortalBucket = publicPortalBucket;
    this.objectMapper = objectMapper;
    this.xmlUtilService = xmlUtilService;
    this.ldmlTransformer = new PublicPortalTransformer(documentBuilderFactory);
    this.portalPublicationJobRepository = portalPublicationJobRepository;
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
      publicPortalBucket.save(ldml.getUniqueId() + ".xml", fileContent.get());
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
    publicPortalBucket.save(changelog.createFileName(), changelogString);
  }

  //                        ↓ day of month (1-31)
  //                      ↓ hour (0-23)
  //                    ↓ minute (0-59)
  //                 ↓ second (0-59)
  // Default:        0 30 4 * * * (After migration: CET: 5:30)
  @Scheduled(cron = "0 30 4 * * *")
  @SchedulerLock(name = "portal-publication-diff-job", lockAtMostFor = "PT15M")
  public void logDatabaseToBucketDiff() {
    log.info(
        "Checking for discrepancies between publishable doc units in database and files in portal bucket.");
    List<String> databaseDocumentNumbers =
        documentationUnitRepository.findAllDocumentNumbersByMatchingPublishCriteria();
    List<String> portalBucketDocumentNumbers =
        publicPortalBucket.getAllFilenames().stream()
            .filter(fileName -> fileName.contains(".xml"))
            .map(fileName -> fileName.substring(0, fileName.lastIndexOf('.')))
            .toList();

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
    log.info(
        "Document numbers found in database but not in bucket: {}",
        inDatabaseNotInBucket.stream().map(Object::toString).collect(Collectors.joining(", ")));
    log.info(
        "Found {} doc units in bucket but not by database query.", inBucketNotInDatabase.size());
    log.info(
        "Document numbers found in bucket but not in database: {}",
        inBucketNotInDatabase.stream().map(Object::toString).collect(Collectors.joining(", ")));
  }

  @Scheduled(cron = "0 30 19 * * *")
  @SchedulerLock(name = "portal-publication-rii-diff-job", lockAtMostFor = "PT15M")
  public void logPortalToRiiDiff() {
    var riiDocumentNumbers = fetchRiiDocumentNumbers();
    var publishJobsDocumentNumbers =
        portalPublicationJobRepository.findAllDocumentNumbersPublishJobs();
    List<String> inRiiNotInPortal =
        riiDocumentNumbers.stream()
            .filter(documentNumber -> !publishJobsDocumentNumbers.contains(documentNumber))
            .toList();

    List<String> inPortalNotInRii =
        publishJobsDocumentNumbers.stream()
            .filter(documentNumber -> !riiDocumentNumbers.contains(documentNumber))
            .toList();

    log.info("Number of documents in Rechtsprechung im Internet: {}", riiDocumentNumbers.size());
    log.info("Number of documents in Portal: {}", publishJobsDocumentNumbers.size());
    log.info(
        "Found {} doc units in Portal but not in Rechtsprechung im Internet.",
        inPortalNotInRii.size());
    log.info(
        "Found {} doc units in Rechtsprechung im Internet but not in Portal.",
        inRiiNotInPortal.size());
    log.info(
        "Document numbers found in Rechtsprechung im Internet but not in Portal: {}",
        inRiiNotInPortal.stream().map(Object::toString).collect(Collectors.joining(", ")));
    log.info(
        "Document numbers found in Portal but not in Rechtsprechung im Internet: {}",
        inPortalNotInRii.stream().map(Object::toString).collect(Collectors.joining(", ")));
  }

  private List<String> fetchRiiDocumentNumbers() {
    final String URL = "https://www.rechtsprechung-im-internet.de/rii-toc.xml";
    List<String> documentNumbers = new ArrayList<>();
    try {
      Document doc =
          Jsoup.connect(URL).maxBodySize(0).parser(Parser.xmlParser()).timeout(10_000).get();

      Elements links = doc.select("link");

      for (Element link : links) {
        String linkText = link.text();
        String documentNumber = extractDocumentNumber(linkText);

        if (documentNumber != null) {
          documentNumbers.add(documentNumber);
        }
      }

      return documentNumbers;
    } catch (IOException e) {
      log.info(
          "Error fetching document numbers from Rechtsprechung im Internet: " + e.getMessage());
    }
    return List.of();
  }

  @SuppressWarnings("java:S5852")
  private static String extractDocumentNumber(String link) {
    String pattern = ".*/jb-([^/]+)\\.zip$";
    return link.matches(pattern) ? link.replaceAll(pattern, "$1") : null;
  }
}
