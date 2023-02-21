package de.bund.digitalservice.ris.norms.framework.adapter.input.restapi.controller

import com.ninjasquad.springmockk.MockkBean
import de.bund.digitalservice.ris.norms.application.port.input.ImportNormUseCase
import io.mockk.every
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
import org.springframework.web.reactive.function.BodyInserters
import reactor.core.publisher.Mono
import java.io.File
import java.util.UUID

@ExtendWith(SpringExtension::class)
@WebFluxTest(controllers = [ImportNormController::class])
@WithMockUser
class ImportNormControllerTest {
    @Autowired lateinit var webClient: WebTestClient

    @MockkBean lateinit var importNormService: ImportNormUseCase

    @Test
    fun `it calls the import norm service with the content of the body as ZIP file`() {
        val zipFile = File.createTempFile("Temp", ".zip")
        every { importNormService.importNorm(any()) } returns Mono.empty()

        webClient
            .mutateWith(csrf())
            .post()
            .uri("/api/v1/norms")
            .body(BodyInserters.fromValue(zipFile))
            .exchange()

        verify(exactly = 1) {
            importNormService.importNorm(withArg { assertThat(it.zipFile).isEqualTo(zipFile) })
        }
    }

    @Test
    fun `it responds with created status when norm was imported`() {
        every { importNormService.importNorm(any()) } returns
            Mono.just(UUID.fromString("761b5537-5aa5-4901-81f7-fbf7e040a7c8"))

        webClient
            .mutateWith(csrf())
            .post()
            .uri("/api/v1/norms")
            .body(BodyInserters.fromValue(File.createTempFile("Temp", ".zip")))
            .exchange()
            .expectStatus()
            .isCreated()
    }

    @Test
    fun `it sends the GUID of the new norm in the response object as property`() {
        every { importNormService.importNorm(any()) } returns
            Mono.just(UUID.fromString("761b5537-5aa5-4901-81f7-fbf7e040a7c8"))

        webClient
            .mutateWith(csrf())
            .post()
            .uri("/api/v1/norms")
            .body(BodyInserters.fromValue(File.createTempFile("Temp", ".zip")))
            .exchange()
            .expectBody()
            .jsonPath("guid")
            .exists()
            .jsonPath("guid")
            .isEqualTo("761b5537-5aa5-4901-81f7-fbf7e040a7c8")
    }

    @Test
    fun `it sends an internal error response if the import norm service throws an exception`() {
        every { importNormService.importNorm(any()) } throws Error()

        webClient
            .mutateWith(csrf())
            .post()
            .uri("/api/v1/norms")
            .body(BodyInserters.fromValue(File.createTempFile("Temp", ".zip")))
            .exchange()
            .expectStatus()
            .is5xxServerError()
    }
}
