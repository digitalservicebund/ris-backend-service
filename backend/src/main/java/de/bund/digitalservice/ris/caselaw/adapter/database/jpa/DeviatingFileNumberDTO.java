package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.NotBlank;
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
// @IdClass(DeviatingFileNumberId.class)
public class DeviatingFileNumberDTO {
  @Id @GeneratedValue private UUID id;

  @Column(nullable = false)
  @NotBlank
  //  @Id
  private String value;

  //  @Column(name = "documentation_unit_id")
  //  @Id
  //  private UUID documentationUnitId;

  //  @ManyToOne
  //  @Column(name = "documentation_unit")
  //  @NotNull
  //  private DocumentationUnitDTO documentationUnit;

  @Transient private Long rank;

  public DeviatingFileNumberDTO(String value) {
    this.value = value;
  }
}

@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
class DeviatingFileNumberId implements Serializable {
  private String value;
  private UUID documentationUnitId;
}
