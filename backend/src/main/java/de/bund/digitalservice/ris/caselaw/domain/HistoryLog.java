package de.bund.digitalservice.ris.caselaw.domain;

import java.time.Instant;
import java.util.UUID;
import lombok.Builder;

@Builder
public record HistoryLog(
    UUID id,
    Instant createdAt,
    String createdBy,
    String documentationOffice,
    String description,
    String eventType) {}
