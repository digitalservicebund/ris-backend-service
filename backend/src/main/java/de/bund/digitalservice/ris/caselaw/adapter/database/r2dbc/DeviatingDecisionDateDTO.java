package de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc;

import java.time.Instant;
import lombok.Builder;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Builder(toBuilder = true)
@Table("deviating_decision_date")
public record DeviatingDecisionDateDTO(@Id Long id, Long documentUnitId, Instant decisionDate) {}
