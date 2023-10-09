package de.bund.digitalservice.ris.norms.framework.adapter.input.restapi.controller

import de.bund.digitalservice.ris.caselaw.config.FlywayConfig
import de.bund.digitalservice.ris.caselaw.config.JacksonConfig
import de.bund.digitalservice.ris.norms.application.port.output.GetNormByGuidOutputPort
import de.bund.digitalservice.ris.norms.application.service.MigrateNormService
import de.bund.digitalservice.ris.norms.domain.entity.Article
import de.bund.digitalservice.ris.norms.domain.value.MetadataSectionName
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType
import de.bund.digitalservice.ris.norms.framework.adapter.output.database.NormsService
import de.bund.digitalservice.ris.norms.framework.adapter.output.database.PostgresTestcontainerIntegrationTest
import de.bund.digitalservice.ris.norms.framework.adapter.output.database.dto.NormDto
import de.bund.digitalservice.ris.norms.framework.adapter.output.juris.JurisConverter
import de.bund.digitalservice.ris.norms.juris.converter.model.DocumentSection
import de.bund.digitalservice.ris.norms.juris.converter.model.value.DocumentSectionType
import java.time.Duration
import java.util.*
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
    JacksonConfig::class,
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

  @Test
  fun `it correctly saves a metadata with sections then calls it back via api`() {
    val guid = UUID.randomUUID()
    val dataJson =
        """
        { "norms": [
            {
                "guid": "$guid",
                "norm": {
                    "text": null,
                    "documentation": [
                        {
                            "guid": "${UUID.randomUUID()}",
                            "order": 1,
                            "title": null,
                            "marker": "§ 1",
                            "paragraphs": [
                                {
                                    "text": "Auf den Schienenpersonenverkehr der öffentlichen Eisenbahnen sind die Vorschriften der Verordnung (EG) Nr. 1371/2007 des Europäischen Parlaments und des Rates vom 23. Oktober 2007 über die Rechte und Pflichten der Fahrgäste im Eisenbahnverkehr (ABl. EU Nr. L 315 S. 14) anzuwenden.<Rec></Rec> Das gilt nach Maßgabe des Artikels 2 Abs. 5 der Verordnung (EG) Nr. 1371/2007 nicht, soweit auf Grund des § 26 Abs. 1 Satz 1 Nr. 1a des Allgemeinen Eisenbahngesetzes vom 27. Dezember 1993 (BGBl. I S. 2378, 2396; 1994 I S. 2439), das zuletzt durch Artikel 2 des Gesetzes vom 26. Mai 2009 (BGBl. I S. 1146) geändert worden ist, für die Beförderung im Schienenpersonennahverkehr etwas anderes bestimmt ist oder soweit es sich um Verkehrsdienste des Schienenpersonennahverkehrs handelt, die hauptsächlich aus Gründen historischen Interesses oder zu touristischen Zwecken betrieben werden.<Rec></Rec>",
                                    "marker": ""
                                }
                            ],
                            "closestArticle" : "§ 1",
                            "closestArticlePosition" : 0
                        },
                        {
                            "guid": "${UUID.randomUUID()}",
                            "title": "I",
                            "marker": "Abschnitt",
                            "order": 2,
                            "type": "${DocumentSectionType.SECTION}",
                            "treeOrder": "010",
                            "closestArticle": "§ 2",
                            "closestArticlePosition": -2,
                            "documentation": [
                                {
                                    "guid": "${UUID.randomUUID()}",
                                    "title": "I",
                                    "marker": "Unterabschnitt",
                                    "order": 1,
                                    "type": "${DocumentSectionType.SUBSECTION}",
                                    "treeOrder": "010010",
                                    "closestArticle": "§ 2",
                                    "closestArticlePosition": -1,
                                    "documentation": [
                                        {
                                            "guid": "${UUID.randomUUID()}",
                                            "order": 1,
                                            "title": null,
                                            "marker": "§ 2",
                                            "paragraphs": [
                                                {
                                                    "text": "Dieses Gesetz tritt mit Ablauf des 2. Dezember 2009 außer Kraft.<Rec></Rec>",
                                                    "marker": ""
                                                }
                                            ],
                                            "closestArticle": "§ 2",
                                            "closestArticlePosition": 0
                                        }
                                    ]
                                }
                            ]
                        }
                    ],
                    "leadList": [
                        {
                            "unit": "III A 4",
                            "jurisdiction": "BMJ"
                        }
                    ],
                    "footnotes": [
                        {
                            "reference": null,
                            "footnoteEuLaw": [],
                            "otherFootnote": [
                                {
                                    "first": 1,
                                    "second": "*T\\n(+++ Textnachweis ab: 29.7.2009 +++)\\n(+++ Amtlicher Hinweis des Normgebers auf EG-Recht:\\n     Durchführung der\\n       EGV 1371/2007           (CELEX Nr: 307R1371) +++)"
                                },
                                {
                                    "first": 2,
                                    "second": "*/\\nDas G wurde als Artikel 1 G v. 26.5.2009 I 1146 vom Bundestag mit Zustimmung \\ndes Bundesrates beschlossen*. Es tritt gem. Art. 4 Satz 1 dieses G am 29.7.2009\\n in Kraft*."
                                }
                            ],
                            "footnoteChange": [],
                            "footnoteComment": [],
                            "footnoteDecision": [],
                            "footnoteStateLaw": []
                        }
                    ],
                    "repealList": ["Dieses G tritt nach seinem § 2 mit Ablauf des 2.12.2009 außer Kraft"],
                    "statusList": [],
                    "celexNumber": null,
                    "reissueList": [],
                    "documentType": {
                        "name": "GE",
                        "categories": ["SN", "ÄN"],
                        "templateNames": []
                    },
                    "definitionList": [],
                    "documentStatus": [
                        {
                            "documentStatusDateYear": null,
                            "documentStatusWorkNote": ["Außerkraft"],
                            "documentStatusReference": null,
                            "documentStatusDescription": null
                        }
                    ],
                    "expirationDate": "2009-12-02",
                    "otherStatusList": [],
                    "risAbbreviation": "FGRVAnwG",
                    "subjectAreaList": [{"fna": "934-2", "gesta": null}, {"fna": null, "gesta": "C177"}],
                    "announcementDate": "2009-05-29",
                    "citationDateList": ["2009-05-26"],
                    "documentCategory": "NR",
                    "frameKeywordList": [
                        "BGBl I 2009, Nr 028",
                        "aufgehobenes Bundesrecht",
                        "Anwendung",
                        "Verordnung (EG) Nr. 1371/2007",
                        "(EG) Nr. 1371/2007",
                        "EGV 1371/2007",
                        "Durchführung EG-Recht",
                        "Durchführung EGV 1371/2007",
                        "Pflicht",
                        "Fahrgast",
                        "Eisenbahnverkehr",
                        "Fahrgastrechteverordnung",
                        "Anwendungsgesetz"
                    ],
                    "normProviderList": [{"entity": "DEU", "decidingBody": "BT", "isResolutionMajority": true}],
                    "validityRuleList": [],
                    "documentTextProof": null,
                    "officialLongTitle": "Gesetz über die Anwendung der Verordnung (EG) Nr. 1371/2007 des Europäischen Parlaments und des Rates vom 23. Oktober 2007 über die Rechte und Pflichten der Fahrgäste im Eisenbahnverkehr",
                    "participationList": [{"type": "EZ", "institution": "BR"}],
                    "entryIntoForceDate": "2009-07-29",
                    "officialShortTitle": "Fahrgastrechteverordnung-Anwendungsgesetz",
                    "expirationDateState": null,
                    "referenceNumberList": [],
                    "applicationScopeArea": null,
                    "officialAbbreviation": null,
                    "categorizedReferences": [{"text": "&A 21 &B § 2 &E X &G 2009-12-03"}, {"text": "&A 84 &E EGV 1371/2007"}],
                    "printAnnouncementList": [{"page": "1146", "year": "2009", "gazette": "BGBl I"}],
                    "ageIndicationStartList": [],
                    "applicationScopeEndDate": null,
                    "digitalAnnouncementList": [],
                    "divergentDocumentNumber": "BJNR114610009",
                    "entryIntoForceDateState": null,
                    "principleExpirationDate": "2009-12-02",
                    "unofficialLongTitleList": [],
                    "unofficialReferenceList": [],
                    "divergentExpirationsList": [],
                    "unofficialShortTitleList": [],
                    "applicationScopeStartDate": null,
                    "unofficialAbbreviationList": [],
                    "ageOfMajorityIndicationList": [],
                    "divergentEntryIntoForceList": [],
                    "principleEntryIntoForceDate": "2009-07-29",
                    "principleExpirationDateState": null,
                    "principleEntryIntoForceDateState": null,
                    "risAbbreviationInternationalLawList": []
                }
            }
    ]}
    """
            .trimIndent()

    webClient
        .mutateWith(csrf())
        .post()
        .uri("/api/v1/norms/migrate")
        .contentType(MediaType.APPLICATION_JSON)
        .body(BodyInserters.fromValue(dataJson))
        .exchange()
        .expectStatus()
        .isCreated

    val norm = normService.getNormByGuid(GetNormByGuidOutputPort.Query(guid)).block()
    val article = norm?.documentation?.first { it.order == 1 }

    assertThat(article?.heading).isNull()
    assertThat(article?.marker).isEqualTo("§ 1")

    val header =
        norm?.documentation?.first { it.order == 2 }
            as de.bund.digitalservice.ris.norms.domain.entity.DocumentSection

    assertThat(header.marker).isEqualTo("Abschnitt")
    assertThat(header.heading).isEqualTo("I")
    assertThat(header.type)
        .isEqualTo(de.bund.digitalservice.ris.norms.domain.value.DocumentSectionType.SECTION)
    assertThat(header.documentation).hasSize(1)
    val subheader =
        header.documentation.first()
            as de.bund.digitalservice.ris.norms.domain.entity.DocumentSection
    assertThat(subheader.marker).isEqualTo("Unterabschnitt")
    assertThat(subheader.heading).isEqualTo("I")
    assertThat(subheader.type)
        .isEqualTo(de.bund.digitalservice.ris.norms.domain.value.DocumentSectionType.SUBSECTION)
    val article2 = subheader.documentation.first() as Article
    assertThat(article2.marker).isEqualTo("§ 2")
    assertThat(article2.paragraphs.first().text)
        .isEqualTo("Dieses Gesetz tritt mit Ablauf des 2. Dezember 2009 außer Kraft.<Rec></Rec>")

    assertThat(
            norm
                .getFirstMetadatum(MetadataSectionName.NORM, MetadatumType.OFFICIAL_LONG_TITLE)
                ?.value)
        .isEqualTo(
            "Gesetz über die Anwendung der Verordnung (EG) Nr. 1371/2007 des Europäischen Parlaments und des Rates vom 23. Oktober 2007 über die Rechte und Pflichten der Fahrgäste im Eisenbahnverkehr")
    assertThat(
            norm.getFirstMetadatum(MetadataSectionName.NORM_PROVIDER, MetadatumType.ENTITY)?.value)
        .isEqualTo("DEU")
    assertThat(
            norm
                .getFirstMetadatum(MetadataSectionName.NORM_PROVIDER, MetadatumType.DECIDING_BODY)
                ?.value)
        .isEqualTo("BT")
    assertThat(
            norm
                .getFirstMetadatum(
                    MetadataSectionName.NORM_PROVIDER, MetadatumType.RESOLUTION_MAJORITY)
                ?.value)
        .isEqualTo(true)
  }
}
