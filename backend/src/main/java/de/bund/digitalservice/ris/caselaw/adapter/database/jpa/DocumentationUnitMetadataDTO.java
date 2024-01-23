package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface DocumentationUnitMetadataDTO {

  UUID getId();

  LocalDate getDecisionDate();

  String getDocumentNumber();

  DocumentTypeDTO getDocumentType();

  List<FileNumberDTO> getFileNumbers();

  String getEcli();

  String getJudicialBody();

  List<InputTypeDTO> getInputTypes();

  CourtDTO getCourt();

  OriginalFileDocumentDTO getOriginalFileDocument();

  DocumentationOfficeDTO getDocumentationOffice();

  List<StatusDTO> getStatus();
}
