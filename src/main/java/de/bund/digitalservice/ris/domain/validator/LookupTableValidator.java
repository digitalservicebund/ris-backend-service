package de.bund.digitalservice.ris.domain.validator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class LookupTableValidator implements ConstraintValidator<LookupTableConstraint, String> {

  private String lookupTableName;
  private static final Map<String, List<String>> lookupTables = new HashMap<>();

  static {
    lookupTables.put("legalEffect", List.of("Ja", "Nein", "Keine Angabe"));
  }

  @Override
  public void initialize(LookupTableConstraint constraint) {
    this.lookupTableName = constraint.lookupTableName();
  }

  @Override
  public boolean isValid(String fieldValue, ConstraintValidatorContext cxt) {
    if (fieldValue == null) {
      return true;
    }
    if (lookupTables.containsKey(lookupTableName)) {
      return lookupTables.get(lookupTableName).contains(fieldValue);
    }
    return false;
  }
}
