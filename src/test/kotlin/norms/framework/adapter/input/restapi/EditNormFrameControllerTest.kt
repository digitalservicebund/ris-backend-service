package de.bund.digitalservice.ris.norms.framework.adapter.input.restapi

import com.ninjasquad.springmockk.MockkBean
import de.bund.digitalservice.ris.norms.application.port.input.EditNormFrameUseCase
import io.mockk.every
import io.mockk.verify
import norms.utils.assertEditNormFramePropertiesAndEditNormRequestSchema
import norms.utils.convertEditNormRequestSchemaToJson
import norms.utils.createRandomEditNormRequestSchema
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
@WebFluxTest(controllers = [EditNormFrameController::class])
@WithMockUser
class EditNormFrameControllerTest {
    @Autowired lateinit var webClient: WebTestClient

    @MockkBean lateinit var editNormFrameService: EditNormFrameUseCase

    @Test
    fun `it correctly maps the parameter and body to the command calling the service`() {
        val editNormRequestSchema = createRandomEditNormRequestSchema()
        val editJson = convertEditNormRequestSchemaToJson(editNormRequestSchema)

        every { editNormFrameService.editNormFrame(any()) } returns Mono.just(true)

        webClient
            .mutateWith(csrf())
            .put()
            .uri("/api/v1/norms/761b5537-5aa5-4901-81f7-fbf7e040a7c8")
            .contentType(APPLICATION_JSON)
            .body(BodyInserters.fromValue(editJson))
            .exchange()

        verify(exactly = 1) {
            editNormFrameService.editNormFrame(
                withArg {
                    assertTrue(it.guid == UUID.fromString("761b5537-5aa5-4901-81f7-fbf7e040a7c8"))
                    assertEditNormFramePropertiesAndEditNormRequestSchema(it.properties, editNormRequestSchema)
                }
            )
        }
    }

    @Test
    fun `it responds with no content status when norm was updated successfully`() {
        every { editNormFrameService.editNormFrame(any()) } returns Mono.just(true)

        webClient
            .mutateWith(csrf())
            .put()
            .uri("/api/v1/norms/761b5537-5aa5-4901-81f7-fbf7e040a7c8")
            .contentType(APPLICATION_JSON)
            .body(BodyInserters.fromValue("""{ "officialLongTitle": "new title" }"""))
            .exchange()
            .expectStatus()
            .isNoContent()
            .expectBody()
            .isEmpty()
    }

    @Test
    fun `it sends an server internal error response if the edit norm frame service throws an exception`() {
        every { editNormFrameService.editNormFrame(any()) } throws Error()

        webClient
            .mutateWith(csrf())
            .put()
            .uri("/api/v1/norms/761b5537-5aa5-4901-81f7-fbf7e040a7c8")
            .contentType(APPLICATION_JSON)
            .body(BodyInserters.fromValue("""{ "officialLongTitle": "new title" }"""))
            .exchange()
            .expectStatus()
            .is5xxServerError()
    }
}
