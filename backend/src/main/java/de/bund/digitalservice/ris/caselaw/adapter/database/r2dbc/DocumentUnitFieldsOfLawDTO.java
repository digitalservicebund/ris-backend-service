package de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc;

import lombok.Builder;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Builder
@Table("document_unit_field_of_law")
public record DocumentUnitFieldsOfLawDTO(@Id Long id, Long documentUnitId, Long fieldOfLawId) {}
