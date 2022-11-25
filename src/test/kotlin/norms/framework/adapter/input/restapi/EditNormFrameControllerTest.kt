package de.bund.digitalservice.ris.norms.framework.adapter.input.restapi

import com.ninjasquad.springmockk.MockkBean
import de.bund.digitalservice.ris.norms.application.port.input.EditNormFrameUseCase
import io.mockk.every
import io.mockk.slot
import io.mockk.verify
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
import java.util.*

@ExtendWith(SpringExtension::class)
@WebFluxTest(controllers = [EditNormFrameController::class])
@WithMockUser
class EditNormFrameControllerTest {
    @Autowired
    lateinit var webClient: WebTestClient

    @MockkBean
    lateinit var editNormFrameService: EditNormFrameUseCase

    @Test
    fun `it correctly maps the parameter and body to the command calling the service`() {
        val editJson = """
            {
              "longTitle": "new title",
              "officialShortTitle": "official short title",
              "officialAbbreviation": "official abbreviation",
              "referenceNumber": "reference number",
              "publicationDate": null,
              "announcementDate": "2020-10-21",
              "citationDate": "2020-10-22",
              "frameKeywords": "frame keywords",
              "authorEntity": "author entity",
              "authorDecidingBody": "author deciding body",
              "authorIsResolutionMajority": true,
              "leadJurisdiction": "lead jurisdiction",
              "leadUnit": "lead unit",
              "participationType": "participation type",
              "participationInstitution": "participation institution",
              "documentTypeName": "document type name",
              "documentNormCategory": "document norm category",
              "documentTemplateName": "document template name",
              "subjectFna": "subject fna",
              "subjectPreviousFna": "subject previous fna",
              "subjectGesta": "subject gesta",
              "subjectBgb3": "subject bgb3"
            }
        """
        every { editNormFrameService.editNormFrame(any()) } returns Mono.just(true)

        webClient
            .mutateWith(csrf())
            .put()
            .uri("/api/v1/norms/761b5537-5aa5-4901-81f7-fbf7e040a7c8")
            .contentType(APPLICATION_JSON)
            .body(BodyInserters.fromValue(editJson))
            .exchange()

        var command = slot<EditNormFrameUseCase.Command>()
        verify(exactly = 1) { editNormFrameService.editNormFrame(capture(command)) }
        assertTrue(command.captured.guid == UUID.fromString("761b5537-5aa5-4901-81f7-fbf7e040a7c8"))
        assertTrue(command.captured.longTitle == "new title")
        assertTrue(command.captured.officialShortTitle == "official short title")
        assertTrue(command.captured.officialAbbreviation == "official abbreviation")
        assertTrue(command.captured.referenceNumber == "reference number")
        assertTrue(command.captured.publicationDate == null)
        assertTrue(command.captured.announcementDate == "2020-10-21")
        assertTrue(command.captured.citationDate == "2020-10-22")
        assertTrue(command.captured.frameKeywords == "frame keywords")
        assertTrue(command.captured.authorEntity == "author entity")
        assertTrue(command.captured.authorDecidingBody == "author deciding body")
        assertTrue(command.captured.authorIsResolutionMajority == true)
        assertTrue(command.captured.leadJurisdiction == "lead jurisdiction")
        assertTrue(command.captured.leadUnit == "lead unit")
        assertTrue(command.captured.participationType == "participation type")
        assertTrue(command.captured.participationInstitution == "participation institution")
        assertTrue(command.captured.documentTypeName == "document type name")
        assertTrue(command.captured.documentNormCategory == "document norm category")
        assertTrue(command.captured.documentTemplateName == "document template name")
        assertTrue(command.captured.subjectFna == "subject fna")
        assertTrue(command.captured.subjectPreviousFna == "subject previous fna")
        assertTrue(command.captured.subjectGesta == "subject gesta")
        assertTrue(command.captured.subjectBgb3 == "subject bgb3")
    }

    @Test
    fun `it responds with no content status when norm was updated successfully`() {
        every { editNormFrameService.editNormFrame(any()) } returns Mono.just(true)

        webClient
            .mutateWith(csrf())
            .put()
            .uri("/api/v1/norms/761b5537-5aa5-4901-81f7-fbf7e040a7c8")
            .contentType(APPLICATION_JSON)
            .body(BodyInserters.fromValue("""{ "longTitle": "new title" }"""))
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
            .body(BodyInserters.fromValue("""{ "longTitle": "new title" }"""))
            .exchange()
            .expectStatus()
            .is5xxServerError()
    }
}
