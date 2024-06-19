package de.bund.digitalservice.ris.caselaw.adapter.transformer;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentationUnitDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.YearOfDisputeDTO;
import de.bund.digitalservice.ris.caselaw.domain.CoreData;
import java.time.Year;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class YearOfDisputeTransformer {

  private YearOfDisputeTransformer() {}

  private static List<Year> transformToDomain(Set<YearOfDisputeDTO> yearOfDisputeDTOs) {
    return yearOfDisputeDTOs.stream()
        .sorted(Comparator.comparing(YearOfDisputeDTO::getRank))
        .map(YearOfDisputeDTO::getValue)
        .distinct()
        .map(Year::parse)
        .toList();
  }

  private static Set<YearOfDisputeDTO> transformToDTO(List<Year> yearsOfDispute) {
    if (yearsOfDispute == null || yearsOfDispute.isEmpty()) return Collections.emptySet();

    var uniqueYears = yearsOfDispute.stream().map(Year::toString).distinct().toList();

    Set<YearOfDisputeDTO> yearOfDisputeDTOS = new HashSet<>();

    for (int i = 0; i < uniqueYears.size(); i++) {
      yearOfDisputeDTOS.add(
          YearOfDisputeDTO.builder().value(uniqueYears.get(i)).rank(i + 1).build());
    }
    return yearOfDisputeDTOS;
  }

  static void addYearsOfDisputeToDTO(
      DocumentationUnitDTO.DocumentationUnitDTOBuilder builder, CoreData coreData) {
    var yearsOfDisputeDTOs = transformToDTO(coreData.yearsOfDispute());
    builder.yearsOfDispute(yearsOfDisputeDTOs);
  }

  static void addYearsOfDisputeToDomain(
      DocumentationUnitDTO currentDto, CoreData.CoreDataBuilder coreDataBuilder) {

    coreDataBuilder.yearsOfDispute(
        YearOfDisputeTransformer.transformToDomain(currentDto.getYearsOfDispute()));
  }
}
