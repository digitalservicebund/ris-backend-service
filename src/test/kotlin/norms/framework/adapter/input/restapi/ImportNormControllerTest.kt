package de.bund.digitalservice.ris.norms.framework.adapter.input.restapi

import com.ninjasquad.springmockk.MockkBean
import de.bund.digitalservice.ris.norms.application.port.input.ImportNormUseCase
import io.mockk.every
import io.mockk.verify
import norms.utils.assertNormDataAndImportNormRequestSchemaWithoutArticles
import norms.utils.convertImportormRequestSchemaToJson
import norms.utils.createRandomImportNormRequestSchema
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
import java.util.UUID

@ExtendWith(SpringExtension::class)
@WebFluxTest(controllers = [ImportNormController::class])
@WithMockUser
class ImportNormControllerTest {
    @Autowired lateinit var webClient: WebTestClient

    @MockkBean lateinit var importNormService: ImportNormUseCase

    @Test
    fun `it correctly maps the data to the command to call the import norm service`() {
        val importNormRequestSchema = createRandomImportNormRequestSchema()
        val importJson = convertImportormRequestSchemaToJson(importNormRequestSchema)

        every { importNormService.importNorm(any()) } returns Mono.just(UUID.randomUUID())

        webClient
            .mutateWith(csrf())
            .post()
            .uri("/api/v1/norms")
            .contentType(APPLICATION_JSON)
            .body(BodyInserters.fromValue(importJson))
            .exchange()

        verify(exactly = 1) {
            importNormService.importNorm(
                withArg {
                    assertTrue(it.data.articles.size == 2)
                    assertTrue(it.data.articles[0].title == importNormRequestSchema.articles[0].title)
                    assertTrue(it.data.articles[0].marker == importNormRequestSchema.articles[0].marker)
                    assertTrue(it.data.articles[0].paragraphs.size == 2)
                    assertTrue(it.data.articles[0].paragraphs[0].marker == importNormRequestSchema.articles[0].paragraphs[0].marker)
                    assertTrue(it.data.articles[0].paragraphs[0].text == importNormRequestSchema.articles[0].paragraphs[0].text)
                    assertTrue(it.data.articles[0].paragraphs[1].marker == importNormRequestSchema.articles[0].paragraphs[1].marker)
                    assertTrue(it.data.articles[0].paragraphs[1].text == importNormRequestSchema.articles[0].paragraphs[1].text)
                    assertTrue(it.data.articles[1].title == importNormRequestSchema.articles[1].title)
                    assertTrue(it.data.articles[1].marker == importNormRequestSchema.articles[1].marker)
                    assertTrue(it.data.articles[1].paragraphs.size == 2)
                    assertTrue(it.data.articles[1].paragraphs[0].marker == importNormRequestSchema.articles[1].paragraphs[0].marker)
                    assertTrue(it.data.articles[1].paragraphs[0].text == importNormRequestSchema.articles[1].paragraphs[0].text)
                    assertTrue(it.data.articles[1].paragraphs[1].marker == importNormRequestSchema.articles[1].paragraphs[1].marker)
                    assertTrue(it.data.articles[1].paragraphs[1].text == importNormRequestSchema.articles[1].paragraphs[1].text)
                    assertNormDataAndImportNormRequestSchemaWithoutArticles(it.data, importNormRequestSchema)
                }
            )
        }
    }

    @Test
    fun `it responds with created status when norm was imported`() {
        every { importNormService.importNorm(any()) } returns Mono.just(UUID.randomUUID())

        webClient
            .mutateWith(csrf())
            .post()
            .uri("/api/v1/norms")
            .contentType(APPLICATION_JSON)
            .body(BodyInserters.fromValue("""{ "officialLongTitle": "long title" }"""))
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
            .body(BodyInserters.fromValue("""{ "officialLongTitle": "long title" }"""))
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
            .body(BodyInserters.fromValue("""{ "officialLongTitle": "long title" }"""))
            .exchange()
            .expectStatus()
            .is5xxServerError()
    }
}
