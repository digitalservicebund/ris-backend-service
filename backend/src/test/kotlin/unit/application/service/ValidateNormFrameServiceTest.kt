package de.bund.digitalservice.ris.norms.application.service

import de.bund.digitalservice.ris.norms.application.port.input.ValidateNormFrameUseCase
import de.bund.digitalservice.ris.norms.domain.entity.MetadataSection
import de.bund.digitalservice.ris.norms.domain.entity.Metadatum
import de.bund.digitalservice.ris.norms.domain.specification.SpecificationResult
import de.bund.digitalservice.ris.norms.domain.specification.section.HasAllMandatoryFields
import de.bund.digitalservice.ris.norms.domain.value.MetadataSectionName
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import reactor.test.StepVerifier

class ValidateNormFrameServiceTest {

  @Test
  fun `it calls the hasMandatoryMetadata specification`() {
    val hasAllMandatoryFields: HasAllMandatoryFields = mockk()
    val service = ValidateNormFrameService(hasAllMandatoryFields)
    val command: ValidateNormFrameUseCase.Command = mockk()
    every { command.metadataSections } returns
        listOf(
            MetadataSection(
                MetadataSectionName.NORM, listOf(Metadatum("foo", MetadatumType.KEYWORD, 0))))
    every { hasAllMandatoryFields.evaluate(command.metadataSections) } returns
        SpecificationResult.Satisfied

    StepVerifier.create(service.validateNormFrame(command)).expectNextCount(1).verifyComplete()

    verify { hasAllMandatoryFields.evaluate(command.metadataSections) }
  }
}
