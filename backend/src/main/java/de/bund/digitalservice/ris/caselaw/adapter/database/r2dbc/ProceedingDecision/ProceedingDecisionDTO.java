package de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.ProceedingDecision;

import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.FileNumberDTO;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Table("doc_unit")
public class ProceedingDecisionDTO {
  @Id Long id;
  UUID uuid;

  @Column("gerichtstyp")
  String courtType;

  @Column("gerichtssitz")
  String courtLocation;

  @Column("decision_date")
  Instant decisionDate;

  @Transient String fileNumber;
}
