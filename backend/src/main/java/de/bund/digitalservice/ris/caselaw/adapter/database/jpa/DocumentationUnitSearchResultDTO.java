package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface DocumentationUnitSearchResultDTO {
  UUID getId();

  String getDocumentNumber();

  CourtDTO getCourt();

  List<FileNumberDTO> getFileNumbers();

  OriginalFileDocumentDTO getOriginalFileDocument();

  LocalDate getDecisionDate();

  DocumentTypeDTO getDocumentType();

  List<StatusDTO> getStatus();
}
