package de.bund.digitalservice.ris.caselaw.adapter.publication;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.JobSyncStatus;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.JobSyncStatusRepository;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@Import({JobSyncStatusService.class})
class JobSyncStatusServiceTest {
  @Autowired JobSyncStatusService jobSyncStatusService;

  @MockitoBean JobSyncStatusRepository jobSyncStatusRepository;

  @Test
  void getLastRun() {
    var jobSyncStatusA = new JobSyncStatus("ADM_REVOKED_SYNC", Instant.now());
    var jobSyncStatusB = new JobSyncStatus("ADM_PASSIVE_CITATION_SYNC", Instant.now());
    when(jobSyncStatusRepository.findById("ADM_REVOKED_SYNC"))
        .thenReturn(Optional.of(jobSyncStatusA));
    when(jobSyncStatusRepository.findById("ADM_PASSIVE_CITATION_SYNC"))
        .thenReturn(Optional.of(jobSyncStatusB));

    assertThat(jobSyncStatusService.getLastRun(SyncJob.ADM_REVOKED_SYNC))
        .isEqualTo(jobSyncStatusA.getLastRun());
    assertThat(jobSyncStatusService.getLastRun(SyncJob.ADM_PASSIVE_CITATION_SYNC))
        .isEqualTo(jobSyncStatusB.getLastRun());
  }

  @Test
  void updateLastRun() {
    var date1 = Instant.now().minusSeconds(60);
    var date2 = Instant.now();
    var jobSyncStatus = new JobSyncStatus("ADM_REVOKED_SYNC", date1);
    when(jobSyncStatusRepository.findById("ADM_REVOKED_SYNC"))
        .thenReturn(Optional.of(jobSyncStatus));

    jobSyncStatusService.updateLastRun(SyncJob.ADM_REVOKED_SYNC, date2);

    verify(jobSyncStatusRepository, times(1)).save(jobSyncStatus);
    assertThat(jobSyncStatus.getLastRun()).isEqualTo(date2);
  }
}
