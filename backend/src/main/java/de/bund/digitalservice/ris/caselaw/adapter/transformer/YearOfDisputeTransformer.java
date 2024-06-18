package de.bund.digitalservice.ris.caselaw.adapter.transformer;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.YearOfDisputeDTO;
import de.bund.digitalservice.ris.caselaw.domain.YearOfDispute;
import java.time.Year;
import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class YearOfDisputeTransformer {

  private YearOfDisputeTransformer() {}

  public static Set<YearOfDispute> transformToDomain(Set<YearOfDisputeDTO> yearOfDisputeDTOs) {
    return yearOfDisputeDTOs.stream()
        .sorted(Comparator.comparing(YearOfDisputeDTO::getRank))
        .map(
            yearOfDisputeDTo ->
                YearOfDispute.builder()
                    .id(yearOfDisputeDTo.getId())
                    .year(Year.parse(yearOfDisputeDTo.getValue()))
                    .build())
        .collect(Collectors.toCollection(TreeSet::new));
  }

  public static Set<YearOfDisputeDTO> transformToDTO(Set<YearOfDispute> yearOfDisputeList) {

    return IntStream.range(0, yearOfDisputeList.size())
        .mapToObj(
            index -> {
              var yearsOfDispute = yearOfDisputeList.iterator().next();
              return YearOfDisputeDTO.builder()
                  .id(yearsOfDispute.id())
                  .value(yearsOfDispute.year().toString())
                  .rank(index)
                  .build();
            })
        .collect(Collectors.toSet());
  }
}
