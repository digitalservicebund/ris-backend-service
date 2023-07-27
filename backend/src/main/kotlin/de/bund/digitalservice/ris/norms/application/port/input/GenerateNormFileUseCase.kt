package de.bund.digitalservice.ris.norms.application.port.input

import de.bund.digitalservice.ris.norms.domain.entity.FileReference
import java.util.UUID
import reactor.core.publisher.Mono

fun interface GenerateNormFileUseCase {
  fun generateNormFile(command: Command): Mono<FileReference>

  data class Command(val guid: UUID)
}
