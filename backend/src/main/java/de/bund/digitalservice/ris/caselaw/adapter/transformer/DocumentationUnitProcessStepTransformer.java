package de.bund.digitalservice.ris.caselaw.adapter.transformer;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentationUnitProcessStepDTO;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnitProcessStep;
import de.bund.digitalservice.ris.caselaw.domain.ProcessStep;
import java.time.LocalDateTime;
import java.util.UUID;

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
    if (entity == null) {
      return null;
    }

    // Use the builder pattern to construct the domain object
    return DocumentationUnitProcessStep.builder()
        .id(entity.getId())
        .userId(entity.getUserId())
        .createdAt(entity.getCreatedAt())
        .processStep(processStep)
        .build();
  }

  /**
   * Transforms a domain object into a JPA Entity for database persistence, using the builder
   * pattern.
   *
   * @param domain The DocumentationUnitProcessStep domain object.
   * @param documentationUnitId The UUID of the DocumentationUnit (required for Entity).
   * @param createdAt The creation timestamp (required for Entity).
   * @return The corresponding DocumentationUnitProcessStepEntity for database persistence.
   */
  public static DocumentationUnitProcessStepDTO toDTO(
      DocumentationUnitProcessStep domain, UUID documentationUnitId, LocalDateTime createdAt) {
    if (domain == null) {
      return null;
    }

    // Access ProcessStep ID, assuming 'id()' if ProcessStep is a record, or 'getId()' if it's a
    // class.
    // Given your previous ProcessStep was a record, using .id() is consistent.
    UUID processStepId = (domain.getProcessStep() != null) ? domain.getProcessStep().uuid() : null;

    return DocumentationUnitProcessStepDTO.builder()
        .id(domain.getId())
        .userId(domain.getUserId())
        .createdAt(createdAt)
        .processStepId(processStepId)
        .documentationUnitId(documentationUnitId)
        .build();
  }
}
