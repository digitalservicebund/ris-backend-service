package de.bund.digitalservice.ris.norms.framework.adapter.input.restapi.controller

import com.fasterxml.jackson.annotation.JsonProperty
import com.ninjasquad.springmockk.MockkBean
import de.bund.digitalservice.ris.norms.application.port.input.EditNormFrameUseCase
import de.bund.digitalservice.ris.norms.domain.value.UndefinedDate
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

    class NormFramePropertiesTestRequestSchema {
        lateinit var officialLongTitle: String
        lateinit var metadataSections: List<EditNormFrameController.MetadataSectionRequestSchema>
        var risAbbreviation: String? = null
        var documentNumber: String? = null
        var documentCategory: String? = null

        var documentTypeName: String? = null
        var documentNormCategory: String? = null
        var documentTemplateName: String? = null

        var providerEntity: String? = null
        var providerDecidingBody: String? = null
        var providerIsResolutionMajority: Boolean? = null

        var officialShortTitle: String? = null
        var officialAbbreviation: String? = null

        var entryIntoForceDate: String? = null
        var entryIntoForceDateState: UndefinedDate? = null
        var principleEntryIntoForceDate: String? = null
        var principleEntryIntoForceDateState: UndefinedDate? = null
        var divergentEntryIntoForceDate: String? = null
        var divergentEntryIntoForceDateState: UndefinedDate? = null
        var entryIntoForceNormCategory: String? = null

        var expirationDate: String? = null
        var expirationDateState: UndefinedDate? = null

        @get:JsonProperty("isExpirationDateTemp")
        var isExpirationDateTemp: Boolean? = null
        var principleExpirationDate: String? = null
        var principleExpirationDateState: UndefinedDate? = null
        var divergentExpirationDate: String? = null
        var divergentExpirationDateState: UndefinedDate? = null
        var expirationNormCategory: String? = null

        var announcementDate: String? = null
        var publicationDate: String? = null

        var citationDate: String? = null
        var citationYear: String? = null

        var printAnnouncementGazette: String? = null
        var printAnnouncementYear: String? = null
        var printAnnouncementNumber: String? = null
        var printAnnouncementPage: String? = null
        var printAnnouncementInfo: String? = null
        var printAnnouncementExplanations: String? = null
        var digitalAnnouncementMedium: String? = null
        var digitalAnnouncementDate: String? = null
        var digitalAnnouncementEdition: String? = null
        var digitalAnnouncementYear: String? = null
        var digitalAnnouncementPage: String? = null
        var digitalAnnouncementArea: String? = null
        var digitalAnnouncementAreaNumber: String? = null
        var digitalAnnouncementInfo: String? = null
        var digitalAnnouncementExplanations: String? = null
        var euAnnouncementGazette: String? = null
        var euAnnouncementYear: String? = null
        var euAnnouncementSeries: String? = null
        var euAnnouncementNumber: String? = null
        var euAnnouncementPage: String? = null
        var euAnnouncementInfo: String? = null
        var euAnnouncementExplanations: String? = null
        var otherOfficialAnnouncement: String? = null

        var completeCitation: String? = null

        var statusNote: String? = null
        var statusDescription: String? = null
        var statusDate: String? = null
        var statusReference: String? = null
        var repealNote: String? = null
        var repealArticle: String? = null
        var repealDate: String? = null
        var repealReferences: String? = null
        var reissueNote: String? = null
        var reissueArticle: String? = null
        var reissueDate: String? = null
        var reissueReference: String? = null
        var otherStatusNote: String? = null

        var documentStatusWorkNote: String? = null
        var documentStatusDescription: String? = null
        var documentStatusDate: String? = null
        var documentStatusReference: String? = null
        var documentStatusEntryIntoForceDate: String? = null
        var documentStatusProof: String? = null
        var documentTextProof: String? = null
        var otherDocumentNote: String? = null

        var applicationScopeArea: String? = null
        var applicationScopeStartDate: String? = null
        var applicationScopeEndDate: String? = null

        var categorizedReference: String? = null

        var otherFootnote: String? = null
        var footnoteChange: String? = null
        var footnoteComment: String? = null
        var footnoteDecision: String? = null
        var footnoteStateLaw: String? = null
        var footnoteEuLaw: String? = null

        var digitalEvidenceLink: String? = null
        var digitalEvidenceRelatedData: String? = null
        var digitalEvidenceExternalDataNote: String? = null
        var digitalEvidenceAppendix: String? = null

        var eli: String? = null

        var celexNumber: String? = null

        var ageIndicationStart: String? = null
        var ageIndicationEnd: String? = null

        var text: String? = null
    }
}
