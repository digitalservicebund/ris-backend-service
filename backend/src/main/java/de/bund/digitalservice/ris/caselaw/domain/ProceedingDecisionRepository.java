package de.bund.digitalservice.ris.caselaw.domain;

import java.util.UUID;
import org.springframework.data.repository.NoRepositoryBean;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@NoRepositoryBean
public interface ProceedingDecisionRepository {
  Flux<ProceedingDecision> findAllForDocumentUnit(UUID parentDocumentUnitUuid);
  Flux<ProceedingDecision> addProceedingDecision(UUID parentDocumentUnitUuid, ProceedingDecision proceedingDecision);
}
