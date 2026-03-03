package de.bund.digitalservice.ris.caselaw.adapter.publication.adm;

import de.bund.digitalservice.ris.caselaw.adapter.publication.JobSyncStatusService;
import de.bund.digitalservice.ris.caselaw.adapter.publication.SyncJob;
import java.time.Instant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class RevokedAdmSyncJob {

  private static final SyncJob job = SyncJob.ADM_REVOKED_SYNC;

  private final AdministrativeRegulationCitationSyncService
      administrativeRegulationCitationSyncService;
  private final JobSyncStatusService jobSyncStatusService;

  public RevokedAdmSyncJob(
      AdministrativeRegulationCitationSyncService administrativeRegulationCitationSyncService,
      JobSyncStatusService jobSyncStatusService) {
    this.administrativeRegulationCitationSyncService = administrativeRegulationCitationSyncService;
    this.jobSyncStatusService = jobSyncStatusService;
  }

  /**
   * Case 3: Sync for revoked documents. This job identifies all documents that point to a now
   * withdrawn/deleted target and triggers a republishing for them.
   */
  // @Scheduled(cron = "${neuris.jobs.adm-repeal-sync.cron:0 0 3 * * *}")
  public void runSync() {
    var startOfRun = Instant.now();
    var lastRun = jobSyncStatusService.getLastRun(job);

    administrativeRegulationCitationSyncService.handleRevokedAfter(lastRun);

    jobSyncStatusService.updateLastRun(job, startOfRun);
  }
}
