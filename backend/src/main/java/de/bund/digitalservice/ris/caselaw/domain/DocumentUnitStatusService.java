package de.bund.digitalservice.ris.caselaw.domain;

import java.time.Instant;
import java.util.UUID;
import reactor.core.publisher.Mono;

public interface DocumentUnitStatusService {

  Mono<DocumentUnit> setInitialStatus(DocumentUnit documentUnit);

  Mono<DocumentUnit> setToPublishing(
      DocumentUnit documentUnit, Instant publishDate, String issuerAddress);

  Mono<Void> update(String documentNumber, Status status)
      throws DocumentationUnitNotExistsException;

  Mono<Void> update(UUID documentUuid, Status status);

  Mono<String> getLatestIssuerAddress(String documentNumber)
      throws DocumentationUnitNotExistsException;

  Mono<PublicationStatus> getLatestStatus(UUID documentUuid);
}
