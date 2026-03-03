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

@Getter
@Entity
@Immutable
@Table(name = "ref_view_active_reference_adm_caselaw", schema = "references_schema")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AdmActiveCaselawReferenceDTO {
  @Id
  @Column(name = "id")
  private UUID id;

  @ManyToOne()
  @JoinColumn(name = "source_documentation_unit_id")
  private AdmDTO source;

  @Column(name = "target_documentation_unit_id")
  private UUID targetDocumentationUnitId;

  @Column(name = "citation_type")
  private String citationType;
}
