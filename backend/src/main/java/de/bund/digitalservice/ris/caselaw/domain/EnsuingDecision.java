package de.bund.digitalservice.ris.caselaw.domain;

import de.bund.digitalservice.ris.caselaw.domain.validator.DateKnownConstraint;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@SuperBuilder(toBuilder = true)
@Getter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@DateKnownConstraint
@Data
public class EnsuingDecision extends RelatedDocumentationUnit {
  private boolean isPending;
  private String note;

  public boolean hasNoValues() {
    return court == null && decisionDate == null && fileNumber == null && documentType == null;
  }
}
