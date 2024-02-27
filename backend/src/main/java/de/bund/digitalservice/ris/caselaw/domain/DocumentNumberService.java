package de.bund.digitalservice.ris.caselaw.domain;

public interface DocumentNumberService {
  String generateDocumentNumber(String documentationOfficeAbbreviation, int maxTries)
      throws DocumentationUnitExistsException,
          DocumentNumberPatternException,
          DocumentNumberFormatterException;

  String generateDocumentNumber(String documentationOfficeAbbreviation)
      throws DocumentNumberPatternException,
          DocumentationUnitExistsException,
          DocumentNumberFormatterException;

  void assertNotExists(String documentNumber) throws DocumentationUnitExistsException;
}
