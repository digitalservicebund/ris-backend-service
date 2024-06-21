package de.bund.digitalservice.ris.caselaw.domain;

import java.time.Instant;
import java.util.List;
import lombok.Builder;

@Builder
public record XmlResultObject(
    String xml,
    String statusCode,
    List<String> statusMessages,
    String fileName,
    Instant publishDate) {}
