package de.bund.digitalservice.ris.norms.framework.adapter.input.restapi.controller

import de.bund.digitalservice.ris.caselaw.config.FlywayConfig
import de.bund.digitalservice.ris.norms.application.port.output.SaveNormOutputPort
import de.bund.digitalservice.ris.norms.application.service.LoadNormAsXmlService
import de.bund.digitalservice.ris.norms.domain.entity.MetadataSection
import de.bund.digitalservice.ris.norms.domain.entity.Metadatum
import de.bund.digitalservice.ris.norms.domain.entity.Norm
import de.bund.digitalservice.ris.norms.domain.value.MetadataSectionName
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType
import de.bund.digitalservice.ris.norms.framework.adapter.output.database.NormsService
import de.bund.digitalservice.ris.norms.framework.adapter.output.database.PostgresTestcontainerIntegrationTest
import de.bund.digitalservice.ris.norms.framework.adapter.output.database.dto.NormDto
import de.bund.digitalservice.ris.norms.framework.adapter.output.xml.ToLegalDocMLConverter
import org.assertj.core.api.Assertions.assertThat
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
import java.time.LocalDate
import java.util.*

@ExtendWith(SpringExtension::class)
@Import(FlywayConfig::class, NormsService::class, ToLegalDocMLConverter::class, LoadNormAsXmlService::class)
@WebFluxTest(controllers = [LoadNormAsXmlController::class])
@WithMockUser
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@AutoConfigureDataR2dbc
class LoadNormAsXmlControllerIntegrationTest : PostgresTestcontainerIntegrationTest() {
    @Autowired
    lateinit var webClient: WebTestClient

    @Autowired
    private lateinit var client: DatabaseClient

    @Autowired
    lateinit var normsService: NormsService

    @Autowired
    lateinit var loadNormAsXmlService: LoadNormAsXmlService

    @Autowired
    lateinit var toLegalDocMLConverter: ToLegalDocMLConverter

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
        val date = LocalDate.of(2022, 10, 10)
        val citationDateSection = MetadataSection(
            MetadataSectionName.CITATION_DATE,
            listOf(Metadatum(date, MetadatumType.DATE)),
        )
        val printAnnouncementSection = MetadataSection(
            MetadataSectionName.OFFICIAL_REFERENCE,
            listOf(),
            1,
            listOf(
                MetadataSection(
                    MetadataSectionName.PRINT_ANNOUNCEMENT,
                    listOf(
                        Metadatum("BGBl I", MetadatumType.ANNOUNCEMENT_GAZETTE),
                        Metadatum("3", MetadatumType.PAGE),
                    ),
                ),
            ),
        )
        val norm = Norm(
            guid = UUID.randomUUID(),
            officialLongTitle = "officialLongTitle",
            metadataSections = listOf(citationDateSection, printAnnouncementSection),
        )
        val saveCommand = SaveNormOutputPort.Command(norm)
        normsService.saveNorm(saveCommand)
            .`as`(StepVerifier::create)
            .expectNextCount(1)
            .verifyComplete()

        val result = webClient
            .mutateWith(csrf())
            .get()
            .uri("/api/v1/norms/xml/eli/bgbl-1/2022/s3")
            .exchange()
            .expectStatus()
            .isOk
            .expectBody()
            .returnResult()

        assertThat(result.toString()).contains("<akn:FRBRuri value=\"eli/bgbl-1/2022/s3\"/>")
    }
}
