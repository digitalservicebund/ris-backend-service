package de.bund.digitalservice.ris.caselaw.domain;

import java.time.Instant;
import reactor.core.publisher.Mono;

public interface DocumentUnitStatusService {

  Mono<DocumentUnit> setInitialStatus(DocumentUnit documentUnit);

  Mono<DocumentUnit> updateStatus(
      DocumentUnit documentUnit,
      DocumentUnitStatus status,
      Instant publishDate,
      String issuerAddress);

  Mono<String> getIssuerAddressOfLatestStatus(String documentNumber);
}
