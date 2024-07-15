package de.bund.digitalservice.ris.caselaw.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.time.Instant;

/** Interface for all event records (Handover, Handover Report, Migration). */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
  @Type(value = HandoverReport.class, name = "HANDOVER_REPORT"),
  @Type(value = HandoverMail.class, name = "HANDOVER"),
  @Type(value = DeltaMigration.class, name = "MIGRATION")
})
@JsonIgnoreProperties({"type"})
public interface EventRecord {
  EventType getType();

  Instant getDate();
}
