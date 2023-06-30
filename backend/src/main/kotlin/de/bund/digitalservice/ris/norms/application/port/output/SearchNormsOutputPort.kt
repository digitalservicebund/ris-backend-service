package de.bund.digitalservice.ris.norms.application.port.output

import de.bund.digitalservice.ris.norms.domain.entity.Norm
import reactor.core.publisher.Flux

fun interface SearchNormsOutputPort {
    fun searchNorms(query: Query): Flux<Norm>

    data class Query(val searchTerm: String)
}
