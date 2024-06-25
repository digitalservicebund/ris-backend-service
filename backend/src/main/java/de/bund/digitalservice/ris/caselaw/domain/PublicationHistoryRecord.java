package de.bund.digitalservice.ris.caselaw.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.time.Instant;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
  @Type(value = PublicationReport.class, name = "PUBLICATION_REPORT"),
  @Type(value = XmlPublication.class, name = "PUBLICATION")
})
@JsonIgnoreProperties({"type"})
public interface PublicationHistoryRecord {
  PublicationHistoryRecordType getType();

  Instant getDate();
}
