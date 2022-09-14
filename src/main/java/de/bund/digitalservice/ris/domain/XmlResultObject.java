package de.bund.digitalservice.ris.domain;

import java.time.Instant;
import java.util.List;

public record XmlResultObject(
    String xml,
    String statusCode,
    List<String> statusMessages,
    String fileName,
    Instant publishDate) {}
