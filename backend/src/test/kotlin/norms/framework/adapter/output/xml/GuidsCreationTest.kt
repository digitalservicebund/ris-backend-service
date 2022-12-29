package de.bund.digitalservice.ris.norms.framework.adapter.output.xml

import norms.utils.createRandomNorm
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.w3c.dom.Node

class GuidsCreationTest {
    @Test
    fun `it creates unique guids for short and long titles`() {
        val document = convertNormToLegalDocML(createRandomNorm())

        val longTitle = document.getElementsByTagName("akn:longTitle").item(0)
        val longTitleP = getFirstChildNodeWithTagName(longTitle, "akn:p")
        val shortTitle = getFirstChildNodeWithTagName(longTitleP, "akn:shortTitle")

        assertElementsHaveUniqueValidGuids(listOf(longTitle, longTitleP, shortTitle))
    }

    private fun assertElementsHaveUniqueValidGuids(nodes: List<Node>) {
        val uuidRegex = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$".toPattern()
        val guids = nodes.map { it.attributes.getNamedItem("GUID").nodeValue }
        assertThat(guids).allMatch(uuidRegex.asMatchPredicate()).hasSize(guids.distinct().size)
    }

    @Test
    fun `it creates unique guids for articles`() {
        val document = convertNormToLegalDocML(createRandomNorm())

        val article = document.getElementsByTagName("akn:article").item(0)
        val marker = getFirstChildNodeWithTagName(article, "akn:num")
        val title = getFirstChildNodeWithTagName(article, "akn:heading")

        assertElementsHaveUniqueValidGuids(listOf(marker, title))
    }

    @Test
    fun `it creates unique guids for paragraphs`() {
        val document = convertNormToLegalDocML(createRandomNorm())

        val paragraph = document.getElementsByTagName("akn:paragraph").item(0)
        val marker = getFirstChildNodeWithTagName(paragraph, "akn:num")
        val content = getFirstChildNodeWithTagName(paragraph, "akn:content")
        val contentP = getFirstChildNodeWithTagName(content, "akn:p")

        assertElementsHaveUniqueValidGuids(listOf(marker, content, contentP))
    }
}
