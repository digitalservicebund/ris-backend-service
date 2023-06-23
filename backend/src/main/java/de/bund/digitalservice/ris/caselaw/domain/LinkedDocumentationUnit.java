package de.bund.digitalservice.ris.caselaw.domain;

import de.bund.digitalservice.ris.caselaw.domain.lookuptable.court.Court;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.documenttype.DocumentType;
import java.beans.Transient;
import java.time.Instant;
import java.util.UUID;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@SuperBuilder(toBuilder = true)
@Getter
@NoArgsConstructor
@EqualsAndHashCode
@ToString
public class LinkedDocumentationUnit {
  protected UUID uuid;
  protected String documentNumber;
  protected Court court;
  protected Instant decisionDate;
  protected String fileNumber;
  protected DocumentType documentType;
  protected DataSource dataSource;
  protected boolean dateKnown;

  @Transient
  public boolean isEmpty() {
    return uuid == null
        && documentNumber == null
        && court == null
        && decisionDate == null
        && fileNumber == null
        && documentType == null
        && dataSource == null
        && !dateKnown;
  }
}
