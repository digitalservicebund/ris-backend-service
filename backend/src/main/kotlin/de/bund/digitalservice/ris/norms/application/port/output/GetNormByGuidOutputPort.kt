package de.bund.digitalservice.ris.norms.application.port.output

import de.bund.digitalservice.ris.norms.domain.entity.Norm
import java.util.UUID
import reactor.core.publisher.Mono

fun interface GetNormByGuidOutputPort {
  fun getNormByGuid(query: Query): Mono<Norm>

  data class Query(val guid: UUID)
}
