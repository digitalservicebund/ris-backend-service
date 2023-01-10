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
  public Mono<String> setPublishState(UUID documentUnitUuid, PublishState publishState) {
    return mailRepository
        .getLastPublishedXmlMail(documentUnitUuid)
        .map(
            xmlMail -> {
              Instant publishDate = Instant.now();

              if (publishState == PublishState.SUCCESS) {
                log.info("Mail delivery was successful: {}", documentUnitUuid);
              } else {
                log.warn(
                    "Mail delivery was not successful: {} ({})", documentUnitUuid, publishState);
              }
              return xmlMail.toBuilder()
                  .publishState(publishState)
                  .publishDate(publishDate)
                  .build();
            })
        .flatMap(mailRepository::save)
        .doOnNext(xmlMail -> log.info("Publish state was set for {}", documentUnitUuid))
        .map(xmlMail -> "Publish state was set successfully")
        .doOnError(ex -> log.error("Couldn't set publish state."));
  }
}
