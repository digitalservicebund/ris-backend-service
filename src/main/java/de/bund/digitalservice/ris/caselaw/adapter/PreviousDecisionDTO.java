package de.bund.digitalservice.ris.caselaw.adapter;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("previous_decision")
public class PreviousDecisionDTO {
  @Id Long id;

  String courtType;

  String courtLocation;

  String decisionDate;

  String fileNumber;

  String documentNumber;
  Long documentUnitId;
}
