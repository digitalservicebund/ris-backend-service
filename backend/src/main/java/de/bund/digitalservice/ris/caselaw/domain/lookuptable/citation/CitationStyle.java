package de.bund.digitalservice.ris.caselaw.domain.lookuptable.citation;

import lombok.Builder;

@Builder
public record CitationStyle(
    String documentType, String citationDocumentType, String jurisShortcut, String label) {}
