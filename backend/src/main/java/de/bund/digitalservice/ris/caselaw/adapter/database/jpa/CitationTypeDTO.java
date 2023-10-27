package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
// TODO @Table ?
@Entity
public class CitationTypeDTO {
  @Id @GeneratedValue private UUID id;

  @Column @NotBlank private String abbreviation;

  @Column @NotBlank private String label;

  @ManyToOne @NotNull private DocumentCategoryDTO documentationUnitDocumentCategory;

  @ManyToOne @NotNull private DocumentCategoryDTO citationDocumentCategory;

  @Column(nullable = false, unique = true, updatable = false)
  @ToString.Include
  @NotNull
  private Integer jurisId;
}
