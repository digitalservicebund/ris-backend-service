package de.bund.digitalservice.ris.norms.application.port.input

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatIllegalArgumentException
import org.assertj.core.api.Assertions.assertThatNoException
import org.junit.jupiter.api.Test
import utils.createRandomImportNormData

class ImportNormCommandTest {
    private val validParagraph = ImportNormUseCase.ParagraphData("maker", "text")
    private val validArticle =
        ImportNormUseCase.ArticleData("title", "marker", listOf(validParagraph))
    private val validNorm = ImportNormUseCase.NormData("long title", listOf(validArticle))

    @Test
    fun `it does not require paragraphs to have a non empty marker`() {
        assertThatNoException().isThrownBy({ ImportNormUseCase.ParagraphData(marker = "", "text") })
    }

    @Test
    fun `it requires paragraphs to have a non empty text`() {
        assertThatIllegalArgumentException()
            .isThrownBy({ ImportNormUseCase.ParagraphData("marker", text = "") })
    }

    @Test
    fun `can create paragraph with valid inut inputs`() {
        val paragraph = ImportNormUseCase.ParagraphData("marker", "text")

        assertThat(paragraph.marker).isEqualTo("marker")
        assertThat(paragraph.text).isEqualTo("text")
    }

    @Test
    fun `it does not require articles to have a non empty title`() {
        assertThatNoException()
            .isThrownBy({ ImportNormUseCase.ArticleData(title = "", "marker", listOf()) })
    }

    @Test
    fun `it requires articles to have a non empty marker`() {
        assertThatIllegalArgumentException()
            .isThrownBy({ ImportNormUseCase.ArticleData("title", marker = "", listOf()) })
    }

    @Test
    fun `can create article with valid data input`() {
        val article = ImportNormUseCase.ArticleData("title", "marker", listOf(validParagraph))

        assertThat(article.title).isEqualTo("title")
        assertThat(article.marker).isEqualTo("marker")
        assertThat(article.paragraphs).isEqualTo(listOf(validParagraph))
    }

    @Test
    fun `it requires norms to have a non empty norm long`() {
        assertThatIllegalArgumentException()
            .isThrownBy({ ImportNormUseCase.NormData(officialLongTitle = "", listOf()) })
    }

    @Test
    fun `can create norm with valid data input`() {
        val norm = ImportNormUseCase.NormData("long title", listOf(validArticle))

        assertThat(norm.officialLongTitle).isEqualTo("long title")
        assertThat(norm.articles).isEqualTo(listOf(validArticle))
    }

    @Test
    fun `can create command with norm data without the optional fields`() {
        val command = ImportNormUseCase.Command(validNorm)

        assertThat(command.data).isEqualTo(validNorm)
    }

    @Test
    fun `can create command with optional fields`() {
        val normData = createRandomImportNormData()
        val command = ImportNormUseCase.Command(normData)

        assertThat(command.data).isEqualTo(normData)
    }
}
