package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDate;
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
@Table(schema = "incremental_migration", name = "deviating_date")
@IdClass(DeviatingDateId.class)
public class DeviatingDateDTO {

  @Column(nullable = false)
  @NotNull
  @Id
  private LocalDate value;

  @Column(name = "documentation_unit_id")
  @Id
  private UUID documentationUnitId;

  private Long rank;

  public DeviatingDateDTO(LocalDate value) {
    this.value = value;
  }
}

@AllArgsConstructor
@EqualsAndHashCode
class DeviatingDateId implements Serializable {
  private String value;
  private UUID documentationUnitId;
}
