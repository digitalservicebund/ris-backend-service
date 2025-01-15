package de.bund.digitalservice.ris.caselaw.domain;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Builder
public class DuplicateRelationStatusRequest {
  @NotNull private DuplicateRelationStatus status;
}
