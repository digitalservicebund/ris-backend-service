package de.bund.digitalservice.ris.norms.application

import de.bund.digitalservice.ris.norms.application.port.input.EditNormFrameUseCase
import de.bund.digitalservice.ris.norms.application.port.output.GetNormByGuidOutputPort
import de.bund.digitalservice.ris.norms.application.port.output.SaveNormOutputPort
import de.bund.digitalservice.ris.norms.domain.entity.Norm
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import java.util.UUID

class EditNormFrameServiceTest {
    @Test
    fun `it fetches the norm by the command GUID from the output port`() {
        val getNormByGuidPort = mockk<GetNormByGuidOutputPort>()
        val saveNormOutputPort = mockk<SaveNormOutputPort>()
        val service = EditNormFrameService(getNormByGuidPort, saveNormOutputPort)
        val guid = UUID.randomUUID()
        val command = EditNormFrameUseCase.Command(guid, "new title")
        val norm = Norm(guid, "title")

        every { getNormByGuidPort.getNormByGuid(any()) } returns Mono.just(norm)
        every { saveNormOutputPort.saveNorm(any()) } returns Mono.just(true)

        StepVerifier.create(service.editNormFrame(command)).expectNextCount(1).verifyComplete()

        verify(exactly = 1) { getNormByGuidPort.getNormByGuid(any()) }
        verify { getNormByGuidPort.getNormByGuid(withArg { assertTrue(it == guid) }) }
    }

    @Test
    fun `it calls the output port to save the norm with changed title`() {
        val getNormByGuidPort = mockk<GetNormByGuidOutputPort>()
        val saveNormOutputPort = mockk<SaveNormOutputPort>()
        val service = EditNormFrameService(getNormByGuidPort, saveNormOutputPort)
        val guid = UUID.randomUUID()
        val command = EditNormFrameUseCase.Command(guid, "new title")
        val norm = Norm(guid, "old title")

        every { getNormByGuidPort.getNormByGuid(any()) } returns Mono.just(norm)
        every { saveNormOutputPort.saveNorm(any()) } returns Mono.just(true)

        StepVerifier.create(service.editNormFrame(command)).expectNextCount(1).verifyComplete()

        verify(exactly = 1) { saveNormOutputPort.saveNorm(any()) }
        verify { saveNormOutputPort.saveNorm(withArg { assertTrue(it.longTitle == "new title") }) }
    }

    @Test
    fun `it does not save the norm if none was retrieved from the output port`() {
        val getNormByGuidPort = mockk<GetNormByGuidOutputPort>()
        val saveNormOutputPort = mockk<SaveNormOutputPort>()
        val service = EditNormFrameService(getNormByGuidPort, saveNormOutputPort)
        val guid = UUID.randomUUID()
        val command = EditNormFrameUseCase.Command(guid, "new title")

        every { getNormByGuidPort.getNormByGuid(any()) } returns Mono.empty()
        every { saveNormOutputPort.saveNorm(any()) } returns Mono.just(true)

        StepVerifier.create(service.editNormFrame(command)).expectNextCount(0).verifyComplete()

        verify(exactly = 0) { saveNormOutputPort.saveNorm(any()) }
    }
}
