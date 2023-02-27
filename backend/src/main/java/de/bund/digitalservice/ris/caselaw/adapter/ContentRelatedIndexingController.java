package de.bund.digitalservice.ris.caselaw.adapter;

import de.bund.digitalservice.ris.caselaw.domain.lookuptable.subjectfield.FieldOfLaw;
import java.util.UUID;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("api/v1/caselaw/documentunits/{uuid}/contentrelatedindexing")
public class ContentRelatedIndexingController {
  private final FieldOfLawService fieldOfLawService;

  public ContentRelatedIndexingController(FieldOfLawService fieldOfLawService) {
    this.fieldOfLawService = fieldOfLawService;
  }

  @GetMapping("fieldsoflaw")
  public Flux<FieldOfLaw> getFieldsOfLaw(@PathVariable("uuid") UUID documentUnitUuid) {
    return fieldOfLawService.getFieldsOfLawForDocumentUnit(documentUnitUuid);
  }

  @PutMapping("fieldsoflaw/{identifier}")
  public Flux<FieldOfLaw> addFieldOfLaw(
      @PathVariable("uuid") UUID documentUnitUuid, @PathVariable("identifier") String identifier) {

    return fieldOfLawService.addFieldOfLawToDocumentUnit(documentUnitUuid, identifier);
  }

  @DeleteMapping("fieldsoflaw/{identifier}")
  public Flux<FieldOfLaw> removeFieldOfLaw(
      @PathVariable("uuid") UUID documentUnitUuid, @PathVariable("identifier") String identifier) {

    return fieldOfLawService.removeFieldOfLawToDocumentUnit(documentUnitUuid, identifier);
  }
}
