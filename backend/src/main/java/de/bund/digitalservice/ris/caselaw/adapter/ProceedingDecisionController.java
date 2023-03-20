package de.bund.digitalservice.ris.caselaw.adapter;

import de.bund.digitalservice.ris.caselaw.domain.ProceedingDecision;
import de.bund.digitalservice.ris.caselaw.domain.ProceedingDecisionService;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.UUID;

@RestController
@RequestMapping("api/v1/caselaw/documentunits/{uuid}/proceedingdecisions")
public class ProceedingDecisionController {
  private final ProceedingDecisionService proceedingDecisionService;

  public ProceedingDecisionController(ProceedingDecisionService proceedingDecisionService) {
    this.proceedingDecisionService = proceedingDecisionService;
  }

  @GetMapping
  public Flux<ProceedingDecision> getProceedingDecisions(@PathVariable UUID documentUnitUuid) {
    return proceedingDecisionService.getProceedingDecisionForDocumentUnit(documentUnitUuid);
  }

  @PutMapping
  public Flux<ProceedingDecision> addProceedingDecision(@PathVariable UUID documentUnitUuid, @RequestBody ProceedingDecision proceedingDecision) {

    return proceedingDecisionService.addProceedingDecision(documentUnitUuid, proceedingDecision);
  }
}
