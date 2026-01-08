package de.bund.digitalservice.ris.caselaw.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import lombok.Builder;

@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
public record Correction(
    UUID id,
    CorrectionType type,
    String description,
    LocalDate date,
    List<Long> borderNumbers,
    String content) {}
