package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
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
@Builder
@Entity
@Table(name = "active_citation_adm", schema = "incremental_migration")
public class ActiveCitationAdministrativeRegulationDTO {
  @Id
  @Column(name = "id", nullable = false)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "source_id")
  private DocumentationUnitDTO source;

  @Column(name = "target_document_number")
  private String targetDocumentNumber;

  @NotNull
  @Column(name = "target_directive", nullable = false)
  private String targetDirective;

  @Column(name = "target_citation")
  private String targetCitation;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "target_citation_type_id")
  private CitationTypeDTO targetCitationType;

  @Column(name = "target_citation_type_raw")
  private String targetCitationTypeRaw;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "target_legal_periodical_id")
  private LegalPeriodicalDTO targetLegalPeriodical;

  @Column(name = "target_legal_periodical_raw")
  private String targetLegalPeriodicalRaw;

  @NotNull
  @Column(name = "rank", nullable = false)
  private Integer rank;
}
