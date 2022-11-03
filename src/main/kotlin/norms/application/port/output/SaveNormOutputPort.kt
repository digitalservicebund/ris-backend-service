package de.bund.digitalservice.ris.norms.application.port.output

import de.bund.digitalservice.ris.norms.domain.entity.Norm
import reactor.core.publisher.Mono

interface SaveNormOutputPort {
    fun saveNorm(norm: Norm): Mono<Boolean>
}
