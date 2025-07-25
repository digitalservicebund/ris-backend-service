package de.bund.digitalservice.ris.caselaw.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.Builder;

@Builder
public record DocumentationOffice(String abbreviation, UUID id, List<ProcessStep> processSteps) {

  public DocumentationOffice(String abbreviation, UUID id) {
    this(abbreviation, id, new ArrayList<>());
  }
}
