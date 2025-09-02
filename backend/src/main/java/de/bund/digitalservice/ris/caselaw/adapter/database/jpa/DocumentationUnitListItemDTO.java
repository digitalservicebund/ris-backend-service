package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/** An interface representing a documentation unit with reduced information to be shown in a list */
public interface DocumentationUnitListItemDTO {
  UUID getId();

  String getDocumentNumber();

  String getJudicialBody();

  String getHeadnote();

  String getGuidingPrinciple();

  CourtDTO getCourt();

  List<FileNumberDTO> getFileNumbers();

  List<AttachmentDTO> getAttachments();

  LocalDate getDate();

  LocalDate getResolutionDate();

  ManagementDataDTO getManagementData();

  LocalDateTime getLastHandoverDateTime();

  LocalDateTime getScheduledPublicationDateTime();

  DocumentTypeDTO getDocumentType();

  StatusDTO getStatus();

  DocumentationOfficeDTO getDocumentationOffice();

  DocumentationOfficeDTO getCreatingDocumentationOffice();

  String getNote();

  List<ProcedureDTO> getProcedureHistory();

  ProcedureDTO getProcedure();

  List<SourceDTO> getSource();

  List<DocumentationUnitProcessStepDTO> getProcessSteps();
}
