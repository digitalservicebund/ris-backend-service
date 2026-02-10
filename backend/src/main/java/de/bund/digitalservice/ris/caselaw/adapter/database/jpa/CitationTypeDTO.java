package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "citation_type")
@Entity
public class CitationTypeDTO {
  @Id @GeneratedValue private UUID id;

  @Column @NotBlank private String abbreviation;

  @Column @NotBlank private String label;

  @ManyToOne
  @JoinColumn(name = "documentation_unit_document_category_id")
  @NotNull
  private DocumentCategoryDTO documentationUnitDocumentCategory;

  @ManyToOne
  @JoinColumn(name = "citation_document_category_id")
  @NotNull
  private DocumentCategoryDTO citationDocumentCategory;
}
