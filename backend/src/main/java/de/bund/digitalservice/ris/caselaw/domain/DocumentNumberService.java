package de.bund.digitalservice.ris.caselaw.domain;

public interface DocumentNumberService {
  String execute(String documentationOfficeAbbreviation, int maxTries)
      throws DocumentationUnitExistsException,
          DocumentNumberPatternException,
          DocumentNumberFormatterException;

  String execute(String documentationOfficeAbbreviation)
      throws DocumentNumberPatternException,
          DocumentationUnitExistsException,
          DocumentNumberFormatterException;

  void assertNotExists(String documentNumber) throws DocumentationUnitExistsException;
}
