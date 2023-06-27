package de.bund.digitalservice.ris.caselaw.domain;

import java.util.UUID;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface XmlMailRepository {
  Mono<XmlMail> save(XmlMail xmlMail);

  Flux<MailResponse> getPublishedMailResponses(UUID documentUnitUuid);

  Mono<XmlMail> getLastPublishedXmlMail(UUID documentUnitUuid);
}
