package de.bund.digitalservice.ris.norms.application.port.input

import de.bund.digitalservice.ris.norms.domain.entity.Norm
import de.bund.digitalservice.ris.norms.domain.value.Guid
import reactor.core.publisher.Mono

interface LoadNormUseCase {
    fun loadNorm(query: Query): Mono<Norm>

    class Query(val guid: Guid)
}
