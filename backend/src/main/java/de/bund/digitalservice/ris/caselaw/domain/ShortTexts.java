package de.bund.digitalservice.ris.caselaw.domain;

import java.util.List;
import lombok.Builder;

@Builder(toBuilder = true)
public record ShortTexts(
    List<String> decisionNames,
    String headline,
    String guidingPrinciple,
    String headnote,
    String otherHeadnote) {}
