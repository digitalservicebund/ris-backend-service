package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "process_step_documentation_office", schema = "incremental_migration")
public class ProcessStepDocumentationOfficeDTO {
  @Id private UUID id;

  @Column(name = "process_step_id", nullable = false)
  private UUID processStepId;

  @Column(name = "rank", nullable = false)
  private Integer rank;

  @Column(name = "documentation_office_id", nullable = false)
  private UUID documentationOfficeId;
}
