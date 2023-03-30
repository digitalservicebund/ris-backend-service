package de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.proceedingdecision;

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
@Table("proceeding_decision_link")
public class ProceedingDecisionLinkDTO {
  @Id Long id;
  Long parentDocumentUnitId;
  Long childDocumentUnitId;
}
