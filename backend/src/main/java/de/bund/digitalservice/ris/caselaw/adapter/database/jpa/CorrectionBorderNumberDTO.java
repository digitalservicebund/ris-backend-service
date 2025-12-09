package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "correction_border_number", schema = "incremental_migration")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CorrectionBorderNumberDTO {
  @Id @GeneratedValue() private UUID id;

  @Column(name = "value")
  private Long borderNumber;

  @Column(name = "rank")
  private Long rank;
}
