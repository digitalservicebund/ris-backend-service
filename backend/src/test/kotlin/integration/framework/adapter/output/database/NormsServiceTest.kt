package de.bund.digitalservice.ris.norms.framework.adapter.output.database

import de.bund.digitalservice.ris.caselaw.config.FlywayConfig
import de.bund.digitalservice.ris.norms.application.port.output.EditNormOutputPort
import de.bund.digitalservice.ris.norms.application.port.output.GetNormByEliOutputPort
import de.bund.digitalservice.ris.norms.application.port.output.GetNormByGuidOutputPort
import de.bund.digitalservice.ris.norms.application.port.output.SaveFileReferenceOutputPort
import de.bund.digitalservice.ris.norms.application.port.output.SaveNormOutputPort
import de.bund.digitalservice.ris.norms.application.port.output.SearchNormsOutputPort
import de.bund.digitalservice.ris.norms.application.port.output.SearchNormsOutputPort.QueryFields.OFFICIAL_LONG_TITLE
import de.bund.digitalservice.ris.norms.domain.entity.Article
import de.bund.digitalservice.ris.norms.domain.entity.FileReference
import de.bund.digitalservice.ris.norms.domain.entity.MetadataSection
import de.bund.digitalservice.ris.norms.domain.entity.Metadatum
import de.bund.digitalservice.ris.norms.domain.entity.Norm
import de.bund.digitalservice.ris.norms.domain.entity.Paragraph
import de.bund.digitalservice.ris.norms.domain.value.MetadataSectionName
import de.bund.digitalservice.ris.norms.domain.value.MetadataSectionName.OFFICIAL_REFERENCE
import de.bund.digitalservice.ris.norms.domain.value.MetadataSectionName.PRINT_ANNOUNCEMENT
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.DATE
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.KEYWORD
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.PARTICIPATION_INSTITUTION
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.PARTICIPATION_TYPE
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.YEAR
import de.bund.digitalservice.ris.norms.domain.value.UndefinedDate
import de.bund.digitalservice.ris.norms.framework.adapter.output.database.dto.MetadatumDto
import de.bund.digitalservice.ris.norms.framework.adapter.output.database.dto.NormDto
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
import java.time.Duration
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
        private val FILE1: FileReference = FileReference("test.zip", "123456789", LocalDateTime.now())
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
        template.delete(NormDto::class.java).all().block(Duration.ofSeconds(1))
    }

    @Test
    fun `save a norm with a text field exceeding 255 charachters`() {
        val longTextField = (1..200).joinToString("") { "Abc" }
        val saveCommand = SaveNormOutputPort.Command(NORM.copy(text = longTextField))

        normsService.saveNorm(saveCommand)
            .`as`(StepVerifier::create)
            .expectNextCount(1)
            .verifyComplete()
    }

    @Test
    fun `save simple norm and verify it was saved with a search to get all norms`() {
        val saveCommand = SaveNormOutputPort.Command(NORM)
        val getAllQuery = SearchNormsOutputPort.Query(emptyList())

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
        val printAnnouncement = MetadataSection(PRINT_ANNOUNCEMENT, listOf(page, gazette))
        val officialReference = MetadataSection(OFFICIAL_REFERENCE, emptyList(), 1, listOf(printAnnouncement))
        val normWithEli = NORM.copy(
            officialLongTitle = "test title",
            announcementDate = LocalDate.parse("2022-02-02"),
            metadataSections = listOf(officialReference),
        )
        val saveCommand = SaveNormOutputPort.Command(normWithEli)
        val parameter = SearchNormsOutputPort.QueryParameter(OFFICIAL_LONG_TITLE, "test title")
        val searchQuery = SearchNormsOutputPort.Query(listOf(parameter))

        assertThat(normWithEli.eli.toString()).isNotNull()

        normsService.saveNorm(saveCommand)
            .`as`(StepVerifier::create)
            .expectNextCount(1)
            .verifyComplete()

        normsService.searchNorms(searchQuery)
            .`as`(StepVerifier::create)
            .assertNext {
                assertThat(it.eli.toString()).isNotEqualTo("")
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
            metadataSections = listOf(referenceSection1),
            officialLongTitle = "Test Title",
            announcementDate = LocalDate.parse("2022-02-02"),
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
            metadataSections = listOf(referenceSection1),
            officialLongTitle = "Test Title",
            announcementDate = LocalDate.parse("2022-02-02"),
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
            metadataSections = listOf(referenceSection2),
            officialLongTitle = "Test Title 2",
            announcementDate = LocalDate.parse("2022-02-02"),
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
            officialLongTitle = "Test Title",
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
            officialLongTitle = "Test Title",
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
        val norm = Norm(guid = UUID.randomUUID(), officialLongTitle = "title", metadataSections = listOf(referenceSection1, referenceSection2))
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
            officialLongTitle = "new title",
            documentNumber = "document number",
            entryIntoForceDate = LocalDate.now(),
            expirationDateState = UndefinedDate.UNDEFINED_FUTURE,
            completeCitation = "complete citation",
            celexNumber = "celex number",
        )

        val editCommand = EditNormOutputPort.Command(updatedNorm)

        normsService.editNorm(editCommand)
            .`as`(StepVerifier::create)
            .expectNextCount(1)
            .verifyComplete()

        normsService.getNormByGuid(guidQuery)
            .`as`(StepVerifier::create)
            .assertNext {
                assertThat(it.officialLongTitle).isEqualTo(updatedNorm.officialLongTitle)
                assertThat(it.documentNumber).isEqualTo(updatedNorm.documentNumber)
                assertThat(it.entryIntoForceDate).isEqualTo(updatedNorm.entryIntoForceDate)
                assertThat(it.expirationDateState).isEqualTo(updatedNorm.expirationDateState)
                assertThat(it.completeCitation).isEqualTo(updatedNorm.completeCitation)
                assertThat(it.celexNumber).isEqualTo(updatedNorm.celexNumber)
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
        val metadatumDto = MetadatumDto(1, "2020-12-23", DATE, 1, 1)
        val result = normsService.metadatumToEntity(metadatumDto)

        assertThat(result.value).isEqualTo(LocalDate.of(2020, 12, 23))
    }
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
