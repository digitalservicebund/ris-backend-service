package de.bund.digitalservice.ris.caselaw.domain;

import java.time.Instant;
import java.util.List;
import lombok.Builder;

@Builder
public record Procedure(
    String label,
    List<DocumentationUnitSearchEntry> documentUnits,
    Integer documentUnitCount,
    Instant created_at) {}
