package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import de.bund.digitalservice.ris.caselaw.domain.DuplicateRelationStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
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
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
@Entity
@Table(schema = "incremental_migration", name = "duplicate_relation")
public class DuplicateRelationDTO {

  @EmbeddedId private DuplicateRelationId id;

  @Column(name = "status")
  @Enumerated(EnumType.STRING)
  //  @JdbcType(type = PostgreSQLEnumJdbcType.class)
  private DuplicateRelationStatus status;

  //
  //  @ManyToOne(fetch = FetchType.LAZY)
  //  @JoinColumn(name = "documentation_unit_id1")
  //  private DocumentationUnitDTO documentationUnit1;
  //
  //  @ManyToOne(fetch = FetchType.LAZY)
  //  @JoinColumn(name = "documentation_unit_id2")
  //  private DocumentationUnitDTO documentationUnit2;

  @Getter
  @Setter
  @NoArgsConstructor
  @EqualsAndHashCode
  @Builder(toBuilder = true)
  @Embeddable
  public static class DuplicateRelationId implements Serializable {

    public DuplicateRelationId(UUID documentationUnitId1, UUID documentationUnitId2) {
      // As a SQL constraint, id1 holds the smaller UUID -> That way the duplicate relationship
      // between doc units A and B is always stored as one of (A,B) or (B,A).
      if (documentationUnitId1.compareTo(documentationUnitId2) >= 0) {
        this.documentationUnitId1 = documentationUnitId1;
        this.documentationUnitId2 = documentationUnitId2;
      } else {
        this.documentationUnitId1 = documentationUnitId2;
        this.documentationUnitId2 = documentationUnitId1;
      }
    }

    @Column(name = "documentation_unit_id1")
    private UUID documentationUnitId1;

    @Column(name = "documentation_unit_id2")
    private UUID documentationUnitId2;
  }
}
