package de.bund.digitalservice.ris.caselaw.domain;

import java.util.Arrays;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
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
    if (Arrays.asList(
            EventEnum.BOUNCES,
            EventEnum.HARDBOUNCES,
            EventEnum.SOFTBOUNCES,
            EventEnum.INVALID,
            EventEnum.SPAM,
            EventEnum.BLOCKED,
            EventEnum.ERROR)
        .contains(EventEnum.fromValue(mailTrackingEvent))) {
      return EmailPublishState.ERROR;
    }
    if (EventEnum.fromValue(mailTrackingEvent) == EventEnum.DELIVERED) {
      return EmailPublishState.SUCCESS;
    }
    return EmailPublishState.UNKNOWN;
  }

  @Override
  public Mono<ResponseEntity<String>> updatePublishingState(UUID documentUnitUuid, String event) {
    EmailPublishState state = getMappedPublishState(event);

    if (state == EmailPublishState.UNKNOWN) {
      log.info("Got Mail event {} for {} that doesn't change the status", event, documentUnitUuid);
      return Mono.just(ResponseEntity.status(HttpStatus.NO_CONTENT).build());
    }

    if (state == EmailPublishState.ERROR) {
      log.warn("Failed to send Mail for {} because of event {}", documentUnitUuid, event);
    }

    return statusService
        .getLatestStatus(documentUnitUuid)
        .filter(documentUnitStatus -> documentUnitStatus == PublicationStatus.PUBLISHING)
        .flatMap(
            documentUnitStatusDTO ->
                statusService.update(
                    documentUnitUuid,
                    DocumentUnitStatus.builder()
                        .status(documentUnitStatusDTO)
                        .withError(state == EmailPublishState.ERROR)
                        .build()))
        .thenReturn(ResponseEntity.status(HttpStatus.OK).build());
  }
}
