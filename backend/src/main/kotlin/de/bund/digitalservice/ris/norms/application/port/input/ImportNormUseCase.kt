package de.bund.digitalservice.ris.norms.application.port.input

import java.util.UUID
import reactor.core.publisher.Mono

fun interface ImportNormUseCase {
  fun importNorm(command: Command): Mono<UUID>

  data class Command(val zipFile: ByteArray, val filename: String, val contentLength: Long)
}
