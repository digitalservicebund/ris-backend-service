package de.bund.digitalservice.ris.norms.framework.adapter.input.restapi.controller

import com.google.gson.Gson
import com.ninjasquad.springmockk.MockkBean
import de.bund.digitalservice.ris.norms.application.port.input.ValidateNormFrameUseCase
import de.bund.digitalservice.ris.norms.domain.specification.SpecificationResult
import de.bund.digitalservice.ris.norms.framework.adapter.input.restapi.schema.MetadataSectionRequestSchema
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.util.UUID
import org.assertj.core.api.Assertions.assertThat
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
import utils.*

@ExtendWith(SpringExtension::class)
@WebFluxTest(controllers = [ValidateNormFrameController::class])
@WithMockUser
class ValidateNormFrameControllerTest {
  @Autowired lateinit var webClient: WebTestClient

  @MockkBean lateinit var validateNormFrameService: ValidateNormFrameUseCase

  @Test
  fun `it correctly maps the body to the command calling the service`() {
    val validateNormRequestSchema = createValidValidateNormFrameTestRequestSchema()
    val sections = validateNormRequestSchema.metadataSections.map { it.toUseCaseData() }
    val command: ValidateNormFrameUseCase.Command = mockk()
    every { command.metadataSections } returns sections
    every { validateNormFrameService.validateNormFrame(any()) } returns
        Mono.just(SpecificationResult.Satisfied)

    webClient
        .mutateWith(csrf())
        .post()
        .uri("/api/v1/norms/norm/validation")
        .contentType(APPLICATION_JSON)
        .body(BodyInserters.fromValue(Gson().toJson(validateNormRequestSchema)))
        .exchange()

    verify(exactly = 1) {
      validateNormFrameService.validateNormFrame(
          withArg {
            assertThat(command)
                .usingRecursiveComparison()
                .ignoringFieldsOfTypes(UUID::class.java)
                .isEqualTo(it)
          })
    }
  }

  @Test
  fun `it responds with no violations when norm was validated successfully`() {
    every { validateNormFrameService.validateNormFrame(any()) } returns
        Mono.just(SpecificationResult.Satisfied)

    webClient
        .mutateWith(csrf())
        .post()
        .uri("/api/v1/norms/norm/validation")
        .contentType(APPLICATION_JSON)
        .body(BodyInserters.fromValue("""{ "metadataSections": [] }"""))
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody()
        .json("[]")
  }

  @Test
  fun `it sends an server internal error response if the validate norm frame service throws an exception`() {
    every { validateNormFrameService.validateNormFrame(any()) } throws Error()

    webClient
        .mutateWith(csrf())
        .post()
        .uri("/api/v1/norms/norm/validation")
        .contentType(APPLICATION_JSON)
        .body(BodyInserters.fromValue("""{ "metadataSections": [] }"""))
        .exchange()
        .expectStatus()
        .is5xxServerError
  }

  data class ValidateNormFrameTestRequestSchema(
      var metadataSections: List<MetadataSectionRequestSchema>
  )
}
