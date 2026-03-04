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
@Table(name = "passive_citation_adm", schema = "incremental_migration")
public class PassiveCitationAdministrativeRegultationDTO {
  @Id
  @Column(name = "id", nullable = false)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "target_id")
  private DocumentationUnitDTO target;

  @Column(name = "source_document_number")
  private String sourceDocumentNumber;

  @NotNull
  @Column(name = "source_directive", nullable = false)
  private String sourceDirective;

  @Column(name = "citation")
  private String citation;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "citation_type_id")
  private CitationTypeDTO citationType;

  @Column(name = "citation_type_raw")
  private String citationTypeRaw;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "legal_periodical_id")
  private LegalPeriodicalDTO legalPeriodical;

  @Column(name = "legal_periodical_raw")
  private String legalPeriodicalRaw;

  @NotNull
  @Column(name = "rank", nullable = false)
  private Integer rank;
}
