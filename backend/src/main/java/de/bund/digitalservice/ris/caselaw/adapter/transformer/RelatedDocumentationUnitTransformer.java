package de.bund.digitalservice.ris.caselaw.adapter.transformer;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.CourtDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentTypeDTO;
import de.bund.digitalservice.ris.caselaw.domain.court.Court;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.documenttype.DocumentType;

public class RelatedDocumentationUnitTransformer {
  RelatedDocumentationUnitTransformer() {}

  static String getFileNumber(String fileNumber) {
    if (fileNumber == null) {
      return null;
    }

    return fileNumber;
  }

  static Court getCourtFromDTO(CourtDTO courtDTO) {
    if (courtDTO == null || courtDTO.getType() == null) {
      return null;
    }

    return Court.builder()
        .id(courtDTO.getId())
        .type(courtDTO.getType())
        .location(courtDTO.getLocation())
        .label(Court.generateLabel(courtDTO.getType(), courtDTO.getLocation()))
        .build();
  }

  static CourtDTO getCourtFromDomain(Court court) {
    if (court == null) {
      return null;
    }

    return CourtDTO.builder()
        .id(court.id())
        .type(court.type())
        .location(court.location())
        // Todo isSuperiorCourt, isForeignCourt, additionalInformation, jurisId?
        .build();
  }

  static DocumentType getDocumentTypeFromDTO(DocumentTypeDTO documentTypeDTO) {

    if (documentTypeDTO == null
        || (documentTypeDTO.getLabel() == null && documentTypeDTO.getAbbreviation() == null)) {
      return null;
    }

    return DocumentType.builder()
        .label(documentTypeDTO.getLabel())
        // Todo is this the correct mapping?
        .jurisShortcut(documentTypeDTO.getAbbreviation())
        .build();
  }

  static DocumentTypeDTO getDocumentTypeFromDomain(DocumentType documentType) {
    if (documentType == null
        || (documentType.label() == null && documentType.jurisShortcut() == null)) {
      return null;
    }

    return DocumentTypeDTO.builder()
        .label(documentType.label())
        .abbreviation(documentType.jurisShortcut())
        // Todo do we need superLabel1, superLabel2, multiple from DTO?
        .build();
  }
}
