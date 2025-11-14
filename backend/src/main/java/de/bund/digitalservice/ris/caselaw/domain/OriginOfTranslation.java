package de.bund.digitalservice.ris.caselaw.domain;

import java.util.List;
import java.util.UUID;
import lombok.Builder;

@Builder
public record OriginOfTranslation(
    UUID id,
    boolean newEntry,
    LanguageCode languageCode,
    TranslationType translationType,
    List<String> translators,
    List<Long> borderNumbers,
    List<String> urls) {}
