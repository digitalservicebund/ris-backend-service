package de.bund.digitalservice.ris.caselaw.adapter.transformer;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.ProcessStepDTO;
import de.bund.digitalservice.ris.caselaw.domain.ProcessStep;
import org.springframework.stereotype.Component;

@Component
public class ProcessStepTransformer {
  private ProcessStepTransformer() {}

  public static ProcessStep toDomain(ProcessStepDTO dto) {
    if (dto == null) return null;
    return new ProcessStep(dto.getId(), dto.getName(), dto.getAbbreviation());
  }
}
