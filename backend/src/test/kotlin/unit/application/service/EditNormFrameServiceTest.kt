package de.bund.digitalservice.ris.norms.application.service

import de.bund.digitalservice.ris.norms.application.port.input.EditNormFrameUseCase
import de.bund.digitalservice.ris.norms.application.port.output.EditNormOutputPort
import de.bund.digitalservice.ris.norms.domain.value.MetadataSectionName
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.DEFINITION
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.KEYWORD
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.NORM_CATEGORY
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.TEMPLATE_NAME
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.TYPE_NAME
import de.bund.digitalservice.ris.norms.domain.value.NormCategory
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import utils.assertNormAndEditNormFrameProperties
import utils.createRandomNormFameProperties
import utils.factory.metadataSection
import java.util.*

class EditNormFrameServiceTest {

    @Test
    fun `it calls the output port to save the norm minimal required properties`() {
        val editNormOutputPort = mockk<EditNormOutputPort>()
        val service = EditNormFrameService(editNormOutputPort)
        val guid = UUID.randomUUID()
        val properties = EditNormFrameUseCase.NormFrameProperties("new title", emptyList())
        val command = EditNormFrameUseCase.Command(guid, properties)

        every { editNormOutputPort.editNorm(any()) } returns Mono.just(true)

        StepVerifier.create(service.editNormFrame(command)).expectNextCount(1).verifyComplete()

        verify(exactly = 1) { editNormOutputPort.editNorm(any()) }
        verify {
            editNormOutputPort.editNorm(
                withArg {
                    assertNormAndEditNormFrameProperties(it.norm, properties)
                },
            )
        }
    }

    @Test
    fun `it calls the output port to save the norm with optional data and fields`() {
        val guid = UUID.randomUUID()
        val metadataSections = listOf(
            metadataSection {
                name = MetadataSectionName.NORM
                metadata {
                    metadatum { value = "foo"; type = KEYWORD; order = 0 }
                    metadatum { value = "bar"; type = KEYWORD; order = 1 }
                    metadatum { value = "definition"; type = DEFINITION; order = 0 }
                }
            },
            metadataSection {
                name = MetadataSectionName.DOCUMENT_TYPE
                metadata {
                    metadatum { value = "documentTypeName"; type = TYPE_NAME }
                    metadatum { value = NormCategory.BASE_NORM; type = NORM_CATEGORY }
                    metadatum { value = "documentTemplateName"; type = TEMPLATE_NAME }
                }
            },
        )
        val properties = createRandomNormFameProperties().copy(
            metadataSections = metadataSections,
        )
        val editNormOutputPort = mockk<EditNormOutputPort>()
        val service = EditNormFrameService(editNormOutputPort)
        val command = EditNormFrameUseCase.Command(guid, properties)

        every { editNormOutputPort.editNorm(any()) } returns Mono.just(true)

        StepVerifier.create(service.editNormFrame(command)).expectNextCount(1).verifyComplete()

        verify(exactly = 1) { editNormOutputPort.editNorm(any()) }

        verify {
            editNormOutputPort.editNorm(
                withArg {
                    assertNormAndEditNormFrameProperties(it.norm, properties)
                },
            )
        }
    }
}
