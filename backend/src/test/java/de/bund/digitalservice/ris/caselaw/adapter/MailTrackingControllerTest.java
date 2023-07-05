package de.bund.digitalservice.ris.caselaw.adapter;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.bund.digitalservice.ris.caselaw.RisWebTestClient;
import de.bund.digitalservice.ris.caselaw.TestConfig;
import de.bund.digitalservice.ris.caselaw.config.SecurityConfig;
import de.bund.digitalservice.ris.caselaw.domain.MailTrackingService;
import de.bund.digitalservice.ris.caselaw.domain.PublishState;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Mono;

@ExtendWith(SpringExtension.class)
@WebFluxTest(controllers = MailTrackingController.class)
@Import({SecurityConfig.class, TestConfig.class})
class MailTrackingControllerTest {
  @Autowired private RisWebTestClient risWebTestClient;

  @MockBean private MailTrackingService service;
  @MockBean private ReactiveClientRegistrationRepository clientRegistrationRepository;

  private static final UUID TEST_UUID = UUID.fromString("88888888-4444-4444-4444-121212121212");

  @Test
  void testSetPublishState_withValidPayload() {
    PublishState expectedPublishState = PublishState.SUCCESS;
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

    when(service.getMappedPublishState(mailTrackingEvent)).thenReturn(expectedPublishState);
    when(service.setPublishState(TEST_UUID, expectedPublishState)).thenReturn(Mono.just(TEST_UUID));

    risWebTestClient
        .withDefaultLogin()
        .post()
        .uri("/admin/webhook")
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(sendInBlueResponse)
        .exchange()
        .expectStatus()
        .isOk();

    verify(service).getMappedPublishState(mailTrackingEvent);
    verify(service).setPublishState(TEST_UUID, expectedPublishState);
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
        .uri("/admin/webhook")
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(sendInBlueResponse)
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
          "tags": ["no-uuid"],
          "ignoredKey": 123
        }""";

    risWebTestClient
        .withDefaultLogin()
        .post()
        .uri("/admin/webhook")
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(sendInBlueResponse)
        .exchange()
        .expectStatus()
        .isNoContent();
  }
}
