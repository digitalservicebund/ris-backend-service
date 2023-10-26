package de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NormElementDTO {
  @Id UUID id;

  private String label;

  @Column("has_number_designation")
  private boolean hasNumberDesignation;

  @Column("norm_code")
  private String normCode;
}
