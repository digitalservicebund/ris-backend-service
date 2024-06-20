package de.bund.digitalservice.ris.caselaw.adapter.transformer;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.YearOfDisputeDTO;
import java.time.Year;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class YearOfDisputeTransformerTest {

  @Test
  void testRankWhenTransformToDTO() {
    List<Year> years = List.of(Year.of(2011), Year.of(2009), Year.of(2017));
    Assertions.assertEquals(
        List.of(1, 2, 3),
        YearOfDisputeTransformer.transformToDTO(years).stream()
            .map(YearOfDisputeDTO::getRank)
            .toList());
  }

  @Test
  void testSortingWhenTransformToDomain() {
    var sortedYears =
        YearOfDisputeTransformer.transformToDomain(
            Set.of(
                YearOfDisputeDTO.builder().value("2011").rank(8).build(),
                YearOfDisputeDTO.builder().value("2009").rank(2).build(),
                YearOfDisputeDTO.builder().value("2000").rank(1).build()));

    Assertions.assertEquals(sortedYears.get(0).getValue(), 2000);
    Assertions.assertEquals(sortedYears.get(1).getValue(), 2009);
    Assertions.assertEquals(sortedYears.get(2).getValue(), 2011);
  }
}
