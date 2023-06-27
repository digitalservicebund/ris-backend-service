package de.bund.digitalservice.ris.caselaw.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.Instant;
import lombok.Builder;

@Builder(toBuilder = true)
public record PublicationReport(
    String documentNumber, String content, @JsonIgnore Instant receivedDate)
    implements PublicationEntry {
  @Override
  public PublicationLogEntryType getType() {
    return PublicationLogEntryType.HTML;
  }

  @Override
  public Instant getDate() {
    return receivedDate;
  }
}
