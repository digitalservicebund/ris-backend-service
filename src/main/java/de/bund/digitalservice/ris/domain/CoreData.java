package de.bund.digitalservice.ris.domain;

public record CoreData(
    String docketNumber,
    String courtType,
    String category,
    String procedure,
    String ecli,
    String appraisalBody,
    String decisionDate,
    String courtLocation,
    String legalEffect,
    String receiptType,
    String center,
    String region) {}
