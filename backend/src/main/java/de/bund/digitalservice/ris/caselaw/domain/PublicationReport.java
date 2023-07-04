package de.bund.digitalservice.ris.caselaw.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.Instant;
import lombok.Builder;

@Builder(toBuilder = true)
public record PublicationReport(
    @JsonIgnore String documentNumber, String content, @JsonIgnore Instant receivedDate)
    implements PublicationHistoryRecord {
  @Override
  public PublicationHistoryRecordType getType() {
    return PublicationHistoryRecordType.PUBLICATION_REPORT;
  }

  @Override
  public Instant getDate() {
    return receivedDate;
  }
}
