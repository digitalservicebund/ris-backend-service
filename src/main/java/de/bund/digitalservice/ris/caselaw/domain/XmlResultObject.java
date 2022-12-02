package de.bund.digitalservice.ris.caselaw.domain;

import java.time.Instant;
import java.util.List;

public record XmlResultObject(
    /** xml representation of the document unit */
    String xml,
    /** status code of the transformation (200 no errors, 400 contains errors) */
    String statusCode,
    /** status messages of the exporter (normally error messages) */
    List<String> statusMessages,
    /** name of the file in which the xml should be saved */
    String fileName,
    /** Timestamp of the publishing */
    Instant publishDate) {}
