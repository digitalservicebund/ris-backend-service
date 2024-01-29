package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import jakarta.persistence.Embeddable;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import java.io.Serializable;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(schema = "incremental_migration", name = "documentation_unit_procedure")
public class DocumentationUnitProcedureDTO {

  @EmbeddedId @Builder.Default
  private DocumentationUnitProcedureId primaryKey = new DocumentationUnitProcedureId();

  @ManyToOne
  @MapsId("documentationUnitId")
  @JoinColumn(name = "documentation_unit_id")
  private DocumentationUnitDTO documentationUnit;

  @ManyToOne
  @MapsId("procedureId")
  @JoinColumn(name = "procedure_id")
  private ProcedureDTO procedure;

  @Transient
  public int getRank() {
    return primaryKey.getRank();
  }

  @Transient
  public void setRank(int rank) {
    primaryKey.setRank(rank);
  }
}

@Embeddable
@NoArgsConstructor
@Getter
@EqualsAndHashCode
class DocumentationUnitProcedureId implements Serializable {
  private UUID documentationUnitId;

  private UUID procedureId;
  @Setter private int rank;

  public DocumentationUnitProcedureId(UUID documentationUnitId, UUID procedureId) {
    this.documentationUnitId = documentationUnitId;
    this.procedureId = procedureId;
  }
}
