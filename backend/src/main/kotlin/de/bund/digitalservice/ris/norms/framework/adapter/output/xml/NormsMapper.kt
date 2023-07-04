package de.bund.digitalservice.ris.norms.framework.adapter.output.xml

import de.bund.digitalservice.ris.norms.domain.entity.Article
import de.bund.digitalservice.ris.norms.domain.entity.Norm
import de.bund.digitalservice.ris.norms.domain.entity.Paragraph
import de.bund.digitalservice.ris.norms.domain.value.MetadataSectionName
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType
import de.bund.digitalservice.ris.norms.framework.adapter.input.restapi.encodeLocalDate
import de.bund.digitalservice.ris.norms.framework.adapter.output.xml.dto.ArticleDto
import de.bund.digitalservice.ris.norms.framework.adapter.output.xml.dto.ContentDto
import de.bund.digitalservice.ris.norms.framework.adapter.output.xml.dto.IdentifiedElement
import de.bund.digitalservice.ris.norms.framework.adapter.output.xml.dto.NormDto
import de.bund.digitalservice.ris.norms.framework.adapter.output.xml.dto.ParagraphDto
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.nodes.TextNode
import java.time.LocalDate

fun mapNormToDto(norm: Norm): NormDto {
    val firstCitationDate = norm.getFirstMetadatum(MetadataSectionName.CITATION_DATE, MetadatumType.DATE)?.let { encodeLocalDate(it.value as LocalDate) }
    val firstCitationYear = norm.getFirstMetadatum(MetadataSectionName.CITATION_DATE, MetadatumType.YEAR)?.let { it.value as String }

    val firstAnnouncementDate = norm.getFirstMetadatum(MetadataSectionName.ANNOUNCEMENT_DATE, MetadatumType.DATE)?.let { encodeLocalDate(it.value as LocalDate) }
    val firstAnnouncementYear = norm.getFirstMetadatum(MetadataSectionName.ANNOUNCEMENT_DATE, MetadatumType.YEAR)?.let { it.value as String }

    val announcementDate = firstAnnouncementDate ?: firstAnnouncementYear

    return NormDto(
        guid = norm.guid.toString(),
        officialLongTitle = IdentifiedElement(norm.getFirstMetadatum(MetadataSectionName.NORM, MetadatumType.OFFICIAL_LONG_TITLE)?.value.toString()),
        officialShortTitle = IdentifiedElement(norm.getFirstMetadatum(MetadataSectionName.NORM, MetadatumType.OFFICIAL_SHORT_TITLE)?.value.toString()),
        announcementDate = announcementDate ?: (firstCitationDate ?: firstCitationYear),
        documentTypeName = getMappedValue(
            Property.DOCUMENT_TYPE_NAME,
            norm.getFirstMetadatum(MetadataSectionName.DOCUMENT_TYPE, MetadatumType.TYPE_NAME)?.value.toString(),
        ),
        documentNormCategory = getMappedValue(
            Property.DOCUMENT_NORM_CATEGORY,
            norm.getFirstMetadatum(MetadataSectionName.DOCUMENT_TYPE, MetadatumType.NORM_CATEGORY)?.value.toString(),
        ),
        providerDecidingBody = getMappedValue(
            Property.PROVIDER_DECIDING_BODY,
            norm.getFirstMetadatum(MetadataSectionName.NORM_PROVIDER, MetadatumType.DECIDING_BODY)?.value.toString(),
        ),
        participationInstitution = getMappedValue(
            Property.PARTICIPATION_INSTITUTION,
            norm.getFirstMetadatum(MetadataSectionName.PARTICIPATION, MetadatumType.PARTICIPATION_INSTITUTION)?.value.toString(),
        ),
        printAnnouncementGazette = norm.eli.gazetteOrMedium,
        printAnnouncementPage = norm.eli.printAnnouncementPage,
        eli = norm.eli.toString(),
        articles = norm.articles.filter { it.marker !in listOf("Eingangsformel", "Schlussformel") }.sortedBy { if (it.marker.contains("§")) it.marker.substring(2).toInt() else it.marker.substring(4).toInt() }
            .mapIndexed { index, article ->
                mapArticleToDto(article, index)
            },
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
        },
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
        content = if (textNoUnknownTags.contains("<DL")) toContentDto(textNoUnknownTags, paragraphMarker) else ContentDto(isText = true, text = IdentifiedElement(textNoUnknownTags)),
    )
}

fun toContentDto(htmlAsString: String, paragraphMarker: String = "0"): ContentDto {
    val contentDto = ContentDto()

    val doc: Document = Jsoup.parse(htmlAsString)

    contentDto.isList = true
    contentDto.intro = IdentifiedElement((doc.body().childNode(0) as TextNode).text())
    contentDto.points.addAll(parseChildren(doc.body().child(0), paragraphMarker))
    contentDto.paragraphMarker = paragraphMarker

    contentDto.points.forEachIndexed { index, p1 ->
        if (p1.marker == "-") {
            p1.markerClean = index.toString()
        }
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
