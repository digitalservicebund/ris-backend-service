package de.bund.digitalservice.ris.norms.application.port.input

import de.bund.digitalservice.ris.norms.juris.converter.model.Norm
import java.util.*
import reactor.core.publisher.Mono

fun interface MigrateNormUseCase {
  fun migrateNorm(command: Command): Mono<Boolean>

  data class Command(val norms: ConverterNorms)

  data class ConverterNorms(val norms: List<ConverterNorm>)

  data class ConverterNorm(val guid: UUID, val norm: Norm)
}
