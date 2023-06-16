package de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc;

import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnitLinkType;
import java.util.UUID;
import lombok.Builder;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Builder(toBuilder = true)
@Table("documentation_unit_link")
public record DocumentationUnitLinkDTO(
    @Id Long id,
    UUID parentDocumentationUnitUuid,
    UUID childDocumentationUnitUuid,
    DocumentationUnitLinkType type) {}
