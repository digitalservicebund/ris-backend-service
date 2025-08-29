package de.bund.digitalservice.ris.caselaw.adapter.transformer;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.HistoryLogDocumentationUnitProcessStepDTO;
import de.bund.digitalservice.ris.caselaw.domain.HistoryLogDocumentationUnitProcessStep;
import javax.annotation.Nullable;

public class HistoryLogDocumentationUnitProcessStepTransformer {

  private HistoryLogDocumentationUnitProcessStepTransformer() {}

  /**
   * Transforms a {@link HistoryLogDocumentationUnitProcessStepDTO} database entity into its
   * corresponding {@link HistoryLogDocumentationUnitProcessStep} domain object.
   *
   * @param dto The DTO to transform.
   * @return The domain object.
   */
  public static HistoryLogDocumentationUnitProcessStep toDomain(
      @Nullable HistoryLogDocumentationUnitProcessStepDTO dto) {
    if (dto == null) {
      return null;
    }

    return HistoryLogDocumentationUnitProcessStep.builder()
        .id(dto.getId())
        .createdAt(dto.getCreatedAt())
        .historyLog(HistoryLogTransformer.transformToDomain(dto.getHistoryLog()))
        .fromDocumentationUnitProcessStep(
            DocumentationUnitProcessStepTransformer.toDomain(
                dto.getFromDocumentationUnitProcessStep()))
        .toDocumentationUnitProcessStep(
            DocumentationUnitProcessStepTransformer.toDomain(
                dto.getToDocumentationUnitProcessStep()))
        .build();
  }
}
