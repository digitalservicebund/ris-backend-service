package de.bund.digitalservice.ris.norms.application.port.output

import de.bund.digitalservice.ris.norms.domain.entity.Norm
import reactor.core.publisher.Mono
import java.util.UUID

interface GetNormByGuidOutputPort {
    fun getNormByGuid(query: Query): Mono<Norm>

    data class Query(val guid: UUID)
}
