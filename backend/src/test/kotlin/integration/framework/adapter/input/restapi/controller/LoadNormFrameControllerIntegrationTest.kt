package de.bund.digitalservice.ris.norms.framework.adapter.input.restapi.controller

import de.bund.digitalservice.ris.caselaw.config.FlywayConfig
import de.bund.digitalservice.ris.norms.application.port.output.SaveNormOutputPort
import de.bund.digitalservice.ris.norms.application.service.LoadNormService
import de.bund.digitalservice.ris.norms.domain.entity.FileReference
import de.bund.digitalservice.ris.norms.domain.entity.MetadataSection
import de.bund.digitalservice.ris.norms.domain.entity.Metadatum
import de.bund.digitalservice.ris.norms.domain.entity.Norm
import de.bund.digitalservice.ris.norms.domain.value.MetadataSectionName
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType
import de.bund.digitalservice.ris.norms.framework.adapter.output.database.NormsService
import de.bund.digitalservice.ris.norms.framework.adapter.output.database.PostgresTestcontainerIntegrationTest
import de.bund.digitalservice.ris.norms.framework.adapter.output.database.dto.NormDto
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.r2dbc.AutoConfigureDataR2dbc
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.context.annotation.Import
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.data.r2dbc.dialect.PostgresDialect
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.csrf
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.reactive.server.WebTestClient
import reactor.test.StepVerifier
import java.time.Duration
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.*

@ExtendWith(SpringExtension::class)
@Import(FlywayConfig::class, NormsService::class, LoadNormService::class)
@WebFluxTest(controllers = [LoadNormController::class])
@WithMockUser
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@AutoConfigureDataR2dbc
class LoadNormFrameControllerIntegrationTest : PostgresTestcontainerIntegrationTest() {
    @Autowired
    lateinit var webClient: WebTestClient

    @Autowired
    private lateinit var client: DatabaseClient

    @Autowired
    lateinit var normsService: NormsService

    @Autowired
    lateinit var loadNormFrameService: LoadNormService

    private lateinit var template: R2dbcEntityTemplate

    @BeforeAll
    fun setup() {
        template = R2dbcEntityTemplate(client, PostgresDialect.INSTANCE)
    }

    @AfterEach
    fun cleanUp() {
        template.delete(NormDto::class.java).all().block(Duration.ofSeconds(1))
    }

    @Test
    fun `it correctly loads a norm with metadata sections via api`() {
        val date = LocalDateTime.now().truncatedTo(ChronoUnit.MICROS)
        val section = MetadataSection(
            MetadataSectionName.CITATION_DATE,
            listOf(Metadatum(date.toLocalDate(), MetadatumType.DATE)),
        )
        val norm = Norm(
            guid = UUID.randomUUID(),
            officialLongTitle = "officialLongTitle",
            metadataSections = listOf(section),
            risAbbreviation = "risAbbreviation",
            files = listOf(FileReference("norm.zip", "hash", date)),
        )
        val saveCommand = SaveNormOutputPort.Command(norm)
        normsService.saveNorm(saveCommand)
            .`as`(StepVerifier::create)
            .expectNextCount(1)
            .verifyComplete()

        webClient
            .mutateWith(csrf())
            .get()
            .uri("/api/v1/norms/" + norm.guid.toString())
            .exchange()
            .expectStatus()
            .isOk
            .expectBody()
            .json(
                """
                {
                  "guid":"${norm.guid}",
                  "articles":[],
                  "metadataSections":[{"name":"CITATION_DATE","order":1,"metadata":[{"value":"${date.toLocalDate()}","type":"DATE","order":1}],"sections":null}],
                  "officialLongTitle":"officialLongTitle",
                  "risAbbreviation":"risAbbreviation",
                  "documentNumber":null,
                  "documentCategory":null,
                  "documentTypeName":null,
                  "documentNormCategory":null,
                  "documentTemplateName":null,
                  "providerEntity":null,
                  "providerDecidingBody":null,
                  "providerIsResolutionMajority":null,
                  "officialShortTitle":null,
                  "officialAbbreviation":null,
                  "entryIntoForceDate":null,
                  "entryIntoForceDateState":null,
                  "principleEntryIntoForceDate":null,
                  "principleEntryIntoForceDateState":null,
                  "divergentEntryIntoForceDate":null,
                  "divergentEntryIntoForceDateState":null,
                  "entryIntoForceNormCategory":null,
                  "expirationDate":null,
                  "expirationDateState":null,
                  "isExpirationDateTemp":null,
                  "principleExpirationDate":null,
                  "principleExpirationDateState":null,
                  "divergentExpirationDate":null,
                  "divergentExpirationDateState":null,
                  "expirationNormCategory":null,
                  "announcementDate":null,
                  "publicationDate":null,
                  "printAnnouncementGazette":null,
                  "printAnnouncementYear":null,
                  "printAnnouncementNumber":null,
                  "printAnnouncementPage":null,
                  "printAnnouncementInfo":null,
                  "printAnnouncementExplanations":null,
                  "digitalAnnouncementMedium":null,
                  "digitalAnnouncementDate":null,
                  "digitalAnnouncementEdition":null,
                  "digitalAnnouncementYear":null,
                  "digitalAnnouncementPage":null,
                  "digitalAnnouncementArea":null,
                  "digitalAnnouncementAreaNumber":null,
                  "digitalAnnouncementInfo":null,
                  "digitalAnnouncementExplanations":null,
                  "euAnnouncementGazette":null,
                  "euAnnouncementYear":null,
                  "euAnnouncementSeries":null,
                  "euAnnouncementNumber":null,
                  "euAnnouncementPage":null,
                  "euAnnouncementInfo":null,
                  "euAnnouncementExplanations":null,
                  "otherOfficialAnnouncement":null,
                  "completeCitation":null,
                  "statusNote":null,
                  "statusDescription":null,
                  "statusDate":null,
                  "statusReference":null,
                  "repealNote":null,
                  "repealArticle":null,
                  "repealDate":null,
                  "repealReferences":null,
                  "reissueNote":null,
                  "reissueArticle":null,
                  "reissueDate":null,
                  "reissueReference":null,
                  "otherStatusNote":null,
                  "documentStatusWorkNote":null,
                  "documentStatusDescription":null,
                  "documentStatusDate":null,
                  "documentStatusReference":null,
                  "documentStatusEntryIntoForceDate":null,
                  "documentStatusProof":null,
                  "documentTextProof":null,
                  "otherDocumentNote":null,
                  "applicationScopeArea":null,
                  "applicationScopeStartDate":null,
                  "applicationScopeEndDate":null,
                  "categorizedReference":null,
                  "otherFootnote":null,
                  "footnoteChange":null,
                  "footnoteComment":null,
                  "footnoteDecision":null,
                  "footnoteStateLaw":null,
                  "footnoteEuLaw":null,
                  "digitalEvidenceLink":null,
                  "digitalEvidenceRelatedData":null,
                  "digitalEvidenceExternalDataNote":null,
                  "digitalEvidenceAppendix":null,
                  "eli":"","celexNumber":null,
                  "text":null,
                  "files":[{"name":"norm.zip","hash":"hash","createdAt":"$date"}]}
                """.trimIndent(),
            )
    }
}
