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

    Stream<NormElement> stream = normElementRepository.findAllByDocumentCategoryLabelR().stream();
    if ("EinigVtr".equals(value.normAbbreviation())) {
      stream =
          stream.filter(
              normElement -> NormCode.EINIGUNGS_VERTRAG.name().equals(normElement.normCode()));
    } else {
      stream =
          stream.filter(
              normElement -> !NormCode.EINIGUNGS_VERTRAG.name().equals(normElement.normCode()));
    }
    Map<String, NormElement> labelMap =
        stream.collect(Collectors.toMap(NormElement::label, Function.identity()));

    String[] parts = value.singleNorm().split(" ");

    if (parts.length == 0) {
      return true;
    }

    boolean nextEntryIsDesignation = false;
    for (String part : parts) {
      if (labelMap.containsKey(part)) {
        if (nextEntryIsDesignation) {
          return false;
        } else {
          nextEntryIsDesignation = labelMap.get(part).hasNumberDesignation();
        }
      } else if (!nextEntryIsDesignation) {
        return false;
      } else {
        nextEntryIsDesignation = false;
      }
    }

    return !nextEntryIsDesignation;
  }
}
