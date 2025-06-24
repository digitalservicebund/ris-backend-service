package de.bund.digitalservice.ris.caselaw.domain;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DecisionDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentationUnitDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.PendingProceedingDTO;
import lombok.Getter;

public enum Kind {
  DECISION(DecisionDTO.class),
  PENDING_PROCEEDING(PendingProceedingDTO.class);

  @Getter private final Class<? extends DocumentationUnitDTO> dtoClass;

  Kind(Class<? extends DocumentationUnitDTO> dtoClass) {
    this.dtoClass = dtoClass;
  }
}
