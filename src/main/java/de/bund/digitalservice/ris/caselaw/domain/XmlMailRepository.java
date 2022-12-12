package de.bund.digitalservice.ris.caselaw.domain;

import java.util.UUID;
import reactor.core.publisher.Mono;

public interface XmlMailRepository {
  Mono<XmlMail> save(XmlMail xmlMail);

  Mono<MailResponse> getLastPublishedXml(UUID documentUnitUuid);
}
