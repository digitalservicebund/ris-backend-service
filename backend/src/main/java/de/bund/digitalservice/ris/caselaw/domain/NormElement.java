package de.bund.digitalservice.ris.caselaw.domain;

import lombok.Builder;

@Builder
public record NormElement(String label, boolean hasNumberDesignation, String normCode) {}
