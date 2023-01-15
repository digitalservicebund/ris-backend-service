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
        articles = norm.articles.sortedBy { it.marker.substring(2).toInt() }.mapIndexed { index, article ->
            mapArticleToDto(article, index)
        }
    )
}

fun mapArticleToDto(article: Article, ordinalNumber: Int = 1): ArticleDto {
    val marker = parseMarkerFromMarkerText(article.marker) ?: "$ordinalNumber"
    var paragraphsToPass = article.paragraphs
    if (article.paragraphs.none { it.marker == null }) {
        paragraphsToPass = article.paragraphs.sortedBy { it.marker!!.substring(1, it.marker!!.length.minus(1)) }
    }

    return ArticleDto(
        guid = article.guid.toString(),
        title = IdentifiedElement(article.title),
        marker = marker,
        markerText = IdentifiedElement(article.marker),
        paragraphs =
        paragraphsToPass.mapIndexed { index, paragraph ->
            mapParagraphToDto(paragraph, marker, index)
        }
    )
}

fun mapParagraphToDto(paragraph: Paragraph, articleMarker: String, ordinalNumber: Int = 1): ParagraphDto {
    val textNoUnknownTags = paragraph.text.replace("<Rec>", "").replace("</Rec>", "")
        .replace("<Citation>", "").replace("</Citation>", "")
        .replace("<KW>", "").replace("</KW>", "")
        .replace("<SUB>", "").replace("</SUB>", "")
        .replace("<NB>", "").replace("</NB>", "")
        .replace("<I>", "").replace("</I>", "")
        .replace("<SUP>", "").replace("</SUP>", "")
        .replace("<BR>", "").replace("</BR>", "")
        .replace(Regex("<FnR[ ]ID=\\\".*\"\\/>"), "") // DIN EN 15940<FnR ID="F816768_02"/>, Ausgabe
        .replace(Regex("<FnR[ ]ID=\\\".*\"><\\/FnR>"), "") // DIN EN 15940<FnR ID="F816768_02"></FnR>, Ausgabe
    val paragraphMarker = parseMarkerFromMarkerText(paragraph.marker) ?: "$ordinalNumber"
    return ParagraphDto(
        guid = paragraph.guid.toString(),
        marker = paragraphMarker,
        markerText = IdentifiedElement(paragraph.marker),
        articleMarker = articleMarker,
        content = if (textNoUnknownTags.contains("<DL")) toContentDto(textNoUnknownTags, paragraphMarker) else ContentDto(isText = true, text = IdentifiedElement(textNoUnknownTags))
    )
}

fun toContentDto(htmlAsString: String, paragraphMarker: String = "0"): ContentDto {
    val contentDto = ContentDto()

    val doc: Document = Jsoup.parse(htmlAsString)

    contentDto.isList = true
    contentDto.intro = IdentifiedElement((doc.body().childNode(0) as TextNode).text())
    contentDto.points.addAll(parseChildren(doc.body().child(0), paragraphMarker))
    contentDto.paragraphMarker = paragraphMarker

    contentDto.points.forEach { p1 ->
        p1.points.forEach { p2 ->
            p2.listMarkerParent = p1.markerClean
            p2.points.forEach { p3 ->
                p3.listMarkerGrandparent = p1.markerClean
                p3.listMarkerParent = p2.markerClean
            }
        }
    }

    return contentDto
}

fun parseChildren(node: Element, paragraphMarker: String, order: Int = 1): MutableList<ContentDto> {
    val childContentDtoList = mutableListOf<ContentDto>()
    val childContentDto = ContentDto(paragraphMarker = paragraphMarker)

    if (node.parentNode()?.nodeName() === "dd") {
        childContentDto.marker = node.parent()?.previousElementSibling()?.text()
        childContentDto.order = order
    }

    if (node.nodeName() === "dl") {
        childContentDto.isList = true
        for (childNumber in 0 until node.childrenSize() step 2) {
            val ddElement = node.child(childNumber + 1)
            childContentDtoList.addAll(parseChildren(ddElement.child(0), paragraphMarker, (childNumber / 2) + 1))
        }
    }
    if (node.nodeName() == "la") {
        if (node.childrenSize() > 0) {
            childContentDto.isList = true
            childContentDto.intro = IdentifiedElement((node.childNode(0) as TextNode).text())
            childContentDto.points = parseChildren(node.child(0), paragraphMarker)
            childContentDto.order = order
            childContentDtoList.add(childContentDto)
        } else {
            childContentDto.isText = true
            childContentDto.text = IdentifiedElement((node.childNode(0) as TextNode).text())
            childContentDto.order = order
            childContentDtoList.add(childContentDto)
        }
    }

    return childContentDtoList
}

const val MARKER_PATTERN = "[a-zäöüß0-9]+(\\.[a-zäöüß0-9]+)*"

fun parseMarkerFromMarkerText(markerText: String?) =
    markerText?.let { MARKER_PATTERN.toRegex().find(markerText)?.value }
