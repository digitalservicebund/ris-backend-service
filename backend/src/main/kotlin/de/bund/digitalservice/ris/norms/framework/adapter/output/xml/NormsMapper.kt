package de.bund.digitalservice.ris.norms.framework.adapter.output.xml

import de.bund.digitalservice.ris.norms.domain.entity.Article
import de.bund.digitalservice.ris.norms.domain.entity.Norm
import de.bund.digitalservice.ris.norms.domain.entity.Paragraph
import de.bund.digitalservice.ris.norms.framework.adapter.output.xml.dto.ArticleDto
import de.bund.digitalservice.ris.norms.framework.adapter.output.xml.dto.ContentDto
import de.bund.digitalservice.ris.norms.framework.adapter.output.xml.dto.IdentifiedElement
import de.bund.digitalservice.ris.norms.framework.adapter.output.xml.dto.NormDto
import de.bund.digitalservice.ris.norms.framework.adapter.output.xml.dto.ParagraphDto
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.nodes.TextNode

fun mapNormToDto(norm: Norm): NormDto {
    return NormDto(
        guid = norm.guid.toString(),
        officialLongTitle = IdentifiedElement(norm.officialLongTitle),
        officialShortTitle = IdentifiedElement(norm.officialShortTitle),
        announcementDate = norm.announcementDate?.toString() ?: norm.citationDate?.toString(),
        documentTypeName = getMappedValue(Property.DOCUMENT_TYPE_NAME, norm.documentTypeName ?: ""),
        documentNormCategory = getMappedValue(Property.DOCUMENT_NORM_CATEGORY, norm.documentNormCategory ?: ""),
        providerDecidingBody = getMappedValue(Property.PROVIDER_DECIDING_BODY, norm.providerDecidingBody ?: ""),
        participationInstitution = getMappedValue(Property.PARTICIPATION_INSTITUTION, norm.participationInstitution ?: ""),
        printAnnouncementGazette = norm.europeanLegalIdentifier.gazette,
        printAnnouncementPage = norm.printAnnouncementPage,
        europeanLegalIdentifier = norm.europeanLegalIdentifier.toString(),
        articles = norm.articles.mapIndexed { index, article ->
            mapArticleToDto(article, index)
        }
    )
}

fun mapArticleToDto(article: Article, ordinalNumber: Int = 1): ArticleDto {
    val marker = parseMarkerFromMarkerText(article.marker) ?: "$ordinalNumber"

    return ArticleDto(
        guid = article.guid.toString(),
        title = IdentifiedElement(article.title),
        marker = marker,
        markerText = IdentifiedElement(article.marker),
        paragraphs =
        article.paragraphs.mapIndexed { index, paragraph ->
            mapParagraphToDto(paragraph, marker, index)
        }
    )
}

fun mapParagraphToDto(paragraph: Paragraph, articleMarker: String, ordinalNumber: Int = 1): ParagraphDto {
    return ParagraphDto(
        guid = paragraph.guid.toString(),
        marker = parseMarkerFromMarkerText(paragraph.marker) ?: "$ordinalNumber",
        markerText = IdentifiedElement(paragraph.marker),
        articleMarker = articleMarker,
        content = if (paragraph.text.contains("<DL")) toContentDto(paragraph.text) else ContentDto(isText = true, text = IdentifiedElement(paragraph.text))
    )
}

fun toContentDto(htmlAsString: String): ContentDto {
    val list = ContentDto()

    val doc: Document = Jsoup.parse(htmlAsString.replace("<Rec>", "").replace("</Rec>", ""))

    list.isList = true
    list.intro = IdentifiedElement((doc.body().childNode(0) as TextNode).text())
    list.points.addAll(parseChildren(doc.body().child(0)))

    return list
}

fun parseChildren(node: Element, order: Int = 1): MutableList<ContentDto> {
    val childListArray = mutableListOf<ContentDto>()
    val childList = ContentDto()

    if (node.parentNode()?.nodeName() === "dd") {
        childList.marker = node.parent()?.previousElementSibling()?.text()
        childList.order = order
    }

    if (node.nodeName() === "dl") {
        childList.isList = true
        for (childNumber in 0 until node.childrenSize() step 2) {
            val ddElement = node.child(childNumber + 1)
            childListArray.addAll(parseChildren(ddElement.child(0), (childNumber / 2) + 1))
        }
    }
    if (node.nodeName() == "la") {
        if (node.childrenSize() > 0) {
            childList.isList = true
            childList.intro = IdentifiedElement((node.childNode(0) as TextNode).text())
            childList.points = parseChildren(node.child(0))
            childList.order = order
            childListArray.add(childList)
        } else {
            childList.isText = true
            childList.text = IdentifiedElement((node.childNode(0) as TextNode).text())
            childList.order = order
            childListArray.add(childList)
        }
    }

    return childListArray
}

const val MARKER_PATTERN = "[a-zäöüß0-9]+(\\.[a-zäöüß0-9]+)*"

fun parseMarkerFromMarkerText(markerText: String?) =
    markerText?.let { MARKER_PATTERN.toRegex().find(markerText)?.value }
