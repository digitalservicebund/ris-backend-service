package de.bund.digitalservice.ris.caselaw.adapter.publication.uli;

import de.bund.digitalservice.ris.caselaw.adapter.publication.PortalPublicationService;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class RevokedUliSyncJob {

  private final UliCitationSyncService uliCitationSyncService;
  private final PortalPublicationService portalPublicationService;

  public RevokedUliSyncJob(
      UliCitationSyncService uliCitationSyncService,
      PortalPublicationService portalPublicationService) {
    this.uliCitationSyncService = uliCitationSyncService;
    this.portalPublicationService = portalPublicationService;
  }

  /**
   * Case 3: Sync for revoked documents. This job identifies all documents that point to a now
   * withdrawn/deleted target and triggers a republishing for them.
   */
  @Scheduled(
      cron = "${neuris.jobs.uli-repeal-sync.cron:0 0 3 * * *}") // Etwas versetzt zum normalen Sync
  public void runRevokedSync() {

    try {
      Set<String> affectedDocNumbers = uliCitationSyncService.handleUliRevoked();

      affectedDocNumbers.forEach(
          docNumber -> {
            try {
              portalPublicationService.publishDocumentationUnit(docNumber);
              log.debug("Successfully republished {} after revoked check", docNumber);
            } catch (Exception e) {
              log.error("Failed to republish {} during ULI revoked sync", docNumber, e);
            }
          });
    } catch (Exception e) {
      log.error("Error during ULI Revoked Sync Job execution", e);
    }
  }
}
