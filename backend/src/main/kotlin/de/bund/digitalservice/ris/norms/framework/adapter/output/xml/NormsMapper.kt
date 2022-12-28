package de.bund.digitalservice.ris.norms.framework.adapter.output.xml

import de.bund.digitalservice.ris.norms.domain.entity.Article
import de.bund.digitalservice.ris.norms.domain.entity.Norm
import de.bund.digitalservice.ris.norms.domain.entity.Paragraph
import de.bund.digitalservice.ris.norms.framework.adapter.output.xml.dto.ArticleDto
import de.bund.digitalservice.ris.norms.framework.adapter.output.xml.dto.NormDto
import de.bund.digitalservice.ris.norms.framework.adapter.output.xml.dto.ParagraphDto

fun mapNormToDto(norm: Norm): NormDto {
    return NormDto(
        guid = norm.guid,
        officialLongTitle = norm.officialLongTitle,
        officialShortTitle = norm.officialShortTitle,
        publicationDate = norm.publicationDate?.toString(),
        documentTypeName = norm.documentTypeName,
        documentNormCategory = norm.documentNormCategory,
        providerDecidingBody = norm.providerDecidingBody,
        participationInstitution = norm.participationInstitution,
        printAnnouncementGazette = norm.printAnnouncementGazette,
        printAnnouncementPage = norm.printAnnouncementPage,
        articles = norm.articles.map { mapArticleToDto(it) }
    )
}

fun mapArticleToDto(article: Article) =
    ArticleDto(
        guid = article.guid.toString(),
        title = article.title,
        marker = article.marker,
        paragraphs = article.paragraphs.map { mapParagraphToDto(it) }
    )

fun mapParagraphToDto(paragraph: Paragraph) =
    ParagraphDto(
        guid = paragraph.guid.toString(),
        marker = paragraph.marker,
        text = paragraph.text
    )
