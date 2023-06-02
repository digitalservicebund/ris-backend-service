package de.bund.digitalservice.ris.caselaw.domain;

import reactor.core.publisher.Mono;

public interface DocumentUnitStatusService {

  Mono<DocumentUnit> setInitialStatus(DocumentUnit documentUnit);
}
