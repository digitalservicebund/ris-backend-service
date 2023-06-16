package de.bund.digitalservice.ris.caselaw.domain.validator;

import de.bund.digitalservice.ris.caselaw.domain.ProceedingDecision;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class DateKnownValidator
    implements ConstraintValidator<DateKnownConstraint, ProceedingDecision> {

  @Override
  public void initialize(DateKnownConstraint constraintAnnotation) {}

  @Override
  public boolean isValid(
      ProceedingDecision proceedingDecision,
      ConstraintValidatorContext constraintValidatorContext) {
    if (!proceedingDecision.isDateKnown() && proceedingDecision.getDecisionDate() != null) {
      return false;
    }
    return true;
  }
}
