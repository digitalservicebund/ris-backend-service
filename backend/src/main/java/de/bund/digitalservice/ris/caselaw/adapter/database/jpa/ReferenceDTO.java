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
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.UuidGenerator;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
@Entity
@Table(name = "reference")
public class ReferenceDTO {

  @Id
  @GeneratedValue
  @UuidGenerator
  @Column(updatable = false, nullable = false)
  private UUID id;

  @ManyToOne
  @NotNull
  @JoinColumn(name = "documentation_unit_id")
  private DocumentationUnitDTO documentationUnit;

  @Column(name = "documentation_unit_rank")
  @NotNull
  private Integer documentationUnitRank;

  @Column @NotBlank private String citation;

  @JoinColumn(name = "legal_periodical_id")
  @ManyToOne
  private LegalPeriodicalDTO legalPeriodical;

  @Column(name = "legal_periodical_raw_value")
  @NotNull
  private String legalPeriodicalRawValue;

  @Column private String type;

  @Column(name = "reference_supplement")
  private String referenceSupplement;

  @Column private String footnote;

  @Column(name = "edition_rank")
  private Integer editionRank;

  @ManyToOne
  @JoinColumn(name = "edition_id")
  private LegalPeriodicalEditionDTO edition;
}
