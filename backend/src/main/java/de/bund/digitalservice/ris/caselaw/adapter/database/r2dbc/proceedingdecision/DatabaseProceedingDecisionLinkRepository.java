package de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.proceedingdecision;

import java.util.List;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface DatabaseProceedingDecisionLinkRepository
    extends R2dbcRepository<ProceedingDecisionLinkDTO, Long> {
  Flux<ProceedingDecisionLinkDTO> findAllByParentDocumentUnitId(Long Id);

  Mono<ProceedingDecisionLinkDTO> findByParentDocumentUnitIdAndChildDocumentUnitId(
      Long parentId, Long childId);

  Flux<ProceedingDecisionLinkDTO> findAllByChildDocumentUnitId(Long Id);

  Flux<ProceedingDecisionLinkDTO> saveAll(List<ProceedingDecisionLinkDTO> links);

  Mono<Long> countByChildDocumentUnitId(Long Id);
}
