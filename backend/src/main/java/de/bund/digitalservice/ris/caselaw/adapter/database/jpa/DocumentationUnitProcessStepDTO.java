package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "documentation_unit_process_step", schema = "incremental_migration")
public class DocumentationUnitProcessStepDTO {
  @Id @GeneratedValue private UUID id;

  @Column(name = "user_id")
  private UUID userId;

  @Column(name = "created_at", nullable = false)
  private LocalDateTime createdAt;

  @ManyToOne
  @JoinColumn(name = "process_step_id", nullable = false)
  private ProcessStepDTO processStep;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "documentation_unit_id", nullable = false)
  private DocumentationUnitDTO documentationUnit;
}
