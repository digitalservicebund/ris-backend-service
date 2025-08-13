package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "history_log_documentation_unit_process_step", schema = "incremental_migration")
public class HistoryLogDocumentationUnitProcessStepDTO {
  @Id
  @GeneratedValue
  @Column(nullable = false)
  private UUID id;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "history_log_id", nullable = false, unique = true)
  private HistoryLogDTO historyLog;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "to_documentation_unit_process_step_id", nullable = false)
  private DocumentationUnitProcessStepDTO toDocumentationUnitProcessStep;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(
      name = "from_documentation_unit_process_step_id",
      nullable = true) // This column is nullable in DB
  private DocumentationUnitProcessStepDTO fromDocumentationUnitProcessStep;
}
