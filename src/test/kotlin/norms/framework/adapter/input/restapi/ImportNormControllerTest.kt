package de.bund.digitalservice.ris.norms.framework.adapter.input.restapi

import com.ninjasquad.springmockk.MockkBean
import de.bund.digitalservice.ris.norms.application.port.input.ImportNormUseCase
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
	"officialLongTitle": "long title",
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
	"providerEntity": "DEU",
	"providerDecidingBody": "BT",
	"providerIsResolutionMajority": true,
	"leadJurisdiction": "BMVI",
	"leadUnit": "G 22",
	"participationType": null,
	"participationInstitution": null,
	"subjectFna": "FNA 703-12",
	"subjectGesta": "GESTA J040",
  "unofficialLongTitle": "unofficial long title",
  "unofficialShortTitle": "unofficial short title",
  "unofficialAbbreviation": "unofficial abbreviation",
  "risAbbreviation": "ris abbreviation"
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

        verify(exactly = 1) {
            importNormService.importNorm(
                withArg {
                    assertTrue(it.data.officialLongTitle == "long title")
                    assertTrue(it.data.articles.size == 1)
                    assertTrue(it.data.articles[0].title == "article title")
                    assertTrue(it.data.articles[0].marker == "§ 1")
                    assertTrue(it.data.articles[0].paragraphs.size == 2)
                    assertTrue(it.data.articles[0].paragraphs[0].marker == "(1)")
                    assertTrue(it.data.articles[0].paragraphs[0].text == "paragraph text 1")
                    assertTrue(it.data.articles[0].paragraphs[1].marker == "(2)")
                    assertTrue(it.data.articles[0].paragraphs[1].text == "paragraph text 2")
                    assertTrue(it.data.officialShortTitle == "official short title")
                    assertTrue(it.data.officialAbbreviation == "official abbreviation")
                    assertTrue(it.data.referenceNumber == null)
                    assertTrue(it.data.announcementDate == LocalDate.parse("2021-06-14"))
                    assertTrue(it.data.citationDate == LocalDate.parse("2021-06-09"))
                    assertTrue(it.data.frameKeywords == "frame keywords")
                    assertTrue(it.data.providerEntity == "DEU")
                    assertTrue(it.data.providerDecidingBody == "BT")
                    assertTrue(it.data.providerIsResolutionMajority == true)
                    assertTrue(it.data.leadJurisdiction == "BMVI")
                    assertTrue(it.data.leadUnit == "G 22")
                    assertTrue(it.data.participationType == null)
                    assertTrue(it.data.participationInstitution == null)
                    assertTrue(it.data.subjectFna == "FNA 703-12")
                    assertTrue(it.data.subjectGesta == "GESTA J040")
                    assertTrue(it.data.unofficialLongTitle == "unofficial long title")
                    assertTrue(it.data.unofficialShortTitle == "unofficial short title")
                    assertTrue(it.data.unofficialAbbreviation == "unofficial abbreviation")
                    assertTrue(it.data.risAbbreviation == "ris abbreviation")
                }
            )
        }
    }

    @Test
    fun `it responds with created status when norm was imported`() {
        every { importNormService.importNorm(any()) } returns Mono.just(UUID.randomUUID())

        webClient
            .mutateWith(csrf())
            .post()
            .uri("/api/v1/norms")
            .contentType(APPLICATION_JSON)
            .body(BodyInserters.fromValue("""{ "officialLongTitle": "long title" }"""))
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
            .body(BodyInserters.fromValue("""{ "officialLongTitle": "long title" }"""))
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
            .body(BodyInserters.fromValue("""{ "officialLongTitle": "long title" }"""))
            .exchange()
            .expectStatus()
            .is5xxServerError()
    }
}
