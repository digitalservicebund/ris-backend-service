package de.bund.digitalservice.ris.norms.application.service

import de.bund.digitalservice.ris.norms.application.port.input.ImportNormUseCase
import de.bund.digitalservice.ris.norms.application.port.output.ParseJurisXmlOutputPort
import de.bund.digitalservice.ris.norms.application.port.output.SaveFileOutputPort
import de.bund.digitalservice.ris.norms.application.port.output.SaveNormOutputPort
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import reactor.core.publisher.Mono
import utils.createRandomNorm
import java.io.File

class ImportNormServiceTest {

    private val zipFile = File.createTempFile("Temp", ".zip")

    @Test
    fun `it should forward the ZIP file to the juris parser`() {
        val parseJurisXmlAdapter = mockk<ParseJurisXmlOutputPort>()
        val saveNormAdapter = mockk<SaveNormOutputPort>()
        val saveFileAdapter = mockk<SaveFileOutputPort>()
        val service = ImportNormService(parseJurisXmlAdapter, saveFileAdapter, saveNormAdapter)
        val command = ImportNormUseCase.Command(zipFile.readBytes(), zipFile.name)

        every { parseJurisXmlAdapter.parseJurisXml(any()) } returns Mono.empty()

        service.importNorm(command).block()

        verify(exactly = 1) {
            parseJurisXmlAdapter.parseJurisXml(
                withArg { assertThat(it.zipFile).isEqualTo(zipFile.readBytes()) },
            )
        }
    }

    @Test
    fun `it forwards the parsed norm to the save norm adapter`() {
        val parseJurisXmlAdapter = mockk<ParseJurisXmlOutputPort>()
        val saveNormAdapter = mockk<SaveNormOutputPort>()
        val saveFileAdapter = mockk<SaveFileOutputPort>()
        val service = ImportNormService(parseJurisXmlAdapter, saveFileAdapter, saveNormAdapter)
        val parsedNorm = createRandomNorm()
        val command = ImportNormUseCase.Command(zipFile.readBytes(), zipFile.name)

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
        val saveFileAdapter = mockk<SaveFileOutputPort>()
        val service = ImportNormService(parseJurisXmlAdapter, saveFileAdapter, saveNormAdapter)
        val commandOne = ImportNormUseCase.Command(zipFile.readBytes(), zipFile.name)
        val commandTwo = ImportNormUseCase.Command(zipFile.readBytes(), zipFile.name)
        val commandThree = ImportNormUseCase.Command(zipFile.readBytes(), zipFile.name)

        every { parseJurisXmlAdapter.parseJurisXml(any()) } returns Mono.just(createRandomNorm())
        every { saveNormAdapter.saveNorm(any()) } returns Mono.just(true)
        every { saveFileAdapter.saveFile(any()) } returns Mono.just(true)

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
        val saveFileAdapter = mockk<SaveFileOutputPort>()
        val service = ImportNormService(parseJurisXmlAdapter, saveFileAdapter, saveNormAdapter)
        val command = ImportNormUseCase.Command(zipFile.readBytes(), zipFile.name)

        every { parseJurisXmlAdapter.parseJurisXml(any()) } returns Mono.just(createRandomNorm())
        every { saveNormAdapter.saveNorm(any()) } returns Mono.just(true)
        every { saveFileAdapter.saveFile(any()) } returns Mono.just(true)

        val guid = service.importNorm(command).block()
        val parseQuery = slot<ParseJurisXmlOutputPort.Query>()
        verify { parseJurisXmlAdapter.parseJurisXml(capture(parseQuery)) }

        assertThat(guid).isEqualTo(parseQuery.captured.newGuid)
    }

    @Test
    fun `it throws an error if file can not be uploaded to bucket`() {
        val parseJurisXmlAdapter = mockk<ParseJurisXmlOutputPort>()
        val saveNormAdapter = mockk<SaveNormOutputPort>()
        val saveFileAdapter = mockk<SaveFileOutputPort>()
        val service = ImportNormService(parseJurisXmlAdapter, saveFileAdapter, saveNormAdapter)
        val command = ImportNormUseCase.Command(zipFile.readBytes(), zipFile.name)

        every { parseJurisXmlAdapter.parseJurisXml(any()) } returns Mono.just(createRandomNorm())
        every { saveNormAdapter.saveNorm(any()) } returns Mono.just(true)
        every { saveFileAdapter.saveFile(any()) } throws Exception("Error occurred")

        try {
            service.importNorm(command).block()
        } catch (exception: Exception) {
            assertThat(exception.message).contains("Error occurred")
        }
    }
}
