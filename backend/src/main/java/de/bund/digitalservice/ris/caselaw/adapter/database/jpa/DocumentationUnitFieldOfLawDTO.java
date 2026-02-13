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
@Table(name = "documentation_unit_field_of_law")
public class DocumentationUnitFieldOfLawDTO {

  @EmbeddedId @Builder.Default
  private DocumentationUnitFieldOfLawId primaryKey = new DocumentationUnitFieldOfLawId();

  @ManyToOne
  @MapsId("documentationUnitId")
  @JoinColumn(name = "documentation_unit_id")
  private DocumentationUnitDTO documentationUnit;

  @ManyToOne
  @MapsId("fieldOfLawId")
  @JoinColumn(name = "field_of_law_id")
  private FieldOfLawDTO fieldOfLaw;

  private int rank;
}

@Embeddable
@NoArgsConstructor
@EqualsAndHashCode
class DocumentationUnitFieldOfLawId implements Serializable {
  private UUID documentationUnitId;

  private UUID fieldOfLawId;

  public DocumentationUnitFieldOfLawId(UUID documentationUnitId, UUID fieldOfLawId) {
    this.documentationUnitId = documentationUnitId;
    this.fieldOfLawId = fieldOfLawId;
  }
}
