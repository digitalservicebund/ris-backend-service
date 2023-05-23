package de.bund.digitalservice.ris.caselaw.adapter.transformer;

import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.DocumentationOfficeDTO;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationOffice;
import java.util.Optional;

public class DocumentationOfficeTransformer {
  public static DocumentationOffice transformDTO(DocumentationOfficeDTO documentationOfficeDTO) {
    return Optional.ofNullable(documentationOfficeDTO)
        .map(
            dto ->
                DocumentationOffice.builder()
                    .label(dto.getLabel())
                    .abbreviation(dto.getAbbreviation())
                    .build())
        .orElse(null);
  }

  public static DocumentationOfficeDTO transform(DocumentationOffice documentationOffice) {
    return Optional.ofNullable(documentationOffice)
        .map(
            domainObject ->
                DocumentationOfficeDTO.builder()
                    .label(domainObject.label())
                    .abbreviation(domainObject.abbreviation())
                    .build())
        .orElse(null);
  }
}
