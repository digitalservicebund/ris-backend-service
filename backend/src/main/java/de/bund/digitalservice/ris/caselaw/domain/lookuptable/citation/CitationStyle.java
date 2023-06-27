package de.bund.digitalservice.ris.caselaw.domain.lookuptable.citation;

import java.util.UUID;
import lombok.Builder;

@Builder
public record CitationStyle(
    UUID uuid,
    Character documentType,
    Character citationDocumentType,
    String jurisShortcut,
    String label) {}
