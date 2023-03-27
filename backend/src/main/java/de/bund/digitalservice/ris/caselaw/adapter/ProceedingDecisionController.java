package de.bund.digitalservice.ris.caselaw.adapter;

import de.bund.digitalservice.ris.caselaw.domain.ProceedingDecision;
import de.bund.digitalservice.ris.caselaw.domain.ProceedingDecisionService;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("api/v1/caselaw/documentunits/{uuid}/proceedingdecisions")
public class ProceedingDecisionController {
  private final ProceedingDecisionService proceedingDecisionService;

  public ProceedingDecisionController(ProceedingDecisionService proceedingDecisionService) {
    this.proceedingDecisionService = proceedingDecisionService;
  }

  @PutMapping
  public Flux<ProceedingDecision> addProceedingDecision(
      @PathVariable("uuid") UUID documentUnitUuid,
      @RequestBody ProceedingDecision proceedingDecision) {
    return proceedingDecisionService.addProceedingDecision(documentUnitUuid, proceedingDecision);
  }
}
