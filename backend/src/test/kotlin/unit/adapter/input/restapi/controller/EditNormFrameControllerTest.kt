package de.bund.digitalservice.ris.norms.framework.adapter.input.restapi.controller

import com.ninjasquad.springmockk.MockkBean
import de.bund.digitalservice.ris.norms.application.port.input.EditNormFrameUseCase
import de.bund.digitalservice.ris.norms.domain.value.MetadataSectionName
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType
import de.bund.digitalservice.ris.norms.framework.adapter.input.restapi.schema.MetadatumRequestSchema
import io.mockk.every
import io.mockk.slot
import io.mockk.verify
import java.time.LocalDate
import java.time.LocalTime
import java.util.UUID
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
import utils.factory.metadataSection

@ExtendWith(SpringExtension::class)
@WebFluxTest(controllers = [EditNormFrameController::class])
@WithMockUser
class EditNormFrameControllerTest {
  @Autowired lateinit var webClient: WebTestClient

  @MockkBean lateinit var editNormFrameService: EditNormFrameUseCase

  @Test
  fun `it responds with no content status when norm was updated successfully`() {
    every { editNormFrameService.editNormFrame(any()) } returns Mono.just(true)

    webClient
        .mutateWith(csrf())
        .put()
        .uri("/api/v1/norms/761b5537-5aa5-4901-81f7-fbf7e040a7c8")
        .contentType(APPLICATION_JSON)
        .body(
            BodyInserters.fromValue(
                """{ "officialLongTitle": "new title", "metadataSections": [] }"""))
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
        .is5xxServerError
  }

  @Test
  fun `it correctly maps a simple metadata section with some metadata`() {

    every { editNormFrameService.editNormFrame(any()) } returns Mono.just(true)

    webClient
        .mutateWith(csrf())
        .put()
        .uri("/api/v1/norms/761b5537-5aa5-4901-81f7-fbf7e040a7c8")
        .contentType(APPLICATION_JSON)
        .body(
            BodyInserters.fromValue(
                """
                      {
                          "metadataSections": [
                              {
                                  "name": "NORM",
                                  "metadata": [
                                      {
                                          "type": "OFFICIAL_LONG_TITLE",
                                          "value": "official long title"
                                      },
                                      {
                                          "type": "RIS_ABBREVIATION",
                                          "value": "RISABB"
                                      },
                                      {
                                          "type": "KEYWORD",
                                          "value": "keyword1",
                                          "order": 1
                                      },
                                      {
                                          "type": "KEYWORD",
                                          "value": "keyword2",
                                          "order": 2
                                      }
                                  ]
                              }
                          ]
                      }
                """))
        .exchange()

    val command = slot<EditNormFrameUseCase.Command>()
    verify(exactly = 1) { editNormFrameService.editNormFrame(capture(command)) }

    assertThat(command.captured.properties.metadataSections)
        .usingRecursiveComparison()
        .ignoringCollectionOrder()
        .ignoringFieldsOfTypes(UUID::class.java)
        .isEqualTo(
            listOf(
                metadataSection {
                  name = MetadataSectionName.NORM
                  metadata {
                    metadatum {
                      type = MetadatumType.OFFICIAL_LONG_TITLE
                      value = "official long title"
                    }
                    metadatum {
                      type = MetadatumType.RIS_ABBREVIATION
                      value = "RISABB"
                    }
                    metadatum {
                      type = MetadatumType.KEYWORD
                      value = "keyword1"
                      order = 1
                    }
                    metadatum {
                      type = MetadatumType.KEYWORD
                      value = "keyword2"
                      order = 2
                    }
                  }
                },
            ))
  }

  @Test
  fun `it correctly maps a norm with repeated sections`() {

    every { editNormFrameService.editNormFrame(any()) } returns Mono.just(true)

    webClient
        .mutateWith(csrf())
        .put()
        .uri("/api/v1/norms/761b5537-5aa5-4901-81f7-fbf7e040a7c8")
        .contentType(APPLICATION_JSON)
        .body(
            BodyInserters.fromValue(
                """
                    {
                      "metadataSections": [
                          {
                              "name": "NORM_PROVIDER",
                              "order": 1,
                              "metadata": [
                                  {
                                      "value": "entity1",
                                      "type": "ENTITY"
                                  },
                                  {
                                      "value": "body1",
                                      "type": "DECIDING_BODY"
                                  }
                              ]
                          },
                          {
                              "name": "NORM_PROVIDER",
                              "order": 2,
                              "metadata": [
                                  {
                                      "value": "entity2",
                                      "type": "ENTITY"
                                  },
                                  {
                                      "value": "body2",
                                      "type": "DECIDING_BODY"
                                  }
                              ]
                          }
                      ]
                  }
                """))
        .exchange()

    val command = slot<EditNormFrameUseCase.Command>()
    verify(exactly = 1) { editNormFrameService.editNormFrame(capture(command)) }

    assertThat(command.captured.properties.metadataSections)
        .usingRecursiveComparison()
        .ignoringCollectionOrder()
        .ignoringFieldsOfTypes(UUID::class.java)
        .isEqualTo(
            listOf(
                metadataSection {
                  name = MetadataSectionName.NORM_PROVIDER
                  order = 1
                  metadata {
                    metadatum {
                      type = MetadatumType.ENTITY
                      value = "entity1"
                    }
                    metadatum {
                      type = MetadatumType.DECIDING_BODY
                      value = "body1"
                    }
                  }
                },
                metadataSection {
                  name = MetadataSectionName.NORM_PROVIDER
                  order = 2
                  metadata {
                    metadatum {
                      type = MetadatumType.ENTITY
                      value = "entity2"
                    }
                    metadatum {
                      type = MetadatumType.DECIDING_BODY
                      value = "body2"
                    }
                  }
                }))
  }

  @Test
  fun `it correctly maps a norm with nested sections`() {

    every { editNormFrameService.editNormFrame(any()) } returns Mono.just(true)

    webClient
        .mutateWith(csrf())
        .put()
        .uri("/api/v1/norms/761b5537-5aa5-4901-81f7-fbf7e040a7c8")
        .contentType(APPLICATION_JSON)
        .body(
            BodyInserters.fromValue(
                """
                  {
                      "metadataSections": [
                          {
                              "name": "OFFICIAL_REFERENCE",
                              "order": 1,
                              "sections": [
                                  {
                                      "name": "PRINT_ANNOUNCEMENT",
                                      "metadata": [
                                          {
                                              "value": "BGBl I",
                                              "type": "ANNOUNCEMENT_GAZETTE"
                                          },
                                          {
                                              "value": "34",
                                              "type": "PAGE"
                                          }
                                      ]
                                  }
                              ]
                          },
                          {
                              "name": "OFFICIAL_REFERENCE",
                              "order": 2,
                              "sections": [
                                  {
                                      "name": "DIGITAL_ANNOUNCEMENT",
                                      "metadata": [
                                          {
                                              "value": "medium",
                                              "type": "ANNOUNCEMENT_MEDIUM"
                                          },
                                          {
                                              "value": "3",
                                              "type": "EDITION"
                                          }
                                      ]
                                  }
                              ]
                          }
                      ]
                  }
                """))
        .exchange()

    val command = slot<EditNormFrameUseCase.Command>()
    verify(exactly = 1) { editNormFrameService.editNormFrame(capture(command)) }

    assertThat(command.captured.properties.metadataSections)
        .usingRecursiveComparison()
        .ignoringCollectionOrder()
        .ignoringFieldsOfTypes(UUID::class.java)
        .isEqualTo(
            listOf(
                metadataSection {
                  name = MetadataSectionName.OFFICIAL_REFERENCE
                  order = 1
                  sections {
                    metadataSection {
                      name = MetadataSectionName.PRINT_ANNOUNCEMENT
                      metadata {
                        metadatum {
                          value = "BGBl I"
                          type = MetadatumType.ANNOUNCEMENT_GAZETTE
                        }
                        metadatum {
                          value = "34"
                          type = MetadatumType.PAGE
                        }
                      }
                    }
                  }
                },
                metadataSection {
                  name = MetadataSectionName.OFFICIAL_REFERENCE
                  order = 2
                  sections {
                    metadataSection {
                      name = MetadataSectionName.DIGITAL_ANNOUNCEMENT
                      metadata {
                        metadatum {
                          value = "medium"
                          type = MetadatumType.ANNOUNCEMENT_MEDIUM
                        }
                        metadatum {
                          value = "3"
                          type = MetadatumType.EDITION
                        }
                      }
                    }
                  }
                }))
  }

  @Test
  fun `it correctly maps the dates from string to localdate in metadata`() {
    val schema = MetadatumRequestSchema()
    schema.value = "2022-12-01"
    schema.type = MetadatumType.DATE

    assertThat(schema.toUseCaseData().value).isEqualTo(LocalDate.of(2022, 12, 1))
  }

  @Test
  fun `it correctly maps the times from string to localtime in metadata`() {
    val schema = MetadatumRequestSchema()
    schema.value = "13:55"
    schema.type = MetadatumType.TIME

    assertThat(schema.toUseCaseData().value).isEqualTo(LocalTime.of(13, 55))
  }
}
