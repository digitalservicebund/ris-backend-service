package de.bund.digitalservice.ris.caselaw.adapter.transformer;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentationUnitProcessStepDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.ProcessStepDTO;
import de.bund.digitalservice.ris.caselaw.domain.ProcessStep;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class ProcessStepTransformer {
  private ProcessStepTransformer() {}

  public static ProcessStep toDomain(ProcessStepDTO dto) {
    if (dto == null) return null;
    return new ProcessStep(dto.getId(), dto.getName(), dto.getAbbreviation());
  }

  public static ProcessStepDTO toDto(ProcessStep domain) {
    if (domain == null) return null;
    return ProcessStepDTO.builder()
        .id(domain.uuid())
        .name(domain.name())
        .abbreviation(domain.abbreviation())
        .build();
  }

  /**
   * Iterate backwards to find the previous process if the process id is different then the last one
   *
   * @param documentationUnitProcessStepsDTOs of the list item
   * @return the previous unique process step
   */
  public static ProcessStep getPreviousProcessStep(
      List<DocumentationUnitProcessStepDTO> documentationUnitProcessStepsDTOs) {

    if (documentationUnitProcessStepsDTOs == null || documentationUnitProcessStepsDTOs.size() < 2) {
      return null;
    }

    UUID currentProcessStepId =
        documentationUnitProcessStepsDTOs.getFirst().getProcessStep().getId();

    return toDomain(
        documentationUnitProcessStepsDTOs.stream()
            .skip(1) // Start from the second item
            .filter(item -> item.getProcessStep().getId() != currentProcessStepId)
            .findFirst()
            .map(DocumentationUnitProcessStepDTO::getProcessStep)
            .orElse(null));
  }
}
