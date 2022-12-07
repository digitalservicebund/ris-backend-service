package de.bund.digitalservice.ris.norms.application.port.input

import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import java.time.LocalDate

class ImportNormCommandTest {
    private val validParagraph = ImportNormUseCase.ParagraphData("maker", "text")
    private val validArticle =
        ImportNormUseCase.ArticleData("title", "marker", listOf(validParagraph))
    private val validNorm = ImportNormUseCase.NormData("long title", listOf(validArticle))

    @Test
    fun `it does not require paragraphs to have a non empty marker`() {
        assertDoesNotThrow(
            { ImportNormUseCase.ParagraphData(marker = "", "text") }
        )
    }

    @Test
    fun `it requires paragraphs to have a non empty text`() {
        assertThrows(
            IllegalArgumentException::class.java,
            { ImportNormUseCase.ParagraphData("marker", text = "") }
        )
    }

    @Test
    fun `can create paragraph with valid inut inputs`() {
        val paragraph = ImportNormUseCase.ParagraphData("marker", "text")

        assertTrue(paragraph.marker == "marker")
        assertTrue(paragraph.text == "text")
    }

    @Test
    fun `it does not require articles to have a non empty title`() {
        assertDoesNotThrow(
            { ImportNormUseCase.ArticleData(title = "", "marker", listOf()) }
        )
    }

    @Test
    fun `it requires articles to have a non empty marker`() {
        assertThrows(
            IllegalArgumentException::class.java,
            { ImportNormUseCase.ArticleData("title", marker = "", listOf()) }
        )
    }

    @Test
    fun `can create article with valid data input`() {
        val article = ImportNormUseCase.ArticleData("title", "marker", listOf(validParagraph))

        assertTrue(article.title == "title")
        assertTrue(article.marker == "marker")
        assertTrue(article.paragraphs == listOf(validParagraph))
    }

    @Test
    fun `it requires norms to have a non empty norm long`() {
        assertThrows(
            IllegalArgumentException::class.java,
            { ImportNormUseCase.NormData(officialLongTitle = "", listOf()) }
        )
    }

    @Test
    fun `can create norm with valid data input`() {
        val norm = ImportNormUseCase.NormData("long title", listOf(validArticle))

        assertTrue(norm.officialLongTitle == "long title")
        assertTrue(norm.articles == listOf(validArticle))
    }

    @Test
    fun `can create command with norm data without the optional fields`() {
        val command = ImportNormUseCase.Command(validNorm)

        assertTrue(command.data == validNorm)
    }

    @Test
    fun `can create command with optional fields`() {
        val normData = ImportNormUseCase.NormData(
            "long title", listOf(validArticle), "official short title", "official abbreviation",
            "reference number", LocalDate.parse("2020-10-27"), LocalDate.parse("2020-10-28"), LocalDate.parse("2020-10-29"),
            "frame keywords", "provider entity", "provider deciding body",
            true, "lead jurisdiction", "lead unit", "participation type",
            "participation institution", "document type name", "document norm category",
            "document template name", "subject fna", "subject previous fna",
            "subject gesta", "subject bgb3", "unofficial title", "unofficial short title",
            "unofficial abbreviation", "ris abbreviation"
        )
        val command = ImportNormUseCase.Command(normData)

        assertTrue(command.data == normData)
    }
}
