package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import com.fasterxml.jackson.annotation.JsonBackReference;
import de.bund.digitalservice.ris.caselaw.domain.DuplicateRelationStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
@Entity
@Table(name = "duplicate_relation")
public class DuplicateRelationDTO {

  @EmbeddedId private DuplicateRelationId id;

  @Column(name = "status")
  @Enumerated(EnumType.STRING)
  @JdbcTypeCode(SqlTypes.NAMED_ENUM)
  private DuplicateRelationStatus relationStatus;

  @ManyToOne(fetch = FetchType.LAZY)
  @MapsId("documentationUnitId1")
  @JoinColumn(name = "documentation_unit_id1")
  @JsonBackReference // Prevent infinite recursion
  private DecisionDTO documentationUnit1;

  @ManyToOne(fetch = FetchType.LAZY)
  @MapsId("documentationUnitId2")
  @JoinColumn(name = "documentation_unit_id2")
  @JsonBackReference // Prevent infinite recursion
  private DecisionDTO documentationUnit2;

  @Getter
  @Setter
  @NoArgsConstructor
  @EqualsAndHashCode
  @Builder(toBuilder = true)
  @Embeddable
  public static class DuplicateRelationId implements Serializable {

    /**
     * As a SQL constraint, id1 holds the smaller UUID -> That way the duplicate relationship
     * between doc units A and B is always stored as one of (A,B) or (B,A).
     */
    public DuplicateRelationId(UUID documentationUnitId1, UUID documentationUnitId2) {
      // Java compares UUIDs numerically (based on their internal binary representation)
      //  whereas PostgresSQL compares UUIDs lexicographically (as strings).
      //  Therefore, it is important to cast them to strings during comparison.
      if (documentationUnitId1.toString().compareTo(documentationUnitId2.toString()) < 0) {
        this.documentationUnitId1 = documentationUnitId1;
        this.documentationUnitId2 = documentationUnitId2;
      } else {
        this.documentationUnitId1 = documentationUnitId2;
        this.documentationUnitId2 = documentationUnitId1;
      }
    }

    private UUID documentationUnitId1;
    private UUID documentationUnitId2;
  }
}
