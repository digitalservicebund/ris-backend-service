package de.bund.digitalservice.ris.norms.application.port.output

import de.bund.digitalservice.ris.norms.application.port.input.MigrateNormUseCase.ConverterNorm
import de.bund.digitalservice.ris.norms.domain.entity.Norm
import reactor.core.publisher.Flux

fun interface ParseJurisArrayOutputPort {
  fun parseJurisArray(query: Query): Flux<Norm>

  data class Query(val norms: List<ConverterNorm>)
}
