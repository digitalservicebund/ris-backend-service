package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;

@Entity
@Immutable
@Table(name = "ref_view_uli", schema = "references_schema")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class PublishedUli {

  @Id private UUID id;

  @Column(name = "document_number")
  private String documentNumber;

  @Column(name = "author")
  private String author;

  @Column(name = "citation")
  private String citation;

  // document_type_id in global lookup table

  @Column(name = "document_type_raw_value")
  private String documentTypeRawValue; // label

  // legal_periodical_id in global lookup table

  @Column(name = "legal_periodical_raw_value")
  private String legalPeriodicalRawValue; // abbreviation

  @Column(name = "published_at")
  private Instant publishedAt;
}
