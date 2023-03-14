package de.bund.digitalservice.ris.caselaw.domain;

import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.ProceedingDecision.ProceedingDecisionDTO;
import org.springframework.data.repository.NoRepositoryBean;
import reactor.core.publisher.Flux;

import java.util.List;

@NoRepositoryBean
public interface ProceedingDecisionRepository {
  Flux<ProceedingDecision> findAllByDocumentUnitId(Long id);

  Flux<ProceedingDecision> saveAll(List<ProceedingDecisionDTO> proceedingDecisionDTOs, Long parentDocumentUnitId);
}
