package de.bund.digitalservice.ris.caselaw.domain.validator;

import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.lookuptable.LegalEffect;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LookupTableValidator implements ConstraintValidator<LookupTableConstraint, String> {

  private String lookupTableName;
  private static final Map<String, List<String>> lookupTables = new HashMap<>();

  static {
    lookupTables.put(
        "legalEffect",
        List.of(
            LegalEffect.YES.getLabel(),
            LegalEffect.NO.getLabel(),
            LegalEffect.NOT_SPECIFIED.getLabel()));
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
