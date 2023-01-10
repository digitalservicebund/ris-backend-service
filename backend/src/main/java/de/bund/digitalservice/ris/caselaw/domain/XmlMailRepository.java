package de.bund.digitalservice.ris.caselaw.domain;

import java.util.UUID;
import reactor.core.publisher.Mono;

public interface XmlMailRepository {
  Mono<XmlMail> save(XmlMail xmlMail);

  Mono<MailResponse> getLastPublishedMailResponse(UUID documentUnitUuid);

  Mono<XmlMail> getLastPublishedXmlMail(UUID documentUnitUuid);
}
