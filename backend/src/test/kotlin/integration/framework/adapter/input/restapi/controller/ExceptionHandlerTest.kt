package de.bund.digitalservice.ris.norms.framework.adapter.input.restapi.controller

import de.bund.digitalservice.ris.caselaw.config.FlywayConfig
import de.bund.digitalservice.ris.norms.application.service.LoadNormService
import de.bund.digitalservice.ris.norms.framework.adapter.output.database.NormsService
import de.bund.digitalservice.ris.norms.framework.adapter.output.database.PostgresTestcontainerIntegrationTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.r2dbc.AutoConfigureDataR2dbc
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.context.annotation.Import
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.csrf
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.reactive.server.WebTestClient

@ExtendWith(SpringExtension::class)
@Import(FlywayConfig::class, NormsService::class, LoadNormService::class)
@WebFluxTest(controllers = [LoadNormController::class])
@WithMockUser
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@AutoConfigureDataR2dbc
class ExceptionHandlerTest : PostgresTestcontainerIntegrationTest() {
    @Autowired
    lateinit var webClient: WebTestClient

    @Test
    fun `it shows proper message on internal server error`() {
        webClient
            .mutateWith(csrf())
            .get()
            .uri("/api/v1/norms/" + "invalidGUID")
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
                        "attribute" : null
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
