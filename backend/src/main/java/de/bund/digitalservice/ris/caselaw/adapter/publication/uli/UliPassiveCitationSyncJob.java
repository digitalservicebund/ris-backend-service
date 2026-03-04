package de.bund.digitalservice.ris.caselaw.adapter.publication.uli;

import de.bund.digitalservice.ris.caselaw.adapter.publication.PortalPublicationService;
import lombok.extern.slf4j.Slf4j;
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
  // @Scheduled(cron = "${neuris.jobs.uli-sync.cron:0 0 2 * * *}")
  public void runSync() {
    log.info("Starting scheduled ULI Passive Citation Sync");
    try {
      uliCitationSyncService.handleUliPassiveSync();
    } catch (Exception e) {
      log.error("Critical error during ULI Passive Citation Sync Job", e);
    }
  }
}
