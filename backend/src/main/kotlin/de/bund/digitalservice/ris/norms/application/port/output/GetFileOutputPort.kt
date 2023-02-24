package de.bund.digitalservice.ris.norms.application.port.output

import reactor.core.publisher.Mono

interface GetFileOutputPort {
    fun getFile(query: Query): Mono<ByteArray>

    data class Query(val hash: String)
}
