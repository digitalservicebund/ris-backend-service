package de.bund.digitalservice.ris.norms.framework.adapter.input.restapi

import com.ninjasquad.springmockk.MockkBean
import de.bund.digitalservice.ris.norms.application.port.input.EditNormFrameUseCase
import io.mockk.every
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
import java.time.LocalDate
import java.util.UUID

@ExtendWith(SpringExtension::class)
@WebFluxTest(controllers = [EditNormFrameController::class])
@WithMockUser
class EditNormFrameControllerTest {
    @Autowired lateinit var webClient: WebTestClient

    @MockkBean lateinit var editNormFrameService: EditNormFrameUseCase

    @Test
    fun `it correctly maps the parameter and body to the command calling the service`() {
        val editJson =
            """
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
              "subjectBgb3": "subject bgb3",
              "unofficialTitle": "unofficial title",
              "unofficialShortTitle": "unofficial short title",
              "unofficialAbbreviation": "unofficial abbreviation",
              "risAbbreviation": "ris abbreviation"
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

        verify(exactly = 1) {
            editNormFrameService.editNormFrame(
                withArg {
                    assertTrue(it.guid == UUID.fromString("761b5537-5aa5-4901-81f7-fbf7e040a7c8"))
                    assertTrue(it.properties.longTitle == "new title")
                    assertTrue(it.properties.officialShortTitle == "official short title")
                    assertTrue(it.properties.officialAbbreviation == "official abbreviation")
                    assertTrue(it.properties.referenceNumber == "reference number")
                    assertTrue(it.properties.publicationDate == null)
                    assertTrue(it.properties.announcementDate == LocalDate.parse("2020-10-21"))
                    assertTrue(it.properties.citationDate == LocalDate.parse("2020-10-22"))
                    assertTrue(it.properties.frameKeywords == "frame keywords")
                    assertTrue(it.properties.authorEntity == "author entity")
                    assertTrue(it.properties.authorDecidingBody == "author deciding body")
                    assertTrue(it.properties.authorIsResolutionMajority == true)
                    assertTrue(it.properties.leadJurisdiction == "lead jurisdiction")
                    assertTrue(it.properties.leadUnit == "lead unit")
                    assertTrue(it.properties.participationType == "participation type")
                    assertTrue(it.properties.participationInstitution == "participation institution")
                    assertTrue(it.properties.documentTypeName == "document type name")
                    assertTrue(it.properties.documentNormCategory == "document norm category")
                    assertTrue(it.properties.documentTemplateName == "document template name")
                    assertTrue(it.properties.subjectFna == "subject fna")
                    assertTrue(it.properties.subjectPreviousFna == "subject previous fna")
                    assertTrue(it.properties.subjectGesta == "subject gesta")
                    assertTrue(it.properties.subjectBgb3 == "subject bgb3")
                    assertTrue(it.properties.unofficialTitle == "unofficial title")
                    assertTrue(it.properties.unofficialShortTitle == "unofficial short title")
                    assertTrue(it.properties.unofficialAbbreviation == "unofficial abbreviation")
                    assertTrue(it.properties.risAbbreviation == "ris abbreviation")
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
