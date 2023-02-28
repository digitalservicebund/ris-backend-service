package de.bund.digitalservice.ris.norms.application.service

import de.bund.digitalservice.ris.norms.application.port.input.GetFileUseCase
import de.bund.digitalservice.ris.norms.application.port.output.GetFileOutputPort
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import reactor.core.publisher.Mono
import java.nio.ByteBuffer
import java.util.*

class GetFileServiceTest {

    private val file: ByteBuffer = ByteBuffer.allocate(0)

    private val guidExample = "761b5537-5aa5-4901-81f7-fbf7e040a7c8"
    private val hashExample = "1c47461aea72b7d4f36c075fe09ae283e78477d261e5b1141c510cb46c941d10"

    @Test
    fun `it should call the get file outport port with the correct parameter`() {
        val getFileAdapter = mockk<GetFileOutputPort>()
        val service = GetFileService(getFileAdapter)
        val command = GetFileUseCase.Command(UUID.fromString(guidExample), hashExample)

        every { getFileAdapter.getFile(any()) } returns Mono.just(file.array())

        service.getFile(command).block()

        verify(exactly = 1) {
            getFileAdapter.getFile(withArg { assertThat(it.hash).isEqualTo(hashExample) })
        }
    }

    @Test
    fun `it should retrieve the file from the bucket`() {
        val getFileAdapter = mockk<GetFileOutputPort>()
        val service = GetFileService(getFileAdapter)
        val command = GetFileUseCase.Command(UUID.fromString(guidExample), hashExample)

        every { getFileAdapter.getFile(any()) } returns Mono.just(file.array())

        val file = service.getFile(command).block()

        assertThat(file).isEqualTo(file)
    }

    @Test
    fun `it throws an error if file can not be downloaded from bucket`() {
        val getFileAdapter = mockk<GetFileOutputPort>()
        val service = GetFileService(getFileAdapter)
        val command = GetFileUseCase.Command(UUID.fromString(guidExample), hashExample)

        every { getFileAdapter.getFile(any()) } throws Exception("Error occurred")

        try {
            service.getFile(command).block()
        } catch (exception: Exception) {
            assertThat(exception.message).contains("Error occurred")
        }
    }
}
