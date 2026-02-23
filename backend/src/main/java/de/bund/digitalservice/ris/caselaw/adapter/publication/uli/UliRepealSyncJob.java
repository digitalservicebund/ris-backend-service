package de.bund.digitalservice.ris.caselaw.adapter.publication.uli;

import de.bund.digitalservice.ris.caselaw.adapter.publication.PortalPublicationService;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class UliRepealSyncJob {

  private final UliCitationSyncService uliCitationSyncService;
  private final PortalPublicationService portalPublicationService;

  public UliRepealSyncJob(
      UliCitationSyncService uliCitationSyncService,
      PortalPublicationService portalPublicationService) {
    this.uliCitationSyncService = uliCitationSyncService;
    this.portalPublicationService = portalPublicationService;
  }

  /**
   * Case 3: Sync for repealed or deleted documents. This job identifies all documents that point to
   * a now withdrawn/deleted target and triggers a republishing for them.
   */
  @Scheduled(
      cron = "${neuris.jobs.uli-repeal-sync.cron:0 0 3 * * *}") // Etwas versetzt zum normalen Sync
  public void runRepealSync() {
    log.info("Starting scheduled ULI Repeal Sync Job");

    try {
      Set<String> affectedDocNumbers = uliCitationSyncService.handleUliRepeals();

      log.info("Found {} documents affected by ULI repeals", affectedDocNumbers.size());

      affectedDocNumbers.forEach(
          docNumber -> {
            try {
              portalPublicationService.publishDocumentationUnit(docNumber);
              log.debug("Successfully republished {} after repeal check", docNumber);
            } catch (Exception e) {
              log.error("Failed to republish {} during ULI repeal sync", docNumber, e);
            }
          });

      log.info("Finished ULI Repeal Sync Job");
    } catch (Exception e) {
      log.error("Error during ULI Repeal Sync Job execution", e);
    }
  }
}
