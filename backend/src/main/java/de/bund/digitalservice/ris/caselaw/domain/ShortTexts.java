package de.bund.digitalservice.ris.caselaw.domain;

import lombok.Builder;

@Builder
public record ShortTexts(
    String decisionName,
    String headline,
    String guidingPrinciple,
    String headnote,
    String otherHeadnote) {}
