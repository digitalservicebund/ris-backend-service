package de.bund.digitalservice.ris.norms.framework.adapter.input.restapi

import com.ninjasquad.springmockk.MockkBean
import de.bund.digitalservice.ris.norms.application.port.input.ListNormsUseCase
import de.bund.digitalservice.ris.norms.application.port.input.LoadNormUseCase
import de.bund.digitalservice.ris.norms.domain.entity.Article
import de.bund.digitalservice.ris.norms.domain.entity.Norm
import de.bund.digitalservice.ris.norms.domain.entity.Paragraph
import de.bund.digitalservice.ris.norms.domain.value.Guid
import io.mockk.every
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.csrf
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.reactive.server.WebTestClient
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@ExtendWith(SpringExtension::class)
@WebFluxTest(controllers = [QueryNormsController::class])
@WithMockUser
class QueryNormsControllerTest {
    @Autowired lateinit var webClient: WebTestClient

    @MockkBean lateinit var listNormsService: ListNormsUseCase

    @MockkBean lateinit var loadNormService: LoadNormUseCase

    @Test
    fun `it calls the load norm service with the correct query to get a norm by GUID`() {
        val norm = Norm(Guid.fromString("761b5537-5aa5-4901-81f7-fbf7e040a7c8"), "long title")

        every { loadNormService.loadNorm(any()) } returns Mono.just(norm)

        webClient
            .mutateWith(csrf())
            .get()
            .uri("/api/v1/norms/761b5537-5aa5-4901-81f7-fbf7e040a7c8")
            .exchange()

        val query = slot<LoadNormUseCase.Query>()
        verify(exactly = 1) { loadNormService.loadNorm(capture(query)) }
        assertTrue(query.captured.guid.toString() == "761b5537-5aa5-4901-81f7-fbf7e040a7c8")
    }

    @Test
    fun `it responds with ok status if the norm was loaded successfully`() {
        val norm = Norm(Guid.fromString("761b5537-5aa5-4901-81f7-fbf7e040a7c8"), "long title")

        every { loadNormService.loadNorm(any()) } returns Mono.just(norm)

        webClient
            .mutateWith(csrf())
            .get()
            .uri("/api/v1/norms/761b5537-5aa5-4901-81f7-fbf7e040a7c8")
            .exchange()
            .expectStatus()
            .isOk()
    }

    @Test
    fun `it maps the norm entity to the expected data schema`() {
        val paragraphGuid = Guid.fromString("e0cbf06c-cd8b-4647-bb8a-263b43f0f974")
        val paragraph = Paragraph(paragraphGuid, "marker", "text")
        val articleGuid = Guid.fromString("53d29ef7-377c-4d14-864b-eb3a85769359")
        val article = Article(articleGuid, "title", "marker", listOf(paragraph))
        val normGuid = Guid.fromString("72631e54-78a4-11d0-bcf7-00aa00b7b32a")
        val norm = Norm(normGuid, "long title", listOf(article))

        every { loadNormService.loadNorm(any()) } returns Mono.just(norm)

        webClient
            .mutateWith(csrf())
            .get()
            .uri("/api/v1/norms/72631e54-78a4-11d0-bcf7-00aa00b7b32a")
            .exchange()
            .expectBody()
            .json(
                """
        {
          "guid": "72631e54-78a4-11d0-bcf7-00aa00b7b32a",
          "longTitle": "long title",
          "articles": [
            {
              "guid": "53d29ef7-377c-4d14-864b-eb3a85769359",
              "title": "title",
              "marker": "marker",
              "paragraphs": [
                {
                  "guid": "e0cbf06c-cd8b-4647-bb8a-263b43f0f974",
                  "marker": "marker",
                  "text": "text"
                }
              ]
            }
          ]
        }
        """
            )
    }

    @Test
    fun `it sends a not found response with empty body if the load norm service responds with empty`() {
        every { loadNormService.loadNorm(any()) } returns Mono.empty()

        webClient
            .mutateWith(csrf())
            .get()
            .uri("/api/v1/norms/72631e54-78a4-11d0-bcf7-00aa00b7b32a")
            .exchange()
            .expectStatus()
            .isNotFound()
            .expectBody()
            .isEmpty()
    }

    @Test
    fun `it sends an internal error response if the load norm service throws an exception`() {
        every { loadNormService.loadNorm(any()) } throws Error()

        webClient
            .mutateWith(csrf())
            .get()
            .uri("/api/v1/norms/72631e54-78a4-11d0-bcf7-00aa00b7b32a")
            .exchange()
            .expectStatus()
            .is5xxServerError()
    }

    @Test
    fun `it calls the list norms service to get all norms`() {
        every { listNormsService.listNorms() } returns Flux.empty()

        webClient.mutateWith(csrf()).get().uri("/api/v1/norms").exchange()

        verify(exactly = 1) { listNormsService.listNorms() }
    }

    @Test
    fun `it always responds an ok status also if the service lists no norms`() {
        every { listNormsService.listNorms() } returns Flux.empty()

        webClient.mutateWith(csrf()).get().uri("/api/v1/norms").exchange().expectStatus().isOk()
    }

    @Test
    fun `it reponds with a data property that holds the list of norms`() {
        val norm = Norm(Guid.generateNew(), "long title")
        every { listNormsService.listNorms() } returns Flux.fromArray(arrayOf(norm))

        webClient
            .mutateWith(csrf())
            .get()
            .uri("/api/v1/norms")
            .exchange()
            .expectBody()
            .jsonPath("data")
            .isArray()
    }

    @Test
    fun `it sends an internal error response if the list norms service throws an exception`() {
        every { listNormsService.listNorms() } throws Error()

        webClient
            .mutateWith(csrf())
            .get()
            .uri("/api/v1/norms")
            .exchange()
            .expectStatus()
            .is5xxServerError()
    }
}
