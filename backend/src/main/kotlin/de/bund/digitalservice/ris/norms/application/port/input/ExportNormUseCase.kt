package de.bund.digitalservice.ris.norms.application.port.input

import reactor.core.publisher.Mono
import java.nio.ByteBuffer
import java.util.UUID

interface ExportNormUseCase {
    fun exportNorm(command: Command): Mono<ByteBuffer>

    data class Command(val guid: UUID, val hash: String)
}
