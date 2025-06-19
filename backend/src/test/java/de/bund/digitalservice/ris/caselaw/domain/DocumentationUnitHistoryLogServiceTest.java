package de.bund.digitalservice.ris.caselaw.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

@ExtendWith(MockitoExtension.class)
class DocumentationUnitHistoryLogServiceTest {

  @Mock DocumentationUnitHistoryLogRepository repository;
  @Mock UserService userService;
  @Mock OidcUser oidcUser;
  @InjectMocks DocumentationUnitHistoryLogService service;

  @Test
  void getHistoryLogs() {
    DocumentationOffice documentationOffice =
        DocumentationOffice.builder().id(UUID.randomUUID()).build();
    User user = User.builder().documentationOffice(documentationOffice).build();
    Decision decision = Decision.builder().build();

    HistoryLog log1 =
        HistoryLog.builder()
            .documentationOffice("BSG")
            .eventType(HistoryLogEventType.UPDATE)
            .description("Updated field")
            .createdBy("mock-user")
            .build();

    when(repository.findByDocumentationUnitId(decision.uuid(), user)).thenReturn(List.of(log1));

    List<HistoryLog> result = service.getHistoryLogs(decision.uuid(), user);

    assertThat(result).hasSize(1).containsExactly(log1);

    verify(repository).findByDocumentationUnitId(decision.uuid(), user);
  }

  @Test
  void saveHistoryLog_NonUpdateType() {
    DocumentationOffice documentationOffice =
        DocumentationOffice.builder().id(UUID.randomUUID()).build();
    User user = User.builder().documentationOffice(documentationOffice).build();
    Decision decision = Decision.builder().build();

    service.saveHistoryLog(decision.uuid(), user, HistoryLogEventType.STATUS, "Status geändert");

    verify(repository, never()).findUpdateLogForDuration(any(), any(), any(), any());
    verify(repository)
        .saveHistoryLog(null, decision.uuid(), user, HistoryLogEventType.STATUS, "Status geändert");
  }

  @Test
  void saveHistoryLog_UpdateTypeWithoutExistingLog() {
    DocumentationOffice documentationOffice =
        DocumentationOffice.builder().id(UUID.randomUUID()).build();
    User user = User.builder().documentationOffice(documentationOffice).build();
    Decision decision = Decision.builder().build();

    when(repository.findUpdateLogForDuration(any(), any(), any(), any()))
        .thenReturn(Optional.empty());

    service.saveHistoryLog(decision.uuid(), user, HistoryLogEventType.UPDATE, "Edit");

    verify(repository, times(1)).findUpdateLogForDuration(any(), any(), any(), any());
    verify(repository)
        .saveHistoryLog(null, decision.uuid(), user, HistoryLogEventType.UPDATE, "Edit");
  }

  @Test
  void saveHistoryLog_UpdateTypeWithExistingLog() {
    DocumentationOffice documentationOffice =
        DocumentationOffice.builder().id(UUID.randomUUID()).build();
    User user = User.builder().documentationOffice(documentationOffice).build();
    Decision decision = Decision.builder().build();

    HistoryLog log =
        HistoryLog.builder()
            .id(UUID.randomUUID())
            .documentationOffice("BSG")
            .eventType(HistoryLogEventType.UPDATE)
            .description("Updated field")
            .createdBy("mock-user")
            .build();
    var startOfDay = Instant.now().truncatedTo(ChronoUnit.DAYS);
    var endOfDay = startOfDay.plus(1, ChronoUnit.DAYS);
    when(repository.findUpdateLogForDuration(decision.uuid(), user, startOfDay, endOfDay))
        .thenReturn(Optional.ofNullable(log));

    service.saveHistoryLog(decision.uuid(), user, HistoryLogEventType.UPDATE, "Edit");

    verify(repository, times(1)).findUpdateLogForDuration(any(), any(), any(), any());
    verify(repository)
        .saveHistoryLog(log.id(), decision.uuid(), user, HistoryLogEventType.UPDATE, "Edit");
  }
}
