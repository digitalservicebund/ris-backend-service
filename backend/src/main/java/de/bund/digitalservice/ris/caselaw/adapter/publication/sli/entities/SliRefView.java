package de.bund.digitalservice.ris.caselaw.adapter.publication.sli.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;

@Entity
@Immutable
@Table(name = "ref_view_sli", schema = "references_schema")
@Getter
@NoArgsConstructor
public class SliRefView {

  @Id private UUID id;

  @Column(name = "document_number")
  private String documentNumber;

  @Column(name = "author")
  private String author;

  @Column(name = "book_title")
  private String bookTitle;

  @Column(name = "year_of_publication")
  private String yearOfPublication;
}
