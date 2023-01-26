package de.bund.digitalservice.ris.caselaw.domain;

import java.time.Instant;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import sibModel.GetEmailEventReportEvents.EventEnum;

@Service
@Slf4j
public class SendInBlueMailTrackingService implements MailTrackingService {

  private final XmlMailRepository mailRepository;

  public SendInBlueMailTrackingService(XmlMailRepository xmlMailRepository) {
    this.mailRepository = xmlMailRepository;
  }

  @Override
  public PublishState getMappedPublishState(String mailTrackingEvent) {
    if (mailTrackingEvent.equals(EventEnum.DELIVERED.getValue())) {
      return PublishState.SUCCESS;
    } else {
      return PublishState.ERROR;
    }
  }

  @Override
  public Mono<UUID> setPublishState(UUID documentUnitUuid, PublishState publishState) {
    return mailRepository
        .getLastPublishedXmlMail(documentUnitUuid)
        .map(
            xmlMail -> {
              Instant publishDate = Instant.now();
              if (publishState == PublishState.SUCCESS) {
                log.info("Mail delivery ({}) was successful ({})", documentUnitUuid, publishState);
              } else {
                log.warn(
                    "Mail delivery ({}) was not successful ({})", documentUnitUuid, publishState);
              }
              return xmlMail.toBuilder()
                  .publishState(publishState)
                  .publishDate(publishDate)
                  .build();
            })
        .flatMap(mailRepository::save)
        .doOnSuccess(
            xmlMail -> {
              if (xmlMail == null) {
                log.warn(
                    "Mail publish state ({}) was not set: invalid DocumentUnitUuid",
                    documentUnitUuid);
              } else {
                log.info("Mail publish state ({}) was set", documentUnitUuid);
              }
            })
        .map(xmlMail -> documentUnitUuid)
        .doOnError(ex -> log.error("Could not set publish state"));
  }
}
