package de.bund.digitalservice.ris.domain;

import java.time.Instant;
import org.springframework.data.annotation.Id;

public record XmlMail(
    @Id Long id,
    Long documentUnitId,
    String receiverAddress,
    String mailSubject,
    String xml,
    String statusCode,
    String statusMessages,
    String fileName,
    Instant publishDate) {}
