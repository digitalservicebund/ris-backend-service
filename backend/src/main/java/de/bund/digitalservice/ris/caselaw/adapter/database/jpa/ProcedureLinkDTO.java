package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
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
  @ManyToOne()
  @JoinColumn(name = "procedure_id", referencedColumnName = "id")
  @NotNull
  ProcedureDTO procedureDTO;

  @Id
  @ManyToOne()
  @JoinColumn(name = "documentation_unit_id", referencedColumnName = "id")
  @NotNull
  DocumentationUnitDTO
      documentationUnitDTO; // TODO should this be a jpa projection to a minimal docUnit?

  @Id
  @Column(name = "rank")
  Long rank;
}

@AllArgsConstructor
@EqualsAndHashCode
class ProcedureLinkId implements Serializable {
  private ProcedureDTO procedureDTO;
  private DocumentationUnitDTO documentationUnitDTO;
  private Long rank;
}
