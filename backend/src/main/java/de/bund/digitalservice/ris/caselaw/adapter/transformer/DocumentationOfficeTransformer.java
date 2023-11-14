package de.bund.digitalservice.ris.caselaw.adapter.transformer;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentationOfficeDTO;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationOffice;
import java.util.Optional;

public class DocumentationOfficeTransformer {
  private DocumentationOfficeTransformer() {}

  public static DocumentationOffice transformDTO(DocumentationOfficeDTO documentationOfficeDTO) {
    return Optional.ofNullable(documentationOfficeDTO)
        .map(dto -> DocumentationOffice.builder().abbreviation(dto.getAbbreviation()).build())
        .orElse(null);
  }

  public static DocumentationOfficeDTO transform(DocumentationOffice documentationOffice) {
    return Optional.ofNullable(documentationOffice)
        .map(
            domainObject ->
                DocumentationOfficeDTO.builder().abbreviation(domainObject.abbreviation()).build())
        .orElse(null);
  }
}
