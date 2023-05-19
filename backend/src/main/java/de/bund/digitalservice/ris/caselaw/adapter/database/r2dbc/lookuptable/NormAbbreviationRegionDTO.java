package de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.lookuptable;

import java.util.UUID;
import org.springframework.data.relational.core.mapping.Table;

@Table("norm_abbreviation_region")
public record NormAbbreviationRegionDTO(UUID normAbbreviationId, UUID regionId) {}
