package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "documentation_unit_process_step")
public class DocumentationUnitProcessStepDTO {
  @Id private UUID id;

  @Column(name = "user_id")
  private UUID userId; // New field from table schema

  @Column(name = "created_at", nullable = false) // Matches table, not started_at
  private LocalDateTime createdAt;

  @Column(name = "process_step_id", nullable = false)
  private UUID processStepId;

  @Column(name = "documentation_unit_id", nullable = false)
  private UUID documentationUnitId;
}
