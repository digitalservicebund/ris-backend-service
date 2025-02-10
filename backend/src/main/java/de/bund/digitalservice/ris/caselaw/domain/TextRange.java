package de.bund.digitalservice.ris.caselaw.domain;

import lombok.Builder;

@Builder
public record TextRange(int start, int end, String text) {}
