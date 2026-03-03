package de.bund.digitalservice.ris.caselaw.adapter.publication.adm;

import de.bund.digitalservice.ris.caselaw.adapter.publication.JobSyncStatusService;
import de.bund.digitalservice.ris.caselaw.adapter.publication.SyncJob;
import java.time.Instant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class AdmPassiveCitationSyncJob {

  private static final SyncJob job = SyncJob.ADM_PASSIVE_CITATION_SYNC;

  private final AdmCitationSyncService admCitationSyncService;
  private final JobSyncStatusService jobSyncStatusService;

  public AdmPassiveCitationSyncJob(
      AdmCitationSyncService admCitationSyncService, JobSyncStatusService jobSyncStatusService) {
    this.admCitationSyncService = admCitationSyncService;
    this.jobSyncStatusService = jobSyncStatusService;
  }

  /**
   * Case 2: Synchronizes passive citations for ADM. It identifies missing or outdated passive links
   * in the database and republishes the affected documents to the portal.
   */
  // @Scheduled(cron = "${neuris.jobs.adm-sync.cron:0 0 2 * * *}")
  public void runSync() {
    var startOfRun = Instant.now();
    var lastRun = jobSyncStatusService.getLastRun(job);

    admCitationSyncService.handleNewlyPublishedAfter(lastRun);

    jobSyncStatusService.updateLastRun(job, startOfRun);
  }
}
