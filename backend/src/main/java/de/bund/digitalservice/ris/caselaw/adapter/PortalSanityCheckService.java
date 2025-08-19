package de.bund.digitalservice.ris.caselaw.adapter;

import com.fasterxml.jackson.core.JsonProcessingException;
import de.bund.digitalservice.ris.caselaw.adapter.exception.PublishException;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnitRepository;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class PortalSanityCheckService {

  private final PortalPublicationService portalPublicationService;
  private final RiiService riiService;
  private final DocumentationUnitRepository documentationUnitRepository;
  private final PortalBucket portalBucket;
  private final Environment env;

  public PortalSanityCheckService(
      PortalPublicationService portalPublicationService,
      RiiService riiService,
      DocumentationUnitRepository documentationUnitRepository,
      PortalBucket portalBucket,
      Environment env) {
    this.portalPublicationService = portalPublicationService;
    this.riiService = riiService;
    this.documentationUnitRepository = documentationUnitRepository;
    this.portalBucket = portalBucket;
    this.env = env;
  }

  //                        ↓ day of month (1-31)
  //                      ↓ hour (0-23)
  //                    ↓ minute (0-59)
  //                 ↓ second (0-59)
  // Default:        0 30 5 * * * (After migration: 5:30)
  @Scheduled(cron = "0 30 5 * * *", zone = "Europe/Berlin")
  @SchedulerLock(name = "portal-publication-diff-job", lockAtMostFor = "PT15M")
  public void logPortalPublicationSanityCheck() {
    Set<String> portalBucketDocumentNumbers =
        portalBucket.getAllFilenames().stream()
            .filter(fileName -> fileName.contains(".xml"))
            .map(fileName -> fileName.substring(fileName.lastIndexOf("/") + 1))
            .map(fileName -> fileName.substring(0, fileName.lastIndexOf('.')))
            .collect(Collectors.toSet());

    logPublicationStatusDiff(portalBucketDocumentNumbers);

    // These checks are only relevant for the Testphase portal
    if (env.matchesProfiles("production")) {
      logDatabaseToBucketDiff(portalBucketDocumentNumbers);
      logBucketToRechtsprechungImInternetDiff(portalBucketDocumentNumbers);
    }
  }

  private void logBucketToRechtsprechungImInternetDiff(Set<String> portalBucketDocumentNumbers) {
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
          inPortalNotInRii.forEach(portalPublicationService::deleteDocumentationUnit);
          portalPublicationService.uploadDeletionChangelog(
              inPortalNotInRii.stream().map(documentNumber -> documentNumber + ".xml").toList());

        } catch (JsonProcessingException | PublishException e) {
          log.error(
              "Deleting documents not in Rechtsprechung im Internet failed with exception: {}",
              e.getMessage());
        }
      }
    }
  }

  private void logDatabaseToBucketDiff(Set<String> portalBucketDocumentNumbers) {
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

  private void logPublicationStatusDiff(Set<String> portalBucketDocumentNumbers) {
    Set<String> databasePublishedDocumentNumbers =
        documentationUnitRepository.findAllPublishedDocumentNumbers();
    List<String> inBucketNotInDatabase =
        portalBucketDocumentNumbers.stream()
            .filter(documentNumber -> !databasePublishedDocumentNumbers.contains(documentNumber))
            .toList();

    List<String> inDatabaseNotInBucket =
        databasePublishedDocumentNumbers.stream()
            .filter(documentNumber -> !portalBucketDocumentNumbers.contains(documentNumber))
            .toList();

    log.atInfo()
        .setMessage(
            "Finished sanity check for published doc units. Compared published doc units in db (status=PUBLISHED) and in bucket (LDML exists).")
        .addKeyValue("inDatabase", databasePublishedDocumentNumbers.size())
        .addKeyValue("inBucket", portalBucketDocumentNumbers.size())
        .addKeyValue("inBucketNotInDatabase", inBucketNotInDatabase.size())
        .addKeyValue("inDatabaseNotInBucket", inDatabaseNotInBucket.size())
        .log();

    if (!inDatabaseNotInBucket.isEmpty()) {
      String docNumbersInDatabaseNotInBucket = String.join(", ", inDatabaseNotInBucket);
      log.atError()
          .setMessage("Published document numbers found in database but not in bucket")
          .addKeyValue("inDatabaseNotInBucket", docNumbersInDatabaseNotInBucket)
          .log();
    }

    if (!inBucketNotInDatabase.isEmpty()) {
      String docNumbersInBucketNotInDatabase = String.join(", ", inBucketNotInDatabase);
      log.atError()
          .setMessage("Published document numbers found in bucket but not in database")
          .addKeyValue("inBucketNotInDatabase", docNumbersInBucketNotInDatabase)
          .log();
    }
  }
}
