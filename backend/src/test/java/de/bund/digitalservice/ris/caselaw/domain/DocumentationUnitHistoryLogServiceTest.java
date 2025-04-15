package de.bund.digitalservice.ris.caselaw.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
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
  void testGetHistoryLogs() {
    DocumentationOffice documentationOffice =
        DocumentationOffice.builder().uuid(UUID.randomUUID()).build();
    User user = User.builder().documentationOffice(documentationOffice).build();
    DocumentationUnit documentationUnit = DocumentationUnit.builder().build();

    HistoryLog log1 =
        HistoryLog.builder()
            .documentationOffice("BSG")
            .eventType("UPDATE")
            .description("Updated field")
            .createdBy("mock-user")
            .build();

    when(userService.getUser(oidcUser)).thenReturn(user);
    when(repository.findByDocumentationUnitId(documentationUnit.uuid(), user))
        .thenReturn(List.of(log1));

    List<HistoryLog> result = service.getHistoryLogs(documentationUnit.uuid(), oidcUser);

    assertThat(result).hasSize(1).containsExactly(log1);

    verify(userService).getUser(oidcUser);
    verify(repository).findByDocumentationUnitId(documentationUnit.uuid(), user);
  }
}
