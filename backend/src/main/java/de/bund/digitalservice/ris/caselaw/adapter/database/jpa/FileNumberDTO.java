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
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "file_number", schema = "incremental_migration")
public class FileNumberDTO {

  @Id @GeneratedValue private UUID id;

  @Column(nullable = false)
  @NotBlank
  private String value;

  @ManyToOne
  @JoinColumn(name = "documentation_unit_id", nullable = false)
  @NotNull
  private DocumentationUnitDTO documentationUnit;

  private Long rank;
}
