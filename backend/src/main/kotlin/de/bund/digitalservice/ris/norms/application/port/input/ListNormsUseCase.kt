package de.bund.digitalservice.ris.norms.application.port.input

import de.bund.digitalservice.ris.norms.domain.value.Eli
import reactor.core.publisher.Flux
import java.util.UUID

interface ListNormsUseCase {
    fun listNorms(query: Query): Flux<NormData>

    data class Query(val searchTerm: String? = null)

    data class NormData(
        val guid: UUID,
        val officialLongTitle: String,
        val eli: Eli
    )
}
