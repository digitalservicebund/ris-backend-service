package de.bund.digitalservice.ris.caselaw.adapter.transformer;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentationUnitProcessStepDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.ProcessStepDTO;
import de.bund.digitalservice.ris.caselaw.domain.ProcessStep;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
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

    if (documentationUnitProcessStepsDTOs == null) return null;

    if (documentationUnitProcessStepsDTOs.size() < 2) return null;

    UUID lastId =
        Optional.ofNullable(documentationUnitProcessStepsDTOs.getLast())
            .map(DocumentationUnitProcessStepDTO::getProcessStep)
            .map(ProcessStepDTO::getId)
            .orElse(null);

    return documentationUnitProcessStepsDTOs
        .subList(0, documentationUnitProcessStepsDTOs.size() - 1) // exclude last one
        .reversed()
        .stream()
        .map(DocumentationUnitProcessStepDTO::getProcessStep)
        .filter(Objects::nonNull)
        .filter(processStepDto -> !Objects.equals(processStepDto.getId(), lastId))
        .map(ProcessStepTransformer::toDomain)
        .findFirst()
        .orElse(null);
  }
}
