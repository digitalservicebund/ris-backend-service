package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "procedure_link")
@IdClass(ProcedureLinkId.class)
public class ProcedureLinkDTO {
  @Id
  @Column(name = "procedure_id")
  @NotNull
  UUID procedureId;

  @Id
  @Column(name = "documentation_unit_id")
  @NotNull
  UUID documentationUnitId;

  @Id
  @Column(name = "rank")
  Long rank;
}

@AllArgsConstructor
@EqualsAndHashCode
class ProcedureLinkId implements Serializable {
  private UUID procedureId;
  private UUID documentationUnitId;
  private Long rank;
}
