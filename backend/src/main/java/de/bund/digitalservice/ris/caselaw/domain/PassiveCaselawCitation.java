package de.bund.digitalservice.ris.caselaw.domain;

import de.bund.digitalservice.ris.caselaw.domain.court.Court;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.citation.CitationType;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.documenttype.DocumentType;
import de.bund.digitalservice.ris.caselaw.domain.validator.DateKnownConstraint;
import java.time.LocalDate;
import java.util.UUID;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@SuperBuilder(toBuilder = true)
@EqualsAndHashCode
@ToString(callSuper = true)
@Getter
@NoArgsConstructor
@DateKnownConstraint
@Data
public class PassiveCaselawCitation {
  private UUID uuid;
  private String sourceDocumentNumber;
  private Court sourceCourt;
  private LocalDate sourceDate;
  private String sourceFileNumber;
  private DocumentType sourceDocumentType;
  private DocumentationOffice sourceDocumentationOffice;
  private CitationType citationType;

  public boolean hasNoValues() {
    return sourceCourt == null
        && sourceDate == null
        && sourceFileNumber == null
        && sourceDocumentType == null
        && citationType == null
        && sourceDocumentNumber == null;
  }
}
