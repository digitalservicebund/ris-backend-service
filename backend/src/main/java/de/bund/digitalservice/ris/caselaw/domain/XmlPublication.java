package de.bund.digitalservice.ris.caselaw.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.Builder;

@Builder(toBuilder = true)
public record XmlPublication(
    UUID documentUnitUuid,
    String receiverAddress,
    String mailSubject,
    String xml,
    String statusCode,
    List<String> statusMessages,
    String fileName,
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY, value = "date") Instant publishDate,
    String publishStateDisplayText)
    implements Publication {
  @Override
  public PublicationHistoryRecordType getType() {
    return PublicationHistoryRecordType.PUBLICATION;
  }

  @Override
  public Instant getDate() {
    return getPublishDate();
  }

  @Override
  public Instant getPublishDate() {
    return publishDate;
  }

  @Override
  public String getStatusCode() {
    return statusCode;
  }
}
