package de.bund.digitalservice.ris.caselaw.adapter;

import de.bund.digitalservice.ris.OpenApiConfiguration;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.fieldoflaw.FieldOfLaw;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("api/v1/caselaw/documentunits/{uuid}/contentrelatedindexing")
@Tag(name = OpenApiConfiguration.CASELAW_TAG)
public class ContentRelatedIndexingController {
  private final FieldOfLawService fieldOfLawService;

  public ContentRelatedIndexingController(FieldOfLawService fieldOfLawService) {
    this.fieldOfLawService = fieldOfLawService;
  }

  @GetMapping("fieldsoflaw")
  @PreAuthorize("@userHasReadAccessByDocumentUnitUuid.apply(#documentUnitUuid)")
  public Mono<List<FieldOfLaw>> getFieldsOfLaw(@PathVariable("uuid") UUID documentUnitUuid) {
    return Mono.just(Collections.emptyList());
  }

  @GetMapping("keywords")
  @PreAuthorize("@userHasReadAccessByDocumentUnitUuid.apply(#documentUnitUuid)")
  @Deprecated
  public Mono<List<String>> getKeywords(@PathVariable("uuid") UUID documentUnitUuid) {
    return Mono.just(Collections.emptyList());
  }
}
