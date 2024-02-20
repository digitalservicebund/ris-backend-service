package de.bund.digitalservice.ris.caselaw.domain;

import java.util.List;
import lombok.Builder;

@Builder
public record Texts(
    String decisionName,
    String headline,
    String guidingPrinciple,
    String headnote,
    String tenor,
    String reasons,
    String caseFacts,
    String decisionReasons,
    List<String> borderNumbers) {}
