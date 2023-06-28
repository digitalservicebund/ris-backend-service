package de.bund.digitalservice.ris.caselaw.domain;

import java.time.Instant;

public interface MailResponse extends PublicationEntry {

  Instant getPublishDate();

  String getStatusCode();
}
