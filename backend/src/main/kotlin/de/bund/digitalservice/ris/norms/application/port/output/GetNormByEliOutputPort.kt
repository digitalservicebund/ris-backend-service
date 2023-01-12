package de.bund.digitalservice.ris.norms.application.port.output

import de.bund.digitalservice.ris.norms.domain.entity.Norm
import reactor.core.publisher.Mono

interface GetNormByEliOutputPort {
    fun getNormByEli(gazette: String, year: String, page: String): Mono<Norm>
}
