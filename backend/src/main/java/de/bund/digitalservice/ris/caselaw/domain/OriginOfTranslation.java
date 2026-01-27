package de.bund.digitalservice.ris.caselaw.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import java.util.UUID;
import lombok.Builder;

@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
public record OriginOfTranslation(
    UUID id,
    LanguageCode languageCode,
    TranslationType translationType,
    List<String> translators,
    List<Long> borderNumbers,
    List<String> urls) {}
