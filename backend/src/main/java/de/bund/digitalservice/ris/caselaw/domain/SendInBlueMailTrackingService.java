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

  private final DocumentUnitStatusService statusService;

  public SendInBlueMailTrackingService(DocumentUnitStatusService statusService) {
    this.statusService = statusService;
  }

  @Override
  public EmailPublishState getMappedPublishState(String mailTrackingEvent) {
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
  public ResponseEntity<String> updatePublishingState(String payloadTag, String event) {

    final UUID documentUnitUuid = parseDocUnitUUID(payloadTag);

    if (documentUnitUuid == null) {
      // No UUID in tag == it's about a forwarded report mail and not the mail to juris
      if (getMappedPublishState(event) == EmailPublishState.ERROR) {
        log.error("Received Mail sending error {} with tag {}", event, payloadTag);
      }
      return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    EmailPublishState state = getMappedPublishState(event);

    if (state == EmailPublishState.UNKNOWN) {
      return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    if (state == EmailPublishState.ERROR) {
      log.error("Failed to send Mail for {} because of event {}", documentUnitUuid, event);
    }

    PublicationStatus latestStatus = statusService.getLatestStatus(documentUnitUuid);
    if (latestStatus != PublicationStatus.PUBLISHED) {
      try {
        statusService.update(
            documentUnitUuid,
            Status.builder()
                .publicationStatus(latestStatus)
                .withError(state == EmailPublishState.ERROR)
                .build());
      } catch (DocumentationUnitNotExistsException e) {
        // TODO: Handle this exception
        throw new RuntimeException(e);
      }
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
