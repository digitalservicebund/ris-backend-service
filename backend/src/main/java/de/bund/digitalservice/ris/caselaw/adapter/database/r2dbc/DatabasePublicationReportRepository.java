package de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc;

import java.util.UUID;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;

@Deprecated
public interface DatabasePublicationReportRepository
    extends R2dbcRepository<PublicationReportDTO, UUID> {
  Flux<PublicationReportDTO> findAllByDocumentUnitId(UUID documentUnitId);
}
