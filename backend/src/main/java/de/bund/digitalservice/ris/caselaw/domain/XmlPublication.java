package de.bund.digitalservice.ris.caselaw.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

@Builder(toBuilder = true)
public record XmlPublication(
    UUID documentUnitUuid,
    String receiverAddress,
    String mailSubject,
    String xml,
    @Getter String statusCode,
    List<String> statusMessages,
    String fileName,
    @Getter @JsonProperty(access = JsonProperty.Access.WRITE_ONLY, value = "date")
        Instant publishDate,
    String publishStateDisplayText,
    @Getter String issuerAddress)
    implements Publication {
  @Override
  public PublicationHistoryRecordType getType() {
    return PublicationHistoryRecordType.PUBLICATION;
  }

  @Override
  public Instant getDate() {
    return getPublishDate();
  }
}
