package de.bund.digitalservice.ris.domain;

public record Texts(
    String decisionName,
    String headline,
    String guidingPrinciple,
    String headnote,
    String tenor,
    String reasons,
    String caseFacts,
    String decisionReasons) {}
