package de.bund.digitalservice.ris.norms.application.port.output

import de.bund.digitalservice.ris.norms.domain.entity.Norm
import reactor.core.publisher.Mono
import java.util.UUID

interface GetNormByGuidOutputPort {
    fun getNormByGuid(guid: UUID): Mono<Norm>
}
