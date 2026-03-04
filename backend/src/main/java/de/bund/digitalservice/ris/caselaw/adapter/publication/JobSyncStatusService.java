package de.bund.digitalservice.ris.caselaw.adapter.publication;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.JobSyncStatus;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.JobSyncStatusRepository;
import java.time.Instant;
import org.springframework.stereotype.Service;

@Service
public class JobSyncStatusService {

  private final JobSyncStatusRepository jobSyncStatusRepository;

  public JobSyncStatusService(JobSyncStatusRepository jobSyncStatusRepository) {
    this.jobSyncStatusRepository = jobSyncStatusRepository;
  }

  public Instant getLastRun(SyncJob job) {
    return jobSyncStatusRepository
        .findById(job.getName())
        .map(JobSyncStatus::getLastRun)
        .orElse(Instant.EPOCH);
  }

  public void updateLastRun(SyncJob job, Instant time) {
    JobSyncStatus status =
        jobSyncStatusRepository
            .findById(job.getName())
            .orElse(new JobSyncStatus(job.getName(), time));
    status.setLastRun(time);
    jobSyncStatusRepository.save(status);
  }
}
