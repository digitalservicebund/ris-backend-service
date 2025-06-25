package de.bund.digitalservice.ris.caselaw.adapter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.AttachmentRepository;
import de.bund.digitalservice.ris.caselaw.adapter.exception.PublishException;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnitRepository;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;

@Slf4j
public class PrototypePortalPublicationService extends CommonPortalPublicationService {

  private final RiiService riiService;
  private final DocumentationUnitRepository documentationUnitRepository;
  private final PrototypePortalBucket prototypePortalBucket;

  public PrototypePortalPublicationService(
      DocumentationUnitRepository documentationUnitRepository,
      AttachmentRepository attachmentRepository,
      XmlUtilService xmlUtilService,
      PrototypePortalBucket prototypePortalBucket,
      ObjectMapper objectMapper,
      PortalTransformer portalTransformer,
      RiiService riiService) {
    super(
        documentationUnitRepository,
        attachmentRepository,
        xmlUtilService,
        prototypePortalBucket,
        objectMapper,
        portalTransformer);
    this.riiService = riiService;
    this.documentationUnitRepository = documentationUnitRepository;
    this.prototypePortalBucket = prototypePortalBucket;
  }

  @Override
  public void publishDocumentationUnitWithChangelog(UUID documentationUnitId) {
    // no-op in prototype
  }

  @Override
  public void uploadChangelog(
      List<String> publishedDocumentNumbers, List<String> deletedDocumentNumbers) {
    // no-op in prototype - delta changelog uploads currently disabled
  }

  //                        ↓ day of month (1-31)
  //                      ↓ hour (0-23)
  //                    ↓ minute (0-59)
  //                 ↓ second (0-59)
  // Default:        0 30 5 * * * (After migration: 5:30)
  @Scheduled(cron = "0 30 5 * * *", zone = "Europe/Berlin")
  @SchedulerLock(name = "portal-publication-diff-job", lockAtMostFor = "PT15M")
  public void logPortalPublicationSanityCheck() {
    List<String> portalBucketDocumentNumbers =
        prototypePortalBucket.getAllFilenames().stream()
            .filter(fileName -> fileName.contains(".xml"))
            .map(fileName -> fileName.substring(0, fileName.lastIndexOf('.')))
            .map(fileName -> fileName.substring(0, fileName.lastIndexOf("/")))
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
          uploadDeletionChangelog(
              inPortalNotInRii.stream()
                  .map(documentNumber -> documentNumber + "/" + documentNumber + ".xml")
                  .toList());

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
