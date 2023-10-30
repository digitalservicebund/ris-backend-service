package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
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
@Table(schema = "incremental_migration", name = "deviating_court")
@IdClass(DeviatingCourtId.class)
public class DeviatingCourtDTO {

  @Column(nullable = false)
  @NotBlank
  @Id
  private String value;

  @Column(name = "documentation_unit_id")
  @Id
  private UUID documentationUnitId;

  private Long rank;

  public DeviatingCourtDTO(String value) {
    this.value = value;
  }
}

@AllArgsConstructor
@EqualsAndHashCode
class DeviatingCourtId implements Serializable {
  private String value;
  private UUID documentationUnitId;
}
