package de.bund.digitalservice.ris.caselaw.adapter.transformer;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentationUnitProcessStepDTO;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnitProcessStep;

public class DocumentationUnitProcessStepTransformer {

  private DocumentationUnitProcessStepTransformer() {}

  /**
   * Transforms a JPA Entity into the domain object of documentation unit's process step. This
   * method expects the associated ProcessStep domain objects to be passed as parameters.
   *
   * @param entity The JPA Entity to transform.
   * @return The corresponding DocumentationUnitProcessStep domain object.
   */
  public static DocumentationUnitProcessStep toDomain(DocumentationUnitProcessStepDTO entity) {
    if (entity == null) {
      return null;
    }
    return DocumentationUnitProcessStep.builder()
        .id(entity.getId())
        .user(UserTransformer.transformToDomain(entity.getUser()))
        .createdAt(entity.getCreatedAt())
        .processStep(ProcessStepTransformer.toDomain(entity.getProcessStep()))
        .build();
  }

  public static DocumentationUnitProcessStepDTO toDto(
      DocumentationUnitProcessStep documentationUnitProcessStep) {
    if (documentationUnitProcessStep == null) {
      return null;
    }

    return DocumentationUnitProcessStepDTO.builder()
        .user(UserTransformer.transformToDTO(documentationUnitProcessStep.getUser()))
        .processStep(ProcessStepTransformer.toDto(documentationUnitProcessStep.getProcessStep()))
        .createdAt(documentationUnitProcessStep.getCreatedAt())
        .id(documentationUnitProcessStep.getId())
        .build();
  }
}
