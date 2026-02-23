package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "active_citation_caselaw", schema = "incremental_migration")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class ActiveCitationCaselawDTO {
  @Id
  @Column(name = "id", nullable = false)
  @Nullable
  @AssignedIdOrUuid
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "source_id", nullable = false)
  @Nullable
  private DecisionDTO source;

  @Column(name = "target_document_number")
  @Nullable
  @EqualsAndHashCode.Include
  private String targetDocumentNumber;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "target_court_id")
  @Nullable
  @EqualsAndHashCode.Include
  private CourtDTO targetCourt;

  @Column(name = "target_date")
  @Nullable
  @EqualsAndHashCode.Include
  private LocalDate targetDate;

  @Column(name = "target_file_number")
  @Nullable
  @EqualsAndHashCode.Include
  private String targetFileNumber;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "target_document_type_id")
  @Nullable
  @EqualsAndHashCode.Include
  private DocumentTypeDTO targetDocumentType;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "citation_type_id")
  @Nullable
  @EqualsAndHashCode.Include
  private CitationTypeDTO citationType;

  @Column(name = "rank", nullable = false)
  @NonNull
  private Integer rank;
}
