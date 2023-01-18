package de.bund.digitalservice.ris.norms.application.port.output

import de.bund.digitalservice.ris.norms.domain.entity.Norm
import reactor.core.publisher.Mono

interface EditNormOutputPort {
    fun editNorm(command: Command): Mono<Boolean>

    data class Command(val norm: Norm)
}
