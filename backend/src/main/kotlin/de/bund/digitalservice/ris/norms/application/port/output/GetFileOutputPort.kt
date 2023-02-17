package de.bund.digitalservice.ris.norms.application.port.output

import reactor.core.publisher.Mono
import java.nio.ByteBuffer

interface GetFileOutputPort {
    fun getFile(query: Query): Mono<ByteBuffer>

    data class Query(val hash: String)
}
