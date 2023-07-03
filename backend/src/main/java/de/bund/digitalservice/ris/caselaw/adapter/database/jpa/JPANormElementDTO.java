package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class JPANormElementDTO {
  @Id UUID id;

  private String label;

  @Column(name = "has_number_designation")
  private boolean hasNumberDesignation;

  @Column(name = "norm_code")
  private String normCode;
}
