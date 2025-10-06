package de.bund.digitalservice.ris.caselaw.domain;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BulkAssignProcessStepRequest {
  @NotNull DocumentationUnitProcessStep documentationUnitProcessStep;
  @NotNull @NotEmpty List<UUID> documentationUnitIds;
}
