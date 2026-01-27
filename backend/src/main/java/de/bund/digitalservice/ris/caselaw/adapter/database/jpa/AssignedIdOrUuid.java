package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import static java.lang.annotation.ElementType.FIELD;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.hibernate.annotations.IdGeneratorType;

/** Use a manually assigned id or if no id is set generated a new uuid. */
@IdGeneratorType(AssignedIdOrUuidGenerator.class)
@Target({FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface AssignedIdOrUuid {}
