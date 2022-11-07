package de.bund.digitalservice.ris.norms.framework.adapter.input.restapi

import com.ninjasquad.springmockk.MockkBean
import de.bund.digitalservice.ris.norms.application.port.input.ImportNormUseCase
import io.mockk.every
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.csrf
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.web.reactive.function.BodyInserters
import reactor.core.publisher.Mono
import java.util.*

@ExtendWith(SpringExtension::class)
@WebFluxTest(controllers = [ImportNormController::class])
@WithMockUser
class ImportNormControllerTest {
    @Autowired lateinit var webClient: WebTestClient

    @MockkBean lateinit var importNormService: ImportNormUseCase

    @Test
    fun `it correctly maps the data to the command to call the import norm service`() {
        val importJson =
            """
        {
          "longTitle": "long title",
          "articles": [
            {
            "title": "title",
            "marker": "marker",
            "paragraphs": [{ "marker": "marker", "text": "text" }]
            }
          ]
        }
    """

        every { importNormService.importNorm(any()) } returns Mono.just(UUID.randomUUID())

        webClient
            .mutateWith(csrf())
            .post()
            .uri("/api/v1/norms")
            .contentType(APPLICATION_JSON)
            .body(BodyInserters.fromValue(importJson))
            .exchange()

        val command = slot<ImportNormUseCase.Command>()
        verify(exactly = 1) { importNormService.importNorm(capture(command)) }

        assertTrue(command.captured.data.longTitle == "long title")
        assertTrue(command.captured.data.articles.size == 1)
        assertTrue(command.captured.data.articles[0].title == "title")
        assertTrue(command.captured.data.articles[0].marker == "marker")
        assertTrue(command.captured.data.articles[0].paragraphs.size == 1)
        assertTrue(command.captured.data.articles[0].paragraphs[0].marker == "marker")
        assertTrue(command.captured.data.articles[0].paragraphs[0].text == "text")
    }

    @Test
    fun `it responds with created status when norm was imported`() {
        every { importNormService.importNorm(any()) } returns Mono.just(UUID.randomUUID())

        webClient
            .mutateWith(csrf())
            .post()
            .uri("/api/v1/norms")
            .contentType(APPLICATION_JSON)
            .body(BodyInserters.fromValue("""{ "longTitle": "long title" }"""))
            .exchange()
            .expectStatus()
            .isCreated()
    }

    @Test
    fun `it uses the new GUID from the service an creates a location header for the imported norm`() {
        every { importNormService.importNorm(any()) } returns
            Mono.just(UUID.fromString("761b5537-5aa5-4901-81f7-fbf7e040a7c8"))

        webClient
            .mutateWith(csrf())
            .post()
            .uri("/api/v1/norms")
            .contentType(APPLICATION_JSON)
            .body(BodyInserters.fromValue("""{ "longTitle": "long title" }"""))
            .exchange()
            .expectHeader()
            .location("/api/v1/norms/761b5537-5aa5-4901-81f7-fbf7e040a7c8")
    }

    @Test
    fun `it sends an internal error response if the import norm service throws an exception`() {
        every { importNormService.importNorm(any()) } throws Error()

        webClient
            .mutateWith(csrf())
            .post()
            .uri("/api/v1/norms")
            .contentType(APPLICATION_JSON)
            .body(BodyInserters.fromValue("""{ "longTitle": "long title" }"""))
            .exchange()
            .expectStatus()
            .is5xxServerError()
    }
}
