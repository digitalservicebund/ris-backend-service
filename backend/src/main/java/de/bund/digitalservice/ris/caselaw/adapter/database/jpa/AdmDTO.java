package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Immutable;

@Getter
@Setter
@Entity
@Immutable
@Table(name = "ref_view_adm", schema = "references_schema")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AdmDTO {
  @Id
  @Column(name = "id")
  private UUID id;

  @Column(name = "document_number")
  private String documentNumber;

  @Column(name = "juris_abbreviation")
  private String jurisAbbreviation;

  @Column(name = "published_at")
  private Instant publishedAt;

  @OneToMany(mappedBy = "source", fetch = FetchType.EAGER)
  private List<AdmActiveCaselawReferenceDTO> activeCaselawReferences;
}
