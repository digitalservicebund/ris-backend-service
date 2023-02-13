package de.bund.digitalservice.ris.norms.framework.adapter.output.xml

import de.bund.digitalservice.ris.norms.domain.entity.Article
import de.bund.digitalservice.ris.norms.domain.entity.Paragraph
import de.bund.digitalservice.ris.norms.framework.adapter.output.xml.dto.ContentDto
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
                text = "test text",
            )

        val data = mapParagraphToDto(paragraph, articleMarker = "2")

        assertThat(data.guid).isEqualTo("53d29ef7-377c-4d14-864b-eb3a85769359")
        assertThat(data.marker).isEqualTo("1")
        assertThat(data.articleMarker).isEqualTo("2")
        assertThat(data.markerText?.value).isEqualTo("1")
        assertThat(data.markerText?.guid).isInstanceOf(UUID::class.java).isNotNull
        assertThat(data.content.text?.value).isEqualTo("test text")
        assertThat(data.content.text?.guid).isInstanceOf(UUID::class.java).isNotNull
    }

    @Test
    fun `it correctly parses the marker from the marker text`() {
        val paragraph = Paragraph(guid = UUID.randomUUID(), marker = "§ 1a", text = "text")

        val data = mapParagraphToDto(paragraph, "")

        assertThat(data.marker).isEqualTo("1a")
        assertThat(data.markerText?.value).isEqualTo("§ 1a")
        assertThat(data.markerText?.guid).isInstanceOf(UUID::class.java).isNotNull
    }

    @Test
    fun `it falls back to the orginal number if no marker could be parsed`() {
        val paragraph = Paragraph(guid = UUID.randomUUID(), marker = "§", text = "text")

        val data = mapParagraphToDto(paragraph, "", ordinalNumber = 2)

        assertThat(data.marker).isEqualTo("2")
        assertThat(data.markerText?.value).isEqualTo("§")
        assertThat(data.markerText?.guid).isInstanceOf(UUID::class.java).isNotNull
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
                paragraphs = listOf(paragraph),
            )

        val data = mapArticleToDto(article)

        assertThat(data.guid).isEqualTo("761b5537-5aa5-4901-81f7-fbf7e040a7c8")
        assertThat(data.title?.value).isEqualTo("test title")
        assertThat(data.title?.guid).isInstanceOf(UUID::class.java).isNotNull
        assertThat(data.marker).isEqualTo("1")
        assertThat(data.paragraphs).hasSize(1)
    }

    @Test
    fun `it correctly parses the marker from the marker text`() {
        val article = Article(guid = UUID.randomUUID(), title = "title", marker = "§ 1.1")

        val data = mapArticleToDto(article)

        assertThat(data.marker).isEqualTo("1.1")
        assertThat(data.markerText.value).isEqualTo("§ 1.1")
        assertThat(data.markerText.guid).isInstanceOf(UUID::class.java).isNotNull
    }

    @Test
    fun `it falls back to the orginal number if no marker could be parsed`() {
        val article = Article(guid = UUID.randomUUID(), title = "title", marker = "")

        val data = mapArticleToDto(article, ordinalNumber = 9)

        assertThat(data.marker).isEqualTo("9")
        assertThat(data.markerText.value).isEqualTo("")
        assertThat(data.markerText.guid).isInstanceOf(UUID::class.java).isNotNull
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

    @Test
    fun `it creates content from html string with simple list`() {
        val content = toContentDto(
            "list intro text:\n" +
                "                    <DL Type=\"arabic\">\n" +
                "                            <DT>1.</DT>\n" +
                "                        <DD Font=\"normal\">\n" +
                "                            <LA>1. text\n" +
                "                            </LA>\n" +
                "                        </DD>\n" +
                "                        <DT>2.</DT>\n" +
                "                        <DD Font=\"normal\">\n" +
                "                            <LA>2. text\n" +
                "                            </LA>\n" +
                "                        </DD>\n" +
                "                    </DL>\n" +
                "                ",
        )
        assertThat(content).isNotNull

        validateListContent(
            content,
            listMarkerParent = null,
            listMarkerGrandparent = null,
            isList = true,
            listIntro = "list intro text:",
            isText = false,
            text = null,
            pointsSize = 2,
        )
        validateListContent(
            content.points[0],
            listMarkerParent = null,
            listMarkerGrandparent = null,
            isList = false,
            listIntro = null,
            isText = true,
            text = "1. text",
            pointsSize = 0,
        )
        validateListContent(
            content.points[1],
            listMarkerParent = null,
            listMarkerGrandparent = null,
            isList = false,
            listIntro = null,
            isText = true,
            text = "2. text",
            pointsSize = 0,
        )
    }

    @Test
    fun `it creates content from html string with nested list`() {
        val content = toContentDto(
            "list intro text:\n" +
                "                    <DL Font=\"normal\" Type=\"arabic\">\n" +
                "                        <DT>1.</DT>\n" +
                "                        <DD Font=\"normal\">\n" +
                "                            <LA Size=\"normal\">1. point text\n" +
                "                                <DL Font=\"normal\" Type=\"alpha\">\n" +
                "                                    <DT>a)</DT>\n" +
                "                                    <DD Font=\"normal\">\n" +
                "                                        <LA Size=\"normal\">a) point text\n" +
                "                                        </LA>\n" +
                "                                    </DD>\n" +
                "                                    <DT>b)</DT>\n" +
                "                                    <DD Font=\"normal\">\n" +
                "                                        <LA Size=\"normal\">b) point text\n" +
                "                                            <DL Font=\"normal\" Type=\"a-alpha\">\n" +
                "                                                <DT>aa)</DT>\n" +
                "                                                <DD Font=\"normal\">\n" +
                "                                                    <LA Size=\"normal\">aa) point text\n" +
                "                                                    </LA>\n" +
                "                                                </DD>\n" +
                "                                                <DT>bb)</DT>\n" +
                "                                                <DD Font=\"normal\">\n" +
                "                                                    <LA Size=\"normal\">bb) point text\n" +
                "                                                    </LA>\n" +
                "                                                </DD>\n" +
                "                                            </DL>\n" +
                "                                        </LA>\n" +
                "                                    </DD>\n" +
                "                                </DL>\n" +
                "                            </LA>\n" +
                "                        </DD>\n" +
                "                    </DL>",
        )
        assertThat(content).isNotNull

        validateListContent(
            content,
            listMarkerParent = null,
            listMarkerGrandparent = null,
            isList = true,
            listIntro = "list intro text:",
            isText = false,
            text = null,
            pointsSize = 1,
        )

        val firstPointFirstLevel = content.points[0]
        validateListContent(
            firstPointFirstLevel,
            listMarkerParent = null,
            listMarkerGrandparent = null,
            isList = true,
            listIntro = "1. point text",
            isText = false,
            text = null,
            pointsSize = 2,
        )

        val firstPointSecondLevel = firstPointFirstLevel.points[0]
        validateListContent(
            firstPointSecondLevel,
            listMarkerParent = "1",
            listMarkerGrandparent = null,
            isList = false,
            listIntro = null,
            isText = true,
            text = "a) point text",
            pointsSize = 0,
        )

        val secondPointSecondLevel = firstPointFirstLevel.points[1]
        validateListContent(
            secondPointSecondLevel,
            listMarkerParent = "1",
            listMarkerGrandparent = null,
            isList = true,
            listIntro = "b) point text",
            isText = false,
            text = null,
            pointsSize = 2,
        )

        val firstPointThirdLevel = secondPointSecondLevel.points[0]
        validateListContent(
            firstPointThirdLevel,
            listMarkerParent = "b",
            listMarkerGrandparent = "1",
            isList = false,
            listIntro = null,
            isText = true,
            text = "aa) point text",
            pointsSize = 0,
        )

        val secondPointThirdLevel = secondPointSecondLevel.points[1]
        validateListContent(
            secondPointThirdLevel,
            listMarkerParent = "b",
            listMarkerGrandparent = "1",
            isList = false,
            listIntro = null,
            isText = true,
            text = "bb) point text",
            pointsSize = 0,
        )
    }

    private fun validateListContent(
        content: ContentDto,
        paragraphMarker: String = "0",
        listMarkerParent: String?,
        listMarkerGrandparent: String?,
        isList: Boolean,
        listIntro: String?,
        isText: Boolean,
        text: String?,
        pointsSize: Int,
    ) {
        assertThat(content.guid).isNotNull
        assertThat(content.paragraphMarker).isEqualTo(paragraphMarker)
        assertThat(content.listMarkerParent).isEqualTo(listMarkerParent)
        assertThat(content.listMarkerGrandparent).isEqualTo(listMarkerGrandparent)
        if (listIntro == null) {
            assertThat(content.intro).isNull()
        } else {
            assertThat(content.intro?.value?.trim()).isEqualTo(listIntro)
            assertThat(content.intro?.guid).isInstanceOf(UUID::class.java).isNotNull
        }

        assertThat(content.isList).isEqualTo(isList)
        assertThat(content.isText).isEqualTo(isText)
        if (text == null) {
            assertThat(content.text).isNull()
        } else {
            assertThat(content.text?.value?.trim()).isEqualTo(text)
            assertThat(content.text?.guid).isInstanceOf(UUID::class.java).isNotNull
        }
        assertThat(content.points).hasSize(pointsSize)
    }
}
