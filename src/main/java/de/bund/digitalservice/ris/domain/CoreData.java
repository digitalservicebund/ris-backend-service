package de.bund.digitalservice.ris.domain;

import lombok.Builder;

@Builder
public record CoreData(
    String fileNumber,
    String courtType,
    String category,
    String procedure,
    String ecli,
    String appraisalBody,
    String decisionDate,
    String courtLocation,
    String legalEffect,
    String inputType,
    String center,
    String region) {}
