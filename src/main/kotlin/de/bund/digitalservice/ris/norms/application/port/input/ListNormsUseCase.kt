package de.bund.digitalservice.ris.norms.application.port.input

import reactor.core.publisher.Flux
import java.util.UUID

interface ListNormsUseCase {
    fun listNorms(): Flux<NormData>

    data class NormData(val guid: UUID, val longTitle: String)
}
