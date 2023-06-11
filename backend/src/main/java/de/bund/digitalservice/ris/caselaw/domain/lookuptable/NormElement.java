package de.bund.digitalservice.ris.caselaw.domain.lookuptable;

public record NormElement(
    String label, Boolean hasNumberDesignation, NormCode normCode, Character categoryLabel) {}
