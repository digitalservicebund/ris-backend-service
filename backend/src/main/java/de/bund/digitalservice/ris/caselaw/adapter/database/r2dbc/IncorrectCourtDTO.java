package de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc;

import lombok.Builder;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Builder(toBuilder = true)
@Table("incorrect_court")
public record IncorrectCourtDTO(@Id Long id, Long documentUnitId, String court) {}
