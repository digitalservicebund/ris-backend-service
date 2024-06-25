package de.bund.digitalservice.ris.caselaw.domain;

import de.bund.digitalservice.ris.caselaw.domain.validator.DateKnownConstraint;
import lombok.Builder;
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
  @Builder.Default private boolean dateKnown = true;
  private String deviatingFileNumber;

  public boolean hasNoValues() {
    return court == null
        && decisionDate == null
        && fileNumber == null
        && documentType == null
        && documentNumber == null
        && status == null
        && deviatingFileNumber == null;
  }
}
