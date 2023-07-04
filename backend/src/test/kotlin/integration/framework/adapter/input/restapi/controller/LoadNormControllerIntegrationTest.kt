package de.bund.digitalservice.ris.norms.framework.adapter.input.restapi.controller

import de.bund.digitalservice.ris.caselaw.config.FlywayConfig
import de.bund.digitalservice.ris.norms.application.port.output.SaveNormOutputPort
import de.bund.digitalservice.ris.norms.application.service.LoadNormService
import de.bund.digitalservice.ris.norms.domain.value.MetadataSectionName
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType
import de.bund.digitalservice.ris.norms.domain.value.NormCategory
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
import utils.factory.norm
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
class LoadNormControllerIntegrationTest : PostgresTestcontainerIntegrationTest() {
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

        val norm = norm {
            files {
                file {
                    name = "norm.zip"
                    hash = "hash"
                    createdAt = date
                }
            }
            metadataSections {
                metadataSection {
                    name = MetadataSectionName.CITATION_DATE
                    metadata {
                        metadatum {
                            value = date.toLocalDate()
                            type = MetadatumType.DATE
                        }
                    }
                }
                metadataSection {
                    name = MetadataSectionName.DOCUMENT_TYPE
                    metadata {
                        metadatum {
                            value = NormCategory.BASE_NORM
                            type = MetadatumType.NORM_CATEGORY
                        }
                        metadatum {
                            value = "documentTypeName"
                            type = MetadatumType.TYPE_NAME
                        }
                        metadatum {
                            value = "documentTemplateName"
                            type = MetadatumType.TEMPLATE_NAME
                        }
                    }
                }
                metadataSection {
                    name = MetadataSectionName.NORM
                    metadata {
                        metadatum { value = "officialLongTitle"; type = MetadatumType.OFFICIAL_LONG_TITLE }
                        metadatum { value = "risAbbreviation"; type = MetadatumType.RIS_ABBREVIATION }
                        metadatum { value = "documentNumber"; type = MetadatumType.DOCUMENT_NUMBER }
                        metadatum { value = "documentCategory"; type = MetadatumType.DOCUMENT_CATEGORY }
                        metadatum { value = "officialShortTitle"; type = MetadatumType.OFFICIAL_SHORT_TITLE }
                        metadatum { value = "officialAbbreviation"; type = MetadatumType.OFFICIAL_ABBREVIATION }
                        metadatum { value = "completeCitation"; type = MetadatumType.COMPLETE_CITATION }
                        metadatum { value = "celexNumber"; type = MetadatumType.CELEX_NUMBER }
                        metadatum { value = "text"; type = MetadatumType.TEXT }
                    }
                }
            }
        }

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
                  "metadataSections":[{"name":"CITATION_DATE","order":1,"metadata":[{"value":"${date.toLocalDate()}","type":"DATE","order":1}],"sections":null}, {"name":"DOCUMENT_TYPE","order":1,"metadata":[{"value":"BASE_NORM","type":"NORM_CATEGORY","order":1}, {"value":"documentTypeName","type":"TYPE_NAME","order":1}, {"value":"documentTemplateName","type":"TEMPLATE_NAME","order":1}],"sections":null}, {"name":"NORM","order":1,"metadata":[{"value":"officialLongTitle","type":"OFFICIAL_LONG_TITLE","order":1}, {"value":"risAbbreviation","type":"RIS_ABBREVIATION","order":1}, {"value":"documentNumber","type":"DOCUMENT_NUMBER","order":1}, {"value":"documentCategory","type":"DOCUMENT_CATEGORY","order":1}, {"value":"officialShortTitle","type":"OFFICIAL_SHORT_TITLE","order":1}, {"value":"officialAbbreviation","type":"OFFICIAL_ABBREVIATION","order":1}, {"value":"completeCitation","type":"COMPLETE_CITATION","order":1}, {"value":"celexNumber","type":"CELEX_NUMBER","order":1}, {"value":"text","type":"TEXT","order":1}],"sections":null}],
                  "eli":"",
                  "files":[{"name":"norm.zip","hash":"hash","createdAt":"$date"}]}
                """.trimIndent(),
            )
    }
}
