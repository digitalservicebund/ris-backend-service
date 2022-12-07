package de.bund.digitalservice.ris.norms.framework.adapter.input.restapi

import com.ninjasquad.springmockk.MockkBean
import de.bund.digitalservice.ris.norms.application.port.input.LoadNormUseCase
import de.bund.digitalservice.ris.norms.domain.entity.Article
import de.bund.digitalservice.ris.norms.domain.entity.Norm
import de.bund.digitalservice.ris.norms.domain.entity.Paragraph
import io.mockk.every
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.csrf
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.reactive.server.WebTestClient
import reactor.core.publisher.Mono
import java.time.LocalDate
import java.util.*

@ExtendWith(SpringExtension::class)
@WebFluxTest(controllers = [LoadNormController::class])
@WithMockUser
class LoadNormControllerTest {
    @Autowired
    lateinit var webClient: WebTestClient

    @MockkBean
    lateinit var loadNormService: LoadNormUseCase

    @Test
    fun `it calls the load norm service with the correct query to get a norm by GUID`() {
        val norm = Norm(UUID.fromString("761b5537-5aa5-4901-81f7-fbf7e040a7c8"), officialLongTitle = "long title")

        every { loadNormService.loadNorm(any()) } returns Mono.just(norm)

        webClient
            .mutateWith(csrf())
            .get()
            .uri("/api/v1/norms/761b5537-5aa5-4901-81f7-fbf7e040a7c8")
            .exchange()

        val query = slot<LoadNormUseCase.Query>()
        verify(exactly = 1) { loadNormService.loadNorm(capture(query)) }
        assertTrue(query.captured.guid.toString() == "761b5537-5aa5-4901-81f7-fbf7e040a7c8")
    }

    @Test
    fun `it responds with ok status if the norm was loaded successfully`() {
        val norm = Norm(UUID.fromString("761b5537-5aa5-4901-81f7-fbf7e040a7c8"), officialLongTitle = "long title")

        every { loadNormService.loadNorm(any()) } returns Mono.just(norm)

        webClient
            .mutateWith(csrf())
            .get()
            .uri("/api/v1/norms/761b5537-5aa5-4901-81f7-fbf7e040a7c8")
            .exchange()
            .expectStatus()
            .isOk()
    }

    @Test
    fun `it maps the norm entity to the expected data schema`() {
        val paragraphGuid = UUID.fromString("e0cbf06c-cd8b-4647-bb8a-263b43f0f974")
        val paragraph = Paragraph(paragraphGuid, "marker", "text")
        val articleGuid = UUID.fromString("53d29ef7-377c-4d14-864b-eb3a85769359")
        val article = Article(articleGuid, "title", "marker", listOf(paragraph))
        val normGuid = UUID.fromString("72631e54-78a4-11d0-bcf7-00aa00b7b32a")
        val norm = Norm(
            normGuid, listOf(article), "long title", "ris abbreviation", "ris abbreviation international law",
            "document number", "divergent document number", "document category",
            "frame keywords", "document type name", "document norm category", "document template name",
            "provider entity", "provider deciding body",
            true, "participation type",
            "participation institution", "lead jurisdiction", "lead unit",
            "subject fna", "subject previous fna",
            "subject gesta", "subject bgb3", "official short title",
            "official abbreviation", "unofficial long title", "unofficial short title",
            "unofficial abbreviation", null, null, null,
            null, null, null, null,
            null, null, null, null,
            null, null, null, LocalDate.parse("2020-10-28"),
            LocalDate.parse("2020-10-27"), LocalDate.parse("2020-10-29"), null, null,
            null, null, null, null, null,
            null, null, null, null, null,
            null, null, null, null,
            null, null, null, null, null,
            null, null, null, null, null,
            null, null, null, null, null, null,
            null, null, null, null, null, null,
            null, null, null, null, null,
            null, null, null, null, null,
            null, null, null, null, null, null,
            null, null, null, null, null,
            null, null, null, null, null
        )

        every { loadNormService.loadNorm(any()) } returns Mono.just(norm)

        webClient
            .mutateWith(csrf())
            .get()
            .uri("/api/v1/norms/72631e54-78a4-11d0-bcf7-00aa00b7b32a")
            .exchange()
            .expectBody()
            .json(
                """
        {
          "guid": "72631e54-78a4-11d0-bcf7-00aa00b7b32a",
          "officialLongTitle": "long title",
          "articles": [
            {
              "guid": "53d29ef7-377c-4d14-864b-eb3a85769359",
              "title": "title",
              "marker": "marker",
              "paragraphs": [
                {
                  "guid": "e0cbf06c-cd8b-4647-bb8a-263b43f0f974",
                  "marker": "marker",
                  "text": "text"
                }
              ]
            }
          ],
          "officialShortTitle": "official short title",
          "officialAbbreviation": "official abbreviation",
          "referenceNumber": null,
          "publicationDate": "2020-10-27",
          "announcementDate": "2020-10-28",
          "citationDate": "2020-10-29",
          "frameKeywords": "frame keywords",
          "providerEntity": "provider entity",
          "providerDecidingBody": "provider deciding body",
          "providerIsResolutionMajority": true,
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
          "unofficialLongTitle": "unofficial long title",
          "unofficialShortTitle": "unofficial short title",
          "unofficialAbbreviation": "unofficial abbreviation",
          "risAbbreviation": "ris abbreviation"
        }
        """,
                true
            )
    }

    @Test
    fun `it sends a not found response with empty body if the load norm service responds with empty`() {
        every { loadNormService.loadNorm(any()) } returns Mono.empty()

        webClient
            .mutateWith(csrf())
            .get()
            .uri("/api/v1/norms/72631e54-78a4-11d0-bcf7-00aa00b7b32a")
            .exchange()
            .expectStatus()
            .isNotFound()
            .expectBody()
            .isEmpty()
    }

    @Test
    fun `it sends an internal error response if the load norm service throws an exception`() {
        every { loadNormService.loadNorm(any()) } throws Error()

        webClient
            .mutateWith(csrf())
            .get()
            .uri("/api/v1/norms/72631e54-78a4-11d0-bcf7-00aa00b7b32a")
            .exchange()
            .expectStatus()
            .is5xxServerError()
    }
}
