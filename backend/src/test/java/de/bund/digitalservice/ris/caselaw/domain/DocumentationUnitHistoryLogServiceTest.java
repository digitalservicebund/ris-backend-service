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
        DocumentationOffice.builder().uuid(UUID.randomUUID()).build();
    User user = User.builder().documentationOffice(documentationOffice).build();
    DocumentationUnit documentationUnit = DocumentationUnit.builder().build();

    HistoryLog log1 =
        HistoryLog.builder()
            .documentationOffice("BSG")
            .eventType(HistoryLogEventType.UPDATE)
            .description("Updated field")
            .createdBy("mock-user")
            .build();

    when(repository.findByDocumentationUnitId(documentationUnit.uuid(), user))
        .thenReturn(List.of(log1));

    List<HistoryLog> result = service.getHistoryLogs(documentationUnit.uuid(), user);

    assertThat(result).hasSize(1).containsExactly(log1);

    verify(repository).findByDocumentationUnitId(documentationUnit.uuid(), user);
  }

  @Test
  void saveHistoryLog_NonUpdateType() {
    DocumentationOffice documentationOffice =
        DocumentationOffice.builder().uuid(UUID.randomUUID()).build();
    User user = User.builder().documentationOffice(documentationOffice).build();
    DocumentationUnit documentationUnit = DocumentationUnit.builder().build();

    service.saveHistoryLog(
        documentationUnit.uuid(), user, HistoryLogEventType.STATUS, "Status geändert");

    verify(repository, never()).findUpdateLogForDuration(any(), any(), any(), any());
    verify(repository)
        .saveHistoryLog(
            null, documentationUnit.uuid(), user, HistoryLogEventType.STATUS, "Status geändert");
  }

  @Test
  void saveHistoryLog_UpdateTypeWithoutExistingLog() {
    DocumentationOffice documentationOffice =
        DocumentationOffice.builder().uuid(UUID.randomUUID()).build();
    User user = User.builder().documentationOffice(documentationOffice).build();
    DocumentationUnit documentationUnit = DocumentationUnit.builder().build();

    when(repository.findUpdateLogForDuration(any(), any(), any(), any()))
        .thenReturn(Optional.empty());

    service.saveHistoryLog(documentationUnit.uuid(), user, HistoryLogEventType.UPDATE, "Edit");

    verify(repository, times(1)).findUpdateLogForDuration(any(), any(), any(), any());
    verify(repository)
        .saveHistoryLog(null, documentationUnit.uuid(), user, HistoryLogEventType.UPDATE, "Edit");
  }

  @Test
  void saveHistoryLog_UpdateTypeWithExistingLog() {
    DocumentationOffice documentationOffice =
        DocumentationOffice.builder().uuid(UUID.randomUUID()).build();
    User user = User.builder().documentationOffice(documentationOffice).build();
    DocumentationUnit documentationUnit = DocumentationUnit.builder().build();

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
    when(repository.findUpdateLogForDuration(documentationUnit.uuid(), user, startOfDay, endOfDay))
        .thenReturn(Optional.ofNullable(log));

    service.saveHistoryLog(documentationUnit.uuid(), user, HistoryLogEventType.UPDATE, "Edit");

    verify(repository, times(1)).findUpdateLogForDuration(any(), any(), any(), any());
    verify(repository)
        .saveHistoryLog(
            log.id(), documentationUnit.uuid(), user, HistoryLogEventType.UPDATE, "Edit");
  }
}
