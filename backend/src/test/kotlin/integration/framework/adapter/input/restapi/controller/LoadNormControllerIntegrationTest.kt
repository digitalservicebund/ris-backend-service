package de.bund.digitalservice.ris.norms.framework.adapter.input.restapi.controller

import de.bund.digitalservice.ris.caselaw.config.FlywayConfig
import de.bund.digitalservice.ris.norms.application.port.output.SaveNormOutputPort
import de.bund.digitalservice.ris.norms.application.service.LoadNormService
import de.bund.digitalservice.ris.norms.domain.value.DocumentSectionType.BOOK
import de.bund.digitalservice.ris.norms.domain.value.DocumentSectionType.PART
import de.bund.digitalservice.ris.norms.domain.value.MetadataSectionName
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType
import de.bund.digitalservice.ris.norms.domain.value.NormCategory
import de.bund.digitalservice.ris.norms.framework.adapter.output.database.NormsService
import de.bund.digitalservice.ris.norms.framework.adapter.output.database.PostgresTestcontainerIntegrationTest
import de.bund.digitalservice.ris.norms.framework.adapter.output.database.dto.NormDto
import java.time.Duration
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.UUID
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

@ExtendWith(SpringExtension::class)
@Import(FlywayConfig::class, NormsService::class, LoadNormService::class)
@WebFluxTest(controllers = [LoadNormController::class])
@WithMockUser
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@AutoConfigureDataR2dbc
class LoadNormControllerIntegrationTest : PostgresTestcontainerIntegrationTest() {
  @Autowired lateinit var webClient: WebTestClient

  @Autowired private lateinit var client: DatabaseClient

  @Autowired lateinit var normsService: NormsService

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
            metadatum {
              value = "officialLongTitle"
              type = MetadatumType.OFFICIAL_LONG_TITLE
            }
            metadatum {
              value = "risAbbreviation"
              type = MetadatumType.RIS_ABBREVIATION
            }
            metadatum {
              value = "documentNumber"
              type = MetadatumType.DOCUMENT_NUMBER
            }
            metadatum {
              value = "documentCategory"
              type = MetadatumType.DOCUMENT_CATEGORY
            }
            metadatum {
              value = "officialShortTitle"
              type = MetadatumType.OFFICIAL_SHORT_TITLE
            }
            metadatum {
              value = "officialAbbreviation"
              type = MetadatumType.OFFICIAL_ABBREVIATION
            }
            metadatum {
              value = "completeCitation"
              type = MetadatumType.COMPLETE_CITATION
            }
            metadatum {
              value = "celexNumber"
              type = MetadatumType.CELEX_NUMBER
            }
            metadatum {
              value = "text"
              type = MetadatumType.TEXT
            }
          }
        }
      }
    }

    val saveCommand = SaveNormOutputPort.Command(norm)
    normsService
        .saveNorm(saveCommand)
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
                  "documentation":[],
                  "metadataSections":[{"name":"CITATION_DATE","order":1,"metadata":[{"value":"${date.toLocalDate()}","type":"DATE","order":1}],"sections":null}, {"name":"DOCUMENT_TYPE","order":1,"metadata":[{"value":"BASE_NORM","type":"NORM_CATEGORY","order":1}, {"value":"documentTypeName","type":"TYPE_NAME","order":1}, {"value":"documentTemplateName","type":"TEMPLATE_NAME","order":1}],"sections":null}, {"name":"NORM","order":1,"metadata":[{"value":"officialLongTitle","type":"OFFICIAL_LONG_TITLE","order":1}, {"value":"risAbbreviation","type":"RIS_ABBREVIATION","order":1}, {"value":"documentNumber","type":"DOCUMENT_NUMBER","order":1}, {"value":"documentCategory","type":"DOCUMENT_CATEGORY","order":1}, {"value":"officialShortTitle","type":"OFFICIAL_SHORT_TITLE","order":1}, {"value":"officialAbbreviation","type":"OFFICIAL_ABBREVIATION","order":1}, {"value":"completeCitation","type":"COMPLETE_CITATION","order":1}, {"value":"celexNumber","type":"CELEX_NUMBER","order":1}, {"value":"text","type":"TEXT","order":1}],"sections":null}],
                  "eli":"",
                  "files":[{"name":"norm.zip","hash":"hash","createdAt":"$date"}]}
                """
                .trimIndent(),
        )
  }

  @Test
  fun `it correctly loads a norm with documentation via api`() {
    val norm = norm {
      guid = UUID.fromString("160da595-25bb-4db1-9527-a949466886fc")
      documentation {
        documentSection {
          guid = UUID.fromString("13760da1-17c2-4a2b-8236-351c85218260")
          order = 1
          type = BOOK
          marker = "1"
          heading = "Book 1"
          documentation {
            documentSection {
              guid = UUID.fromString("07c8fe53-d16b-474c-a5bf-aa2b3aa165e4")
              order = 1
              type = PART
              marker = "1"
              heading = "Part 1"
              documentation {
                article {
                  guid = UUID.fromString("36258e15-e72d-4ee7-8cab-3012a69c4ca7")
                  order = 1
                  marker = "§ 1"
                  heading = "Article 1"
                  paragraphs {
                    paragraph {
                      guid = UUID.fromString("bbdbfbe9-8205-492f-836b-4545a5ec486b")
                      marker = "(1)"
                      text = "paragraph 1 text"
                    }
                  }
                }
                article {
                  guid = UUID.fromString("f42bee15-8fed-495d-abf3-4af02a862b41")
                  order = 2
                  marker = "§ 2"
                  heading = "Article 2"
                  paragraphs {
                    paragraph {
                      guid = UUID.fromString("71e3ffe0-6d27-4473-b1b3-6c84f2d39a47")
                      marker = "(1)"
                      text = "paragraph 1 text"
                    }
                    paragraph {
                      guid = UUID.fromString("e58ed763-928c-4155-bee9-fdbaaadc15f3")
                      marker = "(2)"
                      text = "paragraph 2 text"
                    }
                  }
                }
              }
            }
            documentSection {
              guid = UUID.fromString("b26e1ebd-50f9-4df9-a8b5-08cca0384959")
              order = 2
              type = PART
              marker = "2"
              heading = "Part 2"
            }
          }
        }
      }
    }

    val saveCommand = SaveNormOutputPort.Command(norm)
    normsService
        .saveNorm(saveCommand)
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
                  "guid": "160da595-25bb-4db1-9527-a949466886fc",
                  "documentation": [
                    {
                      "guid": "13760da1-17c2-4a2b-8236-351c85218260",
                      "order": 1,
                      "type": "BOOK",
                      "marker": "1",
                      "heading": "Book 1",
                      "documentation": [
                        {
                          "guid": "07c8fe53-d16b-474c-a5bf-aa2b3aa165e4",
                          "order": 1,
                          "type": "PART",
                          "marker": "1",
                          "heading": "Part 1",
                          "documentation": [
                            {
                              "guid": "36258e15-e72d-4ee7-8cab-3012a69c4ca7",
                              "order": 1,
                              "marker": "§ 1",
                              "heading": "Article 1",
                              "paragraphs": [
                                {
                                  "guid": "bbdbfbe9-8205-492f-836b-4545a5ec486b",
                                  "marker": "(1)",
                                  "text": "paragraph 1 text"
                                }
                              ]
                            },
                            {
                              "guid": "f42bee15-8fed-495d-abf3-4af02a862b41",
                              "order": 2,
                              "marker": "§ 2",
                              "heading": "Article 2",
                              "paragraphs": [
                                {
                                  "guid": "71e3ffe0-6d27-4473-b1b3-6c84f2d39a47",
                                  "marker": "(1)",
                                  "text": "paragraph 1 text"
                                },
                                {
                                  "guid": "e58ed763-928c-4155-bee9-fdbaaadc15f3",
                                  "marker": "(2)",
                                  "text": "paragraph 2 text"
                                }
                              ]
                            }
                          ]
                        },
                        {
                          "guid": "b26e1ebd-50f9-4df9-a8b5-08cca0384959",
                          "order": 2,
                          "type": "PART",
                          "marker": "2",
                          "heading": "Part 2",
                          "documentation": []
                        }
                      ]
                    }
                  ],
                  "metadataSections": [],
                  "eli": "",
                  "files": []
                }
                """
                .trimIndent(),
        )
  }
}
