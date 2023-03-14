package de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.ProceedingDecision;

import java.util.List;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface DatabaseProceedingDecisionLinkRepository extends R2dbcRepository<ProceedingDecisionLinkDTO, Long> {
  Flux<ProceedingDecisionLinkDTO> findAllByParentDocumentUnitId(Long Id);
  Flux<ProceedingDecisionLinkDTO> saveAll(List<ProceedingDecisionLinkDTO> links);
}
