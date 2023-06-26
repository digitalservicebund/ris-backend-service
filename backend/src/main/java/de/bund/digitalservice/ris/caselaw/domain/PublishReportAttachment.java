package de.bund.digitalservice.ris.caselaw.domain;

import java.time.Instant;
import lombok.Builder;

@Builder(toBuilder = true)
public record PublishReportAttachment(
    String documentNumber, String content, Instant receivedDate) {}
