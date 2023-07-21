package de.bund.digitalservice.ris.caselaw.domain.validator;

import de.bund.digitalservice.ris.caselaw.domain.NormElement;
import de.bund.digitalservice.ris.caselaw.domain.NormElementRepository;
import de.bund.digitalservice.ris.caselaw.domain.SingleNormValidationInfo;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.NormCode;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SingleNormValidator
    implements ConstraintValidator<SingleNormConstraint, SingleNormValidationInfo> {

  private final NormElementRepository normElementRepository;

  public SingleNormValidator(NormElementRepository normElementRepository) {
    this.normElementRepository = normElementRepository;
  }

  @Override
  public boolean isValid(SingleNormValidationInfo value, ConstraintValidatorContext context) {
    if (value == null || value.singleNorm() == null) {
      return true;
    }

    Map<String, NormElement> labelMap = generateNormAbbreviationLabelMap(value.normAbbreviation());

    String[] parts = value.singleNorm().replaceAll("\\p{Z}", " ").split(" ");

    if (parts.length == 0) {
      return true;
    }

    boolean lastWasDesignation = false;
    boolean isLabel = false;
    for (int i = parts.length - 1; i >= 0; i--) {
      isLabel = labelMap.containsKey(parts[i]);

      if (isLabel) {
        boolean withDesignation = labelMap.get(parts[i]).hasNumberDesignation();

        if (withDesignation ^ lastWasDesignation) {
          return false;
        }
      }

      lastWasDesignation = !isLabel;
    }

    return isLabel;
  }

  private Map<String, NormElement> generateNormAbbreviationLabelMap(String normAbbreviation) {
    Stream<NormElement> stream = normElementRepository.findAllByDocumentCategoryLabelR().stream();

    return stream
        .filter(
            normElement ->
                // for "EinigVtr" normAbbreviation, only get EINIGUNGS_VERTRAG codes
                "EinigVtr".equals(normAbbreviation)
                    == NormCode.EINIGUNGS_VERTRAG.name().equals(normElement.normCode()))
        .collect(Collectors.toMap(NormElement::label, Function.identity()));
  }
}
