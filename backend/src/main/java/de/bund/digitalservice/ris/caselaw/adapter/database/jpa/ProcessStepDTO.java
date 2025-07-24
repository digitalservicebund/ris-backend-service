package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import de.bund.digitalservice.ris.caselaw.domain.ProcessStepName;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "process_step", schema = "incremental_migration")
public class ProcessStepDTO {
  @Id private UUID id;

  @Column(name = "name", nullable = false, unique = true)
  private ProcessStepName name;

  @Column(name = "abbreviation", nullable = false, unique = true)
  private String abbreviation;
}
