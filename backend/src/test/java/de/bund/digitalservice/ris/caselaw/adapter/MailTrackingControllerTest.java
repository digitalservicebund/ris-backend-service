package de.bund.digitalservice.ris.caselaw.adapter;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.csrf;

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
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

@ExtendWith(SpringExtension.class)
@WebFluxTest(controllers = MailTrackingController.class)
@WithMockUser
class MailTrackingControllerTest {
  @Autowired private WebTestClient webClient;

  @MockBean private MailTrackingService service;

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

    webClient
        .mutateWith(csrf())
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

    webClient
        .mutateWith(csrf())
        .post()
        .uri("/admin/webhook")
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(sendInBlueResponse)
        .exchange()
        .expectStatus()
        .isBadRequest();
  }
}
