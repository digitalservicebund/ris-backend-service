package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(schema = "incremental_migration", name = "deviating_document_number")
public class DeviatingDocumentNumberDTO {
  @Id @GeneratedValue private UUID id;

  @Column(nullable = false)
  @Size(max = 255)
  @NotBlank
  private String value;

  private Long rank;

  @ManyToOne(optional = false)
  @JoinColumn(name = "documentation_unit_id", nullable = false)
  private DocumentationUnitDTO documentationUnit;
}
