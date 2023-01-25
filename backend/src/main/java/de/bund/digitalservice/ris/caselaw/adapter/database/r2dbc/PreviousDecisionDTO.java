package de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Table("previous_decision")
public class PreviousDecisionDTO {
  @Id Long id;

  String courtType;

  String courtLocation;

  Instant decisionDateTimestamp;

  String fileNumber;

  String documentNumber;
  Long documentUnitId;
}
