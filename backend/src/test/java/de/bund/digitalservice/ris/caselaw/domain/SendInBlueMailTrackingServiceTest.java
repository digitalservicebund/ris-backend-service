package de.bund.digitalservice.ris.caselaw.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@Import(SendInBlueMailTrackingService.class)
class SendInBlueMailTrackingServiceTest {

  @SpyBean private SendInBlueMailTrackingService service;

  @Test
  void testGetMappedPublishState_withSuccessfulState() {
    EmailPublishState expectedEmailPublishState = EmailPublishState.SUCCESS;
    EmailPublishState mappedEmailPublishState = service.getMappedPublishState("delivered");

    assertThat(mappedEmailPublishState).isEqualTo(expectedEmailPublishState);
  }

  @Test
  void testGetMappedPublishState_withUnsuccessfulState() {
    EmailPublishState expectedEmailPublishState = EmailPublishState.ERROR;
    EmailPublishState mappedEmailPublishState = service.getMappedPublishState("bounced");

    assertThat(mappedEmailPublishState).isEqualTo(expectedEmailPublishState);
  }
}
