package de.bund.digitalservice.ris.norms.application.port.output

import de.bund.digitalservice.ris.norms.domain.entity.Norm
import reactor.core.publisher.Mono

interface SaveNormOutputPort {
    fun saveNorm(command: Command): Mono<Boolean>

    data class Command(val norm: Norm)
}
