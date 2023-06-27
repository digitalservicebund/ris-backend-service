package de.bund.digitalservice.ris.caselaw.domain;

import de.bund.digitalservice.ris.caselaw.domain.lookuptable.citation.CitationStyle;
import de.bund.digitalservice.ris.caselaw.domain.validator.DateKnownConstraint;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@SuperBuilder(toBuilder = true)
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Getter
@NoArgsConstructor
@DateKnownConstraint
public class ActiveCitation extends LinkedDocumentationUnit {
  private CitationStyle citationStyle;

  public boolean hasNoValues() {
    return court == null
        && decisionDate == null
        && fileNumber == null
        && documentType == null
        && citationStyle == null;
  }
}
