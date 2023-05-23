package de.bund.digitalservice.ris.caselaw.domain;

import java.time.Instant;
import java.util.UUID;
import lombok.Builder;

@Builder
public record DocumentUnitListEntry(
    UUID uuid,
    String documentNumber,
    Instant creationTimestamp,
    DataSource dataSource,
    String fileName,
    String fileNumber) {}
