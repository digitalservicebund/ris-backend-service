package de.bund.digitalservice.ris.norms.application.port.output

import de.bund.digitalservice.ris.norms.domain.entity.Norm
import reactor.core.publisher.Flux

interface SearchNormsOutputPort {
    fun searchNorms(query: Query): Flux<Norm>

    data class Query(val parameters: List<QueryParameter>)

    data class QueryParameter(
        val field: QueryFields,
        val value: String?,
        val isFuzzyMatch: Boolean = false,
    )

    // TODO Add UNOFFICIAL_LONG_TITLE & UNOFFICIAL_SHORT_TITLE once all metadata are migrated
    enum class QueryFields {
        PRINT_ANNOUNCEMENT_GAZETTE,
        ANNOUNCEMENT_DATE,
        CITATION_DATE,
        CITATION_YEAR,
        PRINT_ANNOUNCEMENT_PAGE,
        OFFICIAL_LONG_TITLE,
        OFFICIAL_SHORT_TITLE,
    }
}
