package de.bund.digitalservice.ris.norms.application.port.input

import de.bund.digitalservice.ris.norms.domain.value.Eli
import java.util.UUID
import reactor.core.publisher.Flux

fun interface ListNormsUseCase {
  fun listNorms(query: Query): Flux<NormData>

  data class Query(val searchTerm: String? = null, val eGesetzgebung: Boolean)

  data class NormData(
      val guid: UUID,
      val officialLongTitle: String,
      val eli: Eli,
  )
}
