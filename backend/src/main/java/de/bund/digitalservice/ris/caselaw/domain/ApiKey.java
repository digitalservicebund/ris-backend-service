package de.bund.digitalservice.ris.caselaw.domain;

import java.time.LocalDateTime;
import lombok.Builder;

@Builder
public record ApiKey(String apiKey, LocalDateTime validUntil, boolean valid) {}
