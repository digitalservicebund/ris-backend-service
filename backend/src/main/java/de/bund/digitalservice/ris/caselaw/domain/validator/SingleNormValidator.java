package de.bund.digitalservice.ris.caselaw.domain.validator;

import de.bund.digitalservice.ris.caselaw.domain.NormElement;
import de.bund.digitalservice.ris.caselaw.domain.NormElementRepository;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.List;
import java.util.Objects;

public class SingleNormValidator implements ConstraintValidator<SingleNormConstraint, String> {

  private final NormElementRepository normElementRepository;

  public SingleNormValidator(NormElementRepository normElementRepository) {
    this.normElementRepository = normElementRepository;
  }

  @Override
  public boolean isValid(String value, ConstraintValidatorContext context) {
    List<NormElement> list = normElementRepository.findAllByDocumentCategoryLabelR();
    System.out.println(list);

    // TODO: implement validation logic

    if (Objects.equals(value, "test")) {
      return false;
    }

    return true;
  }
}
