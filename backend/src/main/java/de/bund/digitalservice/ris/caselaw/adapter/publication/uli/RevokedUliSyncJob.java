package de.bund.digitalservice.ris.caselaw.adapter.publication.uli;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class RevokedUliSyncJob {

  private final UliCitationSyncService uliCitationSyncService;

  public RevokedUliSyncJob(UliCitationSyncService uliCitationSyncService) {
    this.uliCitationSyncService = uliCitationSyncService;
  }

  /**
   * Case 3: Sync for revoked documents. This job identifies all documents that point to a now
   * withdrawn/deleted target and triggers a republishing for them.
   */
  // @Scheduled(cron = "${neuris.jobs.uli-repeal-sync.cron:0 0 3 * * *}")
  public void runRevokedSync() {
    try {
      uliCitationSyncService.handleUliRevoked();
    } catch (Exception e) {
      log.error("Error during ULI Revoked Sync Job execution", e);
    }
  }
}
