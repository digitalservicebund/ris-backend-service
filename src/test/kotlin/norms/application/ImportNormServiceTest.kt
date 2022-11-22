package de.bund.digitalservice.ris.norms.application

import de.bund.digitalservice.ris.norms.application.port.input.ImportNormUseCase
import de.bund.digitalservice.ris.norms.application.port.output.SaveNormOutputPort
import de.bund.digitalservice.ris.norms.domain.entity.Norm
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import java.time.LocalDate

// TODO: Improve by using behavior driven testing concept with documentation.
class ImportNormServiceTest {
    @Test
    fun `it saves norm to output port with data from command without optional fields`() {
        val port = mockk<SaveNormOutputPort>()
        val service = ImportNormService(port)
        val paragraph = ImportNormUseCase.ParagraphData("marker", "text")
        val article = ImportNormUseCase.ArticleData("title", "marker", listOf(paragraph))
        val norm = ImportNormUseCase.NormData("long title", listOf(article))
        val command = ImportNormUseCase.Command(norm)

        every { port.saveNorm(any()) } returns Mono.just(true)

        service.importNorm(command)

        verify(exactly = 1) { port.saveNorm(any()) }
        verify {
            port.saveNorm(
                withArg {
                    assertTrue(it.longTitle == "long title")
                    assertTrue(it.articles.size == 1)
                    assertTrue(it.articles[0].title == "title")
                    assertTrue(it.articles[0].marker == "marker")
                    assertTrue(it.articles[0].paragraphs.size == 1)
                    assertTrue(it.articles[0].paragraphs[0].marker == "marker")
                    assertTrue(it.articles[0].paragraphs[0].text == "text")
                    assertTrue(it.officialShortTitle == null)
                    assertTrue(it.officialAbbreviation == null)
                    assertTrue(it.referenceNumber == null)
                    assertTrue(it.publicationDate == null)
                    assertTrue(it.announcementDate == null)
                    assertTrue(it.citationDate == null)
                    assertTrue(it.frameKeywords == null)
                    assertTrue(it.authorEntity == null)
                    assertTrue(it.authorDecidingBody == null)
                    assertTrue(it.authorIsResolutionMajority == null)
                    assertTrue(it.leadJurisdiction == null)
                    assertTrue(it.leadUnit == null)
                    assertTrue(it.participationType == null)
                    assertTrue(it.participationInstitution == null)
                    assertTrue(it.documentTypeName == null)
                    assertTrue(it.documentNormCategory == null)
                    assertTrue(it.documentTemplateName == null)
                    assertTrue(it.subjectFna == null)
                    assertTrue(it.subjectPreviousFna == null)
                    assertTrue(it.subjectGesta == null)
                    assertTrue(it.subjectBgb3 == null)
                }
            )
        }
    }

    @Test
    fun `it saves norm to output port with data from command including optional fields`() {
        val port = mockk<SaveNormOutputPort>()
        val service = ImportNormService(port)
        val paragraph = ImportNormUseCase.ParagraphData("marker", "text")
        val article = ImportNormUseCase.ArticleData("title", "marker", listOf(paragraph))
        val norm = ImportNormUseCase.NormData(
            "long title", listOf(article), "official short title", "official abbreviation",
            "reference number", "2020-10-27", "2020-10-28", "2020-10-29",
            "frame keywords", "author entity", "author deciding body",
            true, "lead jurisdiction", "lead unit", "participation type",
            "participation institution", "document type name", "document norm category",
            "document template name", "subject fna", "subject previous fna",
            "subject gesta", "subject bgb3"
        )
        val command = ImportNormUseCase.Command(norm)

        every { port.saveNorm(any()) } returns Mono.just(true)

        service.importNorm(command)

        verify(exactly = 1) { port.saveNorm(any()) }
        verify {
            port.saveNorm(
                withArg {
                    assertTrue(it.longTitle == "long title")
                    assertTrue(it.articles.size == 1)
                    assertTrue(it.articles[0].title == "title")
                    assertTrue(it.articles[0].marker == "marker")
                    assertTrue(it.articles[0].paragraphs.size == 1)
                    assertTrue(it.articles[0].paragraphs[0].marker == "marker")
                    assertTrue(it.articles[0].paragraphs[0].text == "text")
                    assertTrue(it.officialShortTitle == "official short title")
                    assertTrue(it.officialAbbreviation == "official abbreviation")
                    assertTrue(it.referenceNumber == "reference number")
                    assertTrue(it.publicationDate == LocalDate.parse("2020-10-27"))
                    assertTrue(it.announcementDate == LocalDate.parse("2020-10-28"))
                    assertTrue(it.citationDate == LocalDate.parse("2020-10-29"))
                    assertTrue(it.frameKeywords == "frame keywords")
                    assertTrue(it.authorEntity == "author entity")
                    assertTrue(it.authorDecidingBody == "author deciding body")
                    assertTrue(it.authorIsResolutionMajority == true)
                    assertTrue(it.leadJurisdiction == "lead jurisdiction")
                    assertTrue(it.leadUnit == "lead unit")
                    assertTrue(it.participationType == "participation type")
                    assertTrue(it.participationInstitution == "participation institution")
                    assertTrue(it.documentTypeName == "document type name")
                    assertTrue(it.documentNormCategory == "document norm category")
                    assertTrue(it.documentTemplateName == "document template name")
                    assertTrue(it.subjectFna == "subject fna")
                    assertTrue(it.subjectPreviousFna == "subject previous fna")
                    assertTrue(it.subjectGesta == "subject gesta")
                    assertTrue(it.subjectBgb3 == "subject bgb3")
                }
            )
        }
    }

    @Test
    fun `it generates a new GUID for every norm`() {
        val port = mockk<SaveNormOutputPort>()
        val service = ImportNormService(port)
        val normOne = ImportNormUseCase.NormData("long title one", listOf())
        val normTwo = ImportNormUseCase.NormData("long title two", listOf())
        val normThree = ImportNormUseCase.NormData("long title three", listOf())
        val commandOne = ImportNormUseCase.Command(normOne)
        val commandTwo = ImportNormUseCase.Command(normTwo)
        val commandThree = ImportNormUseCase.Command(normThree)

        every { port.saveNorm(any()) } returns Mono.just(true)

        service.importNorm(commandOne)
        service.importNorm(commandTwo)
        service.importNorm(commandThree)

        val savedNorms = mutableListOf<Norm>()
        verify(exactly = 3) { port.saveNorm(capture(savedNorms)) }
        val usedGuids = savedNorms.map { it.guid }
        assertTrue(usedGuids.toSet().size == usedGuids.size)
    }

    @Test
    fun `it returns the new GUID under which the norm is saved`() {
        val port = mockk<SaveNormOutputPort>()
        val service = ImportNormService(port)
        val norm = ImportNormUseCase.NormData("long title", listOf())
        val command = ImportNormUseCase.Command(norm)

        val savedNorm = slot<Norm>()
        every { port.saveNorm(capture(savedNorm)) } returns Mono.just(true)

        StepVerifier.create(service.importNorm(command))
            .expectNextMatches { it == savedNorm.captured.guid }
            .verifyComplete()
    }
}
