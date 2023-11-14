package de.bund.digitalservice.ris.caselaw.domain;

import java.util.List;
import java.util.UUID;
import reactor.core.publisher.Flux;

public interface PublicationReportRepository {

  Flux<PublicationReport> saveAll(List<PublicationReport> reports);

  Flux<PublicationReport> getAllByDocumentUnitUuid(UUID documentUnitUuid);
}
