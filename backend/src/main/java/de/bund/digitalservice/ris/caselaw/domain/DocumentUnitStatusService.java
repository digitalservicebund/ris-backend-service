package de.bund.digitalservice.ris.caselaw.domain;

import java.time.Instant;
import reactor.core.publisher.Mono;

public interface DocumentUnitStatusService {

  Mono<DocumentUnit> setInitialStatus(DocumentUnit documentUnit);

  Mono<DocumentUnit> setToPublishing(
      DocumentUnit documentUnit, Instant publishDate, String issuerAddress);

  Mono<Void> update(String documentNumber, DocumentUnitStatus status);

  Mono<String> getLatestIssuerAddress(String documentNumber);
}
