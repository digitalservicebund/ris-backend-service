package de.bund.digitalservice.ris.norms.application.service

import de.bund.digitalservice.ris.norms.application.port.input.EditNormFrameUseCase
import de.bund.digitalservice.ris.norms.application.port.output.EditNormOutputPort
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import norms.utils.assertNormAndEditNormFrameProperties
import norms.utils.createRandomNormFameProperties
import org.junit.jupiter.api.Test
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import java.util.UUID

class EditNormFrameServiceTest {

    @Test
    fun `it calls the output port to save the norm with changed title and without optional fields`() {
        val editNormOutputPort = mockk<EditNormOutputPort>()
        val service = EditNormFrameService(editNormOutputPort)
        val guid = UUID.randomUUID()
        val properties = EditNormFrameUseCase.NormFrameProperties("new title")
        val command = EditNormFrameUseCase.Command(guid, properties)

        every { editNormOutputPort.editNorm(any()) } returns Mono.just(true)

        StepVerifier.create(service.editNormFrame(command)).expectNextCount(1).verifyComplete()

        verify(exactly = 1) { editNormOutputPort.editNorm(any()) }
        verify {
            editNormOutputPort.editNorm(
                withArg {
                    assertNormAndEditNormFrameProperties(it, properties)
                }
            )
        }
    }

    @Test
    fun `it calls the output port to save the norm with changed title and optional fields`() {
        val normFrameProperties = createRandomNormFameProperties()

        val editNormOutputPort = mockk<EditNormOutputPort>()
        val service = EditNormFrameService(editNormOutputPort)
        val guid = UUID.randomUUID()
        val command = EditNormFrameUseCase.Command(guid, normFrameProperties)

        every { editNormOutputPort.editNorm(any()) } returns Mono.just(true)

        StepVerifier.create(service.editNormFrame(command)).expectNextCount(1).verifyComplete()

        verify(exactly = 1) { editNormOutputPort.editNorm(any()) }

        verify {
            editNormOutputPort.editNorm(
                withArg {
                    assertNormAndEditNormFrameProperties(it, normFrameProperties)
                }
            )
        }
    }
}
