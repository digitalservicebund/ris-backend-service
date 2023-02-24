package de.bund.digitalservice.ris.norms.framework.adapter.input.restapi.controller

import com.ninjasquad.springmockk.MockkBean
import de.bund.digitalservice.ris.norms.application.port.input.ExportNormUseCase
import de.bund.digitalservice.ris.norms.application.port.output.GetFileOutputPort
import io.mockk.every
import io.mockk.slot
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.csrf
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.reactive.server.WebTestClient
import reactor.core.publisher.Mono
import java.nio.ByteBuffer
import java.util.*

@ExtendWith(SpringExtension::class)
@WebFluxTest(controllers = [ExportNormController::class])
@WithMockUser
class ExportNormControllerTest {
    @Autowired lateinit var webClient: WebTestClient

    @MockkBean lateinit var exportNormService: ExportNormUseCase

    @MockkBean lateinit var getFileOutputPort: GetFileOutputPort

    private val file: ByteBuffer = ByteBuffer.allocate(0)

    private val guidExample = "761b5537-5aa5-4901-81f7-fbf7e040a7c8"
    private val hashExample = "1c47461aea72b7d4f36c075fe09ae283e78477d261e5b1141c510cb46c941d10"
    private val uriExample = "/api/v1/norms/$guidExample/files/$hashExample"

    @Test
    fun `it calls the export norm service with the correct command`() {
        every { exportNormService.exportNorm(any()) } returns Mono.empty()

        webClient
            .mutateWith(csrf())
            .get()
            .uri(uriExample)
            .exchange()

        val query = slot<ExportNormUseCase.Command>()
        verify(exactly = 1) { exportNormService.exportNorm(capture(query)) }
        assertThat(query.captured.guid.toString()).isEqualTo(guidExample)
        assertThat(query.captured.hash).isEqualTo(hashExample)
    }

    @Test
    fun `it responds with file`() {
        every { exportNormService.exportNorm(any()) } returns Mono.just(file.array())

        webClient
            .mutateWith(csrf())
            .get()
            .uri(uriExample)
            .exchange()
            .expectStatus()
            .isOk
            .expectBody(ByteArray::class.java)
            .isEqualTo(file.array())
    }

    @Test
    fun `it sends an internal error response if the export norm service throws an exception`() {
        every { exportNormService.exportNorm(any()) } throws Error()

        webClient
            .mutateWith(csrf())
            .get()
            .uri(uriExample)
            .exchange()
            .expectStatus()
            .is5xxServerError
    }

    @Test
    fun `it sends an internal error response if the get file port throws an exception`() {
        every { getFileOutputPort.getFile(any()) } throws Error()

        webClient
            .mutateWith(csrf())
            .get()
            .uri(uriExample)
            .exchange()
            .expectStatus()
            .is5xxServerError
    }
}
