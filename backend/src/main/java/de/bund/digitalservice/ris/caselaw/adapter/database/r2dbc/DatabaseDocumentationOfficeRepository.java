package de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc;

import java.util.UUID;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Mono;

public interface DatabaseDocumentationOfficeRepository
    extends R2dbcRepository<DocumentationOfficeDTO, UUID> {
  Mono<DocumentationOfficeDTO> findByLabel(String label);
}
