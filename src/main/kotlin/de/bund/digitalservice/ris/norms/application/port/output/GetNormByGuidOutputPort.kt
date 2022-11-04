package de.bund.digitalservice.ris.norms.application.port.output

import de.bund.digitalservice.ris.norms.domain.entity.Norm
import de.bund.digitalservice.ris.norms.domain.value.Guid
import reactor.core.publisher.Mono

interface GetNormByGuidOutputPort {
    fun getNormByGuid(guid: Guid): Mono<Norm>
}
