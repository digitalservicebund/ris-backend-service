package de.bund.digitalservice.ris.domain.validator;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import javax.validation.Constraint;
import javax.validation.Payload;

@Documented
@Constraint(validatedBy = LookupTableValidator.class)
@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface LookupTableConstraint {
  String message() default "Value does not exist in lookup table";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};

  String lookupTableName();
}
