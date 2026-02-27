package de.bund.digitalservice.ris.caselaw.adapter.publication.uli;

import de.bund.digitalservice.ris.caselaw.adapter.publication.PortalPublicationService;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class UliPassiveCitationSyncJob {

  private final UliCitationSyncService uliCitationSyncService;
  private final PortalPublicationService portalPublicationService;

  public UliPassiveCitationSyncJob(
      UliCitationSyncService uliCitationSyncService,
      PortalPublicationService portalPublicationService) {
    this.uliCitationSyncService = uliCitationSyncService;
    this.portalPublicationService = portalPublicationService;
  }

  /**
   * Case 2: Synchronizes passive citations for ULI. It identifies missing or outdated passive links
   * in the database and republishes the affected documents to the portal.
   */
  @Scheduled(cron = "${neuris.jobs.uli-sync.cron:0 0 2 * * *}")
  public void runSync() {
    log.info("Starting scheduled ULI Passive Citation Sync");

    try {
      Set<String> updatedDocNumbers = uliCitationSyncService.handleUliPassiveSync();

      if (updatedDocNumbers.isEmpty()) {
        log.info("ULI Passive Citation Sync finished: No documents required an update.");
        return;
      }

      log.info(
          "ULI Passive Citation Sync: Found {} documents to republish", updatedDocNumbers.size());

      int successCount = 0;
      for (String docNumber : updatedDocNumbers) {
        successCount = publishAndIncrementSuccessCount(docNumber, successCount);
      }

      log.info(
          "ULI Passive Citation Sync finished. Successfully republished {} of {} documents.",
          successCount,
          updatedDocNumbers.size());

    } catch (Exception e) {
      log.error("Critical error during ULI Passive Citation Sync Job", e);
    }
  }

  private int publishAndIncrementSuccessCount(String docNumber, int successCount) {
    try {
      portalPublicationService.publishDocumentationUnit(docNumber);
      log.debug("Successfully republished {} due to ULI sync", docNumber);
      successCount++;
    } catch (Exception e) {
      log.error("Failed to republish {} after ULI sync", docNumber, e);
    }
    return successCount;
  }
}
