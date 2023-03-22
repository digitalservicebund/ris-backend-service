package de.bund.digitalservice.ris.caselaw.adapter;

import de.bund.digitalservice.ris.caselaw.domain.ProceedingDecision;
import de.bund.digitalservice.ris.caselaw.domain.ProceedingDecisionService;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@RequestMapping("api/v1/caselaw/documentunits/{uuid}/proceedingdecisions")
public class ProceedingDecisionController {
  private final ProceedingDecisionService proceedingDecisionService;

  public ProceedingDecisionController(ProceedingDecisionService proceedingDecisionService) {
    this.proceedingDecisionService = proceedingDecisionService;
  }

  @GetMapping
  public Flux<ProceedingDecision> getProceedingDecisions(@PathVariable("uuid") UUID documentUnitUuid) {
    return proceedingDecisionService.getProceedingDecisionsForDocumentUnit(documentUnitUuid);
  }

  @PutMapping
  public Flux<ProceedingDecision> addProceedingDecision(@PathVariable("uuid") UUID documentUnitUuid,
      @RequestBody ProceedingDecision proceedingDecision) {
    return proceedingDecisionService.addProceedingDecision(documentUnitUuid, proceedingDecision);
  }
}
