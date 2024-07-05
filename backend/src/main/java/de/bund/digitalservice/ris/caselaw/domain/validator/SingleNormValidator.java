package de.bund.digitalservice.ris.caselaw.domain.validator;

import de.bund.digitalservice.ris.caselaw.domain.NormElement;
import de.bund.digitalservice.ris.caselaw.domain.NormElementRepository;
import de.bund.digitalservice.ris.caselaw.domain.SingleNormValidationInfo;
import de.bund.digitalservice.ris.caselaw.domain.exception.InvalidSingleNormValueException;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.NormCode;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Map;
import java.util.Set;
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

    Map<String, Boolean> labelNumberDesignationMap =
        generateNormAbbreviationLabelNumberDesignationMap(value.normAbbreviation());

    String normalizedString = value.singleNorm().replaceAll("\\p{Z}", " ");

    try {
      validateSingleNormString(normalizedString, labelNumberDesignationMap);
    } catch (InvalidSingleNormValueException e) {
      return false;
    }

    return true;
  }

  private void validateSingleNormString(String s, Map<String, Boolean> labelNumberDesignationMap)
      throws InvalidSingleNormValueException {
    String remainingString = s;

    while (remainingString.length() > 0) {
      String label = longestMatchingPrefix(remainingString, labelNumberDesignationMap.keySet());

      if (label == null) {
        throw new InvalidSingleNormValueException(
            "Expected norm label but found unexpected character sequence.");
      }

      try {
        remainingString = remainingString.substring(label.length());
      } catch (StringIndexOutOfBoundsException e) {
        throw new InvalidSingleNormValueException(
            "Encountered error when trying to strip label from string.", e);
      }

      if (Boolean.TRUE.equals(labelNumberDesignationMap.get(label))) {
        remainingString = stripOneLeadingWhitespace(remainingString, false);

        remainingString =
            stripNumberDesignation(remainingString, labelNumberDesignationMap.keySet());
        remainingString = stripOneLeadingWhitespace(remainingString, true);
      } else {
        if (remainingString.length() > 0) {
          remainingString = stripOneLeadingWhitespace(remainingString, false);
        }
      }
    }
  }

  private Map<String, Boolean> generateNormAbbreviationLabelNumberDesignationMap(
      String normAbbreviation) {
    Stream<NormElement> stream = normElementRepository.findAllByDocumentCategoryLabelR().stream();

    return stream
        .filter(
            normElement ->
                // for "EinigVtr" normAbbreviation, only get EINIGUNGS_VERTRAG codes
                "EinigVtr".equals(normAbbreviation)
                    == NormCode.EINIGUNGS_VERTRAG.name().equals(normElement.normCode()))
        .collect(Collectors.toMap(NormElement::label, NormElement::hasNumberDesignation));
  }

  private String stripOneLeadingWhitespace(String s, boolean relaxed) {
    if (s.startsWith(" ")) {
      return s.substring(1);
    } else {
      if (relaxed) {
        return s;
      } else {
        throw new InvalidSingleNormValueException("Expected whitespace character missing.");
      }
    }
  }

  private String stripNumberDesignation(String s, Set<String> normLabels)
      throws InvalidSingleNormValueException {
    int numberDesignationEndIndex = s.length() - 1;
    for (String nextLabel : normLabels) {
      int labelIndex = s.indexOf(nextLabel);
      if (labelIndex > -1) {
        numberDesignationEndIndex = Integer.min(numberDesignationEndIndex, labelIndex - 1);
      }
    }

    if (numberDesignationEndIndex > -1) {
      try {
        return s.substring(numberDesignationEndIndex + 1);
      } catch (StringIndexOutOfBoundsException e) {
        throw new InvalidSingleNormValueException(
            "Encountered error when trying to strip number designation from string.", e);
      }
    } else {
      throw new InvalidSingleNormValueException(
          "Expected number designation but found a norm label.");
    }
  }

  private String longestMatchingPrefix(String s, Set<String> prefixes) {
    String longestPrefix = null;
    for (String prefix : prefixes) {
      if (s.startsWith(prefix)
          && (longestPrefix == null || longestPrefix.length() < prefix.length())) {
        longestPrefix = prefix;
      }
    }

    return longestPrefix;
  }
}
