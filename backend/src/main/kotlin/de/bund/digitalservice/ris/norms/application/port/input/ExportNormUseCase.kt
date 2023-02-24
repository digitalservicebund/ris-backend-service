package de.bund.digitalservice.ris.norms.application.port.input

import reactor.core.publisher.Mono
import java.util.UUID

interface ExportNormUseCase {
    fun exportNorm(command: Command): Mono<ByteArray>

    data class Command(val guid: UUID, val hash: String)
}
