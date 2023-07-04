package de.bund.digitalservice.ris.caselaw.domain;

import java.time.Instant;

public interface PublicationHistoryRecord {
  PublicationHistoryRecordType getType();

  Instant getDate();
}
