package de.bund.digitalservice.ris.caselaw.domain;

import java.time.Year;
import java.util.Optional;
import java.util.UUID;

public interface DocumentNumberRecyclingService {

  Optional<String> addForRecycling(
      UUID documentationOfficeId, String documentNumber, String documentationOfficeAbbreviation);

  Optional<String> findDeletedDocumentNumber(String documentationOfficeAbbreviation, Year year);

  void delete(String documentNumber);
}
