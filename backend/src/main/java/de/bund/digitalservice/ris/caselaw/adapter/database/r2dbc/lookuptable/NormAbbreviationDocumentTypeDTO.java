package de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.lookuptable;

import java.util.UUID;
import org.springframework.data.relational.core.mapping.Table;

@Table(name = "norm_abbreviation_document_type")
public record NormAbbreviationDocumentTypeDTO(UUID normAbbreviationId, UUID documentTypeId) {}
