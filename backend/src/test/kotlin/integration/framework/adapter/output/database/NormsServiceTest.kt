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
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.KEYWORD
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.PARTICIPATION_INSTITUTION
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.PARTICIPATION_TYPE
import de.bund.digitalservice.ris.norms.domain.value.UndefinedDate
import de.bund.digitalservice.ris.norms.framework.adapter.output.database.dto.NormDto
import org.assertj.core.api.Assertions
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
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
import utils.assertNormsAreEqual
import utils.createRandomNorm
import utils.createSimpleSections
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
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
    fun `save simple norm and retrieved by eli`() {
        val norm = Norm(
            guid = UUID.randomUUID(),
            articles = listOf(),
            officialLongTitle = "Test Title",
            announcementDate = LocalDate.parse("2022-02-02"),
            printAnnouncementPage = "1125",
            printAnnouncementGazette = "bg-1",
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
        val firstNorm = Norm(
            guid = UUID.randomUUID(),
            articles = listOf(),
            officialLongTitle = "Test Title",
            announcementDate = LocalDate.parse("2022-02-02"),
            printAnnouncementPage = "1125",
            printAnnouncementGazette = "bg-1",
        )
        val secondNorm = Norm(
            guid = UUID.randomUUID(),
            articles = listOf(),
            officialLongTitle = "Test Title 2",
            announcementDate = LocalDate.parse("2022-02-02"),
            printAnnouncementPage = "111",
            printAnnouncementGazette = "bg-1",
        )
        val saveFirstNormCommand = SaveNormOutputPort.Command(firstNorm)
        val saveSecondNormCommand = SaveNormOutputPort.Command(secondNorm)
        val eliQuery = GetNormByEliOutputPort.Query("bg-1", "2022", "111")

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
            .assertNext { assertNormsAreEqual(secondNorm, it) }
            .verifyComplete()
    }

    @Test
    fun `save simple norm and retrieved by eli with citation year`() {
        val norm = Norm(
            guid = UUID.randomUUID(),
            articles = listOf(),
            officialLongTitle = "Test Title",
            citationYear = "2001",
            printAnnouncementPage = "1125",
            printAnnouncementGazette = "bg-1",
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
                Assertions.assertThat(it.metadataSections).isEqualTo(norm.metadataSections)
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
            providerEntity = "provider entity",
            entryIntoForceDate = LocalDate.now(),
            expirationDateState = UndefinedDate.UNDEFINED_FUTURE,
            printAnnouncementGazette = "print gazette",
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
                assertThat(it.officialLongTitle == updatedNorm.officialLongTitle, `is`(true))
                assertThat(it.documentNumber == updatedNorm.documentNumber, `is`(true))
                assertThat(it.providerEntity == updatedNorm.providerEntity, `is`(true))
                assertThat(it.entryIntoForceDate == updatedNorm.entryIntoForceDate, `is`(true))
                assertThat(it.expirationDateState == updatedNorm.expirationDateState, `is`(true))
                assertThat(it.printAnnouncementGazette == updatedNorm.printAnnouncementGazette, `is`(true))
                assertThat(it.completeCitation == updatedNorm.completeCitation, `is`(true))
                assertThat(it.celexNumber == updatedNorm.celexNumber, `is`(true))
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
            .assertNext {
                assertThat(it.files.size == 1, `is`(true))
                assertThat(it.files[0].name == FILE1.name, `is`(true))
                assertThat(it.files[0].hash == FILE1.hash, `is`(true))
            }
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
                assertThat(it.files.size == 1, `is`(true))
                assertThat(it.files[0].name == FILE1.name, `is`(true))
                assertThat(it.files[0].hash == FILE1.hash, `is`(true))
            }
            .verifyComplete()
    }
}
