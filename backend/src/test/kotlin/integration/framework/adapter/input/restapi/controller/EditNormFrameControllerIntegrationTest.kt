package de.bund.digitalservice.ris.norms.framework.adapter.input.restapi.controller

import com.fasterxml.jackson.annotation.JsonProperty
import de.bund.digitalservice.ris.caselaw.config.FlywayConfig
import de.bund.digitalservice.ris.norms.application.port.output.SaveNormOutputPort
import de.bund.digitalservice.ris.norms.application.service.EditNormFrameService
import de.bund.digitalservice.ris.norms.application.service.LoadNormService
import de.bund.digitalservice.ris.norms.domain.entity.Norm
import de.bund.digitalservice.ris.norms.domain.value.MetadataSectionName
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType
import de.bund.digitalservice.ris.norms.framework.adapter.output.database.NormsService
import de.bund.digitalservice.ris.norms.framework.adapter.output.database.PostgresTestcontainerIntegrationTest
import de.bund.digitalservice.ris.norms.framework.adapter.output.database.dto.NormDto
import de.bund.digitalservice.ris.shared.exceptions.NotFoundExceptionHandler
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
import org.springframework.http.MediaType
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.csrf
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.web.reactive.function.BodyInserters
import reactor.test.StepVerifier
import utils.createRandomNorm
import java.time.Duration

@ExtendWith(SpringExtension::class)
@Import(FlywayConfig::class, NormsService::class, EditNormFrameService::class, LoadNormService::class, NotFoundExceptionHandler::class)
@WebFluxTest(controllers = [LoadNormController::class, EditNormFrameController::class])
@WithMockUser
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@AutoConfigureDataR2dbc
class EditNormFrameControllerIntegrationTest : PostgresTestcontainerIntegrationTest() {
    @Autowired
    lateinit var webClient: WebTestClient

    @Autowired
    private lateinit var client: DatabaseClient

    @Autowired
    lateinit var normsService: NormsService

    private lateinit var template: R2dbcEntityTemplate

    @BeforeAll
    fun setup() {
        template = R2dbcEntityTemplate(client, PostgresDialect.INSTANCE)
    }

    @AfterEach
    fun cleanUp() {
        template.delete(NormDto::class.java).all().block(Duration.ofSeconds(1))
    }

    companion object {
        private val NORM: Norm = createRandomNorm()
    }

    @Test
    fun `it correctly saves a metadata with sections then calls it back via api`() {
        val saveCommand = SaveNormOutputPort.Command(NORM)
        normsService.saveNorm(saveCommand)
            .`as`(StepVerifier::create)
            .expectNextCount(1)
            .verifyComplete()

        val metadata = EditNormFrameController.MetadatumRequestSchema()
        metadata.type = MetadatumType.DATE
        metadata.value = "2023-12-25"
        val section = EditNormFrameController.MetadataSectionRequestSchema()
        section.name = MetadataSectionName.CITATION_DATE
        section.metadata = listOf(metadata)
        val editedNorm = NormFramePropertiesTestRequestSchema()
        editedNorm.officialLongTitle = "officialLongTitle"
        editedNorm.metadataSections = listOf()

        webClient
            .mutateWith(csrf())
            .put()
            .uri("/api/v1/norms/" + NORM.guid.toString())
            .contentType(MediaType.APPLICATION_JSON)
            .body(BodyInserters.fromValue(editedNorm))
            .exchange()
            .expectStatus()
            .isNoContent
    }

    class NormFramePropertiesTestRequestSchema {
        lateinit var officialLongTitle: String
        lateinit var metadataSections: List<EditNormFrameController.MetadataSectionRequestSchema>
        var documentNumber: String? = null

        @get:JsonProperty("isExpirationDateTemp")
        var eli: String? = null
        var text: String? = null
    }
}
