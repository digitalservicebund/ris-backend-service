package de.bund.digitalservice.ris.norms.framework.adapter.input.restapi

import com.ninjasquad.springmockk.MockkBean
import de.bund.digitalservice.ris.norms.application.port.input.ImportNormUseCase
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
@WebFluxTest(controllers = [ImportNormController::class])
@WithMockUser
class ImportNormControllerTest {
    @Autowired lateinit var webClient: WebTestClient

    @MockkBean lateinit var importNormService: ImportNormUseCase

    @Test
    fun `it correctly maps the data to the command to call the import norm service`() {
        val importJson =
            """
{
	"longTitle": "long title",
	"articles": [
		{
			"marker": "§ 1",
			"title": "article title",
			"paragraphs": [
				{
					"marker": "(1)",
					"text": "paragraph text 1"
				},
				{
					"marker": "(2)",
					"text": "paragraph text 2"
				}
			]
		}
	],
	"officialShortTitle": "official short title",
	"officialAbbreviation": "official abbreviation",
	"referenceNumber": null,
	"announcementDate": "2021-06-14",
	"citationDate": "2021-06-09",
	"frameKeywords": "frame keywords",
	"authorEntity": "DEU",
	"authorDecidingBody": "BT",
	"authorIsResolutionMajority": true,
	"leadJurisdiction": "BMVI",
	"leadUnit": "G 22",
	"participationType": null,
	"participationInstitution": null,
	"subjectFna": "FNA 703-12",
	"subjectGesta": "GESTA J040"
}
    """

        every { importNormService.importNorm(any()) } returns Mono.just(UUID.randomUUID())

        webClient
            .mutateWith(csrf())
            .post()
            .uri("/api/v1/norms")
            .contentType(APPLICATION_JSON)
            .body(BodyInserters.fromValue(importJson))
            .exchange()

        val command = slot<ImportNormUseCase.Command>()
        verify(exactly = 1) { importNormService.importNorm(capture(command)) }

        assertTrue(command.captured.data.longTitle == "long title")
        assertTrue(command.captured.data.articles.size == 1)
        assertTrue(command.captured.data.articles[0].title == "article title")
        assertTrue(command.captured.data.articles[0].marker == "§ 1")
        assertTrue(command.captured.data.articles[0].paragraphs.size == 2)
        assertTrue(command.captured.data.articles[0].paragraphs[0].marker == "(1)")
        assertTrue(command.captured.data.articles[0].paragraphs[0].text == "paragraph text 1")
        assertTrue(command.captured.data.articles[0].paragraphs[1].marker == "(2)")
        assertTrue(command.captured.data.articles[0].paragraphs[1].text == "paragraph text 2")
        assertTrue(command.captured.data.officialShortTitle == "official short title")
        assertTrue(command.captured.data.officialAbbreviation == "official abbreviation")
        assertTrue(command.captured.data.referenceNumber == null)
        assertTrue(command.captured.data.announcementDate == "2021-06-14")
        assertTrue(command.captured.data.citationDate == "2021-06-09")
        assertTrue(command.captured.data.frameKeywords == "frame keywords")
        assertTrue(command.captured.data.authorEntity == "DEU")
        assertTrue(command.captured.data.authorDecidingBody == "BT")
        assertTrue(command.captured.data.authorIsResolutionMajority == true)
        assertTrue(command.captured.data.leadJurisdiction == "BMVI")
        assertTrue(command.captured.data.leadUnit == "G 22")
        assertTrue(command.captured.data.participationType == null)
        assertTrue(command.captured.data.participationInstitution == null)
        assertTrue(command.captured.data.subjectFna == "FNA 703-12")
        assertTrue(command.captured.data.subjectGesta == "GESTA J040")
    }

    @Test
    fun `it responds with created status when norm was imported`() {
        every { importNormService.importNorm(any()) } returns Mono.just(UUID.randomUUID())

        webClient
            .mutateWith(csrf())
            .post()
            .uri("/api/v1/norms")
            .contentType(APPLICATION_JSON)
            .body(BodyInserters.fromValue("""{ "longTitle": "long title" }"""))
            .exchange()
            .expectStatus()
            .isCreated()
    }

    @Test
    fun `it uses the new GUID from the service an creates a location header for the imported norm`() {
        every { importNormService.importNorm(any()) } returns
            Mono.just(UUID.fromString("761b5537-5aa5-4901-81f7-fbf7e040a7c8"))

        webClient
            .mutateWith(csrf())
            .post()
            .uri("/api/v1/norms")
            .contentType(APPLICATION_JSON)
            .body(BodyInserters.fromValue("""{ "longTitle": "long title" }"""))
            .exchange()
            .expectHeader()
            .location("/api/v1/norms/761b5537-5aa5-4901-81f7-fbf7e040a7c8")
    }

    @Test
    fun `it sends an internal error response if the import norm service throws an exception`() {
        every { importNormService.importNorm(any()) } throws Error()

        webClient
            .mutateWith(csrf())
            .post()
            .uri("/api/v1/norms")
            .contentType(APPLICATION_JSON)
            .body(BodyInserters.fromValue("""{ "longTitle": "long title" }"""))
            .exchange()
            .expectStatus()
            .is5xxServerError()
    }
}
