package de.bund.digitalservice.ris.caselaw.domain;

import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HistoryLogDocumentationUnitProcessStep {
  private UUID id;
  private Instant createdAt;
  private HistoryLog historyLog;
  private DocumentationUnitProcessStep fromDocumentationUnitProcessStep;
  private DocumentationUnitProcessStep toDocumentationUnitProcessStep;
}
