package de.bund.digitalservice.ris.caselaw.adapter.transformer;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.JPADocumentationOfficeDTO;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationOffice;
import java.util.Optional;

public class DocumentationOfficeTransformer {
  private DocumentationOfficeTransformer() {}

  public static DocumentationOffice transformDTO(JPADocumentationOfficeDTO documentationOfficeDTO) {
    return Optional.ofNullable(documentationOfficeDTO)
        .map(
            dto ->
                DocumentationOffice.builder()
                    .label(dto.getLabel())
                    .abbreviation(dto.getAbbreviation())
                    .build())
        .orElse(null);
  }

  public static JPADocumentationOfficeDTO transform(DocumentationOffice documentationOffice) {
    return Optional.ofNullable(documentationOffice)
        .map(
            domainObject ->
                JPADocumentationOfficeDTO.builder()
                    .label(domainObject.label())
                    .abbreviation(domainObject.abbreviation())
                    .build())
        .orElse(null);
  }
}
