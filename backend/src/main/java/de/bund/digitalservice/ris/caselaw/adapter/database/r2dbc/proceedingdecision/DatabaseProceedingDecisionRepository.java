package de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.proceedingdecision;

import java.util.UUID;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface DatabaseProceedingDecisionRepository
    extends R2dbcRepository<ProceedingDecisionDTO, Long> {

  Flux<ProceedingDecisionDTO> findAllById(Long documentUnitId);

  Mono<ProceedingDecisionDTO> findByUuid(UUID documentUnitUuid);
}
