package de.bund.digitalservice.ris.caselaw.domain;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import lombok.Builder;

@Builder
public record Correction(
    UUID id,
    boolean newEntry,
    CorrectionType type,
    String description,
    LocalDate date,
    List<Long> borderNumbers,
    String content) {}
