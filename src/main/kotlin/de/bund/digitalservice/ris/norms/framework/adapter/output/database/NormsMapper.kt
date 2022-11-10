package de.bund.digitalservice.ris.norms.framework.adapter.output.database

import de.bund.digitalservice.ris.norms.domain.entity.Article
import de.bund.digitalservice.ris.norms.domain.entity.Norm
import de.bund.digitalservice.ris.norms.domain.entity.Paragraph
import de.bund.digitalservice.ris.norms.framework.adapter.output.database.dto.ArticleDto
import de.bund.digitalservice.ris.norms.framework.adapter.output.database.dto.NormDto
import de.bund.digitalservice.ris.norms.framework.adapter.output.database.dto.ParagraphDto

interface NormsMapper {
    fun normToEntity(normDto: NormDto, articles: List<Article>): Norm {
        return Norm(normDto.guid, normDto.longTitle, articles)
    }

    fun paragraphToEntity(paragraphDto: ParagraphDto): Paragraph {
        return Paragraph(paragraphDto.guid, paragraphDto.marker, paragraphDto.text)
    }

    fun articleToEntity(articleDto: ArticleDto, paragraphs: List<Paragraph>): Article {
        return Article(articleDto.guid, articleDto.title, articleDto.marker, paragraphs)
    }

    fun normToDto(norm: Norm): NormDto {
        return NormDto(0, norm.guid, norm.longTitle)
    }

    fun articlesToDto(articles: List<Article>, normId: Int): List<ArticleDto> {
        return articles.map { ArticleDto(0, it.guid, it.marker, it.title, normId) }
    }

    fun paragraphsToDto(paragraphs: List<Paragraph>, articleId: Int): List<ParagraphDto> {
        return paragraphs.map { ParagraphDto(0, it.guid, it.marker, it.text, articleId) }
    }
}
