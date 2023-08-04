package de.bund.digitalservice.ris.norms.framework.adapter.input.restapi.controller

import com.google.gson.Gson
import de.bund.digitalservice.ris.norms.application.service.ValidateNormFrameService
import de.bund.digitalservice.ris.norms.domain.entity.MetadataSection
import de.bund.digitalservice.ris.norms.domain.specification.section.HasAllMandatoryFields
import de.bund.digitalservice.ris.norms.framework.adapter.output.database.PostgresTestcontainerIntegrationTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.r2dbc.AutoConfigureDataR2dbc
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.csrf
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.web.reactive.function.BodyInserters
import utils.createValidValidateNormFrameTestRequestSchema

@ExtendWith(SpringExtension::class)
@Import(ValidateNormFrameService::class)
@WebFluxTest(controllers = [ValidateNormFrameController::class])
@WithMockUser
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@AutoConfigureDataR2dbc
class ValidateNormFrameControllerIntegrationTest : PostgresTestcontainerIntegrationTest() {
  @Autowired lateinit var webClient: WebTestClient

  @Test
  fun `it correctly validates a norm frame`() {
    val validateNormRequestSchema = createValidValidateNormFrameTestRequestSchema()

    webClient
        .mutateWith(csrf())
        .post()
        .uri("/api/v1/norms/norm/validation")
        .contentType(MediaType.APPLICATION_JSON)
        .body(BodyInserters.fromValue(Gson().toJson(validateNormRequestSchema)))
        .exchange()
        .expectStatus()
        .isOk
        .expectBody()
        .json("[]")
  }

  @Test
  fun `it retuns violations`() {
    val validateNormRequestSchema =
        ValidateNormFrameControllerTest.ValidateNormFrameTestRequestSchema(emptyList())
    val violations = HasAllMandatoryFields().evaluate(emptyList<MetadataSection>()).violations
    assert(violations.isNotEmpty())

    webClient
        .mutateWith(csrf())
        .post()
        .uri("/api/v1/norms/norm/validation")
        .contentType(MediaType.APPLICATION_JSON)
        .body(BodyInserters.fromValue(Gson().toJson(validateNormRequestSchema)))
        .exchange()
        .expectStatus()
        .isOk
        .expectBody()
        .json(Gson().toJson(violations))
  }
}
