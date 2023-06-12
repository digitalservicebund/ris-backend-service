package de.bund.digitalservice.ris.caselaw.domain;

import de.bund.digitalservice.ris.caselaw.domain.lookuptable.court.Court;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.documenttype.DocumentType;
import java.time.Instant;
import java.util.UUID;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Getter
@NoArgsConstructor
@EqualsAndHashCode
public class LinkedDocumentationUnit {
  protected UUID uuid;
  protected String documentNumber;
  protected Court court;
  protected Instant decisionDate;
  protected String fileNumber;
  protected DocumentType documentType;
  protected DataSource dataSource;
}
