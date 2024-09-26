package de.bund.digitalservice.ris.caselaw.domain;

import lombok.Builder;

@Builder
public record LongTexts(
    String tenor,
    String reasons,
    String caseFacts,
    String decisionReasons,
    String dissentingOpinion,
    String otherLongText,
    String outline) {}
