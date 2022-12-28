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
        assertThat(data.markerText).isEqualTo("1")
        assertThat(data.text).isEqualTo("test text")
    }

    @Test
    fun `it correctly parses the marker from the marker text`() {
        val paragraph = Paragraph(guid = UUID.randomUUID(), marker = "§ 1a", text = "text")

        val data = mapParagraphToDto(paragraph)

        assertThat(data.marker).isEqualTo("1a")
        assertThat(data.markerText).isEqualTo("§ 1a")
    }

    @Test
    fun `it falls back to the orginal number if no marker could be parsed`() {
        val paragraph = Paragraph(guid = UUID.randomUUID(), marker = "§", text = "text")

        val data = mapParagraphToDto(paragraph, 2)

        assertThat(data.marker).isEqualTo("2")
        assertThat(data.markerText).isEqualTo("§")
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

    @Test
    fun `it correctly parses the marker from the marker text`() {
        val article = Article(guid = UUID.randomUUID(), title = "title", marker = "§ 1.1")

        val data = mapArticleToDto(article)

        assertThat(data.marker).isEqualTo("1.1")
        assertThat(data.markerText).isEqualTo("§ 1.1")
    }

    @Test
    fun `it falls back to the orginal number if no marker could be parsed`() {
        val article = Article(guid = UUID.randomUUID(), title = "title", marker = "")

        val data = mapArticleToDto(article, ordinalNumber = 9)

        assertThat(data.marker).isEqualTo("9")
        assertThat(data.markerText).isEqualTo("")
    }
}

class ParseMarkerFromMarkerTextTest {
    @Test
    fun `it correctly cuts off a typical paragraph sign`() {
        val marker = parseMarkerFromMarkerText("§ 1")

        assertThat(marker).isEqualTo("1")
    }

    @Test
    fun `it accepts dots within the marker section`() {
        val marker = parseMarkerFromMarkerText("§ 2.1")

        assertThat(marker).isEqualTo("2.1")
    }

    @Test
    fun `it can parse markers including alphabetical characters`() {
        val marker = parseMarkerFromMarkerText("§ 3a")

        assertThat(marker).isEqualTo("3a")
    }

    @Test
    fun `it only takes the first match in the marker text`() {
        val marker = parseMarkerFromMarkerText("1a 2b foo 7.8a")

        assertThat(marker).isEqualTo("1a")
    }

    @Test
    fun `it returns Null if no marker could be found`() {
        val marker = parseMarkerFromMarkerText("§")

        assertThat(marker).isNull()
    }
}
