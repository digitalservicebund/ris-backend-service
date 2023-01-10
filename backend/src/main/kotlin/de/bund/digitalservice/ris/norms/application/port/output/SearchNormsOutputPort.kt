package de.bund.digitalservice.ris.norms.application.port.output

import de.bund.digitalservice.ris.norms.domain.entity.Norm
import reactor.core.publisher.Flux

interface SearchNormsOutputPort {
    fun searchNorms(query: List<QueryParameter>): Flux<Norm>

    data class QueryParameter(
        val name: String,
        val value: String,
        val isFuzzyMatch: Boolean = false,
        val isYearForDate: Boolean = false
    )
}
