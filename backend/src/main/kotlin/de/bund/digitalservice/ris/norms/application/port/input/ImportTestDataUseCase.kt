package de.bund.digitalservice.ris.norms.application.port.input

import de.bund.digitalservice.ris.norms.domain.entity.Norm
import reactor.core.publisher.Mono

fun interface ImportTestDataUseCase {
  fun importTestData(command: Command): Mono<Boolean>

  data class Command(val norm: Norm)
}
