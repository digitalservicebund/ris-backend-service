package de.bund.digitalservice.ris.norms.framework.adapter.output.xml

import de.bund.digitalservice.ris.norms.domain.entity.Article
import de.bund.digitalservice.ris.norms.domain.entity.Norm
import de.bund.digitalservice.ris.norms.domain.entity.Paragraph
import de.bund.digitalservice.ris.norms.framework.adapter.output.xml.dto.ArticleDto
import de.bund.digitalservice.ris.norms.framework.adapter.output.xml.dto.NormDto
import de.bund.digitalservice.ris.norms.framework.adapter.output.xml.dto.ParagraphDto

fun normToDto(norm: Norm): NormDto {
    return NormDto(
        norm.guid,
        norm.officialLongTitle,
        norm.officialShortTitle,
        norm.publicationDate?.toString(),
        norm.documentTypeName,
        norm.documentNormCategory,
        norm.providerDecidingBody,
        norm.participationInstitution,
        norm.printAnnouncementGazette,
        norm.printAnnouncementPage,
        articlesToDto(norm.articles)
    )
}

fun articlesToDto(articles: List<Article>): List<ArticleDto> {
    return articles.map { ArticleDto(it.guid, it.title, it.marker, paragraphsToDto(it.paragraphs)) }
}

fun paragraphsToDto(paragraphs: List<Paragraph>): List<ParagraphDto> {
    return paragraphs.map { ParagraphDto(it.guid, it.marker, it.text) }
}
