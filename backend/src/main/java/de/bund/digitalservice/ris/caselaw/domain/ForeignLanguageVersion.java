package de.bund.digitalservice.ris.caselaw.domain;

import java.util.UUID;
import lombok.Builder;

@Builder
public record ForeignLanguageVersion(
    UUID id, boolean newEntry, LanguageCode languageCode, String link) {}
