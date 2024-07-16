package de.bund.digitalservice.ris.caselaw.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import lombok.Builder;

/** Domain object for delta migration runs or import events */
@Builder(toBuilder = true)
public record DeltaMigration(
    String xml,
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY, value = "date") Instant migratedDate)
    implements EventRecord {
  @Override
  public EventType getType() {
    return EventType.MIGRATION;
  }

  @Override
  public Instant getDate() {
    return migratedDate;
  }

  public String getXml() {
    return xml;
  }
}
