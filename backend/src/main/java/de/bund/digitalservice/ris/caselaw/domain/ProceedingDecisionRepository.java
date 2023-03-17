package de.bund.digitalservice.ris.caselaw.domain;

import java.util.UUID;
import org.springframework.data.repository.NoRepositoryBean;
import reactor.core.publisher.Mono;

@NoRepositoryBean
public interface ProceedingDecisionRepository {
  Mono<Void> addProceedingDecision(UUID parentDocumentUnitUuid, UUID childDocumentUnitUuid);
}
