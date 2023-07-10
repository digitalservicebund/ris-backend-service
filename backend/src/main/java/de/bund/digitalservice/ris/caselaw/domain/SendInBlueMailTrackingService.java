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

  private final XmlPublicationRepository mailRepository;

  public SendInBlueMailTrackingService(XmlPublicationRepository xmlPublicationRepository) {
    this.mailRepository = xmlPublicationRepository;
  }

  @Override
  public EmailPublishState getMappedPublishState(String mailTrackingEvent) {
    if (mailTrackingEvent.equals(EventEnum.DELIVERED.getValue())) {
      return EmailPublishState.SUCCESS;
    } else {
      return EmailPublishState.ERROR;
    }
  }

  @Override
  public Mono<UUID> setPublishState(UUID documentUnitUuid, EmailPublishState emailPublishState) {
    return mailRepository
        .getLastXmlPublication(documentUnitUuid)
        .map(
            xmlPublication -> {
              Instant publishDate = Instant.now();
              if (emailPublishState == EmailPublishState.SUCCESS) {
                log.info(
                    "Mail delivery ({}) was successful ({})", documentUnitUuid, emailPublishState);
              } else {
                log.warn(
                    "Mail delivery ({}) was not successful ({})",
                    documentUnitUuid,
                    emailPublishState);
              }
              return XmlPublication.builder()
                  .publishState(emailPublishState)
                  .publishDate(publishDate)
                  .build();
            })
        .flatMap(mailRepository::save)
        .doOnSuccess(
            xmlPublication -> {
              if (xmlPublication == null) {
                log.warn(
                    "Mail publish state ({}) was not set: invalid DocumentUnitUuid",
                    documentUnitUuid);
              } else {
                log.info("Mail publish state ({}) was set", documentUnitUuid);
              }
            })
        .map(xmlPublication -> documentUnitUuid)
        .doOnError(ex -> log.error("Could not set publish state"));
  }
}
