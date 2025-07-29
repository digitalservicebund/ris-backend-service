package de.bund.digitalservice.ris.caselaw.adapter.transformer;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentationOfficeDTO;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationOffice;
import java.util.Optional;

public class DocumentationOfficeTransformer {
  private DocumentationOfficeTransformer() {}

  public static DocumentationOffice transformToDomain(
      DocumentationOfficeDTO documentationOfficeDTO) {
    return Optional.ofNullable(documentationOfficeDTO)
        .map(
            dto ->
                DocumentationOffice.builder()
                    .id(dto.getId())
                    .abbreviation(dto.getAbbreviation())
                    .build())
        .orElse(null);
  }

  public static DocumentationOfficeDTO transformToDTO(DocumentationOffice documentationOffice) {
    return Optional.ofNullable(documentationOffice)
        .map(
            domain ->
                DocumentationOfficeDTO.builder()
                    .id(domain.id())
                    .abbreviation(domain.abbreviation())
                    .build())
        .orElse(null);
  }
}
