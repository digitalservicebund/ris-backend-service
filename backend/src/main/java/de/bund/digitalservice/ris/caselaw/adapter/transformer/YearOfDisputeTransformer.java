package de.bund.digitalservice.ris.caselaw.adapter.transformer;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.YearOfDisputeDTO;
import java.time.Year;
import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

public class YearOfDisputeTransformer {

  public static Set<Year> transformToDomain(Set<YearOfDisputeDTO> yearOfDisputeDTOs) {
    return yearOfDisputeDTOs.stream()
        .sorted(Comparator.comparing(YearOfDisputeDTO::getRank))
        .map(yearOfDisputeDTo -> Year.parse(yearOfDisputeDTo.getValue()))
        .collect(Collectors.toCollection(TreeSet::new));
  }
}
