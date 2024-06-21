package de.bund.digitalservice.ris.caselaw.adapter.transformer;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.YearOfDisputeDTO;
import java.time.Year;

public class YearOfDisputeTransformer {

  private YearOfDisputeTransformer() {}

  public static Year transformToDomain(YearOfDisputeDTO dto) {
    return Year.parse(dto.getValue());
  }

  public static YearOfDisputeDTO transformToDTO(Year year, int rank) {
    return YearOfDisputeDTO.builder().value(year.toString()).rank(rank).build();
  }
}
