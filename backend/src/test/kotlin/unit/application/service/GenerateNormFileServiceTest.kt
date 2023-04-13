package de.bund.digitalservice.ris.norms.application.service

import de.bund.digitalservice.ris.norms.application.port.input.GenerateNormFileUseCase
import de.bund.digitalservice.ris.norms.application.port.output.GenerateNormFileOutputPort
import de.bund.digitalservice.ris.norms.application.port.output.GetFileOutputPort
import de.bund.digitalservice.ris.norms.application.port.output.GetNormByGuidOutputPort
import de.bund.digitalservice.ris.norms.application.port.output.SaveFileOutputPort
import de.bund.digitalservice.ris.norms.application.port.output.SaveFileReferenceOutputPort
import de.bund.digitalservice.ris.norms.domain.entity.getHashFromContent
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import reactor.core.publisher.Mono
import utils.createRandomFileReference
import utils.createRandomNorm
import java.util.*

class GenerateNormFileServiceTest {
    @Test
    fun `it calls the get norm by GUID adapter using the input query GUID parameter`() {
        val getNormByGuidOutputPort = mockk<GetNormByGuidOutputPort>()
        val getFileOutputPort = mockk<GetFileOutputPort>()
        val generateNormFileOutputPort = mockk<GenerateNormFileOutputPort>()
        val saveFileOutputPort = mockk<SaveFileOutputPort>()
        val saveFileReferenceOutputPort = mockk<SaveFileReferenceOutputPort>()
        val service = GenerateNormFileService(getNormByGuidOutputPort, getFileOutputPort, generateNormFileOutputPort, saveFileOutputPort, saveFileReferenceOutputPort)
        val guid = UUID.randomUUID()
        val command = GenerateNormFileUseCase.Command(guid)

        every { getNormByGuidOutputPort.getNormByGuid(any()) } returns Mono.empty()

        service.generateNormFile(command).block()

        verify(exactly = 1) { getNormByGuidOutputPort.getNormByGuid(withArg { assertThat(it.guid).isEqualTo(guid) }) }
    }

    @Test
    fun `it calls the get file by hash adapter using the found norm`() {
        val getNormByGuidOutputPort = mockk<GetNormByGuidOutputPort>()
        val getFileOutputPort = mockk<GetFileOutputPort>()
        val generateNormFileOutputPort = mockk<GenerateNormFileOutputPort>()
        val saveFileOutputPort = mockk<SaveFileOutputPort>()
        val saveFileReferenceOutputPort = mockk<SaveFileReferenceOutputPort>()
        val service = GenerateNormFileService(getNormByGuidOutputPort, getFileOutputPort, generateNormFileOutputPort, saveFileOutputPort, saveFileReferenceOutputPort)
        val guid = UUID.randomUUID()
        val command = GenerateNormFileUseCase.Command(guid)

        val norm = createRandomNorm()
        val fileReference = createRandomFileReference()
        norm.files = listOf(fileReference)

        every { getNormByGuidOutputPort.getNormByGuid(any()) } returns Mono.just(norm)
        every { getFileOutputPort.getFile(any()) } returns Mono.empty()

        service.generateNormFile(command).block()

        verify(exactly = 1) { getFileOutputPort.getFile(withArg { assertThat(it.hash).isEqualTo(fileReference.hash) }) }
    }

    @Test
    fun `it calls the generate file adapter using the found norm and the retrieved file`() {
        val getNormByGuidOutputPort = mockk<GetNormByGuidOutputPort>()
        val getFileOutputPort = mockk<GetFileOutputPort>()
        val generateNormFileOutputPort = mockk<GenerateNormFileOutputPort>()
        val saveFileOutputPort = mockk<SaveFileOutputPort>()
        val saveFileReferenceOutputPort = mockk<SaveFileReferenceOutputPort>()
        val service = GenerateNormFileService(getNormByGuidOutputPort, getFileOutputPort, generateNormFileOutputPort, saveFileOutputPort, saveFileReferenceOutputPort)
        val guid = UUID.randomUUID()
        val command = GenerateNormFileUseCase.Command(guid)

        val norm = createRandomNorm().apply { files = listOf(createRandomFileReference()) }
        val file = ByteArray(10)

        every { getNormByGuidOutputPort.getNormByGuid(any()) } returns Mono.just(norm)
        every { getFileOutputPort.getFile(any()) } returns Mono.just(file)
        every { generateNormFileOutputPort.generateNormFile(any()) } returns Mono.empty()

        service.generateNormFile(command).block()

        verify(exactly = 1) {
            generateNormFileOutputPort.generateNormFile(
                withArg {
                    assertThat(it.norm).isEqualTo(norm)
                    assertThat(it.previousFile).isEqualTo(file)
                },
            )
        }
    }

    @Test
    fun `it calls the save file adapter with the new file, its content length and previous file name`() {
        val getNormByGuidOutputPort = mockk<GetNormByGuidOutputPort>()
        val getFileOutputPort = mockk<GetFileOutputPort>()
        val generateNormFileOutputPort = mockk<GenerateNormFileOutputPort>()
        val saveFileOutputPort = mockk<SaveFileOutputPort>()
        val saveFileReferenceOutputPort = mockk<SaveFileReferenceOutputPort>()
        val service = GenerateNormFileService(getNormByGuidOutputPort, getFileOutputPort, generateNormFileOutputPort, saveFileOutputPort, saveFileReferenceOutputPort)
        val guid = UUID.randomUUID()
        val command = GenerateNormFileUseCase.Command(guid)

        val norm = createRandomNorm()
        val fileReference = createRandomFileReference()
        norm.files = listOf(fileReference)
        val filePrevious = ByteArray(10)
        val newFile = ByteArray(11)

        every { getNormByGuidOutputPort.getNormByGuid(any()) } returns Mono.just(norm)
        every { getFileOutputPort.getFile(any()) } returns Mono.just(filePrevious)
        every { generateNormFileOutputPort.generateNormFile(any()) } returns Mono.just(newFile)
        every { saveFileOutputPort.saveFile(any()) } returns Mono.empty()

        service.generateNormFile(command).block()

        verify(exactly = 1) {
            saveFileOutputPort.saveFile(
                withArg {
                    assertThat(it.file).isEqualTo(newFile)
                    assertThat(it.contentLength).isEqualTo(newFile.size.toLong())
                    assertThat(it.filename).isEqualTo(fileReference.name)
                },
            )
        }
    }

    @Test
    fun `it calls the save file reference adapter with the new file reference`() {
        val getNormByGuidOutputPort = mockk<GetNormByGuidOutputPort>()
        val getFileOutputPort = mockk<GetFileOutputPort>()
        val generateNormFileOutputPort = mockk<GenerateNormFileOutputPort>()
        val saveFileOutputPort = mockk<SaveFileOutputPort>()
        val saveFileReferenceOutputPort = mockk<SaveFileReferenceOutputPort>()
        val service = GenerateNormFileService(getNormByGuidOutputPort, getFileOutputPort, generateNormFileOutputPort, saveFileOutputPort, saveFileReferenceOutputPort)
        val guid = UUID.randomUUID()
        val command = GenerateNormFileUseCase.Command(guid)

        val norm = createRandomNorm()
        val fileReference = createRandomFileReference()
        norm.files = listOf(fileReference)
        val filePrevious = ByteArray(10)
        val newFile = ByteArray(11)

        every { getNormByGuidOutputPort.getNormByGuid(any()) } returns Mono.just(norm)
        every { getFileOutputPort.getFile(any()) } returns Mono.just(filePrevious)
        every { generateNormFileOutputPort.generateNormFile(any()) } returns Mono.just(newFile)
        every { saveFileOutputPort.saveFile(any()) } returns Mono.just(true)
        every { saveFileReferenceOutputPort.saveFileReference(any()) } returns Mono.empty()

        service.generateNormFile(command).block()

        verify(exactly = 1) {
            saveFileReferenceOutputPort.saveFileReference(
                withArg {
                    assertThat(it.norm).isEqualTo(norm)
                    assertThat(it.fileReference.name).isEqualTo(fileReference.name)
                    assertThat(it.fileReference.hash).isEqualTo(getHashFromContent(newFile))
                    assertThat(it.fileReference.createdAt).isNotNull()
                },
            )
        }
    }
}
