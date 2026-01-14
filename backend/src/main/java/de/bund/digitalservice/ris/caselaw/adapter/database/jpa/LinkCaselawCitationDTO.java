package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

@Getter
@Setter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "citation_caselaw_link", schema = "incremental_migration")
public non-sealed class LinkCaselawCitationDTO implements ActiveCaselawCitationDTO {
  @Id
  // @GeneratedValue -- we currently manage the id manually to keep it in sync with
  // related_documentation
  private UUID id;

  @NonNull
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "source_id", nullable = false)
  private DecisionDTO sourceDocument;

  @NonNull
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "target_id", nullable = false)
  private DecisionDTO targetDocument;

  @Nullable
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "citation_type_id", nullable = false)
  private CitationTypeDTO citationType;

  @Column @NonNull private Integer rank;
}
