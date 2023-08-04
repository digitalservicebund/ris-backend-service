package de.bund.digitalservice.ris.norms.application.port.input

import de.bund.digitalservice.ris.norms.domain.entity.MetadataSection
import de.bund.digitalservice.ris.norms.domain.specification.SpecificationResult
import reactor.core.publisher.Mono

fun interface ValidateNormFrameUseCase {
  fun validateNormFrame(command: Command): Mono<SpecificationResult>

  data class Command(val metadataSections: List<MetadataSection>)
}
