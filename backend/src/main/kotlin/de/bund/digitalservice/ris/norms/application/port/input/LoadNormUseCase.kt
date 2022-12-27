package de.bund.digitalservice.ris.norms.application.port.input

import de.bund.digitalservice.ris.norms.domain.entity.Norm
import reactor.core.publisher.Mono
import java.util.UUID

interface LoadNormUseCase {
    fun loadNorm(query: Query): Mono<Norm>

    class Query(val guid: UUID)
}
