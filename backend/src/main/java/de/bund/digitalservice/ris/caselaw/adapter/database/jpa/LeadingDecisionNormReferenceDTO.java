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
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "leading_decision_norm_reference", schema = "incremental_migration")
public class LeadingDecisionNormReferenceDTO {

  @EmbeddedId @Builder.Default
  private DocumentationUnitLeadingDecisionNormReferenceId primaryKey =
      new DocumentationUnitLeadingDecisionNormReferenceId();

  @ManyToOne
  @MapsId("documentationUnitId")
  @JoinColumn(name = "documentation_unit_id")
  private DocumentationUnitDTO documentationUnit;

  @ManyToOne
  @MapsId("normReferenceId")
  @JoinColumn(name = "norm_reference")
  private String normReference;

  private int rank;
}

@Embeddable
@NoArgsConstructor
@EqualsAndHashCode
class DocumentationUnitLeadingDecisionNormReferenceId implements Serializable {
  private UUID documentationUnitId;
  private String normReferenceId;

  public DocumentationUnitLeadingDecisionNormReferenceId(
      UUID documentationUnitId, String normReference) {
    this.documentationUnitId = documentationUnitId;
    this.normReferenceId = normReference;
  }
}
