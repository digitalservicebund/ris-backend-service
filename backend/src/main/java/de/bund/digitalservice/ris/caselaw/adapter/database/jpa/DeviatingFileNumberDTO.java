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
@Table(schema = "incremental_migration", name = "deviating_file_number")
public class DeviatingFileNumberDTO {
  @Id @GeneratedValue private UUID id;

  @Column(nullable = false)
  @NotBlank
  private String value;

  @ManyToOne
  @JoinColumn(name = "documentation_unit_id", insertable = false, updatable = false)
  @NotNull
  private DocumentationUnitDTO documentationUnit;

  private Long rank;
}

@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
class DeviatingFileNumberId implements Serializable {
  private String value;
  private UUID documentationUnitId;
}
