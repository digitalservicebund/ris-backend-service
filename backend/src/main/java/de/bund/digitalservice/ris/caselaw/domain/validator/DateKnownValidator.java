package de.bund.digitalservice.ris.caselaw.domain.validator;

import de.bund.digitalservice.ris.caselaw.domain.ProceedingDecision;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class DateKnownValidator
    implements ConstraintValidator<DateKnownConstraint, ProceedingDecision> {

  @Override
  public void initialize(DateKnownConstraint constraintAnnotation) {
    // nothing to initialize
  }

  @Override
  public boolean isValid(
      ProceedingDecision proceedingDecision,
      ConstraintValidatorContext constraintValidatorContext) {
    return proceedingDecision.isDateKnown() || proceedingDecision.getDecisionDate() == null;
  }
}
