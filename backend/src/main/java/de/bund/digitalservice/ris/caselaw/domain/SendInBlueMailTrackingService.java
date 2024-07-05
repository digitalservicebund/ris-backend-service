package de.bund.digitalservice.ris.caselaw.domain;

import java.util.List;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import sibModel.GetEmailEventReportEvents.EventEnum;

@Service
@Slf4j
public class SendInBlueMailTrackingService implements MailTrackingService {

  private final DocumentUnitService documentUnitService;

  public SendInBlueMailTrackingService(DocumentUnitService documentUnitService) {
    this.documentUnitService = documentUnitService;
  }

  @Override
  public EmailPublishState mapEventToPublishState(String mailTrackingEvent) {
    var event = EventEnum.fromValue(mailTrackingEvent);
    if (event == null) {
      return EmailPublishState.UNKNOWN;
    }
    if (List.of(
            EventEnum.BOUNCES,
            EventEnum.HARDBOUNCES,
            EventEnum.SOFTBOUNCES,
            EventEnum.INVALID,
            EventEnum.SPAM,
            EventEnum.BLOCKED,
            EventEnum.ERROR)
        .contains(event)) {
      return EmailPublishState.ERROR;
    }
    if (event == EventEnum.DELIVERED) {
      return EmailPublishState.SUCCESS;
    }
    return EmailPublishState.UNKNOWN;
  }

  @Override
  // TODO alert on the errors
  public ResponseEntity<String> processMailSendingState(String payloadTag, String event) {

    EmailPublishState state = mapEventToPublishState(event);
    if (state == EmailPublishState.UNKNOWN) {
      return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    if (state == EmailPublishState.ERROR) {
      DocumentUnit documentUnit = documentUnitService.getByUuid(parseDocUnitUUID(payloadTag));
      log.error(
          documentUnit == null
              ? "Received Mail sending error for forwarded email. Event: {}, Tag {}"
              : "Failed to send Mail for documentation unit {} because of event {}",
          payloadTag,
          event);
    }

    return ResponseEntity.status(HttpStatus.OK).build();
  }

  private UUID parseDocUnitUUID(String string) {
    try {
      return UUID.fromString(string);
    } catch (IllegalArgumentException e) {
      return null;
    }
  }
}
