package de.bund.digitalservice.ris.norms.application.service

import de.bund.digitalservice.ris.norms.application.port.input.ImportNormUseCase
import de.bund.digitalservice.ris.norms.application.port.output.SaveNormOutputPort
import de.bund.digitalservice.ris.norms.domain.entity.Norm
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import norms.utils.assertNormAndNormDataWithoutArticles
import norms.utils.createRandomImportNormData
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import reactor.core.publisher.Mono
import reactor.test.StepVerifier

// TODO: Improve by using behavior driven testing concept with documentation.
class ImportNormServiceTest {
    @Test
    fun `it saves norm to output port with data from command without optional fields`() {
        val port = mockk<SaveNormOutputPort>()
        val service = ImportNormService(port)
        val paragraph = ImportNormUseCase.ParagraphData("marker", "text")
        val article = ImportNormUseCase.ArticleData("title", "marker", listOf(paragraph))
        val normData = ImportNormUseCase.NormData("long title", listOf(article))
        val command = ImportNormUseCase.Command(normData)

        every { port.saveNorm(any()) } returns Mono.just(true)

        service.importNorm(command)

        verify(exactly = 1) { port.saveNorm(any()) }
        verify {
            port.saveNorm(
                withArg {
                    assertThat(it.articles).hasSize(1)
                    assertThat(it.articles[0].title).isEqualTo("title")
                    assertThat(it.articles[0].marker).isEqualTo("marker")
                    assertThat(it.articles[0].paragraphs).hasSize(1)
                    assertThat(it.articles[0].paragraphs[0].marker).isEqualTo("marker")
                    assertThat(it.articles[0].paragraphs[0].text).isEqualTo("text")
                    assertNormAndNormDataWithoutArticles(it, normData)
                }
            )
        }
    }

    @Test
    fun `it saves norm to output port with data from command including optional fields`() {
        val port = mockk<SaveNormOutputPort>()
        val service = ImportNormService(port)

        val normData = createRandomImportNormData()
        val command = ImportNormUseCase.Command(normData)

        every { port.saveNorm(any()) } returns Mono.just(true)

        service.importNorm(command)

        verify(exactly = 1) { port.saveNorm(any()) }
        verify {
            port.saveNorm(
                withArg {
                    assertThat(it.articles.size).isEqualTo(2)
                    assertThat(it.articles[0].title).isEqualTo(normData.articles[0].title)
                    assertThat(it.articles[0].marker).isEqualTo(normData.articles[0].marker)
                    assertThat(it.articles[0].paragraphs).hasSize(2)
                    assertThat(it.articles[0].paragraphs[0].marker)
                        .isEqualTo(normData.articles[0].paragraphs[0].marker)
                    assertThat(it.articles[0].paragraphs[0].text)
                        .isEqualTo(normData.articles[0].paragraphs[0].text)
                    assertThat(it.articles[0].paragraphs[1].marker)
                        .isEqualTo(normData.articles[0].paragraphs[1].marker)
                    assertThat(it.articles[0].paragraphs[1].text)
                        .isEqualTo(normData.articles[0].paragraphs[1].text)
                    assertThat(it.articles[1].title).isEqualTo(normData.articles[1].title)
                    assertThat(it.articles[1].marker).isEqualTo(normData.articles[1].marker)
                    assertThat(it.articles[1].paragraphs).hasSize(2)
                    assertThat(it.articles[1].paragraphs[0].marker)
                        .isEqualTo(normData.articles[1].paragraphs[0].marker)
                    assertThat(it.articles[1].paragraphs[0].text)
                        .isEqualTo(normData.articles[1].paragraphs[0].text)
                    assertThat(it.articles[1].paragraphs[1].marker)
                        .isEqualTo(normData.articles[1].paragraphs[1].marker)
                    assertThat(it.articles[1].paragraphs[1].text)
                        .isEqualTo(normData.articles[1].paragraphs[1].text)
                    assertNormAndNormDataWithoutArticles(it, normData)
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
        assertThat(usedGuids.toSet().size).isEqualTo(usedGuids.size)
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
