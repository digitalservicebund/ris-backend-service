package de.bund.digitalservice.ris.caselaw.adapter.transformer;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentationUnitProcessStepDTO;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnitProcessStep;
import de.bund.digitalservice.ris.caselaw.domain.User;
import java.util.UUID;

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
    User user = null;
    if (entity.getUserId() != null) {
      user = User.builder().id(entity.getUserId()).build();
    }
    return DocumentationUnitProcessStep.builder()
        .id(entity.getId())
        .user(user)
        .createdAt(entity.getCreatedAt())
        .processStep(ProcessStepTransformer.toDomain(entity.getProcessStep()))
        .build();
  }

  public static DocumentationUnitProcessStepDTO toDto(
      DocumentationUnitProcessStep documentationUnitProcessStep) {
    if (documentationUnitProcessStep == null) {
      return null;
    }

    UUID userId = null;
    if (documentationUnitProcessStep.getUser() != null) {
      userId = documentationUnitProcessStep.getUser().id();
    }

    return DocumentationUnitProcessStepDTO.builder()
        .userId(userId)
        .processStep(ProcessStepTransformer.toDto(documentationUnitProcessStep.getProcessStep()))
        .createdAt(documentationUnitProcessStep.getCreatedAt())
        .id(documentationUnitProcessStep.getId())
        .build();
  }
}
