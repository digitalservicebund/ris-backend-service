package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.UuidGenerator;
import org.jspecify.annotations.Nullable;

@Getter
@Setter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EqualsAndHashCode
@ToString
@Table(name = "passive_citation_uli", schema = "incremental_migration")
public class PassiveCitationUliDTO {

  @Id @GeneratedValue @UuidGenerator @EqualsAndHashCode.Exclude private UUID id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "target_id", nullable = false)
  @EqualsAndHashCode.Exclude
  private DecisionDTO target;

  @Nullable
  @Size(max = 255)
  @Column(name = "source_literature_document_number")
  private String sourceLiteratureDocumentNumber;

  @Nullable
  @Size(max = 255)
  @Column(name = "source_author")
  private String sourceAuthor;

  @NotBlank
  @Size(max = 255)
  @Column(name = "source_citation")
  private String sourceCitation;

  @Nullable
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "source_document_type_id")
  @EqualsAndHashCode.Exclude
  private DocumentTypeDTO sourceDocumentType;

  @Nullable
  @Size(max = 255)
  @Column(name = "source_document_type_raw_value")
  private String sourceDocumentTypeRawValue;

  @Nullable
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "source_legal_periodical_id")
  @EqualsAndHashCode.Exclude
  private LegalPeriodicalDTO sourceLegalPeriodical;

  @Nullable
  @Size(max = 255)
  @Column(name = "source_legal_periodical_raw_value")
  private String sourceLegalPeriodicalRawValue;

  @ManyToOne
  @Nullable
  @JoinColumn(name = "edition_id")
  private LegalPeriodicalEditionDTO edition;

  @Nullable
  @Column(name = "edition_rank")
  @EqualsAndHashCode.Exclude
  private Integer editionRank;

  @NotNull
  @Column(name = "rank")
  @EqualsAndHashCode.Exclude
  private Integer rank;
}
