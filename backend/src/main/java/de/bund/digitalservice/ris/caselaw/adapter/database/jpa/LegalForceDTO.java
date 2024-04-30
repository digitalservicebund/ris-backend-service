package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Entity
@Table(name = "legal_force", schema = "incremental_migration")
public class LegalForceDTO {
  @Id @GeneratedValue private UUID id;

  @ManyToOne
  @JoinColumn(name = "legal_force_type_id", updatable = false)
  private LegalForceTypeDTO legalForceType;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "norm_abbreviation_id", updatable = false, insertable = false)
  private NormAbbreviationDTO normAbbreviation;

  @Column(name = "norm_abbreviation_raw_value", updatable = false, insertable = false)
  @Size(max = 255)
  private String normAbbreviationRawValue;

  @Column(name = "single_norm", updatable = false, insertable = false)
  @Size(max = 255)
  private String singleNorm;

  @Column(name = "date_of_version", updatable = false, insertable = false)
  private LocalDate dateOfVersion;

  @Column(name = "date_of_relevance", updatable = false, insertable = false)
  @Size(max = 4)
  private String dateOfRelevance;

  @ManyToOne
  @JoinColumn(name = "region_id", updatable = false)
  private RegionDTO region;

  @ManyToOne
  @JoinColumn(name = "documentation_unit_id", updatable = false, insertable = false)
  private DocumentationUnitDTO documentationUnit;
}
