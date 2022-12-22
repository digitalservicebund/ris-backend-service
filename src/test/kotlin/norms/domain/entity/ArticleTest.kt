package de.bund.digitalservice.ris.norms.domain.entity

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.util.UUID

class ArticleTest {
    @Test
    fun `can create a simple paragraph instance`() {
        val paragraph = Paragraph(UUID.randomUUID(), "marker", "text")
        val guid = UUID.randomUUID()
        val article = Article(guid, "title", "marker", listOf(paragraph))

        assertThat(article.guid).isEqualTo(guid)
        assertThat(article.title).isEqualTo("title")
        assertThat(article.marker).isEqualTo("marker")
        assertThat(article.paragraphs).isEqualTo(listOf(paragraph))
    }
}
