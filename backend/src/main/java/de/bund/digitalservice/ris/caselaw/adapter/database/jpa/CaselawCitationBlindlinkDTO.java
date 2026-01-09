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
import java.time.LocalDate;
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
@Table(name = "caselaw_citation_blindlink", schema = "incremental_migration")
public class CaselawCitationBlindlinkDTO {
  @Id @GeneratedValue private UUID id;

  @NonNull
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(
      name = "source_document_number",
      nullable = false,
      referencedColumnName = "document_number")
  private DecisionDTO sourceDocument;

  @Nullable
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "target_court_id")
  private CourtDTO targetCourt;

  @Nullable
  @Column(name = "target_date")
  private LocalDate targetDate;

  @Nullable
  @Column(name = "target_document_number")
  private String targetDocumentNumber;

  @Nullable
  @Column(name = "target_file_number")
  private String targetFileNumber;

  @Nullable
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "target_document_type_id")
  private DocumentTypeDTO targetDocumentType;

  @NonNull
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "citation_type_id", nullable = false)
  private CitationTypeDTO citationType;

  @Column @NotNull private Integer rank;
}
