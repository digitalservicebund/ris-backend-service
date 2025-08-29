package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
@EqualsAndHashCode(callSuper = true)
@ToString(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "pending_proceeding", schema = "incremental_migration")
@SuppressWarnings(
    "java:S6539") // This class depends on many classes, because it's the key part and merging
// everything.
public class PendingProceedingDTO extends DocumentationUnitDTO {

  // Erledigungsvermerk
  @Column(name = "resolution_note")
  private String resolutionNote;

  // Erledigung
  @Column(name = "is_resolved")
  boolean isResolved;

  // Rechtsfrage
  @Column(name = "legal_issue")
  String legalIssue;

  // Rechtsmittelzulassung
  @Column(name = "admission_of_appeal")
  String admissionOfAppeal;

  // Rechtsmittelführer
  @Column String appellant;

  // Erledigungsmitteilung
  @Column(name = "resolution_date")
  LocalDate resolutionDate;

  // TODO: Passivzitierung Verwaltungsvorschriften

  @Override
  public String getHeadnote() {
    return "";
  }

  @Override
  public String getGuidingPrinciple() {
    return "";
  }

  @Override
  public LocalDateTime getLastHandoverDateTime() {
    return null;
  }

  @Override
  public LocalDateTime getScheduledPublicationDateTime() {
    return null;
  }

  @Override
  public DocumentationOfficeDTO getCreatingDocumentationOffice() {
    return null;
  }

  @Override
  public String getNote() {
    return null;
  }

  @Override
  public List<ProcedureDTO> getProcedureHistory() {
    return List.of();
  }

  @Override
  public ProcedureDTO getProcedure() {
    return null;
  }

  @Override
  public List<SourceDTO> getSource() {
    return List.of();
  }
}
