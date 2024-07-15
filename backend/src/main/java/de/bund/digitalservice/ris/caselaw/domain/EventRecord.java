package de.bund.digitalservice.ris.caselaw.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.time.Instant;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
  @Type(value = HandoverReport.class, name = "PUBLICATION_REPORT"),
  @Type(value = XmlHandoverMail.class, name = "PUBLICATION"),
  @Type(value = DeltaMigration.class, name = "MIGRATION")
})
@JsonIgnoreProperties({"type"})
public interface EventRecord {
  EventType getType();

  Instant getDate();
}
