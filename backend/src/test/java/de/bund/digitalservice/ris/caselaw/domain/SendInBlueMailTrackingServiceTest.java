package de.bund.digitalservice.ris.caselaw.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
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
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@Import(SendInBlueMailTrackingService.class)
class SendInBlueMailTrackingServiceTest {

  @SpyBean private SendInBlueMailTrackingService service;

  @MockBean private DocumentUnitService documentUnitService;

  private static final UUID TEST_UUID = UUID.fromString("88888888-4444-4444-4444-121212121212");

  @Test
  void testMapMailEventToState_withSuccessfulState() {
    EmailStatus expectedEmailStatus = EmailStatus.SUCCESS;
    EmailStatus mappedEmailStatus = service.mapEventToStatus("delivered");

    assertThat(mappedEmailStatus).isEqualTo(expectedEmailStatus);
  }

  @Test
  void testMapMailEventToState_withUnsuccessfulState() {
    EmailStatus expectedEmailStatus = EmailStatus.ERROR;
    EmailStatus mappedEmailStatus = service.mapEventToStatus("bounces");

    assertThat(mappedEmailStatus).isEqualTo(expectedEmailStatus);
  }

  @Test
  void testMailEventToState_withNeutralState() {
    EmailStatus expectedEmailStatus = EmailStatus.UNKNOWN;
    EmailStatus mappedEmailStatus = service.mapEventToStatus("opened");

    assertThat(mappedEmailStatus).isEqualTo(expectedEmailStatus);
  }

  @Test
  void testProcessDeliveredEvent_shouldNotSearchUUIDInDb() {
    when(documentUnitService.getByUuid(TEST_UUID))
        .thenReturn(DocumentUnit.builder().uuid(TEST_UUID).build());

    service.processMailSendingState("88888888-4444-4444-4444-121212121212", "delivered");
    verifyNoInteractions(documentUnitService);
  }

  @Test
  void testProcessDeliveredEventOfKnownUUID_shouldSearchInDb() {
    when(documentUnitService.getByUuid(TEST_UUID))
        .thenReturn(DocumentUnit.builder().uuid(TEST_UUID).build());

    service.processMailSendingState(TEST_UUID.toString(), "error");
    verify(documentUnitService).getByUuid(TEST_UUID);
  }

  @Test
  void testProcessFailedDeliveryEventOfUnknownId_shouldSearchInDb() {
    when(documentUnitService.getByUuid(TEST_UUID))
        .thenReturn(DocumentUnit.builder().uuid(TEST_UUID).build());

    service.processMailSendingState("88888888-4444-4444-4444-121212121212", "error");
    verify(documentUnitService).getByUuid(UUID.fromString("88888888-4444-4444-4444-121212121212"));
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        // no-op event
        "clicks",
        // unknown event
        "randomEvent"
      })
  void testUpdateState_noReactionOnOtherState(String event) {
    ResponseEntity<String> responseEntity =
        service.processMailSendingState(TEST_UUID.toString(), event);
    assertThat(responseEntity.getStatusCode().isSameCodeAs(HttpStatusCode.valueOf(204))).isTrue();
    verifyNoInteractions(documentUnitService);
  }
}
