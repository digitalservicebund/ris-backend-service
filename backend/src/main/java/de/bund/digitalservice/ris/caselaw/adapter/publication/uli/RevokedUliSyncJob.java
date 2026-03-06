package de.bund.digitalservice.ris.caselaw.adapter.publication.uli;

import de.bund.digitalservice.ris.caselaw.adapter.publication.JobSyncStatusService;
import de.bund.digitalservice.ris.caselaw.adapter.publication.SyncJob;
import java.time.Instant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class RevokedUliSyncJob {
  private static final SyncJob job = SyncJob.ULI_REVOKED_SYNC;

  private final UliCitationSyncService uliCitationSyncService;
  private final JobSyncStatusService jobSyncStatusService;

  public RevokedUliSyncJob(
      UliCitationSyncService uliCitationSyncService, JobSyncStatusService jobSyncStatusService) {
    this.uliCitationSyncService = uliCitationSyncService;
    this.jobSyncStatusService = jobSyncStatusService;
  }

  /**
   * Case 3: Sync for revoked documents. This job identifies all documents that point to a now
   * withdrawn/deleted target and triggers a republishing for them.
   */
  // @Scheduled(cron = "${neuris.jobs.uli-repeal-sync.cron:0 0 3 * * *}")
  public void runRevokedSync() {
    var startOfRun = Instant.now();
    var lastRun = jobSyncStatusService.getLastRun(job);
    try {
      uliCitationSyncService.handleRevokedAfter(lastRun);
      jobSyncStatusService.updateLastRun(job, startOfRun);
    } catch (Exception e) {
      log.error("Error during ULI Revoked Sync Job execution", e);
    }
  }
}
