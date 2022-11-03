package de.bund.digitalservice.ris.norms.application.port.input

import de.bund.digitalservice.ris.norms.domain.entity.Norm
import reactor.core.publisher.Flux

interface ListNormsUseCase {
    fun listNorms(): Flux<Norm>
}
