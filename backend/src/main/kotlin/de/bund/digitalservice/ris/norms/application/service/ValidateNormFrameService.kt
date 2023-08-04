package de.bund.digitalservice.ris.norms.application.service

import de.bund.digitalservice.ris.norms.application.port.input.ValidateNormFrameUseCase
import de.bund.digitalservice.ris.norms.domain.specification.SpecificationResult
import de.bund.digitalservice.ris.norms.domain.specification.section.HasAllMandatoryFields
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
class ValidateNormFrameService(
    private val hasAllMandatoryFields: HasAllMandatoryFields = HasAllMandatoryFields()
) : ValidateNormFrameUseCase {

  override fun validateNormFrame(
      command: ValidateNormFrameUseCase.Command
  ): Mono<SpecificationResult> {
    return Mono.just(hasAllMandatoryFields.evaluate(command.metadataSections))
  }
}
