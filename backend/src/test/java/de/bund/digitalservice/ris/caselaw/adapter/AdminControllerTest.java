package de.bund.digitalservice.ris.caselaw.adapter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.bund.digitalservice.ris.caselaw.TestConfig;
import de.bund.digitalservice.ris.caselaw.config.SecurityConfig;
import de.bund.digitalservice.ris.caselaw.domain.EnvironmentResponse;
import de.bund.digitalservice.ris.caselaw.domain.MailStatus;
import de.bund.digitalservice.ris.caselaw.domain.MailTrackingService;
import de.bund.digitalservice.ris.caselaw.domain.UserService;
import de.bund.digitalservice.ris.caselaw.webtestclient.RisWebTestClient;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@WebMvcTest(controllers = AdminController.class)
@Import({SecurityConfig.class, TestConfig.class, DocumentNumberPatternConfig.class})
class AdminControllerTest {
  @Autowired private RisWebTestClient risWebTestClient;
  @MockitoBean private MailTrackingService mailTrackingService;
  @MockitoBean private EnvironmentService environmentService;
  @MockitoBean private ClientRegistrationRepository clientRegistrationRepository;
  @MockitoBean private OAuth2AuthorizedClientManager oAuth2AuthorizedClientManager;
  @MockitoBean private UserService userService;

  private static final UUID TEST_UUID = UUID.fromString("88888888-4444-4444-4444-121212121212");

  @Test
  void testSetPublishState_withValidPayload() {
    MailStatus expectedMailStatus = MailStatus.SUCCESS;
    String mailTrackingEvent = "delivered";
    String sendInBlueResponse =
        String.format(
            """
                                {
                                  "event": "%s",
                                  "tags": ["%s"],
                                  "ignoredKey": 123
                                }""",
            mailTrackingEvent, TEST_UUID);

    when(mailTrackingService.mapEventToStatus(mailTrackingEvent)).thenReturn(expectedMailStatus);
    when(mailTrackingService.processMailSendingState(TEST_UUID.toString(), mailTrackingEvent))
        .thenReturn(ResponseEntity.ok().build());

    risWebTestClient
        .withDefaultLogin()
        .post()
        .uri("/api/v1/admin/webhook")
        .contentType(MediaType.APPLICATION_JSON)
        .bodyJsonString(sendInBlueResponse)
        .exchange()
        .expectStatus()
        .isOk();

    verify(mailTrackingService).processMailSendingState(TEST_UUID.toString(), mailTrackingEvent);
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        // no event
        """
                            {"tags": ["%s"]}""",
        // no tags
        """
                            {"event": "%s"}""",
        // empty tags
        """
                            {"event": "%s", "tags": []}""",
      })
  void testSetPublishState_withInvalidPayload(String jsonString) {
    String sendInBlueResponse = String.format(jsonString, TEST_UUID);

    risWebTestClient
        .withDefaultLogin()
        .post()
        .uri("/api/v1/admin/webhook")
        .contentType(MediaType.APPLICATION_JSON)
        .bodyJsonString(sendInBlueResponse)
        .exchange()
        .expectStatus()
        .isBadRequest();
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        // no event
        """
                            {"tags": ["%s"]}""",
        // no tags
        """
                            {"event": "%s"}""",
        // empty tags
        """
                            {"event": "%s", "tags": []}""",
      })
  void testSetPublishState_withDifferentTags(String jsonString) {

    String sendInBlueResponse =
        """
                        {
                          "event": "delivered",
                          "tags": ["no-id"],
                          "ignoredKey": 123
                        }""";

    when(mailTrackingService.processMailSendingState("no-id", "delivered"))
        .thenReturn(ResponseEntity.noContent().build());
    when(mailTrackingService.mapEventToStatus("delivered")).thenReturn(MailStatus.SUCCESS);

    risWebTestClient
        .withDefaultLogin()
        .post()
        .uri("/api/v1/admin/webhook")
        .contentType(MediaType.APPLICATION_JSON)
        .bodyJsonString(sendInBlueResponse)
        .exchange()
        .expectStatus()
        .isNoContent();

    verify(mailTrackingService).processMailSendingState("no-id", "delivered");
  }

  @Test
  void testGetEnv() {
    when(environmentService.getEnvironment())
        .thenReturn(
            EnvironmentResponse.builder().environment("staging").portalUrl("portal-url").build());

    var result =
        risWebTestClient
            .withDefaultLogin()
            .get()
            .uri("/api/v1/admin/env")
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(String.class)
            .returnResult();

    assertThat(result.getResponseBody())
        .isEqualTo("{\"environment\":\"staging\",\"portalUrl\":\"portal-url\"}");
  }

  @Test
  void testGetAccountManagementUrl() {
    when(environmentService.getAccountManagementUrl()).thenReturn("some-url");

    var result =
        risWebTestClient
            .withDefaultLogin()
            .get()
            .uri("/api/v1/admin/accountManagementUrl")
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(String.class)
            .returnResult();

    assertThat(result.getResponseBody()).isEqualTo("some-url");
  }
}
