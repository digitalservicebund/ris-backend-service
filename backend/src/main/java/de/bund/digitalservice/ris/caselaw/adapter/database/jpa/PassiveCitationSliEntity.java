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
@Table(name = "passive_citation_sli", schema = "incremental_migration")
public class PassiveCitationSliEntity {

  @Id @GeneratedValue @EqualsAndHashCode.Exclude private UUID id;

  @NonNull
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "target_id", nullable = false)
  @EqualsAndHashCode.Exclude
  private DocumentationUnitDTO target;

  @Nullable
  @Column(name = "source_id")
  private UUID sourceId;

  @Nullable
  @Column(name = "source_literature_document_number")
  @Size(max = 255)
  private String sourceDocumentNumber;

  @NotBlank
  @Column(name = "source_author")
  @Size(max = 255)
  private String sourceAuthor;

  @NotBlank
  @Column(name = "source_book_title")
  @Size(max = 1000)
  private String sourceBookTitle;

  @NotBlank
  @Column(name = "source_year_of_publication", nullable = false)
  @Size(max = 255)
  private String sourceYearOfPublication;

  @NotNull
  @Column(name = "rank")
  @EqualsAndHashCode.Exclude
  private Integer rank;
}
