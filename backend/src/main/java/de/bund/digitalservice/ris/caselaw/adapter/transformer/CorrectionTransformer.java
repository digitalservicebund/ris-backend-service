package de.bund.digitalservice.ris.caselaw.adapter.transformer;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.CorrectionBorderNumberDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.CorrectionDTO;
import de.bund.digitalservice.ris.caselaw.domain.Correction;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

/**
 * This class is responsible for transforming a correction from its domain representation into a
 * database object and back
 */
@Slf4j
public class CorrectionTransformer {
  private CorrectionTransformer() {}

  public static List<CorrectionDTO> transformToDTOs(List<Correction> domainObjects) {
    if (domainObjects == null) {
      return null;
    }

    var dtos = new ArrayList<CorrectionDTO>();
    long nextRank = 1L;
    for (Correction domainObject : domainObjects) {
      dtos.add(transformToDTO(domainObject, nextRank));
      nextRank++;
    }

    return dtos;
  }

  public static CorrectionDTO transformToDTO(Correction domainObject, long rank) {
    if (domainObject == null) {
      return null;
    }

    var builder =
        CorrectionDTO.builder()
            .id(domainObject.id())
            .type(domainObject.type())
            .content(domainObject.content())
            .description(domainObject.description())
            .date(domainObject.date())
            .rank(rank);

    var borderNumbers = domainObject.borderNumbers();
    if (borderNumbers != null && !borderNumbers.isEmpty()) {
      Long nextRank = 1L;
      for (Long borderNumber : borderNumbers) {
        builder.borderNumber(
            CorrectionBorderNumberDTO.builder().borderNumber(borderNumber).rank(nextRank).build());
        nextRank++;
      }
    }

    return builder.build();
  }

  public static Correction transformToDomain(CorrectionDTO dto) {
    if (dto == null) {
      return null;
    }

    return Correction.builder()
        .id(dto.getId())
        .type(dto.getType())
        .content(dto.getContent())
        .description(dto.getDescription())
        .date(dto.getDate())
        .borderNumbers(
            dto.getBorderNumbers().stream()
                .map(CorrectionBorderNumberDTO::getBorderNumber)
                .toList())
        .build();
  }
}
