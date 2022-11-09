package de.bund.digitalservice.ris.norms.application.port.input

import reactor.core.publisher.Mono
import java.util.UUID

interface EditNormFrameUseCase {
    fun editNormFrame(command: Command): Mono<Boolean>

    data class Command(val guid: UUID, val longTitle: String)
}
