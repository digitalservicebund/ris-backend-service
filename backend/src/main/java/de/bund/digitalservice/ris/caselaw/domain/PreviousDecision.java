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
public class PreviousDecision extends RelatedDocumentationUnit {
  private boolean dateKnown = true;

  public boolean hasNoValues() {
    return court == null
        && decisionDate == null
        && fileNumber == null
        && documentType == null
        && documentNumber == null;
  }
}
