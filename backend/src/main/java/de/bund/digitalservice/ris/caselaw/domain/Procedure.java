package de.bund.digitalservice.ris.caselaw.domain;

import java.time.Instant;
import lombok.Builder;

@Builder
public record Procedure(String label, Integer documentUnitCount, Instant createdAt) {}
