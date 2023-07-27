package de.bund.digitalservice.ris.norms.application.port.output

import de.bund.digitalservice.ris.norms.domain.entity.Norm
import reactor.core.publisher.Mono

fun interface GenerateNormFileOutputPort {
  fun generateNormFile(command: Command): Mono<ByteArray>

  data class Command(val norm: Norm, val previousFile: ByteArray)
}
