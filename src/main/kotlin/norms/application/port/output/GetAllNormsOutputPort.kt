package de.bund.digitalservice.ris.norms.application.port.output

import de.bund.digitalservice.ris.norms.domain.entity.Norm
import reactor.core.publisher.Flux

interface GetAllNormsOutputPort {
    fun getAllNorms(): Flux<Norm>
}
