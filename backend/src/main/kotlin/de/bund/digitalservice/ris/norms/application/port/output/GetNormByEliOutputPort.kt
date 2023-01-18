package de.bund.digitalservice.ris.norms.application.port.output

import de.bund.digitalservice.ris.norms.domain.entity.Norm
import reactor.core.publisher.Mono

interface GetNormByEliOutputPort {
    fun getNormByEli(query: Query): Mono<Norm>

    data class Query(val gazette: String, val year: String, val page: String)
}
