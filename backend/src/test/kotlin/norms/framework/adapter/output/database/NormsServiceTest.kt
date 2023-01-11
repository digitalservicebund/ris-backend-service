package de.bund.digitalservice.ris.norms.framework.adapter.output.database

import de.bund.digitalservice.ris.caselaw.config.FlywayConfig
import de.bund.digitalservice.ris.norms.application.port.output.SearchNormsOutputPort.QueryFields
import de.bund.digitalservice.ris.norms.application.port.output.SearchNormsOutputPort.QueryParameter
import de.bund.digitalservice.ris.norms.domain.entity.Article
import de.bund.digitalservice.ris.norms.domain.entity.Norm
import de.bund.digitalservice.ris.norms.domain.entity.Paragraph
import de.bund.digitalservice.ris.norms.domain.value.UndefinedDate
import de.bund.digitalservice.ris.norms.framework.adapter.output.database.dto.NormDto
import norms.utils.createRandomNorm
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
import java.time.Duration
import java.time.LocalDate
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
    fun `save simple norm and verify it was saved with get all norms`() {
        normsService.getAllNorms()
            .`as`(StepVerifier::create)
            .expectNextCount(0)
            .verifyComplete()

        normsService.saveNorm(NORM)
            .`as`(StepVerifier::create)
            .expectNextCount(1)
            .verifyComplete()

        normsService.getAllNorms()
            .`as`(StepVerifier::create)
            .expectNextCount(1)
            .verifyComplete()
    }

    @Test
    fun `save simple norm and retrieved by eli`() {
        val norm = NORM
        norm.announcementDate = LocalDate.parse("2022-02-02")
        norm.printAnnouncementPage = "1125"
        norm.printAnnouncementGazette = "bg-1"

        normsService.saveNorm(norm).log()
            .`as`(StepVerifier::create)
            .expectNextCount(1)
            .verifyComplete()

        val query = listOf(
            QueryParameter(QueryFields.PRINT_ANNOUNCEMENT_GAZETTE, "bg-1"),
            QueryParameter(QueryFields.PRINT_ANNOUNCEMENT_PAGE, "1125"),
            QueryParameter(QueryFields.ANNOUNCEMENT_OR_CITATION_YEAR, "2022", isYearForDate = true)
        )

        normsService.searchNorms(query).log()
            .`as`(StepVerifier::create)
            .expectNextCount(1)
            .verifyComplete()
    }

    @Test
    fun `save two norms and retrieved both by eli`() {
        val normWithAnnouncementDate = NORM
        normWithAnnouncementDate.announcementDate = LocalDate.parse("2022-02-02")
        normWithAnnouncementDate.citationDate = null
        normWithAnnouncementDate.printAnnouncementPage = "1125"
        normWithAnnouncementDate.printAnnouncementGazette = "bg-1"

        normsService.saveNorm(normWithAnnouncementDate).log()
            .`as`(StepVerifier::create)
            .expectNextCount(1)
            .verifyComplete()

        val normWithCitationDate = NORM
        normWithCitationDate.citationDate = LocalDate.parse("2022-02-02")
        normWithAnnouncementDate.announcementDate = null
        normWithCitationDate.printAnnouncementPage = "1125"
        normWithCitationDate.printAnnouncementGazette = "bg-1"

        normsService.saveNorm(normWithCitationDate).log()
            .`as`(StepVerifier::create)
            .expectNextCount(1)
            .verifyComplete()

        val query = listOf(
            QueryParameter(QueryFields.PRINT_ANNOUNCEMENT_GAZETTE, "bg-1"),
            QueryParameter(QueryFields.PRINT_ANNOUNCEMENT_PAGE, "1125"),
            QueryParameter(QueryFields.ANNOUNCEMENT_OR_CITATION_YEAR, "2022", isYearForDate = true)
        )

        normsService.searchNorms(query).log()
            .`as`(StepVerifier::create)
            .expectNextCount(2)
            .verifyComplete()
    }

    @Test
    fun `save simple norm and retrieved by guid`() {
        normsService.saveNorm(NORM)
            .`as`(StepVerifier::create)
            .expectNextCount(1)
            .verifyComplete()

        normsService.getNormByGuid(NORM.guid)
            .`as`(StepVerifier::create)
            .expectNextCount(1)
            .verifyComplete()
    }

    @Test
    fun `save norm with 1 article and 2 paragraphs and retrieved by guid`() {
        val article = ARTICLE1.copy(paragraphs = listOf(PARAGRAPH1, PARAGRAPH2))
        val norm = NORM.copy(articles = listOf(article))

        normsService.saveNorm(norm)
            .`as`(StepVerifier::create)
            .expectNextCount(1)
            .verifyComplete()

        normsService.getNormByGuid(norm.guid)
            .`as`(StepVerifier::create)
            .assertNext { validateNorm(norm, it) }
            .verifyComplete()
    }

    @Test
    fun `save norm with 2 article and 2 paragraphs each and retrieved by guid`() {
        val article1 = ARTICLE1.copy(paragraphs = listOf(PARAGRAPH1, PARAGRAPH2))
        val article2 = ARTICLE2.copy(paragraphs = listOf(PARAGRAPH3, PARAGRAPH4))
        val norm = NORM.copy(articles = listOf(article1, article2))

        normsService.saveNorm(norm)
            .`as`(StepVerifier::create)
            .expectNextCount(1)
            .verifyComplete()

        normsService.getNormByGuid(norm.guid)
            .`as`(StepVerifier::create)
            .assertNext { validateNorm(norm, it) }
            .verifyComplete()
    }

    @Test
    fun `save norm and edit norm`() {
        normsService.saveNorm(NORM)
            .`as`(StepVerifier::create)
            .expectNextCount(1)
            .verifyComplete()

        normsService.getNormByGuid(NORM.guid)
            .`as`(StepVerifier::create)
            .expectNextCount(1)
            .verifyComplete()

        val updatedNorm = NORM.copy(
            officialLongTitle = "new title", documentNumber = "document number",
            providerEntity = "provider entity", entryIntoForceDate = LocalDate.now(),
            expirationDateState = UndefinedDate.UNDEFINED_FUTURE, printAnnouncementGazette = "print gazette",
            completeCitation = "complete citation", unofficialAbbreviation = "unofficial abbreviation",
            celexNumber = "celex number"
        )
        normsService.editNorm(updatedNorm)
            .`as`(StepVerifier::create)
            .expectNextCount(1)
            .verifyComplete()

        normsService.getNormByGuid(NORM.guid)
            .`as`(StepVerifier::create)
            .assertNext { validateNorm(updatedNorm, it) }
            .verifyComplete()
    }

    private fun validateNorm(normBeforePersist: Norm, normAfterPersist: Norm) {
        assertThat(normBeforePersist == normAfterPersist, `is`(true))
    }
}
