package de.bund.digitalservice.ris.caselaw.adapter;

import de.bund.digitalservice.ris.caselaw.domain.DocumentUnitService;
import de.bund.digitalservice.ris.caselaw.domain.ProceedingDecision;
import java.util.UUID;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("api/v1/caselaw/documentunits/{uuid}/proceedingdecisions")
public class ProceedingDecisionController {
  private final DocumentUnitService service;

  public ProceedingDecisionController(DocumentUnitService service) {
    this.service = service;
  }

  @PutMapping
  public Flux<ProceedingDecision> addProceedingDecision(
      @PathVariable("uuid") UUID documentUnitUuid,
      @RequestBody ProceedingDecision proceedingDecision) {
    return service.addProceedingDecision(documentUnitUuid, proceedingDecision);
  }
}
