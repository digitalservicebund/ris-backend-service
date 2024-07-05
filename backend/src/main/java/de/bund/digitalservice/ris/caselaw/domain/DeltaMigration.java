package de.bund.digitalservice.ris.caselaw.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import lombok.Builder;

/** Domain object for delta migration runs */
@Builder(toBuilder = true)
public record DeltaMigration(
    String xml,
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY, value = "date") Instant migratedDate)
    implements PublicationHistoryRecord {
  @Override
  public PublicationHistoryRecordType getType() {
    return PublicationHistoryRecordType.MIGRATION;
  }

  @Override
  public Instant getDate() {
    return migratedDate;
  }

  public String getXml() {
    return xml;
  }
}
