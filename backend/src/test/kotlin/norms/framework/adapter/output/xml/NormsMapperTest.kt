package de.bund.digitalservice.ris.norms.framework.adapter.output.xml

import de.bund.digitalservice.ris.norms.domain.entity.Article
import de.bund.digitalservice.ris.norms.domain.entity.Paragraph
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.util.UUID

class MapParagraphToDto {
    @Test
    fun `it sets correctly all data properties`() {
        val paragraph =
            Paragraph(
                guid = UUID.fromString("53d29ef7-377c-4d14-864b-eb3a85769359"),
                marker = "1",
                text = "test text"
            )

        val data = mapParagraphToDto(paragraph)

        assertThat(data.guid).isEqualTo("53d29ef7-377c-4d14-864b-eb3a85769359")
        assertThat(data.marker).isEqualTo("1")
        assertThat(data.text).isEqualTo("test text")
    }
}

class MapArticleToDto {
    @Test
    fun `it sets correctly all data properties`() {
        val paragraph = Paragraph(UUID.randomUUID(), null, "text")
        val article =
            Article(
                guid = UUID.fromString("761b5537-5aa5-4901-81f7-fbf7e040a7c8"),
                title = "test title",
                marker = "1",
                paragraphs = listOf(paragraph)
            )

        val data = mapArticleToDto(article)

        assertThat(data.guid).isEqualTo("761b5537-5aa5-4901-81f7-fbf7e040a7c8")
        assertThat(data.title).isEqualTo("test title")
        assertThat(data.marker).isEqualTo("1")
        assertThat(data.paragraphs).hasSize(1)
    }
}
