package de.bund.digitalservice.ris.norms.framework.adapter.input.restapi.controller

import com.ninjasquad.springmockk.MockkBean
import de.bund.digitalservice.ris.norms.application.port.input.GenerateNormFileUseCase
import io.mockk.every
import io.mockk.slot
import io.mockk.verify
import java.time.LocalDateTime
import java.util.*
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
import utils.factory.file

@ExtendWith(SpringExtension::class)
@WebFluxTest(controllers = [GenerateNormFileController::class])
@WithMockUser
class GenerateNormFileControllerTest {
  @Autowired lateinit var webClient: WebTestClient

  @MockkBean lateinit var generateNormFileService: GenerateNormFileUseCase

  @Test
  fun `it calls the generate norm file service with the correct command`() {

    val fileReference = file {
      guid = UUID.randomUUID()
      name = "norm.zip"
      hash = "1c47461aea72b7d4f36c075fe09ae283e78477d261e5b1141c510cb46c941d10"
      createdAt = LocalDateTime.now()
    }

    every { generateNormFileService.generateNormFile(any()) } returns Mono.just(fileReference)

    webClient
        .mutateWith(csrf())
        .post()
        .uri("/api/v1/norms/761b5537-5aa5-4901-81f7-fbf7e040a7c8/files")
        .exchange()

    val command = slot<GenerateNormFileUseCase.Command>()
    verify(exactly = 1) { generateNormFileService.generateNormFile(capture(command)) }
    assertThat(command.captured.guid.toString()).isEqualTo("761b5537-5aa5-4901-81f7-fbf7e040a7c8")
  }

  @Test
  fun `it responds with ok status if the file reference was created successfully`() {

    val fileReference = file {
      guid = UUID.randomUUID()
      name = "norm.zip"
      hash = "1c47461aea72b7d4f36c075fe09ae283e78477d261e5b1141c510cb46c941d10"
      createdAt = LocalDateTime.now()
    }

    every { generateNormFileService.generateNormFile(any()) } returns Mono.just(fileReference)

    webClient
        .mutateWith(csrf())
        .post()
        .uri("/api/v1/norms/761b5537-5aa5-4901-81f7-fbf7e040a7c8/files")
        .exchange()
        .expectStatus()
        .isOk()
  }

  @Test
  fun `it maps the file reference entity to the expected response schema`() {
    val fileReference = file {
      name = "norm.zip"
      hash = "1c47461aea72b7d4f36c075fe09ae283e78477d261e5b1141c510cb46c941d10"
      createdAt = LocalDateTime.parse("2023-09-05T09:31:21.390936")
      guid = UUID.randomUUID()
    }

    every { generateNormFileService.generateNormFile(any()) } returns Mono.just(fileReference)

    webClient
        .mutateWith(csrf())
        .post()
        .uri("/api/v1/norms/72631e54-78a4-11d0-bcf7-00aa00b7b32a/files")
        .exchange()
        .expectBody()
        .json(
            """
                {
                    "name": "norm.zip",
                    "hash": "1c47461aea72b7d4f36c075fe09ae283e78477d261e5b1141c510cb46c941d10",
                    "createdAt": "2023-09-05T09:31:21.390936",
                    "guid": "${fileReference.guid}"
                }
        """)
  }

  @Test
  fun `it sends a not found response with an error list if the the norm with the guid was not found in the service`() {
    every { generateNormFileService.generateNormFile(any()) } returns Mono.empty()

    webClient
        .mutateWith(csrf())
        .post()
        .uri("/api/v1/norms/72631e54-78a4-11d0-bcf7-00aa00b7b32a/files")
        .exchange()
        .expectStatus()
        .isNotFound()
        .expectBody()
        .json(
            """
                {
                  "errors" : [
                      {
                        "code" : "NOT_FOUND",
                        "instance" : "72631e54-78a4-11d0-bcf7-00aa00b7b32a",
                        "message": ""
                      }
                  ]
                }
                """
                .trimIndent(),
        )
  }

  @Test
  fun `it sends an internal error response if the generate norm file service throws an exception`() {
    every { generateNormFileService.generateNormFile(any()) } throws Error()

    webClient
        .mutateWith(csrf())
        .post()
        .uri("/api/v1/norms/72631e54-78a4-11d0-bcf7-00aa00b7b32a/files")
        .exchange()
        .expectStatus()
        .is5xxServerError()
  }
}
