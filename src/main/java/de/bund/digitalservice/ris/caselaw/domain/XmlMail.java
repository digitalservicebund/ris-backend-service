package de.bund.digitalservice.ris.caselaw.domain;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.Builder;

@Builder
public record XmlMail(
    UUID documentUnitUuid,
    String receiverAddress,
    String mailSubject,
    String xml,
    String statusCode,
    List<String> statusMessages,
    String fileName,
    Instant publishDate) {}
