package de.bund.digitalservice.ris.caselaw.domain;

import java.time.Instant;

public interface PublicationEntry {
  PublicationLogEntryType getType();

  Instant getDate();
}
