package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;

@Entity
@Immutable
@Table(name = "ref_view_active_citation_uli_caselaw", schema = "references_schema")
@Getter
@NoArgsConstructor
public class ActiveCitationUliCaselaw {

  @Id
  @Column(name = "id")
  private String id;

  // ULI document
  @Column(name = "source_id")
  private UUID sourceId;

  // caselaw document
  @Column(name = "target_id")
  private UUID targetId;
}
