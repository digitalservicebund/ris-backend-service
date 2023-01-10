package de.bund.digitalservice.ris.caselaw.domain;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.Builder;

@Builder(toBuilder = true)
public record XmlMail(
    UUID documentUnitUuid,
    String receiverAddress,
    String mailSubject,
    String xml,
    String statusCode,
    List<String> statusMessages,
    String fileName,
    Instant publishDate,
    PublishState publishState) {
  public static final XmlMail EMPTY =
      new XmlMail(null, null, null, null, null, null, null, null, null);
}
