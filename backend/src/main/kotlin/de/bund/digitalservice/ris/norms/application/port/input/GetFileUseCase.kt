package de.bund.digitalservice.ris.norms.application.port.input

import java.util.UUID
import reactor.core.publisher.Mono

fun interface GetFileUseCase {
  fun getFile(command: Command): Mono<ByteArray>

  data class Command(val guid: UUID, val hash: String)
}
