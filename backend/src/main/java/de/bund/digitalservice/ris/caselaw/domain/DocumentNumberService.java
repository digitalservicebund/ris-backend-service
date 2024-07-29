package de.bund.digitalservice.ris.caselaw.domain;

import de.bund.digitalservice.ris.caselaw.domain.exception.DocumentNumberFormatterException;
import de.bund.digitalservice.ris.caselaw.domain.exception.DocumentNumberPatternException;
import de.bund.digitalservice.ris.caselaw.domain.exception.DocumentationUnitExistsException;

public interface DocumentNumberService {
  String generateDocumentNumber(String documentationOfficeAbbreviation)
      throws DocumentNumberPatternException,
          DocumentationUnitExistsException,
          DocumentNumberFormatterException;

  void assertNotExists(String documentNumber) throws DocumentationUnitExistsException;
}
