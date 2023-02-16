package de.bund.digitalservice.ris.norms.application.service

import de.bund.digitalservice.ris.norms.application.port.input.ImportNormUseCase
import de.bund.digitalservice.ris.norms.application.port.output.ParseJurisXmlOutputPort
import de.bund.digitalservice.ris.norms.application.port.output.SaveNormOutputPort
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import reactor.core.publisher.Mono
import utils.createRandomNorm
import java.nio.ByteBuffer

class ImportNormServiceTest {

    @Test
    fun `it should forward the ZIP file to the juris parser`() {
        val parseJurisXmlAdapter = mockk<ParseJurisXmlOutputPort>()
        val saveNormAdapter = mockk<SaveNormOutputPort>()
        val service = ImportNormService(parseJurisXmlAdapter, saveNormAdapter)
        val zipFile = ByteBuffer.allocate(0)
        val command = ImportNormUseCase.Command(zipFile)

        every { parseJurisXmlAdapter.parseJurisXml(any()) } returns Mono.empty()

        service.importNorm(command).block()

        verify(exactly = 1) {
            parseJurisXmlAdapter.parseJurisXml(
                withArg { assertThat(it.zipFile).isEqualTo(zipFile) },
            )
        }
    }

    @Test
    fun `it forwards the parsed norm to the save norm adapter`() {
        val parseJurisXmlAdapter = mockk<ParseJurisXmlOutputPort>()
        val saveNormAdapter = mockk<SaveNormOutputPort>()
        val service = ImportNormService(parseJurisXmlAdapter, saveNormAdapter)
        val parsedNorm = createRandomNorm()
        val command = ImportNormUseCase.Command(ByteBuffer.allocate(0))

        every { parseJurisXmlAdapter.parseJurisXml(any()) } returns Mono.just(parsedNorm)
        every { saveNormAdapter.saveNorm(any()) } returns Mono.empty()

        service.importNorm(command).block()

        verify(exactly = 1) {
            saveNormAdapter.saveNorm(withArg { assertThat(it.norm).isEqualTo(parsedNorm) })
        }
    }

    @Test
    fun `it generates a new GUID for every norm`() {
        val parseJurisXmlAdapter = mockk<ParseJurisXmlOutputPort>()
        val saveNormAdapter = mockk<SaveNormOutputPort>()
        val service = ImportNormService(parseJurisXmlAdapter, saveNormAdapter)
        val commandOne = ImportNormUseCase.Command(ByteBuffer.allocate(0))
        val commandTwo = ImportNormUseCase.Command(ByteBuffer.allocate(0))
        val commandThree = ImportNormUseCase.Command(ByteBuffer.allocate(0))

        every { parseJurisXmlAdapter.parseJurisXml(any()) } returns Mono.just(createRandomNorm())
        every { saveNormAdapter.saveNorm(any()) } returns Mono.just(true)

        val guidOne = service.importNorm(commandOne).block()
        val guidTwo = service.importNorm(commandTwo).block()
        val guidThree = service.importNorm(commandThree).block()

        val guidList = listOf(guidOne, guidTwo, guidThree)
        assertThat(guidList.toSet().size).isEqualTo(guidList.size)
    }

    @Test
    fun `it uses the same GUID for the whole process`() {
        val parseJurisXmlAdapter = mockk<ParseJurisXmlOutputPort>()
        val saveNormAdapter = mockk<SaveNormOutputPort>()
        val service = ImportNormService(parseJurisXmlAdapter, saveNormAdapter)
        val command = ImportNormUseCase.Command(ByteBuffer.allocate(0))

        every { parseJurisXmlAdapter.parseJurisXml(any()) } returns Mono.just(createRandomNorm())
        every { saveNormAdapter.saveNorm(any()) } returns Mono.just(true)

        val guid = service.importNorm(command).block()
        val parseCommand = slot<ParseJurisXmlOutputPort.Command>()
        verify { parseJurisXmlAdapter.parseJurisXml(capture(parseCommand)) }

        assertThat(guid).isEqualTo(parseCommand.captured.newGuid)
    }
}
