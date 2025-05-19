package de.bund.digitalservice.ris.caselaw.domain;

public interface TransformationService {

  void getDataFromEurlex(String celexNumber, DocumentationUnit documentationUnit, User user);
}
