package de.bund.digitalservice.ris.norms.framework.adapter.input.restapi.controller

import com.ninjasquad.springmockk.MockkBean
import de.bund.digitalservice.ris.caselaw.config.FlywayConfig
import de.bund.digitalservice.ris.norms.application.port.input.LoadNormUseCase
import de.bund.digitalservice.ris.norms.application.service.LoadNormService
import de.bund.digitalservice.ris.norms.framework.adapter.output.database.NormsService
import de.bund.digitalservice.ris.norms.framework.adapter.output.database.PostgresTestcontainerIntegrationTest
import io.mockk.every
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.r2dbc.AutoConfigureDataR2dbc
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.context.annotation.Import
import org.springframework.http.HttpStatus
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.csrf
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.web.client.HttpServerErrorException

@ExtendWith(SpringExtension::class)
@Import(FlywayConfig::class, NormsService::class, LoadNormService::class)
@WebFluxTest(controllers = [LoadNormController::class])
@WithMockUser
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@AutoConfigureDataR2dbc
class ExceptionHandlerTest : PostgresTestcontainerIntegrationTest() {
    @Autowired
    lateinit var webClient: WebTestClient

    @MockkBean
    lateinit var loadNormService: LoadNormUseCase

    @Test
    fun `it shows proper message on internal server error`() {
        val message = "Internal Server Error"
        val exception = HttpServerErrorException(
            HttpStatus.INTERNAL_SERVER_ERROR,
            message,
        )
        every { loadNormService.loadNorm(any()) } throws exception

        webClient
            .mutateWith(csrf())
            .get()
            .uri("/api/v1/norms/" + "2fb72e06-60c4-4912-9f27-0a776a91852d")
            .exchange()
            .expectStatus()
            .is5xxServerError
            .expectBody()
            .json(
                """
                {
                  "errors" : [
                      {
                        "code" : "SERVER_ERROR",
                        "attribute" : "",
                        "message": "server error"
                      }
                  ]
                }
                """.trimIndent(),
            )
    }

    @Test
    fun `it shows proper message on page not found`() {
        webClient
            .mutateWith(csrf())
            .get()
            .uri("/api/v1/normss")
            .exchange()
            .expectStatus()
            .isNotFound
    }
}
