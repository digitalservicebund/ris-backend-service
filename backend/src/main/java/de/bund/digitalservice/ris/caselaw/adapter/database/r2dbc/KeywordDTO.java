package de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc;

import lombok.Builder;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

/**
 * @deprecated use {@link de.bund.digitalservice.ris.caselaw.adapter.database.jpa.KeywordDTO}
 *     instead
 */
@Builder
@Table("keyword")
@Deprecated
public record KeywordDTO(@Id Long id, Long documentUnitId, String keyword) {}
