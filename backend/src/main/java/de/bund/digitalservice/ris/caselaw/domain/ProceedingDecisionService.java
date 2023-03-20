package de.bund.digitalservice.ris.caselaw.domain;

import de.bund.digitalservice.ris.caselaw.domain.ProceedingDecisionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import java.util.UUID;

@Service
@Slf4j
public class ProceedingDecisionService {
  private final ProceedingDecisionRepository repository;

  public ProceedingDecisionService(
          ProceedingDecisionRepository repository) {
    this.repository = repository;
  }

  public Flux<ProceedingDecision> getProceedingDecisionForDocumentUnit(UUID documentUnitUuid) {
    return repository.findAllForDocumentUnit(documentUnitUuid);
  }

  public Flux<ProceedingDecision> addProceedingDecision(UUID documentUnitUuid, ProceedingDecision proceedingDecision) {
    return repository.addProceedingDecision(documentUnitUuid, proceedingDecision);
  }

}
