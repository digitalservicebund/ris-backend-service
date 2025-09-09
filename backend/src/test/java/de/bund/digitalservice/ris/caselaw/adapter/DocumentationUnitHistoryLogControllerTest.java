package de.bund.digitalservice.ris.caselaw.adapter;

import static de.bund.digitalservice.ris.caselaw.AuthUtils.buildDSDocOffice;
import static de.bund.digitalservice.ris.caselaw.AuthUtils.buildDSuser;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.type.TypeReference;
import de.bund.digitalservice.ris.caselaw.TestConfig;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseApiKeyRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDocumentationOfficeRepository;
import de.bund.digitalservice.ris.caselaw.config.SecurityConfig;
import de.bund.digitalservice.ris.caselaw.domain.CoreData;
import de.bund.digitalservice.ris.caselaw.domain.Decision;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationOffice;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnitHistoryLogService;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnitService;
import de.bund.digitalservice.ris.caselaw.domain.HandoverService;
import de.bund.digitalservice.ris.caselaw.domain.HistoryLog;
import de.bund.digitalservice.ris.caselaw.domain.HistoryLogEventType;
import de.bund.digitalservice.ris.caselaw.domain.ProcedureService;
import de.bund.digitalservice.ris.caselaw.domain.User;
import de.bund.digitalservice.ris.caselaw.domain.UserGroupService;
import de.bund.digitalservice.ris.caselaw.domain.UserService;
import de.bund.digitalservice.ris.caselaw.domain.exception.DocumentationUnitNotExistsException;
import de.bund.digitalservice.ris.caselaw.webtestclient.RisWebTestClient;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@WebMvcTest(controllers = DocumentationUnitHistoryLogController.class)
@Import({SecurityConfig.class, TestConfig.class, OAuthService.class, KeycloakUserService.class})
class DocumentationUnitHistoryLogControllerTest {
  @Autowired private RisWebTestClient risWebTestClient;
  @Autowired private OAuthService oAuthService;
  @MockitoBean private DocumentationUnitHistoryLogService service;
  @MockitoBean private DocumentationUnitService docUnitService;
  @MockitoBean private HandoverService handoverService;
  @MockitoBean private ClientRegistrationRepository clientRegistrationRepository;
  @MockitoBean private OAuth2AuthorizedClientManager oAuth2AuthorizedClientManager;
  @MockitoBean private DatabaseApiKeyRepository databaseApiKeyRepository;
  @MockitoBean private DatabaseDocumentationOfficeRepository databaseDocumentationOfficeRepository;
  @MockitoBean private UserGroupService userGroupService;
  @MockitoBean private ProcedureService procedureService;
  @MockitoBean private UserService userService;

  private static final String HISTORY_LOG_ENDPOINT = "/api/v1/caselaw/documentunits/";
  private static final UUID TEST_UUID = UUID.randomUUID();
  private final DocumentationOffice docOffice = buildDSDocOffice();
  private final User user = buildDSuser();

  @BeforeEach
  void setup() throws DocumentationUnitNotExistsException {
    doReturn(docOffice)
        .when(userService)
        .getDocumentationOffice(
            argThat(
                (OidcUser oidcUser) -> {
                  List<String> groups = oidcUser.getAttribute("groups");
                  return Objects.requireNonNull(groups).getFirst().equals("/DS");
                }));

    doReturn(true).when(userService).isInternal(any());
    when(docUnitService.getByUuid(TEST_UUID))
        .thenReturn(
            Decision.builder()
                .coreData(CoreData.builder().documentationOffice(docOffice).build())
                .build());
  }

  @Test
  void getHistoryLog_shouldReturnList_whenUserIsInternal() {
    // given
    HistoryLog log1 =
        HistoryLog.builder()
            .id(UUID.randomUUID())
            .eventType(HistoryLogEventType.UPDATE)
            .description("something was updated")
            .createdAt(Instant.now())
            .createdBy("test user")
            .build();
    HistoryLog log2 =
        HistoryLog.builder()
            .id(UUID.randomUUID())
            .eventType(HistoryLogEventType.UPDATE)
            .description("something was again updated")
            .createdAt(Instant.now())
            .createdBy("system")
            .build();

    when(service.getHistoryLogs(eq(TEST_UUID), any(User.class))).thenReturn(List.of(log1, log2));

    risWebTestClient
        .withDefaultLogin()
        .get()
        .uri(HISTORY_LOG_ENDPOINT + TEST_UUID + "/historylogs")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(new TypeReference<List<HistoryLog>>() {});

    verify(service, times(1)).getHistoryLogs(any(), any());
  }

  @Test
  void getHistoryLog_shouldReturn403_whenUserIsInternalAndNoWriteAccess() {
    Mockito.reset(userService);
    DocumentationOffice office = DocumentationOffice.builder().abbreviation("BGH").build();
    when(userService.getDocumentationOffice(any())).thenReturn(Optional.of(office));

    risWebTestClient
        .withDefaultLogin()
        .get()
        .uri(HISTORY_LOG_ENDPOINT + TEST_UUID + "/historylogs")
        .exchange()
        .expectStatus()
        .isForbidden();

    verify(service, times(0)).getHistoryLogs(any(), any());
  }

  @Test
  void getHistoryLog_shouldReturn403_whenExternalUser() {

    risWebTestClient
        .withExternalLogin()
        .get()
        .uri(HISTORY_LOG_ENDPOINT + TEST_UUID + "/historylogs")
        .exchange()
        .expectStatus()
        .isForbidden();

    verify(service, times(0)).getHistoryLogs(any(), any());
  }
}
