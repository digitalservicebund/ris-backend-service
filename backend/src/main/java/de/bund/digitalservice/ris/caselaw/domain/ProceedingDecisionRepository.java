package de.bund.digitalservice.ris.caselaw.domain;

import java.util.UUID;

import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.proceedingdecision.ProceedingDecisionLinkDTO;
import org.springframework.data.repository.NoRepositoryBean;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@NoRepositoryBean
public interface ProceedingDecisionRepository {
  Flux<ProceedingDecision> findAllForDocumentUnit(UUID parentDocumentUnitUuid);
  Mono<ProceedingDecisionLinkDTO> linkProceedingDecisions(UUID parentDocumentUnitUuid, UUID childDocumentUnitUuid);
}
