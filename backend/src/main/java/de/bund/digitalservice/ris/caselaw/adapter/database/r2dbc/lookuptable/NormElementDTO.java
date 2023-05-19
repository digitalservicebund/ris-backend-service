package de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.lookuptable;

import de.bund.digitalservice.ris.caselaw.domain.lookuptable.NormCode;
import java.util.UUID;
import org.springframework.data.relational.core.mapping.Table;

@Table("norm_element")
public record NormElementDTO(
    UUID id, String Label, boolean hasNumberDesignation, NormCode normCode, UUID categoryId) {}
