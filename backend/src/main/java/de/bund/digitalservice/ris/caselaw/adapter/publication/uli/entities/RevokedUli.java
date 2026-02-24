package de.bund.digitalservice.ris.caselaw.adapter.publication.uli.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;

@Entity
@Immutable
@Table(name = "revoked_uli", schema = "references_schema")
@Getter
@NoArgsConstructor
public class RevokedUli {

  @Id private UUID id;

  @Column(name = "doc_unit_id")
  private UUID docUnitId;

  @Column(name = "revoked_at")
  private Instant revokedAt;
}
