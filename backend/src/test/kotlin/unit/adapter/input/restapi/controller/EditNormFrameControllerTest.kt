package de.bund.digitalservice.ris.norms.framework.adapter.input.restapi.controller

import com.ninjasquad.springmockk.MockkBean
import de.bund.digitalservice.ris.norms.application.port.input.EditNormFrameUseCase
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType
import io.mockk.every
import io.mockk.verify
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
import utils.assertEditNormFramePropertiesAndEditNormRequestSchema
import utils.convertEditNormRequestTestSchemaToJson
import utils.createRandomEditNormRequestTestSchema
import java.time.LocalDate
import java.time.LocalTime
import java.util.UUID

@ExtendWith(SpringExtension::class)
@WebFluxTest(controllers = [EditNormFrameController::class])
@WithMockUser
class EditNormFrameControllerTest {
    @Autowired lateinit var webClient: WebTestClient

    @MockkBean lateinit var editNormFrameService: EditNormFrameUseCase

    @Test
    fun `it correctly maps the parameter and body to the command calling the service`() {
        val editNormRequestSchema = createRandomEditNormRequestTestSchema()
        val editJson = convertEditNormRequestTestSchemaToJson(editNormRequestSchema)

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
                    assertThat(it.guid)
                        .isEqualTo(UUID.fromString("761b5537-5aa5-4901-81f7-fbf7e040a7c8"))
                    assertEditNormFramePropertiesAndEditNormRequestSchema(
                        it.properties,
                        editNormRequestSchema,
                    )
                },
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
            .body(BodyInserters.fromValue("""{ "officialLongTitle": "new title", "metadataSections": [] }"""))
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

    @Test
    fun `it correctly maps the dates from string to localdate in metadata`() {
        val schema = EditNormFrameController.MetadatumRequestSchema()
        schema.value = "2022-12-01"
        schema.type = MetadatumType.DATE

        assertThat(schema.toUseCaseData().value).isEqualTo(LocalDate.of(2022, 12, 1))
    }

    @Test
    fun `it correctly maps the times from string to localtime in metadata`() {
        val schema = EditNormFrameController.MetadatumRequestSchema()
        schema.value = "13:55"
        schema.type = MetadatumType.TIME

        assertThat(schema.toUseCaseData().value).isEqualTo(LocalTime.of(13, 55))
    }

    class NormFramePropertiesTestRequestSchema {
        lateinit var officialLongTitle: String
        lateinit var metadataSections: List<EditNormFrameController.MetadataSectionRequestSchema>
        var risAbbreviation: String? = null
        var documentNumber: String? = null
        var documentCategory: String? = null

        var officialShortTitle: String? = null
        var officialAbbreviation: String? = null

        var announcementDate: String? = null

        var completeCitation: String? = null

        var celexNumber: String? = null

        var text: String? = null
    }
}
