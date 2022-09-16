package de.bund.digitalservice.ris.domain;

import java.util.List;

public record Texts(
    String decisionName,
    String headline,
    String guidingPrinciple,
    String headnote,
    String tenor,
    String reasons,
    String caseFacts,
    String decisionReasons,
    List<PreviousDecision> previousDecisions) {}
