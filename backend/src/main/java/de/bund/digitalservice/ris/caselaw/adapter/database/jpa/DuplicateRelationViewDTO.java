package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
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
@Table(schema = "incremental_migration", name = "duplicate_relation_view")
public class DuplicateRelationViewDTO {

  @EmbeddedId private DuplicateRelationId id;

  @Column(name = "reason")
  private String reason;

  @Getter
  @Setter
  @AllArgsConstructor
  @NoArgsConstructor
  @EqualsAndHashCode
  @Builder(toBuilder = true)
  @Embeddable
  public static class DuplicateRelationId implements Serializable {
    private UUID id_a; // NOSONAR: Should it be document_unit_id1?
    private UUID id_b; // NOSONAR: Unclear if same name would be clearer
  }
}
