package de.bund.digitalservice.ris.caselaw.domain.exception;

import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnit;
import de.bund.digitalservice.ris.caselaw.domain.textcheck.CategoryType;

public class TextCheckNotAllowedCategoryTypeException extends RuntimeException {
  public TextCheckNotAllowedCategoryTypeException(
      CategoryType categoryType, Class<? extends DocumentationUnit> documentationUnitClass) {
    super(
        "Category type '"
            + categoryType
            + "' is not allowed to use in '"
            + documentationUnitClass.getSimpleName()
            + "'");
  }
}
