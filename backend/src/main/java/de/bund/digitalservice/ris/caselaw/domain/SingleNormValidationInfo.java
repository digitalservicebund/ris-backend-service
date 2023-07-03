package de.bund.digitalservice.ris.caselaw.domain;

import de.bund.digitalservice.ris.caselaw.domain.validator.SingleNormConstraint;

@SingleNormConstraint
public record SingleNormValidationInfo(String singleNorm, String normAbbreviation) {}
