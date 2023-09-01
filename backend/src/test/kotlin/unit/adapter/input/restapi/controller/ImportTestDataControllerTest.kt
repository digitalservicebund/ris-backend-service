package de.bund.digitalservice.ris.norms.framework.adapter.input.restapi.controller

import com.ninjasquad.springmockk.MockkBean
import de.bund.digitalservice.ris.norms.application.port.input.ImportTestDataUseCase
import de.bund.digitalservice.ris.norms.domain.value.DocumentSectionType.BOOK
import de.bund.digitalservice.ris.norms.domain.value.DocumentSectionType.CHAPTER
import de.bund.digitalservice.ris.norms.domain.value.MetadataSectionName.NORM
import de.bund.digitalservice.ris.norms.domain.value.MetadataSectionName.OFFICIAL_REFERENCE
import de.bund.digitalservice.ris.norms.domain.value.MetadataSectionName.PRINT_ANNOUNCEMENT
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.CELEX_NUMBER
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.OFFICIAL_LONG_TITLE
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.PAGE
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.YEAR
import io.mockk.every
import io.mockk.slot
import io.mockk.verify
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
import utils.factory.article
import utils.factory.documentSection
import utils.factory.metadataSection
import utils.factory.recitals

@ExtendWith(SpringExtension::class)
@WebFluxTest(controllers = [ImportTestDataController::class])
@WithMockUser
class ImportTestDataControllerTest {
  @Autowired lateinit var webClient: WebTestClient

  @MockkBean lateinit var importTestDataService: ImportTestDataUseCase

  @Test
  fun `it calls the import test data service with a generated GUID`() {
    every { importTestDataService.importTestData(any()) } returns Mono.just(true)

    webClient
        .mutateWith(csrf())
        .post()
        .uri("/api/v1/norms/test-data")
        .contentType(APPLICATION_JSON)
        .body(BodyInserters.fromValue("{}"))
        .exchange()

    verify(exactly = 1) {
      importTestDataService.importTestData(withArg { assertThat(it.norm.guid).isNotNull() })
    }
  }

  @Test
  fun `it sends the GUID of the new norm within the response body`() {
    every { importTestDataService.importTestData(any()) } returns Mono.just(true)

    webClient
        .mutateWith(csrf())
        .post()
        .uri("/api/v1/norms/test-data")
        .contentType(APPLICATION_JSON)
        .body(BodyInserters.fromValue("{}"))
        .exchange()
        .expectBody()
        .jsonPath("guid")
        .exists()
  }

  @Test
  fun `it responds with created status when norm was imported`() {
    every { importTestDataService.importTestData(any()) } returns Mono.just(true)

    webClient
        .mutateWith(csrf())
        .post()
        .uri("/api/v1/norms/test-data")
        .contentType(APPLICATION_JSON)
        .body(BodyInserters.fromValue("{}"))
        .exchange()
        .expectStatus()
        .isCreated
  }

  @Test
  fun `it sets the eGesetzgebungsflag to false for the new norm`() {
    every { importTestDataService.importTestData(any()) } returns Mono.just(true)

    webClient
        .mutateWith(csrf())
        .post()
        .uri("/api/v1/norms/test-data")
        .contentType(APPLICATION_JSON)
        .body(BodyInserters.fromValue("{}"))
        .exchange()

    verify(exactly = 1) {
      importTestDataService.importTestData(withArg { assertThat(it.norm.eGesetzgebung).isFalse() })
    }
  }

  @Test
  fun `it correctly maps a metadata section with some metadata`() {
    every { importTestDataService.importTestData(any()) } returns Mono.just(true)

    webClient
        .mutateWith(csrf())
        .post()
        .uri("/api/v1/norms/test-data")
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
                          "value": "test title"
                        },
                        {
                          "type": "CELEX_NUMBER",
                          "value": "test number"
                        }
                      ]
                    }
                  ]
                }
                """))
        .exchange()
        .expectStatus()
        .is2xxSuccessful()

    val command = slot<ImportTestDataUseCase.Command>()
    verify(exactly = 1) { importTestDataService.importTestData(capture(command)) }

    assertThat(command.captured.norm.metadataSections)
        .usingRecursiveComparison()
        .ignoringCollectionOrder()
        .ignoringFieldsOfTypes(UUID::class.java)
        .isEqualTo(
            listOf(
                metadataSection {
                  name = NORM
                  metadata {
                    metadatum {
                      type = OFFICIAL_LONG_TITLE
                      value = "test title"
                    }
                    metadatum {
                      type = CELEX_NUMBER
                      value = "test number"
                    }
                  }
                },
            ))
  }

  @Test
  fun `it uses the order value 1 per default for metadata sections and metadata`() {
    every { importTestDataService.importTestData(any()) } returns Mono.just(true)

    webClient
        .mutateWith(csrf())
        .post()
        .uri("/api/v1/norms/test-data")
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
                          "value": "test title"
                        }
                      ]
                    }
                  ]
                }
                """))
        .exchange()
        .expectStatus()
        .is2xxSuccessful()

    val command = slot<ImportTestDataUseCase.Command>()
    verify(exactly = 1) { importTestDataService.importTestData(capture(command)) }

    assertThat(command.captured.norm.metadataSections)
        .usingRecursiveComparison()
        .ignoringCollectionOrder()
        .ignoringFieldsOfTypes(UUID::class.java)
        .isEqualTo(
            listOf(
                metadataSection {
                  name = NORM
                  order = 1
                  metadata {
                    metadatum {
                      type = OFFICIAL_LONG_TITLE
                      order = 1
                      value = "test title"
                    }
                  }
                },
            ))
  }

  @Test
  fun `it correctly maps the some empty repeated metadata sections`() {
    every { importTestDataService.importTestData(any()) } returns Mono.just(true)

    webClient
        .mutateWith(csrf())
        .post()
        .uri("/api/v1/norms/test-data")
        .contentType(APPLICATION_JSON)
        .body(
            BodyInserters.fromValue(
                """
                {
                  "metadataSections": [
                    {
                      "name": "NORM",
                      "order": 1
                    },
                    {
                      "name": "NORM",
                      "order": 2
                    }
                  ]
                }
                """))
        .exchange()
        .expectStatus()
        .is2xxSuccessful()

    val command = slot<ImportTestDataUseCase.Command>()
    verify(exactly = 1) { importTestDataService.importTestData(capture(command)) }

    assertThat(command.captured.norm.metadataSections)
        .usingRecursiveComparison()
        .ignoringCollectionOrder()
        .ignoringFieldsOfTypes(UUID::class.java)
        .isEqualTo(
            listOf(
                metadataSection {
                  name = NORM
                  order = 1
                },
                metadataSection {
                  name = NORM
                  order = 2
                },
            ))
  }

  @Test
  fun `it correctly maps the a metadata section with a child section`() {
    every { importTestDataService.importTestData(any()) } returns Mono.just(true)

    webClient
        .mutateWith(csrf())
        .post()
        .uri("/api/v1/norms/test-data")
        .contentType(APPLICATION_JSON)
        .body(
            BodyInserters.fromValue(
                """{
                  "metadataSections": [
                    {
                      "name": "OFFICIAL_REFERENCE",
                      "sections": [
                        {
                          "name": "PRINT_ANNOUNCEMENT"
                        }
                      ]
                    }
                  ]
                }"""))
        .exchange()
        .expectStatus()
        .is2xxSuccessful()

    val command = slot<ImportTestDataUseCase.Command>()
    verify(exactly = 1) { importTestDataService.importTestData(capture(command)) }

    assertThat(command.captured.norm.metadataSections)
        .usingRecursiveComparison()
        .ignoringCollectionOrder()
        .ignoringFieldsOfTypes(UUID::class.java)
        .isEqualTo(
            listOf(
                metadataSection {
                  name = OFFICIAL_REFERENCE
                  order = 1
                  sections { metadataSection { name = PRINT_ANNOUNCEMENT } }
                }))
  }

  @Test
  fun `it correctly maps the some nested metadata`() {
    every { importTestDataService.importTestData(any()) } returns Mono.just(true)

    webClient
        .mutateWith(csrf())
        .post()
        .uri("/api/v1/norms/test-data")
        .contentType(APPLICATION_JSON)
        .body(
            BodyInserters.fromValue(
                """
                {
                  "metadataSections": [
                    {
                      "name": "OFFICIAL_REFERENCE",
                      "sections": [
                        {
                          "name": "PRINT_ANNOUNCEMENT",
                          "metadata": [
                            {
                              "type": "YEAR",
                              "value": "1985"
                            },
                            {
                              "type": "PAGE",
                              "value": "176"
                            }
                          ]
                        }
                      ]
                    }
                  ]
                }
                """))
        .exchange()
        .expectStatus()
        .is2xxSuccessful()

    val command = slot<ImportTestDataUseCase.Command>()
    verify(exactly = 1) { importTestDataService.importTestData(capture(command)) }

    assertThat(command.captured.norm.metadataSections)
        .usingRecursiveComparison()
        .ignoringCollectionOrder()
        .ignoringFieldsOfTypes(UUID::class.java)
        .isEqualTo(
            listOf(
                metadataSection {
                  name = OFFICIAL_REFERENCE
                  sections {
                    metadataSection {
                      name = PRINT_ANNOUNCEMENT
                      metadata {
                        metadatum {
                          type = YEAR
                          value = "1985"
                        }
                        metadatum {
                          type = PAGE
                          value = "176"
                        }
                      }
                    }
                  }
                }))
  }

  @Test
  fun `it correctly maps some top level articles without paragraphs`() {
    every { importTestDataService.importTestData(any()) } returns Mono.just(true)

    webClient
        .mutateWith(csrf())
        .post()
        .uri("/api/v1/norms/test-data")
        .contentType(APPLICATION_JSON)
        .body(
            BodyInserters.fromValue(
                """
                {
                  "documentation": [
                    {
                      "order": 1,
                      "marker": "§ 1",
                      "paragraphs": []
                    },
                    {
                      "order": 2,
                      "marker": "§ 2",
                      "heading": "Second Article",
                      "paragraphs": []
                    }
                  ]
                }
                """))
        .exchange()
        .expectStatus()
        .is2xxSuccessful()

    val command = slot<ImportTestDataUseCase.Command>()
    verify(exactly = 1) { importTestDataService.importTestData(capture(command)) }

    assertThat(command.captured.norm.documentation)
        .usingRecursiveComparison()
        .ignoringCollectionOrder()
        .ignoringFieldsOfTypes(UUID::class.java)
        .isEqualTo(
            listOf(
                article {
                  order = 1
                  marker = "§ 1"
                },
                article {
                  order = 2
                  marker = "§ 2"
                  heading = "Second Article"
                },
            ))
  }

  @Test
  fun `it correctly maps some top level articles with some paragraphs`() {
    every { importTestDataService.importTestData(any()) } returns Mono.just(true)

    webClient
        .mutateWith(csrf())
        .post()
        .uri("/api/v1/norms/test-data")
        .contentType(APPLICATION_JSON)
        .body(
            BodyInserters.fromValue(
                """
                {
                  "documentation": [
                    {
                      "order": 1,
                      "marker": "§ 1",
                      "paragraphs": [
                        {
                          "marker": "(1)",
                          "text": "some text"
                        },
                        {
                          "marker": "(2)",
                          "text": "more text"
                        }
                      ]
                    },
                    {
                      "order": 2,
                      "marker": "§ 2",
                      "paragraphs": [
                        {
                          "marker": "(1)",
                          "text": "even more"
                        }
                      ]
                    }
                  ]
                }
                """))
        .exchange()
        .expectStatus()
        .is2xxSuccessful()

    val command = slot<ImportTestDataUseCase.Command>()
    verify(exactly = 1) { importTestDataService.importTestData(capture(command)) }

    assertThat(command.captured.norm.documentation)
        .usingRecursiveComparison()
        .ignoringCollectionOrder()
        .ignoringFieldsOfTypes(UUID::class.java)
        .isEqualTo(
            listOf(
                article {
                  order = 1
                  marker = "§ 1"
                  paragraphs {
                    paragraph {
                      marker = "(1)"
                      text = "some text"
                    }
                    paragraph {
                      marker = "(2)"
                      text = "more text"
                    }
                  }
                },
                article {
                  order = 2
                  marker = "§ 2"
                  paragraphs {
                    paragraph {
                      marker = "(1)"
                      text = "even more"
                    }
                  }
                },
            ))
  }

  @Test
  fun `it correctly maps document sections with articles`() {
    every { importTestDataService.importTestData(any()) } returns Mono.just(true)

    webClient
        .mutateWith(csrf())
        .post()
        .uri("/api/v1/norms/test-data")
        .contentType(APPLICATION_JSON)
        .body(
            BodyInserters.fromValue(
                """
                {
                  "documentation": [
                    {
                      "marker": "1",
                      "heading": "Book 1",
                      "type": "BOOK",
                      "documentation": [
                        {
                          "marker": "§ 1",
                          "paragraphs": []
                        },
                        {
                          "marker": "§ 2",
                          "heading": "Article 2",
                          "paragraphs": []
                        }
                      ]
                    }
                  ]
                }
                """))
        .exchange()
        .expectStatus()
        .is2xxSuccessful()

    val command = slot<ImportTestDataUseCase.Command>()
    verify(exactly = 1) { importTestDataService.importTestData(capture(command)) }

    assertThat(command.captured.norm.documentation)
        .usingRecursiveComparison()
        .ignoringCollectionOrder()
        .ignoringFieldsOfTypes(UUID::class.java)
        .isEqualTo(
            listOf(
                documentSection {
                  marker = "1"
                  heading = "Book 1"
                  type = BOOK
                  documentation {
                    article { marker = "§ 1" }
                    article {
                      marker = "§ 2"
                      heading = "Article 2"
                    }
                  }
                },
            ),
        )
  }

  @Test
  fun `it uses the order value 1 per default for document sections and articles`() {
    every { importTestDataService.importTestData(any()) } returns Mono.just(true)

    webClient
        .mutateWith(csrf())
        .post()
        .uri("/api/v1/norms/test-data")
        .contentType(APPLICATION_JSON)
        .body(
            BodyInserters.fromValue(
                """
                {
                  "documentation": [
                    {
                      "marker": "1",
                      "heading": "Book 1",
                      "type": "BOOK",
                      "documentation": [
                        {
                          "marker": "§ 1",
                          "paragraphs": []
                        }
                      ]
                    }
                  ]
                }
                """))
        .exchange()
        .expectStatus()
        .is2xxSuccessful()

    val command = slot<ImportTestDataUseCase.Command>()
    verify(exactly = 1) { importTestDataService.importTestData(capture(command)) }

    assertThat(command.captured.norm.documentation)
        .usingRecursiveComparison()
        .ignoringCollectionOrder()
        .ignoringFieldsOfTypes(UUID::class.java)
        .isEqualTo(
            listOf(
                documentSection {
                  order = 1
                  marker = "1"
                  heading = "Book 1"
                  type = BOOK
                  documentation {
                    article {
                      order = 1
                      marker = "§ 1"
                    }
                  }
                },
            ),
        )
  }

  @Test
  fun `it correctly maps some empty repeated document sections`() {
    every { importTestDataService.importTestData(any()) } returns Mono.just(true)

    webClient
        .mutateWith(csrf())
        .post()
        .uri("/api/v1/norms/test-data")
        .contentType(APPLICATION_JSON)
        .body(
            BodyInserters.fromValue(
                """
                {
                  "documentation": [
                    {
                      "order": 1,
                      "marker": "1",
                      "heading": "Book 1",
                      "type": "BOOK"
                    },
                    {
                      "order": 2,
                      "marker": "2",
                      "heading": "Book 2",
                      "type": "BOOK"
                    }
                  ]
                }
                """))
        .exchange()
        .expectStatus()
        .is2xxSuccessful()

    val command = slot<ImportTestDataUseCase.Command>()
    verify(exactly = 1) { importTestDataService.importTestData(capture(command)) }

    assertThat(command.captured.norm.documentation)
        .usingRecursiveComparison()
        .ignoringCollectionOrder()
        .ignoringFieldsOfTypes(UUID::class.java)
        .isEqualTo(
            listOf(
                documentSection {
                  order = 1
                  marker = "1"
                  heading = "Book 1"
                  type = BOOK
                },
                documentSection {
                  order = 2
                  marker = "2"
                  heading = "Book 2"
                  type = BOOK
                },
            ),
        )
  }

  @Test
  fun `it correctly maps a document section with a child document section`() {
    every { importTestDataService.importTestData(any()) } returns Mono.just(true)

    webClient
        .mutateWith(csrf())
        .post()
        .uri("/api/v1/norms/test-data")
        .contentType(APPLICATION_JSON)
        .body(
            BodyInserters.fromValue(
                """
                {
                  "documentation": [
                    {
                      "marker": "1",
                      "heading": "Book 1",
                      "type": "BOOK",
                      "documentation": [
                        {
                          "marker": "1",
                          "heading": "Chapter 1",
                          "type": "CHAPTER"
                        }
                      ]
                    }
                  ]
                }
                """))
        .exchange()
        .expectStatus()
        .is2xxSuccessful()

    val command = slot<ImportTestDataUseCase.Command>()
    verify(exactly = 1) { importTestDataService.importTestData(capture(command)) }

    assertThat(command.captured.norm.documentation)
        .usingRecursiveComparison()
        .ignoringCollectionOrder()
        .ignoringFieldsOfTypes(UUID::class.java)
        .isEqualTo(
            listOf(
                documentSection {
                  marker = "1"
                  heading = "Book 1"
                  type = BOOK
                  documentation {
                    documentSection {
                      marker = "1"
                      heading = "Chapter 1"
                      type = CHAPTER
                    }
                  }
                },
            ),
        )
  }

  @Test
  fun `it correctly maps some nested documentation`() {
    every { importTestDataService.importTestData(any()) } returns Mono.just(true)

    webClient
        .mutateWith(csrf())
        .post()
        .uri("/api/v1/norms/test-data")
        .contentType(APPLICATION_JSON)
        .body(
            BodyInserters.fromValue(
                """
                {
                  "documentation": [
                    {
                      "marker": "1",
                      "heading": "Book 1",
                      "type": "BOOK",
                      "documentation": [
                        {
                          "marker": "1",
                          "heading": "Chapter 1",
                          "type": "CHAPTER",
                          "documentation": [
                            {
                              "marker": "§ 1",
                              "paragraphs": []
                            },
                            {
                              "marker": "§ 2",
                              "paragraphs": []
                            }
                          ]
                        },
                        {
                          "marker": "2",
                          "heading": "Chapter 2",
                          "type": "CHAPTER"
                        }
                      ]
                    }
                  ]
                }
                """))
        .exchange()
        .expectStatus()
        .is2xxSuccessful()

    val command = slot<ImportTestDataUseCase.Command>()
    verify(exactly = 1) { importTestDataService.importTestData(capture(command)) }

    assertThat(command.captured.norm.documentation)
        .usingRecursiveComparison()
        .ignoringCollectionOrder()
        .ignoringFieldsOfTypes(UUID::class.java)
        .isEqualTo(
            listOf(
                documentSection {
                  marker = "1"
                  heading = "Book 1"
                  type = BOOK
                  documentation {
                    documentSection {
                      marker = "1"
                      heading = "Chapter 1"
                      type = CHAPTER
                      documentation {
                        article { marker = "§ 1" }
                        article { marker = "§ 2" }
                      }
                    }
                    documentSection {
                      marker = "2"
                      heading = "Chapter 2"
                      type = CHAPTER
                    }
                  }
                },
            ),
        )
  }

  @Test
  fun `it correctly maps some an optional recitals`() {
    every { importTestDataService.importTestData(any()) } returns Mono.just(true)

    webClient
        .mutateWith(csrf())
        .post()
        .uri("/api/v1/norms/test-data")
        .contentType(APPLICATION_JSON)
        .body(
            BodyInserters.fromValue(
                """
                {
                  "recitals": {
                    "marker": "test marker",
                    "heading": "Recitals",
                    "text": "some text"
                  }
                }
                """))
        .exchange()
        .expectStatus()
        .is2xxSuccessful()

    val command = slot<ImportTestDataUseCase.Command>()
    verify(exactly = 1) { importTestDataService.importTestData(capture(command)) }

    assertThat(command.captured.norm.recitals)
        .usingRecursiveComparison()
        .ignoringCollectionOrder()
        .ignoringFieldsOfTypes(UUID::class.java)
        .isEqualTo(
            recitals {
              marker = "test marker"
              heading = "Recitals"
              text = "some text"
            })
  }

  fun `it sends an internal error response if the import norm service throws an exception`() {
    every { importTestDataService.importTestData(any()) } throws Error()

    webClient
        .mutateWith(csrf())
        .post()
        .uri("/api/v1/norms/test-data")
        .contentType(APPLICATION_JSON)
        .body(BodyInserters.fromValue("{}"))
        .exchange()
        .expectStatus()
        .is5xxServerError
  }

  @Test
  fun `it sends an internal error response if the import norm service emits nothing`() {
    every { importTestDataService.importTestData(any()) } returns Mono.empty()

    webClient
        .mutateWith(csrf())
        .post()
        .uri("/api/v1/norms/test-data")
        .contentType(APPLICATION_JSON)
        .body(BodyInserters.fromValue("{}"))
        .exchange()
        .expectStatus()
        .is5xxServerError
  }
}
