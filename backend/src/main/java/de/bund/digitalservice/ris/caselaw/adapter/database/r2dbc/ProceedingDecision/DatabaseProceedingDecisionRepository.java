package de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.ProceedingDecision;

import java.util.List;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface DatabaseProceedingDecisionRepository
    extends R2dbcRepository<ProceedingDecisionDTO, Long> {

}
