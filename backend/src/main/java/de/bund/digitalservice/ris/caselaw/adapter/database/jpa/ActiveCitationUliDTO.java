package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
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
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

@Getter
@Setter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EqualsAndHashCode
@ToString
@Table(name = "active_citation_uli", schema = "incremental_migration")
public class ActiveCitationUliDTO {

  @Id @GeneratedValue @EqualsAndHashCode.Exclude private UUID id;

  @NonNull
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "source_id", nullable = false)
  @EqualsAndHashCode.Exclude
  private DecisionDTO source;

  @Nullable
  @Column(name = "target_literature_document_number")
  @Size(max = 255)
  private String targetLiteratureDocumentNumber;

  @Nullable
  @Column(name = "target_author")
  @Size(max = 255)
  private String targetAuthor;

  @NonNull
  @Column(name = "target_citation", columnDefinition = "TEXT")
  @Size(max = 255)
  private String targetCitation;

  @Nullable
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "target_legal_periodical_id")
  @EqualsAndHashCode.Exclude
  private LegalPeriodicalDTO targetLegalPeriodical;

  @Nullable
  @Column(name = "target_legal_periodical_raw_value")
  @Size(max = 255)
  private String targetLegalPeriodicalRawValue;

  @Column @NotNull @EqualsAndHashCode.Exclude private Integer rank;
}
