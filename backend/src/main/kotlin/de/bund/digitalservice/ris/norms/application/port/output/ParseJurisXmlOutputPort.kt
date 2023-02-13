package de.bund.digitalservice.ris.norms.application.port.output

import de.bund.digitalservice.ris.norms.domain.entity.Norm
import reactor.core.publisher.Mono
import java.nio.ByteBuffer
import java.util.UUID

interface ParseJurisXmlOutputPort {
    fun parseJurisXml(command: Command): Mono<Norm>
    data class Command(val newGuid: UUID, val zipFile: ByteBuffer)
}
