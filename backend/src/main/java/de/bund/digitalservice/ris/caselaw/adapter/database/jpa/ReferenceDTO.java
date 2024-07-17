package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** A reference to a legal periodical (Fundstelle). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(schema = "incremental_migration", name = "reference")
public class ReferenceDTO {
  @Id @GeneratedValue private UUID id;

  @ManyToOne
  @JoinColumn(name = "documentation_unit_id")
  private DocumentationUnitDTO documentationUnit;

  private Integer rank;

  @NotBlank private String citation;

  // Klammerzusatz
  @Column(name = "reference_supplement")
  private String referenceSupplement;

  private String footnote;

  @JoinColumn(name = "legal_periodical_id")
  @ManyToOne
  private LegalPeriodicalDTO legalPeriodical;

  @Column(name = "legal_periodical_raw_value")
  @NotNull
  private String legalPeriodicalRawValue;
}
