package de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.lookuptable;

import java.util.UUID;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;

public interface DatabaseNormAbbreviationRegionRepository
    extends R2dbcRepository<NormAbbreviationRegionDTO, UUID> {
  Flux<NormAbbreviationRegionDTO> findAllByNormAbbreviationId(UUID normAbbreviationId);
}
