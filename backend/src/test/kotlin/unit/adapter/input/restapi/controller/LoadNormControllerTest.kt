package de.bund.digitalservice.ris.norms.framework.adapter.input.restapi.controller

import com.ninjasquad.springmockk.MockkBean
import de.bund.digitalservice.ris.norms.application.port.input.LoadNormUseCase
import de.bund.digitalservice.ris.norms.domain.entity.*
import de.bund.digitalservice.ris.norms.domain.value.DocumentSectionType
import de.bund.digitalservice.ris.norms.domain.value.MetadataSectionName
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType
import io.mockk.every
import io.mockk.slot
import io.mockk.verify
import java.time.LocalDateTime
import java.util.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.csrf
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.reactive.server.WebTestClient
import reactor.core.publisher.Mono
import utils.factory.norm

@ExtendWith(SpringExtension::class)
@WebFluxTest(controllers = [LoadNormController::class])
@WithMockUser
class LoadNormControllerTest {
  @Autowired lateinit var webClient: WebTestClient

  @MockkBean lateinit var loadNormService: LoadNormUseCase

  @Test
  fun `it calls the load norm service with the correct query to get a norm by GUID`() {
    val norm = norm { guid = UUID.fromString("761b5537-5aa5-4901-81f7-fbf7e040a7c8") }
    every { loadNormService.loadNorm(any()) } returns Mono.just(norm)

    webClient
        .mutateWith(csrf())
        .get()
        .uri("/api/v1/norms/761b5537-5aa5-4901-81f7-fbf7e040a7c8")
        .exchange()

    val query = slot<LoadNormUseCase.Query>()
    verify(exactly = 1) { loadNormService.loadNorm(capture(query)) }
    assertThat(query.captured.guid.toString()).isEqualTo("761b5537-5aa5-4901-81f7-fbf7e040a7c8")
  }

  @Test
  fun `it responds with ok status if the norm was loaded successfully`() {
    val norm = norm { guid = UUID.fromString("761b5537-5aa5-4901-81f7-fbf7e040a7c8") }
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
  fun `it correctly maps a norm with a file reference`() {
    val norm = norm {
      guid = UUID.randomUUID()
      files {
        file {
          name = "norm.zip"
          hash = "hash"
          createdAt = LocalDateTime.parse("2023-09-05T09:31:21.390936")
        }
      }
    }

    every { loadNormService.loadNorm(any()) } returns Mono.just(norm)

    webClient
        .mutateWith(csrf())
        .get()
        .uri("/api/v1/norms/${norm.guid}")
        .exchange()
        .expectBody()
        .json(
            """
                {
                    "guid": "${norm.guid}",
                    "files": [
                        {
                            "name": "norm.zip",
                            "hash": "hash",
                            "createdAt": "2023-09-05T09:31:21.390936"
                        }
                    ]
                }
        """)
  }

  @Test
  fun `it correctly maps a norm with a simple metadata section`() {
    val norm = norm {
      guid = UUID.randomUUID()
      metadataSections {
        metadataSection {
          name = MetadataSectionName.NORM
          metadata {
            metadatum {
              value = "official long title"
              type = MetadatumType.OFFICIAL_LONG_TITLE
            }
            metadatum {
              value = "RISABB"
              type = MetadatumType.RIS_ABBREVIATION
            }
            metadatum {
              value = "keyword1"
              type = MetadatumType.KEYWORD
              order = 1
            }
            metadatum {
              value = "keyword2"
              type = MetadatumType.KEYWORD
              order = 2
            }
          }
        }
      }
    }

    every { loadNormService.loadNorm(any()) } returns Mono.just(norm)

    webClient
        .mutateWith(csrf())
        .get()
        .uri("/api/v1/norms/${norm.guid}")
        .exchange()
        .expectBody()
        .json(
            """
                {
                    "guid": "${norm.guid}",
                    "metadataSections": [
                        {
                              "name": "NORM",
                              "metadata": [
                                  {
                                      "value": "official long title",
                                      "type": "OFFICIAL_LONG_TITLE"
                                  },
                                  {
                                      "value": "RISABB",
                                      "type": "RIS_ABBREVIATION"
                                  },
                                  {
                                      "value": "keyword1",
                                      "type": "KEYWORD"
                                  },
                                  {
                                      "value": "keyword2",
                                      "type": "KEYWORD"
                                  }
                              ]
                          }
                    ]
                }
        """,
        )
  }

  @Test
  fun `it correctly maps a norm with repeated sections`() {
    val norm = norm {
      guid = UUID.randomUUID()
      metadataSections {
        metadataSection {
          name = MetadataSectionName.NORM_PROVIDER
          order = 1
          metadata {
            metadatum {
              value = "entity1"
              type = MetadatumType.ENTITY
            }
            metadatum {
              value = "body1"
              type = MetadatumType.DECIDING_BODY
            }
          }
        }
        metadataSection {
          name = MetadataSectionName.NORM_PROVIDER
          order = 2
          metadata {
            metadatum {
              value = "entity2"
              type = MetadatumType.ENTITY
            }
            metadatum {
              value = "body2"
              type = MetadatumType.DECIDING_BODY
            }
          }
        }
      }
    }

    every { loadNormService.loadNorm(any()) } returns Mono.just(norm)

    webClient
        .mutateWith(csrf())
        .get()
        .uri("/api/v1/norms/fc513c68-1126-4797-955f-96492a1a7b3b")
        .exchange()
        .expectBody()
        .json(
            """
                {
                    "guid": "${norm.guid}",
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
        """,
        )
  }

  @Test
  fun `it correctly maps a norm with nested sections and eli`() {
    val norm = norm {
      guid = UUID.randomUUID()
      metadataSections {
        metadataSection {
          name = MetadataSectionName.ANNOUNCEMENT_DATE
          metadata {
            metadatum {
              value = "2023"
              type = MetadatumType.YEAR
            }
          }
        }
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
        }
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
        }
      }
    }

    every { loadNormService.loadNorm(any()) } returns Mono.just(norm)

    webClient
        .mutateWith(csrf())
        .get()
        .uri("/api/v1/norms/fc513c68-1126-4797-955f-96492a1a7b3b")
        .exchange()
        .expectBody()
        .json(
            """
            {
              "guid": "${norm.guid}",
              "eli": "eli/bgbl-1/2023/s34",
              "metadataSections": [
                  {
                      "name": "ANNOUNCEMENT_DATE",
                      "order": 1,
                      "metadata": [
                          {
                              "value": "2023",
                              "type": "YEAR"
                          }
                      ]
                  },
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
        """,
        )
  }

  @Test
  fun `it correctly maps a norm with top level articles`() {
    val norm = norm {
      guid = UUID.randomUUID()
      documentation {
        article {
          order = 1
          marker = "§ 1"
          heading = "article one"
          paragraphs {
            paragraph {
              marker = "(1)"
              text = "paragraph one"
            }
            paragraph {
              marker = "(2)"
              text = "paragraph two"
            }
          }
        }
        article {
          order = 2
          marker = "§ 2"
          heading = "article two"
          paragraphs {
            paragraph {
              marker = "(3)"
              text = "paragraph three"
            }
            paragraph {
              marker = "(4)"
              text = "paragraph four"
            }
          }
        }
      }
    }

    every { loadNormService.loadNorm(any()) } returns Mono.just(norm)

    webClient
        .mutateWith(csrf())
        .get()
        .uri("/api/v1/norms/${norm.guid}")
        .exchange()
        .expectBody()
        .json(
            """
                {
                    "guid": "${norm.guid}",
                    "documentation": [
                        {
                            "marker": "§ 1",
                            "heading": "article one",
                            "order": 1,
                            "paragraphs": [
                                {
                                "marker": "(1)",
                                "text": "paragraph one"
                                },
                                 {
                                "marker": "(2)",
                                "text": "paragraph two"
                                }
                            ]
                        },
                        {
                            "marker": "§ 2",
                            "heading": "article two",
                            "order": 2,
                            "paragraphs": [
                                {
                                "marker": "(3)",
                                "text": "paragraph three"
                                },
                                 {
                                "marker": "(4)",
                                "text": "paragraph four"
                                }
                            ]
                        }
                    ]
                }
        """)
  }

  @Test
  fun `it correctly maps a norm with nested sections and articles at the end`() {
    val norm = norm {
      guid = UUID.randomUUID()
      documentation {
        documentSection {
          marker = "Teil I"
          heading = "Das ist der erste Teil"
          type = DocumentSectionType.PART
          documentation {
            documentSection {
              marker = "Abschnitt 1"
              heading = "Das ist der erste Abschnitt"
              type = DocumentSectionType.SECTION
              documentation {
                documentSection {
                  marker = "Unterabschnitt 1"
                  heading = "Das ist der erste Unterabschnitt"
                  type = DocumentSectionType.SUBSECTION
                  documentation {
                    article {
                      order = 1
                      marker = "§ 1"
                      heading = "article one"
                      paragraphs {
                        paragraph {
                          marker = "(1)"
                          text = "paragraph one"
                        }
                      }
                    }
                  }
                }
              }
            }
          }
        }
      }
    }

    every { loadNormService.loadNorm(any()) } returns Mono.just(norm)

    webClient
        .mutateWith(csrf())
        .get()
        .uri("/api/v1/norms/${norm.guid}")
        .exchange()
        .expectBody()
        .json(
            """
                {
                    "guid": "${norm.guid}",
                    "documentation": [
                        {
                            "type": "PART",
                            "marker": "Teil I",
                            "heading": "Das ist der erste Teil",
                            "documentation": [
                                {
                                    "type": "SECTION",
                                    "marker": "Abschnitt 1",
                                    "heading": "Das ist der erste Abschnitt",
                                    "documentation": [
                                        {
                                            "type": "SUBSECTION",
                                            "marker": "Unterabschnitt 1",
                                            "heading": "Das ist der erste Unterabschnitt",
                                            "documentation": [
                                                {
                                                    "marker": "§ 1",
                                                    "heading": "article one",
                                                    "paragraphs": [
                                                        {
                                                            "marker": "(1)",
                                                            "text": "paragraph one"
                                                        }
                                                    ]
                                                }
                                            ]
                                        }
                                    ]
                                }
                            ]
                        }
                    ]
                }
        """)
  }

  @Test
  fun `it sends a not found response with an json error list if the load norm service responds with empty`() {
    every { loadNormService.loadNorm(any()) } returns Mono.empty()

    webClient
        .mutateWith(csrf())
        .get()
        .uri("/api/v1/norms/72631e54-78a4-11d0-bcf7-00aa00b7b32a")
        .exchange()
        .expectStatus()
        .isNotFound()
        .expectBody()
        .json(
            """
                {
                  "errors" : [
                      {
                        "code" : "NOT_FOUND",
                        "instance" : "72631e54-78a4-11d0-bcf7-00aa00b7b32a",
                        "message": ""
                      }
                  ]
                }
                """
                .trimIndent(),
        )
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
