package de.bund.digitalservice.ris.caselaw.domain;

public interface DocumentNumberService {
  String generateNextAvailableDocumentNumber(DocumentationOffice documentationOffice)
      throws DocumentNumberPatternException, DocumentNumberFormatterException;

  String execute(String abbriviation)
      throws DocumentNumberPatternException,
          DocumentationUnitExistsException,
          DocumentNumberFormatterException;

  void assertNotExists(String documentNumber) throws DocumentationUnitExistsException;
}
