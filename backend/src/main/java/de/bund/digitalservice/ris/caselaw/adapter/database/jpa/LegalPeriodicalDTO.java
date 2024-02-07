package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "legal_periodical", schema = "incremental_migration")
public class LegalPeriodicalDTO {
  @Id @GeneratedValue private UUID id;

  @NotBlank private String abbreviation;
}
