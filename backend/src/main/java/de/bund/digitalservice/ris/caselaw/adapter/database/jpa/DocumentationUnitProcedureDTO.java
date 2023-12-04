package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import jakarta.persistence.Embeddable;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.EqualsAndHashCode.Include;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@Table(schema = "incremental_migration", name = "documentation_unit_procedure")
public class DocumentationUnitProcedureDTO {

  @EmbeddedId @Include
  private DocumentationUnitProcedureId primaryKey = new DocumentationUnitProcedureId();

  @ManyToOne
  @MapsId("documentationUnitId")
  @JoinColumn(name = "documentation_unit_id")
  private DocumentationUnitDTO documentationUnit;

  @ManyToOne
  @MapsId("procedureId")
  @JoinColumn(name = "procedure_id")
  private ProcedureDTO procedure;

  private int rank;
}

@Embeddable
@NoArgsConstructor
@Getter
@EqualsAndHashCode
class DocumentationUnitProcedureId implements Serializable {
  private UUID documentationUnitId;

  private UUID procedureId;

  public DocumentationUnitProcedureId(UUID documentationUnitId, UUID procedureId) {
    this.documentationUnitId = documentationUnitId;
    this.procedureId = procedureId;
  }
}
