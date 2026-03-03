package de.bund.digitalservice.ris.caselaw.adapter.publication.adm;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.assertArg;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.bund.digitalservice.ris.caselaw.adapter.publication.JobSyncStatusService;
import de.bund.digitalservice.ris.caselaw.adapter.publication.SyncJob;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@Import({RevokedAdmSyncJob.class})
class RevokedAdmSyncJobTest {
  @Autowired RevokedAdmSyncJob revokedAdmSyncJob;

  @MockitoBean AdmCitationSyncService admCitationSyncService;
  @MockitoBean JobSyncStatusService jobSyncStatusService;

  @Test
  void shouldRunWithLastRunDateAndUpdateLastRun() {
    var lastRun = Instant.parse("2025-01-01T00:00:00Z");

    when(jobSyncStatusService.getLastRun(SyncJob.ADM_REVOKED_SYNC)).thenReturn(lastRun);

    revokedAdmSyncJob.runSync();

    verify(admCitationSyncService).handleRevokedAfter(lastRun);
    verify(jobSyncStatusService)
        .updateLastRun(
            eq(SyncJob.ADM_REVOKED_SYNC),
            assertArg(
                newLastRun -> {
                  assertThat(newLastRun).isAfter(lastRun);
                }));
  }
}
