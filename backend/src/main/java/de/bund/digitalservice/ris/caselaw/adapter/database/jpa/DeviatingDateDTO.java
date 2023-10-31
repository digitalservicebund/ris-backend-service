package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
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
public class DeviatingDateDTO {
  @Id @GeneratedValue private UUID id;

  @Column(nullable = false)
  @NotNull
  private LocalDate value;

  //  @Column(name = "documentation_unit_id")
  //  private UUID documentationUnitId;

  @Transient private Long rank;
}

@AllArgsConstructor
@EqualsAndHashCode
class DeviatingDateId implements Serializable {
  private String value;
  private UUID documentationUnitId;
}
