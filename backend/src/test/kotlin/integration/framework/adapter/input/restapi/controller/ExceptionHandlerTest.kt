package de.bund.digitalservice.ris.norms.framework.adapter.input.restapi.controller

import com.ninjasquad.springmockk.MockkBean
import de.bund.digitalservice.ris.exceptions.exception.NotFoundWithInstanceException
import de.bund.digitalservice.ris.exceptions.exception.ValidationException
import de.bund.digitalservice.ris.exceptions.response.ErrorDetails
import de.bund.digitalservice.ris.norms.application.port.input.LoadNormUseCase
import de.bund.digitalservice.ris.norms.application.service.LoadNormService
import de.bund.digitalservice.ris.norms.framework.adapter.output.database.PostgresTestcontainerIntegrationTest
import io.mockk.every
import java.net.URI
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.r2dbc.AutoConfigureDataR2dbc
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.context.annotation.Import
import org.springframework.data.crossstore.ChangeSetPersister
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.csrf
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.reactive.server.WebTestClient

@ExtendWith(SpringExtension::class)
@Import(LoadNormService::class)
@WebFluxTest(controllers = [LoadNormController::class])
@WithMockUser
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@AutoConfigureDataR2dbc
class ExceptionHandlerTest : PostgresTestcontainerIntegrationTest() {
  @Autowired lateinit var webClient: WebTestClient

  @MockkBean lateinit var loadNormService: LoadNormUseCase

  @Test
  fun `it shows validation error`() {
    every { loadNormService.loadNorm(any()) } throws
        ValidationException(
            mutableListOf(ErrorDetails("ERROR")),
        )

    webClient
        .mutateWith(csrf())
        .get()
        .uri("/api/v1/norms/" + "2fb72e06-60c4-4912-9f27-0a776a91852d")
        .exchange()
        .expectStatus()
        .is4xxClientError
        .expectBody()
        .json(
            """
                {
                  "errors" : [
                      {
                        "code" : "ERROR",
                        "instance" : "",
                        "message": ""
                      }
                  ]
                }
                """
                .trimIndent(),
        )
  }

  @Test
  fun `it shows proper message on page not found`() {
    every { loadNormService.loadNorm(any()) } throws ChangeSetPersister.NotFoundException()

    webClient
        .mutateWith(csrf())
        .get()
        .uri("/api/v1/norms/" + "2fb72e06-60c4-4912-9f27-0a776a91852d")
        .exchange()
        .expectStatus()
        .isNotFound
        .expectBody()
        .json(
            """
                {
                  "errors" : [
                      {
                        "code" : "NOT_FOUND",
                        "instance" : "",
                        "message": ""
                      }
                  ]
                }
                """
                .trimIndent(),
        )
  }

  @Test
  fun `it shows instance on page not found`() {
    every { loadNormService.loadNorm(any()) } throws NotFoundWithInstanceException(URI("test"))

    webClient
        .mutateWith(csrf())
        .get()
        .uri("/api/v1/norms/" + "2fb72e06-60c4-4912-9f27-0a776a91852d")
        .exchange()
        .expectStatus()
        .isNotFound
        .expectBody()
        .json(
            """
                {
                  "errors" : [
                      {
                        "code" : "NOT_FOUND",
                        "instance" : "test",
                        "message": ""
                      }
                  ]
                }
                """
                .trimIndent(),
        )
  }
}
