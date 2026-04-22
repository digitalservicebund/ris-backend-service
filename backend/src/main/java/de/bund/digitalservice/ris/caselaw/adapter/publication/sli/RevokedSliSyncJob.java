package de.bund.digitalservice.ris.caselaw.adapter.publication.sli;

import de.bund.digitalservice.ris.caselaw.adapter.publication.JobSyncStatusService;
import de.bund.digitalservice.ris.caselaw.adapter.publication.SyncJob;
import java.time.Instant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class RevokedSliSyncJob {

  private static final SyncJob job = SyncJob.SLI_REVOKED_SYNC;

  private final SliCitationSyncService sliCitationSyncService;
  private final JobSyncStatusService jobSyncStatusService;

  public RevokedSliSyncJob(
      SliCitationSyncService sliCitationSyncService, JobSyncStatusService jobSyncStatusService) {
    this.sliCitationSyncService = sliCitationSyncService;
    this.jobSyncStatusService = jobSyncStatusService;
  }

  /**
   * Case 3: Sync for revoked documents. This job identifies all documents that point to a now
   * withdrawn/deleted target and triggers a republishing for them.
   */
  // @Scheduled(cron = "${neuris.jobs.sli-repeal-sync.cron:0 0 3 * * *}")
  public void runSync() {
    var startOfRun = Instant.now();
    var lastRun = jobSyncStatusService.getLastRun(job);

    sliCitationSyncService.handleRevokedAfter(lastRun);

    jobSyncStatusService.updateLastRun(job, startOfRun);
  }
}
