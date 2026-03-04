package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Immutable;

@Entity
@Immutable
@Table(name = "ref_view_sli", schema = "references_schema")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SliDTO {

  @Id private UUID id;

  @Column(name = "document_number")
  private String documentNumber;

  @Column(name = "author")
  private String author;

  @Column(name = "book_title")
  private String bookTitle;

  @Column(name = "year_of_publication")
  private String yearOfPublication;

  @Column(name = "published_at")
  private Instant publishedAt;

  @OneToMany(mappedBy = "source")
  private List<SliActiveCaselawReferenceDTO> activeCaselawReferences;
}
