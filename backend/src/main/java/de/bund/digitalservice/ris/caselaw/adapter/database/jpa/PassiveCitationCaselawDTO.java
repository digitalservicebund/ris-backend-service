package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "passive_citation_caselaw")
public class PassiveCitationCaselawDTO {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "id", nullable = false)
  @Nullable
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @OnDelete(action = OnDeleteAction.CASCADE)
  @JoinColumn(name = "target_id", nullable = false)
  @Nullable
  private DecisionDTO target;

  @Column(name = "source_document_number")
  @Nullable
  private String sourceDocumentNumber;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "source_court_id")
  @Nullable
  private CourtDTO sourceCourt;

  @Column(name = "source_date")
  @Nullable
  private LocalDate sourceDate;

  @Column(name = "source_file_number")
  @Nullable
  private String sourceFileNumber;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "source_document_type_id")
  @Nullable
  private DocumentTypeDTO sourceDocumentType;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "citation_type_id")
  @Nullable
  private CitationTypeDTO citationType;

  @Column(name = "rank", nullable = false)
  @NonNull
  private Integer rank;
}
