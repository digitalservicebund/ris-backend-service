package de.bund.digitalservice.ris.caselaw.domain;

public interface TransformationService {

  void getDataFromEurlex(String celexNumber, Decision decision, User user);
}
