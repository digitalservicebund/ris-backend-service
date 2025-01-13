package de.bund.digitalservice.ris.caselaw.domain;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class DuplicateRelationStatusRequest {
  @NotNull private DuplicateRelationStatus status;
}
