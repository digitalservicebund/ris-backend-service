package de.bund.digitalservice.ris.caselaw.domain.lookuptable;

import lombok.Builder;

@Builder
public record DocumentTypeNew(
    String abbreviation,
    String label,
    boolean multiple,
    String superLabel1,
    String superLabel2,
    Character categoryLabel) {}
