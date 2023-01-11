package de.bund.digitalservice.ris.norms.framework.adapter.output.xml

import de.bund.digitalservice.ris.norms.domain.entity.Article
import de.bund.digitalservice.ris.norms.domain.entity.Norm
import de.bund.digitalservice.ris.norms.domain.entity.Paragraph
import de.bund.digitalservice.ris.norms.framework.adapter.output.xml.dto.ArticleDto
import de.bund.digitalservice.ris.norms.framework.adapter.output.xml.dto.IdentifiedElement
import de.bund.digitalservice.ris.norms.framework.adapter.output.xml.dto.NormDto
import de.bund.digitalservice.ris.norms.framework.adapter.output.xml.dto.ParagraphDto

fun mapNormToDto(norm: Norm): NormDto {
    return NormDto(
        guid = norm.guid.toString(),
        officialLongTitle = IdentifiedElement(norm.officialLongTitle),
        officialShortTitle = IdentifiedElement(norm.officialShortTitle),
        publicationDate = norm.publicationDate?.toString(),
        documentTypeName = getMappedValue(Property.DOCUMENT_TYPE_NAME, norm.documentTypeName ?: ""),
        documentNormCategory = getMappedValue(Property.DOCUMENT_NORM_CATEGORY, norm.documentNormCategory ?: ""),
        providerDecidingBody = getMappedValue(Property.PROVIDER_DECIDING_BODY, norm.providerDecidingBody ?: ""),
        participationInstitution = getMappedValue(Property.PARTICIPATION_INSTITUTION, norm.participationInstitution ?: ""),
        printAnnouncementGazette = norm.printAnnouncementGazette,
        printAnnouncementPage = norm.printAnnouncementPage,
        europeanLegalIdentifier = norm.europeanLegalIdentifier(),
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

fun mapParagraphToDto(paragraph: Paragraph, articleMarker: String, ordinalNumber: Int = 1) =
    ParagraphDto(
        guid = paragraph.guid.toString(),
        marker = parseMarkerFromMarkerText(paragraph.marker) ?: "$ordinalNumber",
        markerText = IdentifiedElement(paragraph.marker),
        articleMarker = articleMarker,
        text = IdentifiedElement(paragraph.text)
    )

const val MARKER_PATTERN = "[a-zäöüß0-9]+(\\.[a-zäöüß0-9]+)*"

fun parseMarkerFromMarkerText(markerText: String?) =
    markerText?.let { MARKER_PATTERN.toRegex().find(markerText)?.value }
