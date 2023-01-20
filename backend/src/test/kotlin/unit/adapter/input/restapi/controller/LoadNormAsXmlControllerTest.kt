package de.bund.digitalservice.ris.norms.framework.adapter.input.restapi.controller

import com.ninjasquad.springmockk.MockkBean
import de.bund.digitalservice.ris.norms.application.port.input.LoadNormAsXmlUseCase
import io.mockk.every
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.csrf
import org.springframework.test.web.reactive.server.WebTestClient
import reactor.core.publisher.Mono

@WebFluxTest(controllers = [LoadNormAsXmlController::class])
@WithMockUser
class LoadNormAsXmlControllerTest {
    @Autowired lateinit var webClient: WebTestClient

    @MockkBean lateinit var loadNormAsXmlService: LoadNormAsXmlUseCase

    @Test
    fun `it calls the load service with the correct query to get the XML`() {
        every { loadNormAsXmlService.loadNormAsXml(any()) } returns Mono.just("")

        webClient
            .mutateWith(csrf())
            .get()
            .uri("/api/v1/norms/xml/eli/bg-1/2022/s1125")
            .exchange()

        verify(exactly = 1) {
            loadNormAsXmlService.loadNormAsXml(
                withArg {
                    assertThat(it.printAnnouncementGazette)
                        .isEqualTo("bg-1")
                    assertThat(it.announcementOrCitationYear)
                        .isEqualTo("2022")
                    assertThat(it.printAnnouncementPage)
                        .isEqualTo("1125")
                }
            )
        }
    }

    @Test
    fun `it responds with ok status if the norm was loaded successfully`() {
        every { loadNormAsXmlService.loadNormAsXml(any()) } returns Mono.just("")

        webClient
            .mutateWith(csrf())
            .get()
            .uri("/api/v1/norms/xml/eli/bg-1/2022/s1125")
            .exchange()
            .expectStatus()
            .isOk()
    }

    @Test
    fun `it includes the converted XML content within the response body`() {
        every { loadNormAsXmlService.loadNormAsXml(any()) } returns
            Mono.just("<?xml version=\"1.0\" encoding=\"utf-8\"?><foo/>")

        webClient
            .mutateWith(csrf())
            .get()
            .uri("/api/v1/norms/xml/eli/bg-1/2022/s1125")
            .exchange()
            .expectBody()
            .xml("<?xml version=\"1.0\" encoding=\"utf-8\"?><foo/>")
    }

    @Test
    fun `it sends a not found response with empty body if the load service responds with empty`() {
        every { loadNormAsXmlService.loadNormAsXml(any()) } returns Mono.empty()

        webClient
            .mutateWith(csrf())
            .get()
            .uri("/api/v1/norms/xml/eli/bg-1/2022/s1125")
            .exchange()
            .expectStatus()
            .isNotFound()
            .expectBody()
            .isEmpty()
    }

    @Test
    fun `it sends an internal error response if the load service throws an exception`() {
        every { loadNormAsXmlService.loadNormAsXml(any()) } throws Error()

        webClient
            .mutateWith(csrf())
            .get()
            .uri("/api/v1/norms/xml/eli/bg-1/2022/s1125")
            .exchange()
            .expectStatus()
            .is5xxServerError()
    }
}
