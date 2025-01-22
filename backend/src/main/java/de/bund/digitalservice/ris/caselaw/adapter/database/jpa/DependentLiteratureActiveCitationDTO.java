package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
@Table(schema = "incremental_migration", name = "dependent_literature_active_citation")
public class DependentLiteratureActiveCitationDTO {
  @Id private UUID id;

  private String author;

  @NotBlank private String citation;

  @JoinColumn(name = "document_type_id")
  @ManyToOne
  private DocumentTypeDTO documentType;

  @Column(name = "legal_periodical_raw_value")
  @NotNull
  private String legalPeriodicalRawValue;

  @JoinColumn(name = "legal_periodical_id")
  @ManyToOne
  private LegalPeriodicalDTO legalPeriodical;

  @ManyToOne
  @JoinColumn(name = "documentation_unit_id")
  private DocumentationUnitDTO documentationUnit;

  @Column(name = "document_type_raw_value")
  private String documentTypeRawValue;

  private Integer rank;

  @Transient private Integer editionRank;
}
