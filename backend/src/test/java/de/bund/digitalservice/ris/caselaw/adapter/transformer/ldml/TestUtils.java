package de.bund.digitalservice.ris.caselaw.adapter.transformer.ldml;

import java.time.LocalDate;
import org.xmlunit.diff.ComparisonResult;
import org.xmlunit.diff.ComparisonType;
import org.xmlunit.diff.DifferenceEvaluator;

public class TestUtils {

  public static DifferenceEvaluator ignoreAttributeEvaluator =
      (comparison, outcome) -> {
        if (outcome == ComparisonResult.DIFFERENT
            && comparison.getType() == ComparisonType.ATTR_VALUE) {
          String controlXPath = comparison.getControlDetails().getXPath();
          String testXPath = comparison.getTestDetails().getXPath();

          // Ignore "Übergreifende ID"
          if ("/akomaNtoso[1]/judgment[1]/meta[1]/identification[1]/FRBRWork[1]/FRBRalias[1]/@value"
                  .equals(controlXPath)
              || "/akomaNtoso[1]/judgment[1]/meta[1]/identification[1]/FRBRWork[1]/FRBRalias[1]/@value"
                  .equals(testXPath)) {
            return ComparisonResult.EQUAL;
          }

          // Ignore "XML Transformation"
          if ("/akomaNtoso[1]/judgment[1]/meta[1]/identification[1]/FRBRManifestation[1]/FRBRdate[1]/@date"
                  .equals(controlXPath)
              || "/akomaNtoso[1]/judgment[1]/meta[1]/identification[1]/FRBRManifestation[1]/FRBRdate[1]/@date"
                  .equals(testXPath)) {
            return LocalDate.now().toString().equals(comparison.getTestDetails().getValue())
                ? ComparisonResult.EQUAL
                : ComparisonResult.DIFFERENT;
          }
        }

        return outcome;
      };
}
