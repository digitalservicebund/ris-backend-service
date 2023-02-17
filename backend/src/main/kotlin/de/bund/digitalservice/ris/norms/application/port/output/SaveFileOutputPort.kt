package de.bund.digitalservice.ris.norms.application.port.output

import reactor.core.publisher.Mono
import java.nio.ByteBuffer

interface SaveFileOutputPort {
    fun saveFile(command: Command): Mono<Boolean>

    data class Command(val file: ByteBuffer)
}
