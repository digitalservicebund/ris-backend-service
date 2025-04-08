package de.bund.digitalservice.ris.caselaw.domain;

import java.time.Instant;
import java.util.UUID;
import lombok.Builder;

@Builder
public record HistoryLog(
    UUID id,
    Instant createdAt,
    UUID documentationUnitId,
    UUID documentationOffice,
    UUID userId,
    String userName,
    String systemName,
    String description,
    String eventType) {}
