package de.bund.digitalservice.ris.caselaw.domain;

import java.util.UUID;
import lombok.Builder;

@Builder
public record LanguageCode(UUID id, String label, String isoCode, String isoCode3Letters) {}
