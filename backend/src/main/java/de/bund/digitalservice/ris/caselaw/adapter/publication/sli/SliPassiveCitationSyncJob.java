package de.bund.digitalservice.ris.caselaw.adapter.publication.sli;

import de.bund.digitalservice.ris.caselaw.adapter.publication.JobSyncStatusService;
import de.bund.digitalservice.ris.caselaw.adapter.publication.SyncJob;
import java.time.Instant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class SliPassiveCitationSyncJob {

  private static final SyncJob job = SyncJob.SLI_PASSIVE_CITATION_SYNC;

  private final SliCitationSyncService sliCitationSyncService;
  private final JobSyncStatusService jobSyncStatusService;

  public SliPassiveCitationSyncJob(
      SliCitationSyncService sliCitationSyncService, JobSyncStatusService jobSyncStatusService) {
    this.sliCitationSyncService = sliCitationSyncService;
    this.jobSyncStatusService = jobSyncStatusService;
  }

  /**
   * Case 2: Synchronizes passive citations for SLI. It identifies missing or outdated passive links
   * in the database and republishes the affected documents to the portal.
   */
  // @Scheduled(cron = "${neuris.jobs.sli-sync.cron:0 0 2 * * *}")
  public void runSync() {
    var startOfRun = Instant.now();
    var lastRun = jobSyncStatusService.getLastRun(job);

    sliCitationSyncService.handleNewlyPublishedAfter(lastRun);

    jobSyncStatusService.updateLastRun(job, startOfRun);
  }
}
