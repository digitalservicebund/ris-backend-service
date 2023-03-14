package de.bund.digitalservice.ris.caselaw.domain;

import org.springframework.data.repository.NoRepositoryBean;
import reactor.core.publisher.Flux;

import java.util.UUID;

@NoRepositoryBean
public class ProceedingDecisionLinkRepository {
    Flux<Void> linkProceedingDecisions(UUID parentUuid, UUID childUuid);
}
