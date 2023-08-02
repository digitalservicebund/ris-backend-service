package de.bund.digitalservice.ris.norms.framework.adapter.input.restapi.controller

import de.bund.digitalservice.ris.caselaw.config.FlywayConfig
import de.bund.digitalservice.ris.norms.application.service.MigrateNormService
import de.bund.digitalservice.ris.norms.framework.adapter.output.database.NormsService
import de.bund.digitalservice.ris.norms.framework.adapter.output.database.PostgresTestcontainerIntegrationTest
import de.bund.digitalservice.ris.norms.framework.adapter.output.database.dto.NormDto
import de.bund.digitalservice.ris.norms.framework.adapter.output.juris.JurisConverter
import de.bund.digitalservice.ris.norms.juris.converter.model.Norm
import java.time.Duration
import java.util.*
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

@ExtendWith(SpringExtension::class)
@Import(
    FlywayConfig::class,
    MigrateNormService::class,
    NormsService::class,
    JurisConverter::class,
)
@WebFluxTest(controllers = [MigrateNormController::class])
@WithMockUser
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@AutoConfigureDataR2dbc
class MigrateNormControllerIntegrationTest : PostgresTestcontainerIntegrationTest() {
  @Autowired lateinit var webClient: WebTestClient

  @Autowired private lateinit var client: DatabaseClient

  private lateinit var template: R2dbcEntityTemplate

  @Autowired lateinit var migrateNormService: MigrateNormService

  @Autowired lateinit var normService: NormsService

  @Autowired lateinit var jurisConverter: JurisConverter

  @BeforeAll
  fun setup() {
    template = R2dbcEntityTemplate(client, PostgresDialect.INSTANCE)
  }

  @AfterEach
  fun cleanUp() {
    template.delete(NormDto::class.java).all().block(Duration.ofSeconds(1))
  }

  companion object {
    private val data =
        ConverterNormsSchema(
            norms =
                listOf(ConverterNormSchema(UUID.randomUUID(), Norm(officialLongTitle = "Test"))))
  }

  @Test
  fun `it correctly saves a metadata with sections then calls it back via api`() {
    webClient
        .mutateWith(csrf())
        .post()
        .uri("/api/v1/norms/migrate")
        .contentType(MediaType.APPLICATION_JSON)
        .body(BodyInserters.fromValue(data))
        .exchange()
        .expectStatus()
        .isCreated
  }

  data class ConverterNormsSchema(var norms: List<ConverterNormSchema> = emptyList())

  data class ConverterNormSchema(val guid: UUID, val norm: Norm)
}
