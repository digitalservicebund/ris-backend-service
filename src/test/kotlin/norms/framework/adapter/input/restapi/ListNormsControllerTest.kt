package de.bund.digitalservice.ris.norms.framework.adapter.input.restapi

import com.ninjasquad.springmockk.MockkBean
import de.bund.digitalservice.ris.norms.application.port.input.ListNormsUseCase
import de.bund.digitalservice.ris.norms.application.port.input.ListNormsUseCase.NormData
import io.mockk.every
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.csrf
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.reactive.server.WebTestClient
import reactor.core.publisher.Flux
import java.util.UUID

@ExtendWith(SpringExtension::class)
@WebFluxTest(controllers = [ListNormsController::class])
@WithMockUser
class ListNormsControllerTest {
    @Autowired lateinit var webClient: WebTestClient

    @MockkBean lateinit var listNormsService: ListNormsUseCase

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
        val norm = NormData(UUID.randomUUID(), "long title")
        every { listNormsService.listNorms() } returns Flux.fromArray(arrayOf(norm))

        webClient
            .mutateWith(csrf())
            .get()
            .uri("/api/v1/norms")
            .exchange()
            .expectBody()
            .jsonPath("data")
            .exists()
            .jsonPath("data")
            .isArray()
    }

    @Test
    fun `it maps the norm entity to the expected data schema`() {
        val normOne = NormData(UUID.fromString("761b5537-5aa5-4901-81f7-fbf7e040a7c8"), "first title")
        val normTwo = NormData(UUID.fromString("53d29ef7-377c-4d14-864b-eb3a85769359"), "second title")
        every { listNormsService.listNorms() } returns Flux.fromArray(arrayOf(normOne, normTwo))

        webClient
            .mutateWith(csrf())
            .get()
            .uri("/api/v1/norms")
            .exchange()
            .expectBody()
            .json(
                """
        {
          "data": [
            { "guid": "761b5537-5aa5-4901-81f7-fbf7e040a7c8", "officialLongTitle": "first title" },
            { "guid": "53d29ef7-377c-4d14-864b-eb3a85769359", "officialLongTitle": "second title" }
          ]
        }
        """
            )
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
