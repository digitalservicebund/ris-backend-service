package de.bund.digitalservice.ris.caselaw.domain;

import java.time.Instant;

public interface Publication extends PublicationHistoryRecord {

  Instant getPublishDate();

  String getStatusCode();
}
