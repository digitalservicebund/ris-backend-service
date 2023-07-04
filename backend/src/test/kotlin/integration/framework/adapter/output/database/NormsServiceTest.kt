package de.bund.digitalservice.ris.norms.framework.adapter.output.database

import de.bund.digitalservice.ris.caselaw.config.FlywayConfig
import de.bund.digitalservice.ris.norms.application.port.output.EditNormOutputPort
import de.bund.digitalservice.ris.norms.application.port.output.GetNormByEliOutputPort
import de.bund.digitalservice.ris.norms.application.port.output.GetNormByGuidOutputPort
import de.bund.digitalservice.ris.norms.application.port.output.SaveFileReferenceOutputPort
import de.bund.digitalservice.ris.norms.application.port.output.SaveNormOutputPort
import de.bund.digitalservice.ris.norms.application.port.output.SearchNormsOutputPort
import de.bund.digitalservice.ris.norms.domain.entity.Article
import de.bund.digitalservice.ris.norms.domain.entity.FileReference
import de.bund.digitalservice.ris.norms.domain.entity.MetadataSection
import de.bund.digitalservice.ris.norms.domain.entity.Metadatum
import de.bund.digitalservice.ris.norms.domain.entity.Norm
import de.bund.digitalservice.ris.norms.domain.entity.Paragraph
import de.bund.digitalservice.ris.norms.domain.value.MetadataSectionName
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.CELEX_NUMBER
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.COMPLETE_CITATION
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.DATE
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.DOCUMENT_NUMBER
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.KEYWORD
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.OFFICIAL_LONG_TITLE
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.PARTICIPATION_INSTITUTION
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.PARTICIPATION_TYPE
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.YEAR
import de.bund.digitalservice.ris.norms.framework.adapter.output.database.dto.ArticleDto
import de.bund.digitalservice.ris.norms.framework.adapter.output.database.dto.FileReferenceDto
import de.bund.digitalservice.ris.norms.framework.adapter.output.database.dto.MetadataSectionDto
import de.bund.digitalservice.ris.norms.framework.adapter.output.database.dto.MetadatumDto
import de.bund.digitalservice.ris.norms.framework.adapter.output.database.dto.NormDto
import de.bund.digitalservice.ris.norms.framework.adapter.output.database.dto.ParagraphDto
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest
import org.springframework.context.annotation.Import
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.data.r2dbc.dialect.PostgresDialect
import org.springframework.r2dbc.core.DatabaseClient
import reactor.test.StepVerifier
import utils.createRandomNorm
import utils.createSimpleSections
import utils.factory.norm
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.*

@DataR2dbcTest
@Import(FlywayConfig::class, NormsService::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class NormsServiceTest : PostgresTestcontainerIntegrationTest() {

    companion object {
        private val NORM: Norm = createRandomNorm()
        private val ARTICLE1: Article = Article(UUID.randomUUID(), "Article1 title", "§ 1")
        private val ARTICLE2: Article = Article(UUID.randomUUID(), "Article2 title", "§ 2")
        private val PARAGRAPH1: Paragraph = Paragraph(UUID.randomUUID(), "(1)", "Text1")
        private val PARAGRAPH2: Paragraph = Paragraph(UUID.randomUUID(), "(2)", "Text2")
        private val PARAGRAPH3: Paragraph = Paragraph(UUID.randomUUID(), "(1)", "Text3")
        private val PARAGRAPH4: Paragraph = Paragraph(UUID.randomUUID(), "(2)", "Text4")
        private val FILE1: FileReference = FileReference("test.zip", "123456789", LocalDateTime.now(), UUID.randomUUID())
    }

    @Autowired
    private lateinit var normsService: NormsService

    @Autowired
    private lateinit var client: DatabaseClient

    private lateinit var template: R2dbcEntityTemplate

    @BeforeAll
    fun setup() {
        template = R2dbcEntityTemplate(client, PostgresDialect.INSTANCE)
    }

    @AfterEach
    fun cleanUp() {
        template.delete(NormDto::class.java).all().block()
        template.delete(ArticleDto::class.java).all().block()
        template.delete(ParagraphDto::class.java).all().block()
        template.delete(FileReferenceDto::class.java).all().block()
        template.delete(MetadataSectionDto::class.java).all().block()
        template.delete(MetadatumDto::class.java).all().block()
    }

    @Test
    fun `save a norm with a text field exceeding 255 charachters`() {
        val longTextField = (1..200).joinToString("") { "Abc" }
        val norm = norm {
            metadataSections {
                metadataSection {
                    name = MetadataSectionName.NORM
                    metadata {
                        metadatum { value = longTextField; type = MetadatumType.TEXT }
                    }
                }
            }
        }
        val saveCommand = SaveNormOutputPort.Command(norm)

        normsService.saveNorm(saveCommand)
            .`as`(StepVerifier::create)
            .expectNextCount(1)
            .verifyComplete()
    }

    @Test
    fun `save simple norm and verify it was saved with a search to get all norms`() {
        val saveCommand = SaveNormOutputPort.Command(NORM)
        val getAllQuery = SearchNormsOutputPort.Query("")

        normsService.searchNorms(getAllQuery)
            .`as`(StepVerifier::create)
            .expectNextCount(0)
            .verifyComplete()

        normsService.saveNorm(saveCommand)
            .`as`(StepVerifier::create)
            .expectNextCount(1)
            .verifyComplete()

        normsService.searchNorms(getAllQuery)
            .`as`(StepVerifier::create)
            .expectNextCount(1)
            .verifyComplete()
    }

    @Test
    fun `the search norm result includes their ELI property`() {
        val page = Metadatum("1125", MetadatumType.PAGE)
        val gazette = Metadatum("bg-1", MetadatumType.ANNOUNCEMENT_GAZETTE)
        val officialLongTitle = Metadatum("test title", MetadatumType.OFFICIAL_LONG_TITLE)
        val printAnnouncement = MetadataSection(MetadataSectionName.PRINT_ANNOUNCEMENT, listOf(page, gazette))
        val officialReference = MetadataSection(MetadataSectionName.OFFICIAL_REFERENCE, emptyList(), 1, listOf(printAnnouncement))
        val normSection = MetadataSection(MetadataSectionName.NORM, listOf(officialLongTitle))
        val normWithEli = NORM.copy(
            metadataSections = listOf(officialReference, normSection, createAnnouncementSection("2022-02-02")),
        )
        val saveCommand = SaveNormOutputPort.Command(normWithEli)
        val searchQuery = SearchNormsOutputPort.Query(officialLongTitle.value)

        assertThat(normWithEli.eli.toString()).isNotNull()

        normsService.saveNorm(saveCommand)
            .`as`(StepVerifier::create)
            .expectNextCount(1)
            .verifyComplete()

        normsService.searchNorms(searchQuery)
            .`as`(StepVerifier::create)
            .assertNext {
                assertThat(it.eli.toString()).isNotEqualTo("")
                assertThat(it.metadataSections.first { it.name == MetadataSectionName.NORM }.metadata.first { it.type == OFFICIAL_LONG_TITLE }).isEqualTo(officialLongTitle)
            }
            .verifyComplete()
    }

    @Test
    fun `save simple norm and retrieved by eli`() {
        val printAnnouncementSection = MetadataSection(
            MetadataSectionName.PRINT_ANNOUNCEMENT,
            listOf(
                Metadatum("1125", MetadatumType.PAGE),
                Metadatum("bg-1", MetadatumType.ANNOUNCEMENT_GAZETTE),
            ),
        )
        val referenceSection1 = MetadataSection(MetadataSectionName.OFFICIAL_REFERENCE, listOf(), 1, listOf(printAnnouncementSection))

        val norm = Norm(
            guid = UUID.randomUUID(),
            articles = listOf(),
            metadataSections = listOf(referenceSection1, createAnnouncementSection("2022-02-02")),
        )
        val saveCommand = SaveNormOutputPort.Command(norm)
        val eliQuery = GetNormByEliOutputPort.Query("bg-1", "2022", "1125")

        normsService.saveNorm(saveCommand)
            .`as`(StepVerifier::create)
            .expectNextCount(1)
            .verifyComplete()

        normsService.getNormByEli(eliQuery)
            .`as`(StepVerifier::create)
            .expectNextCount(1)
            .verifyComplete()
    }

    @Test
    fun `save multiple norms and retrieve only one by eli parameters`() {
        val printAnnouncementSection1 = MetadataSection(
            MetadataSectionName.PRINT_ANNOUNCEMENT,
            listOf(
                Metadatum("1125", MetadatumType.PAGE),
                Metadatum("bg-1", MetadatumType.ANNOUNCEMENT_GAZETTE),
            ),
        )
        val referenceSection1 = MetadataSection(MetadataSectionName.OFFICIAL_REFERENCE, listOf(), 1, listOf(printAnnouncementSection1))

        val firstNorm = Norm(
            guid = UUID.randomUUID(),
            articles = listOf(),
            metadataSections = listOf(referenceSection1, createAnnouncementSection("2022-02-02")),
        )
        val printAnnouncementSection2 = MetadataSection(
            MetadataSectionName.PRINT_ANNOUNCEMENT,
            listOf(
                Metadatum("111", MetadatumType.PAGE),
                Metadatum("bg-1", MetadatumType.ANNOUNCEMENT_GAZETTE),
            ),
        )
        val referenceSection2 = MetadataSection(MetadataSectionName.OFFICIAL_REFERENCE, listOf(), 1, listOf(printAnnouncementSection2))
        val secondNorm = Norm(
            guid = UUID.randomUUID(),
            articles = listOf(),
            metadataSections = listOf(referenceSection2, createAnnouncementSection("2022-02-02")),
        )
        val saveFirstNormCommand = SaveNormOutputPort.Command(firstNorm)
        val saveSecondNormCommand = SaveNormOutputPort.Command(secondNorm)
        val eliQuery = GetNormByEliOutputPort.Query("bg-1", "2022", "1125")

        normsService.saveNorm(saveFirstNormCommand)
            .`as`(StepVerifier::create)
            .expectNextCount(1)
            .verifyComplete()

        normsService.saveNorm(saveSecondNormCommand)
            .`as`(StepVerifier::create)
            .expectNextCount(1)
            .verifyComplete()

        normsService.getNormByEli(eliQuery)
            .`as`(StepVerifier::create)
            .assertNext { assertNormsAreEqual(firstNorm, it) }
            .verifyComplete()
    }

    @Test
    fun `save simple norm and retrieved by eli with citation year`() {
        val citationDate = Metadatum("2001", YEAR)
        val citationDateSection = MetadataSection(MetadataSectionName.CITATION_DATE, listOf(citationDate))
        val printAnnouncementSection = MetadataSection(
            MetadataSectionName.PRINT_ANNOUNCEMENT,
            listOf(
                Metadatum("1125", MetadatumType.PAGE),
                Metadatum("bg-1", MetadatumType.ANNOUNCEMENT_GAZETTE),
            ),
        )
        val referenceSection1 = MetadataSection(MetadataSectionName.OFFICIAL_REFERENCE, listOf(), 1, listOf(printAnnouncementSection))
        val norm = Norm(
            guid = UUID.randomUUID(),
            articles = listOf(),
            metadataSections = listOf(citationDateSection, referenceSection1),
        )
        val saveCommand = SaveNormOutputPort.Command(norm)
        val eliQuery = GetNormByEliOutputPort.Query("bg-1", "2001", "1125")

        normsService.saveNorm(saveCommand)
            .`as`(StepVerifier::create)
            .expectNextCount(1)
            .verifyComplete()

        normsService.getNormByEli(eliQuery)
            .`as`(StepVerifier::create)
            .expectNextCount(1)
            .verifyComplete()
    }

    @Test
    fun `save simple norm and retrieved by eli with citation date`() {
        val citationDate = Metadatum(LocalDate.of(2020, 2, 15), DATE)
        val citationDateSection = MetadataSection(MetadataSectionName.CITATION_DATE, listOf(citationDate))
        val printAnnouncementSection = MetadataSection(
            MetadataSectionName.PRINT_ANNOUNCEMENT,
            listOf(
                Metadatum("1125", MetadatumType.PAGE),
                Metadatum("bg-1", MetadatumType.ANNOUNCEMENT_GAZETTE),
            ),
        )
        val referenceSection1 = MetadataSection(MetadataSectionName.OFFICIAL_REFERENCE, listOf(), 1, listOf(printAnnouncementSection))
        val norm = Norm(
            guid = UUID.randomUUID(),
            articles = listOf(),
            metadataSections = listOf(citationDateSection, referenceSection1),
        )
        val saveCommand = SaveNormOutputPort.Command(norm)
        val eliQuery = GetNormByEliOutputPort.Query("bg-1", "2020", "1125")

        normsService.saveNorm(saveCommand)
            .`as`(StepVerifier::create)
            .expectNextCount(1)
            .verifyComplete()

        normsService.getNormByEli(eliQuery)
            .`as`(StepVerifier::create)
            .expectNextCount(1)
            .verifyComplete()
    }

    @Test
    fun `save simple norm and dont retrieved by eli with although citation year matches because announcement date does not`() {
        val citationDate = Metadatum("2001", YEAR)
        val citationDateSection = MetadataSection(MetadataSectionName.CITATION_DATE, listOf(citationDate))
        val printAnnouncementSection = MetadataSection(
            MetadataSectionName.PRINT_ANNOUNCEMENT,
            listOf(
                Metadatum("1125", MetadatumType.PAGE),
                Metadatum("bg-1", MetadatumType.ANNOUNCEMENT_GAZETTE),
            ),
        )
        val referenceSection1 = MetadataSection(MetadataSectionName.OFFICIAL_REFERENCE, listOf(), 1, listOf(printAnnouncementSection))
        val norm = Norm(
            guid = UUID.randomUUID(),
            articles = listOf(),
            metadataSections = listOf(citationDateSection, referenceSection1, createAnnouncementSection("2022-02-02")),
        )
        val saveCommand = SaveNormOutputPort.Command(norm)
        val eliQuery = GetNormByEliOutputPort.Query("bg-1", "2001", "1125")

        normsService.saveNorm(saveCommand)
            .`as`(StepVerifier::create)
            .expectNextCount(1)
            .verifyComplete()

        normsService.getNormByEli(eliQuery)
            .`as`(StepVerifier::create)
            .expectNextCount(0)
            .verifyComplete()
    }

    @Test
    fun `save simple norm and retrieved by guid`() {
        val saveCommand = SaveNormOutputPort.Command(NORM)
        val guidQuery = GetNormByGuidOutputPort.Query(NORM.guid)

        normsService.saveNorm(saveCommand)
            .`as`(StepVerifier::create)
            .expectNextCount(1)
            .verifyComplete()
        normsService.getNormByGuid(guidQuery)
            .`as`(StepVerifier::create)
            .expectNextCount(1)
            .verifyComplete()
    }

    @Test
    fun `save a norm with official reference sections and retrieve it by guid`() {
        val printAnnouncementSection = MetadataSection(
            MetadataSectionName.PRINT_ANNOUNCEMENT,
            listOf(
                Metadatum("gazette1", MetadatumType.ANNOUNCEMENT_GAZETTE, 1),
                Metadatum("gazette2", MetadatumType.ANNOUNCEMENT_GAZETTE, 2),
            ),
            1,
        )
        val digitalAnnouncementSection = MetadataSection(
            MetadataSectionName.DIGITAL_ANNOUNCEMENT,
            listOf(
                Metadatum("medium1", MetadatumType.ANNOUNCEMENT_MEDIUM, 1),
                Metadatum("medium2", MetadatumType.ANNOUNCEMENT_MEDIUM, 2),
            ),
            1,
        )
        val referenceSection1 = MetadataSection(MetadataSectionName.OFFICIAL_REFERENCE, listOf(), 1, listOf(printAnnouncementSection))
        val referenceSection2 = MetadataSection(MetadataSectionName.OFFICIAL_REFERENCE, listOf(), 2, listOf(digitalAnnouncementSection))
        val norm = Norm(guid = UUID.randomUUID(), metadataSections = listOf(referenceSection1, referenceSection2))
        val saveCommand = SaveNormOutputPort.Command(norm)
        val guidQuery = GetNormByGuidOutputPort.Query(norm.guid)

        normsService.saveNorm(saveCommand)
            .`as`(StepVerifier::create)
            .expectNextCount(1)
            .verifyComplete()

        normsService.getNormByGuid(guidQuery)
            .`as`(StepVerifier::create)
            .expectNextCount(1)
            .verifyComplete()
    }

    @Test
    fun `save norm with metadata and retrieve it by its GUID`() {
        val norm = NORM.copy(metadataSections = createSimpleSections())
        val saveCommand = SaveNormOutputPort.Command(norm)
        val guidQuery = GetNormByGuidOutputPort.Query(norm.guid)

        normsService.saveNorm(saveCommand)
            .`as`(StepVerifier::create)
            .expectNextCount(1)
            .verifyComplete()

        normsService.getNormByGuid(guidQuery)
            .`as`(StepVerifier::create)
            .assertNext { assertNormsAreEqual(norm, it) }
            .verifyComplete()
    }

    @Test
    fun `save norm with 1 article and 2 paragraphs and retrieved by guid`() {
        val article = ARTICLE1.copy(paragraphs = listOf(PARAGRAPH1, PARAGRAPH2))
        val norm = NORM.copy(articles = listOf(article))
        val saveCommand = SaveNormOutputPort.Command(norm)
        val guidQuery = GetNormByGuidOutputPort.Query(norm.guid)

        normsService.saveNorm(saveCommand)
            .`as`(StepVerifier::create)
            .expectNextCount(1)
            .verifyComplete()

        normsService.getNormByGuid(guidQuery)
            .`as`(StepVerifier::create)
            .assertNext { assertNormsAreEqual(norm, it) }
            .verifyComplete()
    }

    @Test
    fun `save norm with 2 article and 2 paragraphs each and retrieved by guid`() {
        val article1 = ARTICLE1.copy(paragraphs = listOf(PARAGRAPH1, PARAGRAPH2))
        val article2 = ARTICLE2.copy(paragraphs = listOf(PARAGRAPH3, PARAGRAPH4))
        val norm = NORM.copy(articles = listOf(article1, article2))
        val saveCommand = SaveNormOutputPort.Command(norm)
        val guidQuery = GetNormByGuidOutputPort.Query(norm.guid)

        normsService.saveNorm(saveCommand)
            .`as`(StepVerifier::create)
            .expectNextCount(1)
            .verifyComplete()

        normsService.getNormByGuid(guidQuery)
            .`as`(StepVerifier::create)
            .assertNext { assertNormsAreEqual(norm, it) }
            .verifyComplete()
    }

    @Test
    fun `save norm with 2 sections that are having the same types and get them back`() {
        val section1 = MetadataSection(
            MetadataSectionName.PARTICIPATION,
            listOf(
                Metadatum("Foo", PARTICIPATION_TYPE),
                Metadatum("Bar", PARTICIPATION_INSTITUTION),
            ),
            1,
        )
        val section2 = MetadataSection(
            MetadataSectionName.PARTICIPATION,
            listOf(
                Metadatum("Hello", PARTICIPATION_TYPE),
                Metadatum("World", PARTICIPATION_INSTITUTION),
            ),
            2,
        )

        val norm = NORM.copy(metadataSections = listOf(section1, section2))
        val saveCommand = SaveNormOutputPort.Command(norm)
        val guidQuery = GetNormByGuidOutputPort.Query(norm.guid)

        normsService.saveNorm(saveCommand)
            .`as`(StepVerifier::create)
            .expectNextCount(1)
            .verifyComplete()

        normsService.getNormByGuid(guidQuery)
            .`as`(StepVerifier::create)
            .assertNext {
                assertThat(it.metadataSections.groupBy { it.order }).isEqualTo(norm.metadataSections.groupBy { it.order })
            }
            .verifyComplete()
    }

    @Test
    fun `save norm and edit norm`() {
        val saveCommand = SaveNormOutputPort.Command(NORM)
        val guidQuery = GetNormByGuidOutputPort.Query(NORM.guid)

        normsService.saveNorm(saveCommand)
            .`as`(StepVerifier::create)
            .expectNextCount(1)
            .verifyComplete()

        normsService.getNormByGuid(guidQuery)
            .`as`(StepVerifier::create)
            .expectNextCount(1)
            .verifyComplete()

        val updatedNorm = NORM.copy(
            metadataSections = listOf(
                MetadataSection(
                    name = MetadataSectionName.NORM,
                    metadata = listOf(
                        Metadatum("document number", MetadatumType.DOCUMENT_NUMBER),
                        Metadatum("complete citation", MetadatumType.COMPLETE_CITATION),
                        Metadatum("celex number", MetadatumType.CELEX_NUMBER),
                    ),
                ),
            ),
        )

        val editCommand = EditNormOutputPort.Command(updatedNorm)

        normsService.editNorm(editCommand)
            .`as`(StepVerifier::create)
            .expectNextCount(1)
            .verifyComplete()

        normsService.getNormByGuid(guidQuery)
            .`as`(StepVerifier::create)
            .assertNext {
                val metadata = it.metadataSections.first().metadata
                assertThat(metadata.first { it.type == CELEX_NUMBER }.value).isEqualTo("celex number")
                assertThat(metadata.first { it.type == COMPLETE_CITATION }.value).isEqualTo("complete citation")
                assertThat(metadata.first { it.type == DOCUMENT_NUMBER }.value).isEqualTo("document number")
            }
            .verifyComplete()
    }

    @Test
    fun `it replaces the metadata when editing a norm`() {
        val initialNorm = NORM.copy(metadataSections = createSimpleSections())
        val saveCommand = SaveNormOutputPort.Command(initialNorm)
        val guidQuery = GetNormByGuidOutputPort.Query(initialNorm.guid)

        normsService.saveNorm(saveCommand)
            .`as`(StepVerifier::create)
            .expectNextCount(1)
            .verifyComplete()

        val section = MetadataSection(
            MetadataSectionName.NORM,
            listOf(Metadatum("baz", KEYWORD, 0)),
        )
        val updatedNorm = initialNorm.copy(metadataSections = listOf(section))
        val editCommand = EditNormOutputPort.Command(updatedNorm)

        normsService.editNorm(editCommand)
            .`as`(StepVerifier::create)
            .expectNextCount(1)
            .verifyComplete()

        normsService.getNormByGuid(guidQuery)
            .`as`(StepVerifier::create)
            .assertNext { assertNormsAreEqual(updatedNorm, it) }
            .verifyComplete()
    }

    @Test
    fun `save a norm with 1 file and retrieve it by guid`() {
        val norm = NORM.copy(files = listOf(FILE1))
        val saveCommand = SaveNormOutputPort.Command(norm)
        val guidQuery = GetNormByGuidOutputPort.Query(NORM.guid)

        normsService.saveNorm(saveCommand)
            .`as`(StepVerifier::create)
            .expectNextCount(1)
            .verifyComplete()
        normsService.getNormByGuid(guidQuery)
            .`as`(StepVerifier::create)
            .assertNext { assertNormsAreEqual(norm, it) }
            .verifyComplete()
    }

    @Test
    fun `save a file reference to a norm and retrieve norm by guid`() {
        val saveNormCommand = SaveNormOutputPort.Command(NORM.copy(files = listOf()))
        val saveFileReferenceCommand = SaveFileReferenceOutputPort.Command(FILE1, NORM)
        val guidQuery = GetNormByGuidOutputPort.Query(NORM.guid)

        normsService.saveNorm(saveNormCommand)
            .`as`(StepVerifier::create)
            .expectNextCount(1)
            .verifyComplete()

        normsService.saveFileReference(saveFileReferenceCommand)
            .`as`(StepVerifier::create)
            .expectNextCount(1)
            .verifyComplete()

        normsService.getNormByGuid(guidQuery)
            .`as`(StepVerifier::create)
            .assertNext {
                assertThat(it.files).hasSize(1)
                assertThat(it.files[0].name).isEqualTo(FILE1.name)
                assertThat(it.files[0].hash).isEqualTo(FILE1.hash)
            }
            .verifyComplete()
    }

    @Test
    fun `it maps metadatum of date type to entity properly`() {
        val guid: UUID = UUID.randomUUID()
        val metadatumDto = MetadatumDto(guid, "2020-12-23", DATE, 1, UUID.randomUUID())
        val result = normsService.metadatumToEntity(metadatumDto)

        assertThat(result.value).isEqualTo(LocalDate.of(2020, 12, 23))
    }

    @Test
    fun `save a norm with no print announcement and retrieve by eli with digital announcement and edition`() {
        val digitalAnnouncement = MetadataSection(
            MetadataSectionName.DIGITAL_ANNOUNCEMENT,
            listOf(
                Metadatum("medium", MetadatumType.ANNOUNCEMENT_MEDIUM),
                Metadatum("999", MetadatumType.EDITION),
            ),
        )
        val referenceSection = MetadataSection(MetadataSectionName.OFFICIAL_REFERENCE, listOf(), 1, listOf(digitalAnnouncement))
        val norm = Norm(
            guid = UUID.randomUUID(),
            articles = listOf(),
            metadataSections = listOf(referenceSection, createAnnouncementSection("2022-02-02")),
        )
        val saveNormCommand = SaveNormOutputPort.Command(norm)

        val eliQuery = GetNormByEliOutputPort.Query("medium", "2022", "999")

        normsService.saveNorm(saveNormCommand)
            .`as`(StepVerifier::create)
            .expectNextCount(1)
            .verifyComplete()

        normsService.getNormByEli(eliQuery)
            .`as`(StepVerifier::create)
            .assertNext { assertNormsAreEqual(norm, it) }
            .verifyComplete()
    }

    @Test
    fun `save a norm with no print announcement and retrieve by eli with digital announcement and page and no edition`() {
        val digitalAnnouncement = MetadataSection(
            MetadataSectionName.DIGITAL_ANNOUNCEMENT,
            listOf(
                Metadatum("medium", MetadatumType.ANNOUNCEMENT_MEDIUM),
                Metadatum("999", MetadatumType.PAGE),
            ),
        )
        val referenceSection = MetadataSection(MetadataSectionName.OFFICIAL_REFERENCE, listOf(), 1, listOf(digitalAnnouncement))
        val norm = Norm(
            guid = UUID.randomUUID(),
            articles = listOf(),
            metadataSections = listOf(referenceSection, createAnnouncementSection("2022-02-02")),
        )
        val saveNormCommand = SaveNormOutputPort.Command(norm)

        val eliQuery = GetNormByEliOutputPort.Query("medium", "2022", "999")

        normsService.saveNorm(saveNormCommand)
            .`as`(StepVerifier::create)
            .expectNextCount(1)
            .verifyComplete()

        normsService.getNormByEli(eliQuery)
            .`as`(StepVerifier::create)
            .assertNext { assertNormsAreEqual(norm, it) }
            .verifyComplete()
    }

    @Test
    fun `save a norm with no print announcement and do not retrieve by eli because digital announcement has both page and edition and only page matches`() {
        val digitalAnnouncement = MetadataSection(
            MetadataSectionName.DIGITAL_ANNOUNCEMENT,
            listOf(
                Metadatum("medium", MetadatumType.ANNOUNCEMENT_MEDIUM),
                Metadatum("000", MetadatumType.EDITION),
                Metadatum("999", MetadatumType.PAGE),

            ),
        )
        val referenceSection = MetadataSection(MetadataSectionName.OFFICIAL_REFERENCE, listOf(), 1, listOf(digitalAnnouncement))
        val norm = Norm(
            guid = UUID.randomUUID(),
            articles = listOf(),
            metadataSections = listOf(referenceSection, createAnnouncementSection("2022-02-02")),
        )
        val saveNormCommand = SaveNormOutputPort.Command(norm)

        val eliQuery = GetNormByEliOutputPort.Query("medium", "2022", "999")

        normsService.saveNorm(saveNormCommand)
            .`as`(StepVerifier::create)
            .expectNextCount(1)
            .verifyComplete()

        normsService.getNormByEli(eliQuery)
            .`as`(StepVerifier::create)
            .expectNextCount(0)
            .verifyComplete()
    }

    @Test
    fun `save a norm with print announcement and digital announcement and dont retrieve by eli because only digital announcement matches`() {
        val printAnnouncement = MetadataSection(
            MetadataSectionName.PRINT_ANNOUNCEMENT,
            listOf(
                Metadatum("no-matching", MetadatumType.ANNOUNCEMENT_GAZETTE),
                Metadatum("000", MetadatumType.PAGE),
            ),
        )
        val digitalAnnouncement = MetadataSection(
            MetadataSectionName.DIGITAL_ANNOUNCEMENT,
            listOf(
                Metadatum("matching", MetadatumType.ANNOUNCEMENT_MEDIUM),
                Metadatum("999", MetadatumType.EDITION),
            ),
        )
        val referenceSectionPrint = MetadataSection(MetadataSectionName.OFFICIAL_REFERENCE, listOf(), 1, listOf(printAnnouncement))
        val referenceSectionDigital = MetadataSection(MetadataSectionName.OFFICIAL_REFERENCE, listOf(), 2, listOf(digitalAnnouncement))
        val norm = Norm(
            guid = UUID.randomUUID(),
            articles = listOf(),
            metadataSections = listOf(referenceSectionPrint, referenceSectionDigital, createAnnouncementSection("2022-02-02")),
        )
        val saveNormCommand = SaveNormOutputPort.Command(norm)

        val eliQuery = GetNormByEliOutputPort.Query("matching", "2022", "999")

        normsService.saveNorm(saveNormCommand)
            .`as`(StepVerifier::create)
            .expectNextCount(1)
            .verifyComplete()

        normsService.getNormByEli(eliQuery)
            .`as`(StepVerifier::create)
            .expectNextCount(0)
            .verifyComplete()
    }

    @Test
    fun `save a norm with print announcement and digital announcement and retrieve by eli because digital announcement matches and page in print missing`() {
        val printAnnouncement = MetadataSection(
            MetadataSectionName.PRINT_ANNOUNCEMENT,
            listOf(
                Metadatum("no-matching", MetadatumType.ANNOUNCEMENT_GAZETTE),
            ),
        )
        val digitalAnnouncement = MetadataSection(
            MetadataSectionName.DIGITAL_ANNOUNCEMENT,
            listOf(
                Metadatum("matching", MetadatumType.ANNOUNCEMENT_MEDIUM),
                Metadatum("999", MetadatumType.EDITION),
            ),
        )
        val referenceSectionPrint = MetadataSection(MetadataSectionName.OFFICIAL_REFERENCE, listOf(), 1, listOf(printAnnouncement))
        val referenceSectionDigital = MetadataSection(MetadataSectionName.OFFICIAL_REFERENCE, listOf(), 2, listOf(digitalAnnouncement))
        val norm = Norm(
            guid = UUID.randomUUID(),
            articles = listOf(),
            metadataSections = listOf(referenceSectionPrint, referenceSectionDigital, createAnnouncementSection("2022-02-02")),
        )
        val saveNormCommand = SaveNormOutputPort.Command(norm)

        val eliQuery = GetNormByEliOutputPort.Query("matching", "2022", "999")

        normsService.saveNorm(saveNormCommand)
            .`as`(StepVerifier::create)
            .expectNextCount(1)
            .verifyComplete()

        normsService.getNormByEli(eliQuery)
            .`as`(StepVerifier::create)
            .assertNext { assertNormsAreEqual(norm, it) }
            .verifyComplete()
    }
}

private fun createAnnouncementSection(dateString: String): MetadataSection {
    return MetadataSection(
        MetadataSectionName.ANNOUNCEMENT_DATE,
        listOf(
            Metadatum(LocalDate.parse(dateString), DATE),
        ),
    )
}

private fun assertNormsAreEqual(norm1: Norm, norm2: Norm) = assertThat(norm1)
    .usingRecursiveComparison()
    .ignoringCollectionOrder()
    .withEqualsForType(::localDatesAreEqualUpToMilliseconds, LocalDateTime::class.java)
    .isEqualTo(norm2)

/**
 * We need to reduce to milliseconds (don't care about micro or nano seconds) since
 * the PostgreSQL database can only save up to 6 digits and automatically rounds up.
 */
private fun localDatesAreEqualUpToMilliseconds(first: LocalDateTime, second: LocalDateTime): Boolean {
    return first.truncatedTo(ChronoUnit.MILLIS) == second.truncatedTo(ChronoUnit.MILLIS)
}
