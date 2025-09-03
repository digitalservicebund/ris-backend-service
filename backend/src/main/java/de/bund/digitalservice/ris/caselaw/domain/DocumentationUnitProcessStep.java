package de.bund.digitalservice.ris.caselaw.domain;

import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Builder
public class DocumentationUnitProcessStep {
  private UUID id;
  private User user;
  private LocalDateTime createdAt;
  private ProcessStep processStep;
}
