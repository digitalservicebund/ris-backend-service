package de.bund.digitalservice.ris.caselaw.domain.extraction.rulebased;

import java.util.Map;
import java.util.UUID;

public record ExtractionMatch(
    UUID id,
    String extractionClass,
    String extractionText,
    CharInterval charInterval,
    Map<String, Object> attributes,
    Integer priority,
    String annotation) {}

record CharInterval(Integer startPos, Integer endPos) {}
