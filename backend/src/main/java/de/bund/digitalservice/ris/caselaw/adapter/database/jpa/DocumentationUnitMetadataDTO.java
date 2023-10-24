package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface DocumentationUnitMetadataDTO {

  UUID getId();

  String getCaseFacts();

  LocalDate getDecisionDate();

  String getDocumentNumber();

  DocumentTypeDTO getDocumentType();

  List<FileNumberDTO> getFileNumbers();

  String getEcli();

  String getJudicialBody();

  String getInputType();
}
