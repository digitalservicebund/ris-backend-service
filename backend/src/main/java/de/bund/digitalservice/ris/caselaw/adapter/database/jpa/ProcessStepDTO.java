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
@Entity
@Builder(toBuilder = true)
@Table(name = "process_step")
public class ProcessStepDTO {
  @Id private UUID id;

  @Column(name = "name", nullable = false, unique = true)
  private String name;

  @Column(name = "abbreviation", nullable = false, unique = true)
  private String abbreviation;
}
