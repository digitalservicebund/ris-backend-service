package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "procedure_link")
public class JPAProcedureLinkDTO {
  @Id @GeneratedValue private UUID id;

  @Column(name = "created_at", insertable = false, updatable = false)
  private Instant createdAt;

  @Column(name = "documentation_unit_id")
  UUID documentationUnitId;

  @ManyToOne()
  @JoinColumn(name = "procedure_id", referencedColumnName = "id")
  JPAProcedureDTO procedureDTO;
}
