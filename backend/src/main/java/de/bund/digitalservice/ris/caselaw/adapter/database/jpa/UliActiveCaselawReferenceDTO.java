package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;

@Entity
@Immutable
@Table(name = "ref_view_active_citation_uli_caselaw", schema = "references_schema")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class UliActiveCaselawReferenceDTO {

  @Id
  @Column(name = "id")
  private UUID id;

  // ULI document
  @ManyToOne()
  @JoinColumn(name = "source_documentation_unit_id")
  private UliDTO source;

  // caselaw document
  @Column(name = "target_documentation_unit_id")
  private UUID targetDocumentationUnitId;
}
