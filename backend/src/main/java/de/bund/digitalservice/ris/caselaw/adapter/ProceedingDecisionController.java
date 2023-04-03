package de.bund.digitalservice.ris.caselaw.adapter;

import de.bund.digitalservice.ris.caselaw.domain.DocumentUnit;
import de.bund.digitalservice.ris.caselaw.domain.DocumentUnitService;
import de.bund.digitalservice.ris.caselaw.domain.ProceedingDecision;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("api/v1/caselaw/documentunits/{uuid}/proceedingdecisions")
public class ProceedingDecisionController {

  private final DocumentUnitService service;

  public ProceedingDecisionController(DocumentUnitService service) {
    this.service = service;
  }

  @PutMapping
  public Flux<ProceedingDecision> createProceedingDecision(
      @PathVariable("uuid") UUID documentUnitUuid,
      @RequestBody ProceedingDecision proceedingDecision) {
    return service.createProceedingDecision(documentUnitUuid, proceedingDecision);
  }

  @PutMapping(value = "/{childUuid}")
  public Mono<ResponseEntity<DocumentUnit>> linkProceedingDecision(
      @PathVariable("uuid") UUID parentUuid, @PathVariable UUID childUuid) {
    return service
        .linkProceedingDecision(parentUuid, childUuid)
        .map(documentUnit -> ResponseEntity.status(HttpStatus.CREATED).body(documentUnit));
  }

  @DeleteMapping(value = "/{childUuid}")
  public Mono<ResponseEntity<String>> removeProceedingDecision(
      @PathVariable("uuid") UUID parentUuid, @PathVariable UUID childUuid) {

    return service
        .removeProceedingDecision(parentUuid, childUuid)
        .map(str -> ResponseEntity.status(HttpStatus.OK).body(str))
        .onErrorReturn(
            ResponseEntity.internalServerError().body("Couldn't remove the ProceedingDecision"));
  }
}
