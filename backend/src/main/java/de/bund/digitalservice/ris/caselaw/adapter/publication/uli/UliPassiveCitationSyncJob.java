package de.bund.digitalservice.ris.caselaw.adapter.publication.uli;

import de.bund.digitalservice.ris.caselaw.adapter.publication.JobSyncStatusService;
import de.bund.digitalservice.ris.caselaw.adapter.publication.SyncJob;
import java.time.Instant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class UliPassiveCitationSyncJob {
  private static final SyncJob job = SyncJob.ULI_PASSIVE_CITATION_SYNC;

  private final UliCitationSyncService uliCitationSyncService;
  private final JobSyncStatusService jobSyncStatusService;

  public UliPassiveCitationSyncJob(
      UliCitationSyncService uliCitationSyncService, JobSyncStatusService jobSyncStatusService) {
    this.uliCitationSyncService = uliCitationSyncService;
    this.jobSyncStatusService = jobSyncStatusService;
  }

  /**
   * Case 2: Synchronizes passive citations for ULI. It identifies missing or outdated passive links
   * in the database and republishes the affected documents to the portal.
   */
  // @Scheduled(cron = "${neuris.jobs.uli-sync.cron:0 0 2 * * *}")

  public void runSync() {
    var startOfRun = Instant.now();
    try {
      var lastRun = jobSyncStatusService.getLastRun(job);
      uliCitationSyncService.handleNewlyPublishedAfter(lastRun);
      jobSyncStatusService.updateLastRun(job, startOfRun);
    } catch (Exception e) {
      log.error("Critical error during ULI Passive Citation Sync Job", e);
    }
  }
}
