package de.bund.digitalservice.ris.caselaw.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatusCode;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(SpringExtension.class)
@Import(SendInBlueMailTrackingService.class)
class SendInBlueMailTrackingServiceTest {

  @SpyBean private SendInBlueMailTrackingService service;

  @MockBean private DocumentUnitStatusService documentUnitStatusService;

  private static final UUID TEST_UUID = UUID.fromString("88888888-4444-4444-4444-121212121212");

  @Test
  void testGetMappedPublishState_withSuccessfulState() {
    EmailPublishState expectedEmailPublishState = EmailPublishState.SUCCESS;
    EmailPublishState mappedEmailPublishState = service.getMappedPublishState("delivered");

    assertThat(mappedEmailPublishState).isEqualTo(expectedEmailPublishState);
  }

  @Test
  void testGetMappedPublishState_withUnsuccessfulState() {
    EmailPublishState expectedEmailPublishState = EmailPublishState.ERROR;
    EmailPublishState mappedEmailPublishState = service.getMappedPublishState("bounces");

    assertThat(mappedEmailPublishState).isEqualTo(expectedEmailPublishState);
  }

  @Test
  void testGetMappedPublishState_withNeutralState() {
    EmailPublishState expectedEmailPublishState = EmailPublishState.UNKNOWN;
    EmailPublishState mappedEmailPublishState = service.getMappedPublishState("opened");

    assertThat(mappedEmailPublishState).isEqualTo(expectedEmailPublishState);
  }

  @Test
  void testUpdatePublishingState_success() {
    when(documentUnitStatusService.getLatestStatus(TEST_UUID))
        .thenReturn(Mono.just(PublicationStatus.PUBLISHING));
    when(documentUnitStatusService.update(
            TEST_UUID,
            DocumentUnitStatus.builder()
                .publicationStatus(PublicationStatus.PUBLISHING)
                .withError(false)
                .build()))
        .thenReturn(Mono.empty());

    StepVerifier.create(service.updatePublishingState(TEST_UUID, "delivered"))
        .consumeNextWith(
            responseEntity -> {
              assertThat(responseEntity.getStatusCode().is2xxSuccessful()).isTrue();
            })
        .verifyComplete();

    verify(documentUnitStatusService).getLatestStatus(TEST_UUID);
    verify(documentUnitStatusService)
        .update(
            TEST_UUID,
            DocumentUnitStatus.builder()
                .publicationStatus(PublicationStatus.PUBLISHING)
                .withError(false)
                .build());
  }

  @Test
  void testUpdatePublishingState_error() {
    when(documentUnitStatusService.getLatestStatus(TEST_UUID))
        .thenReturn(Mono.just(PublicationStatus.PUBLISHING));
    when(documentUnitStatusService.update(
            TEST_UUID,
            DocumentUnitStatus.builder()
                .publicationStatus(PublicationStatus.PUBLISHING)
                .withError(true)
                .build()))
        .thenReturn(Mono.empty());

    StepVerifier.create(service.updatePublishingState(TEST_UUID, "error"))
        .consumeNextWith(
            responseEntity -> {
              assertThat(responseEntity.getStatusCode().is2xxSuccessful()).isTrue();
            })
        .verifyComplete();

    verify(documentUnitStatusService).getLatestStatus(TEST_UUID);
    verify(documentUnitStatusService)
        .update(
            TEST_UUID,
            DocumentUnitStatus.builder()
                .publicationStatus(PublicationStatus.PUBLISHING)
                .withError(true)
                .build());
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        // no-op event
        "clicks",
        // unknown event
        "randomEvent"
      })
  void testUpdatePublishingState_noReactionOnOtherState(String event) {
    StepVerifier.create(service.updatePublishingState(TEST_UUID, event))
        .consumeNextWith(
            responseEntity -> {
              assertThat(responseEntity.getStatusCode().isSameCodeAs(HttpStatusCode.valueOf(204)))
                  .isTrue();
            })
        .verifyComplete();

    verifyNoInteractions(documentUnitStatusService);
  }

  @Test
  void testUpdatePublishingState_noReactionOnPublishedDocs() {
    when(documentUnitStatusService.getLatestStatus(TEST_UUID))
        .thenReturn(Mono.just(PublicationStatus.PUBLISHED));

    StepVerifier.create(service.updatePublishingState(TEST_UUID, "error"))
        .consumeNextWith(
            responseEntity -> {
              assertThat(responseEntity.getStatusCode().is2xxSuccessful()).isTrue();
            })
        .verifyComplete();

    verify(documentUnitStatusService).getLatestStatus(TEST_UUID);
    verifyNoMoreInteractions(documentUnitStatusService);
  }
}
