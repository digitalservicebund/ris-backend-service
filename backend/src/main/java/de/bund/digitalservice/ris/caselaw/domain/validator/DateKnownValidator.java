package de.bund.digitalservice.ris.caselaw.domain.validator;

import de.bund.digitalservice.ris.caselaw.domain.PreviousDecision;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class DateKnownValidator
    implements ConstraintValidator<DateKnownConstraint, PreviousDecision> {

  @Override
  public void initialize(DateKnownConstraint constraintAnnotation) {
    // nothing to initialize
  }

  @Override
  public boolean isValid(
      PreviousDecision previousDecision, ConstraintValidatorContext constraintValidatorContext) {
    return previousDecision.getDateKnown() || previousDecision.getDecisionDate() == null;
  }
}
