package de.bund.digitalservice.ris.caselaw.domain;

import java.util.List;
import lombok.Builder;

@Builder
public record Procedure(
    String label, Integer documentUnitCount, List<DocumentationUnitSearchEntry> documentUnits) {}
