package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import jakarta.persistence.Embeddable;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "documentation_unit_keyword")
public class DocumentationUnitKeywordDTO {

  @EmbeddedId @Builder.Default
  private DocumentationUnitKeywordId primaryKey = new DocumentationUnitKeywordId();

  @ManyToOne
  @MapsId("documentationUnitId")
  @JoinColumn(name = "documentation_unit_id")
  private DocumentationUnitDTO documentationUnit;

  @ManyToOne
  @MapsId("keywordId")
  @JoinColumn(name = "keyword_id")
  private KeywordDTO keyword;

  private int rank;
}

@Embeddable
@NoArgsConstructor
@EqualsAndHashCode
class DocumentationUnitKeywordId implements Serializable {
  private UUID documentationUnitId;

  private UUID keywordId;

  public DocumentationUnitKeywordId(UUID documentationUnitId, UUID keywordId) {
    this.documentationUnitId = documentationUnitId;
    this.keywordId = keywordId;
  }
}
