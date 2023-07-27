package de.bund.digitalservice.ris.norms.application.port.input

import de.bund.digitalservice.ris.norms.domain.entity.MetadataSection
import java.util.UUID
import reactor.core.publisher.Mono

fun interface EditNormFrameUseCase {
  fun editNormFrame(command: Command): Mono<Boolean>

  data class Command(val guid: UUID, val properties: NormFrameProperties)

  data class NormFrameProperties(
      val metadataSections: List<MetadataSection>,
  )
}
