package de.bund.digitalservice.ris.norms.framework.adapter.output.xml

import de.bund.digitalservice.ris.norms.domain.entity.Article
import de.bund.digitalservice.ris.norms.domain.entity.Norm
import de.bund.digitalservice.ris.norms.domain.entity.Paragraph
import de.bund.digitalservice.ris.norms.framework.adapter.output.xml.dto.ArticleDto
import de.bund.digitalservice.ris.norms.framework.adapter.output.xml.dto.NormDto
import de.bund.digitalservice.ris.norms.framework.adapter.output.xml.dto.ParagraphDto

fun normToDto(norm: Norm): NormDto {
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
        articles = articlesToDto(norm.articles)
    )
}

fun articlesToDto(articles: List<Article>): List<ArticleDto> {
    return articles.map { ArticleDto(it.guid, it.title, it.marker, paragraphsToDto(it.paragraphs)) }
}

fun paragraphsToDto(paragraphs: List<Paragraph>): List<ParagraphDto> {
    return paragraphs.map { ParagraphDto(it.guid, it.marker, it.text) }
}
