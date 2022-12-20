package de.bund.digitalservice.ris.norms.application.port.input

import reactor.core.publisher.Mono
import java.util.UUID

interface LoadNormAsXmlUseCase {
    fun loadNormAsXml(query: Query): Mono<String>

    data class Query(val guid: UUID)
}
