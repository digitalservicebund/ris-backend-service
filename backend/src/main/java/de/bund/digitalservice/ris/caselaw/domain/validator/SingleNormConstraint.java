package de.bund.digitalservice.ris.caselaw.domain.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Constraint(validatedBy = SingleNormValidator.class)
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface SingleNormConstraint {
  String message() default "Single norm string is not valid";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}
