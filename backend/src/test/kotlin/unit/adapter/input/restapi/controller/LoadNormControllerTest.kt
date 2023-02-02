package de.bund.digitalservice.ris.norms.framework.adapter.input.restapi.controller

import com.ninjasquad.springmockk.MockkBean
import de.bund.digitalservice.ris.norms.application.port.input.LoadNormUseCase
import de.bund.digitalservice.ris.norms.domain.entity.Norm
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
import utils.convertNormToJson
import utils.createRandomNorm
import java.util.*

@ExtendWith(SpringExtension::class)
@WebFluxTest(controllers = [LoadNormController::class])
@WithMockUser
class LoadNormControllerTest {
    @Autowired lateinit var webClient: WebTestClient

    @MockkBean lateinit var loadNormService: LoadNormUseCase

    @Test
    fun `it calls the load norm service with the correct query to get a norm by GUID`() {
        val norm =
            Norm(
                UUID.fromString("761b5537-5aa5-4901-81f7-fbf7e040a7c8"),
                officialLongTitle = "long title",
            )

        every { loadNormService.loadNorm(any()) } returns Mono.just(norm)

        webClient
            .mutateWith(csrf())
            .get()
            .uri("/api/v1/norms/761b5537-5aa5-4901-81f7-fbf7e040a7c8")
            .exchange()

        val query = slot<LoadNormUseCase.Query>()
        verify(exactly = 1) { loadNormService.loadNorm(capture(query)) }
        assertThat(query.captured.guid.toString()).isEqualTo("761b5537-5aa5-4901-81f7-fbf7e040a7c8")
    }

    @Test
    fun `it responds with ok status if the norm was loaded successfully`() {
        val norm =
            Norm(
                UUID.fromString("761b5537-5aa5-4901-81f7-fbf7e040a7c8"),
                officialLongTitle = "long title",
            )

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
        val norm = createRandomNorm()
        val responseJson = convertNormToJson(LoadNormController.NormResponseSchema.fromUseCaseData(norm))

        every { loadNormService.loadNorm(any()) } returns Mono.just(norm)

        webClient
            .mutateWith(csrf())
            .get()
            .uri("/api/v1/norms/72631e54-78a4-11d0-bcf7-00aa00b7b32a")
            .exchange()
            .expectBody()
            .json(responseJson, true)
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
}
