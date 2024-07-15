package de.bund.digitalservice.ris.caselaw.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import lombok.Builder;

@Builder(toBuilder = true)
public record HandoverReport(
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY) String documentNumber,
    String content,
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY, value = "date") Instant receivedDate)
    implements EventRecord {
  @Override
  public EventType getType() {
    return EventType.PUBLICATION_REPORT;
  }

  @Override
  public Instant getDate() {
    return receivedDate;
  }
}
