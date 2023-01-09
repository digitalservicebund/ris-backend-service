package de.bund.digitalservice.ris.caselaw.domain.lookuptable.documenttype;

import lombok.Builder;

@Builder
public record DocumentType(String jurisShortcut, String label) {}
