package de.bund.digitalservice.ris.caselaw.domain;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import sibModel.GetEmailEventReportEvents.EventEnum;

@Service
@Slf4j
public class SendInBlueMailTrackingService implements MailTrackingService {

  @Override
  public EmailPublishState getMappedPublishState(String mailTrackingEvent) {
    if (mailTrackingEvent.equals(EventEnum.DELIVERED.getValue())) {
      return EmailPublishState.SUCCESS;
    } else {
      return EmailPublishState.ERROR;
    }
  }
}
