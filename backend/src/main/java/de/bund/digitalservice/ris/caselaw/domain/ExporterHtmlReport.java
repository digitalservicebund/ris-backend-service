package de.bund.digitalservice.ris.caselaw.domain;

import java.time.Instant;
import lombok.Builder;

@Builder(toBuilder = true)
public record ExporterHtmlReport(String documentNumber, String html, Instant receivedDate) {
  public static final ExporterHtmlReport EMPTY = new ExporterHtmlReport(null, null, null);
}
