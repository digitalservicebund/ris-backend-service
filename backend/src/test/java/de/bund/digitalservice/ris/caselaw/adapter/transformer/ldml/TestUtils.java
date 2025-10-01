package de.bund.digitalservice.ris.caselaw.adapter.transformer.ldml;

import org.xmlunit.diff.ComparisonResult;
import org.xmlunit.diff.ComparisonType;
import org.xmlunit.diff.DifferenceEvaluator;

public class TestUtils {

  public static DifferenceEvaluator ignoreIdAttributeEvaluator =
      (comparison, outcome) -> {
        if (outcome == ComparisonResult.DIFFERENT
            && comparison.getType() == ComparisonType.ATTR_VALUE
            && ("/akomaNtoso[1]/judgment[1]/meta[1]/identification[1]/FRBRWork[1]/FRBRalias[1]/@value"
                    .equals(comparison.getControlDetails().getXPath())
                || "/akomaNtoso[1]/judgment[1]/meta[1]/identification[1]/FRBRWork[1]/FRBRalias[1]/@value"
                    .equals(comparison.getTestDetails().getXPath()))) {
          return ComparisonResult.EQUAL;
        }

        return outcome;
      };
}
