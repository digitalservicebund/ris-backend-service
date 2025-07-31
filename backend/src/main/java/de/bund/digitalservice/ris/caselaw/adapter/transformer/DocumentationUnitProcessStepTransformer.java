package de.bund.digitalservice.ris.caselaw.adapter.transformer;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentationUnitProcessStepDTO;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnitProcessStep;
import de.bund.digitalservice.ris.caselaw.domain.ProcessStep;
import de.bund.digitalservice.ris.caselaw.domain.User;

public class DocumentationUnitProcessStepTransformer {

  private DocumentationUnitProcessStepTransformer() {}

  /**
   * Transforms a JPA Entity into the domain object of documentation unit's process step. This
   * method expects the associated ProcessStep domain objects to be passed as parameters.
   *
   * @param entity The JPA Entity to transform.
   * @param processStep The ProcessStep domain object (already fetched).
   * @return The corresponding DocumentationUnitProcessStep domain object.
   */
  public static DocumentationUnitProcessStep toDomain(
      DocumentationUnitProcessStepDTO entity, ProcessStep processStep) {
    if (entity == null || processStep == null) {
      return null;
    }

    return DocumentationUnitProcessStep.builder()
        .id(entity.getId())
        .user(User.builder().id(entity.getUserId()).build())
        .createdAt(entity.getCreatedAt())
        .processStep(processStep)
        .build();
  }
}
