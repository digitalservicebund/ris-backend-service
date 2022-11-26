package de.bund.digitalservice.ris.norms.framework.adapter.output.database

import de.bund.digitalservice.ris.norms.domain.entity.Article
import de.bund.digitalservice.ris.norms.domain.entity.Norm
import de.bund.digitalservice.ris.norms.domain.entity.Paragraph
import de.bund.digitalservice.ris.norms.framework.adapter.output.database.dto.ArticleDto
import de.bund.digitalservice.ris.norms.framework.adapter.output.database.dto.NormDto
import de.bund.digitalservice.ris.norms.framework.adapter.output.database.dto.ParagraphDto

interface NormsMapper {
    fun normToEntity(normDto: NormDto, articles: List<Article>): Norm {
        return Norm(
            normDto.guid, normDto.longTitle, articles, normDto.officialShortTitle, normDto.officialAbbreviation,
            normDto.referenceNumber, normDto.publicationDate, normDto.announcementDate, normDto.citationDate,
            normDto.frameKeywords, normDto.authorEntity, normDto.authorDecidingBody, normDto.authorIsResolutionMajority,
            normDto.leadJurisdiction, normDto.leadUnit, normDto.participationType, normDto.participationInstitution,
            normDto.documentTypeName, normDto.documentNormCategory, normDto.documentTemplateName,
            normDto.subjectFna, normDto.subjectPreviousFna, normDto.subjectGesta, normDto.subjectBgb3,
            normDto.unofficialTitle, normDto.unofficialShortTitle, normDto.unofficialAbbreviation, normDto.risAbbreviation
        )
    }

    fun paragraphToEntity(paragraphDto: ParagraphDto): Paragraph {
        return Paragraph(paragraphDto.guid, paragraphDto.marker, paragraphDto.text)
    }

    fun articleToEntity(articleDto: ArticleDto, paragraphs: List<Paragraph>): Article {
        return Article(articleDto.guid, articleDto.title, articleDto.marker, paragraphs)
    }

    fun normToDto(norm: Norm, id: Int = 0): NormDto {
        return NormDto(
            id, norm.guid, norm.longTitle, officialShortTitle = norm.officialShortTitle,
            officialAbbreviation = norm.officialAbbreviation, referenceNumber = norm.referenceNumber,
            publicationDate = norm.publicationDate,
            announcementDate = norm.announcementDate,
            citationDate = norm.citationDate,
            frameKeywords = norm.frameKeywords, authorEntity = norm.authorEntity,
            authorDecidingBody = norm.authorDecidingBody, authorIsResolutionMajority = norm.authorIsResolutionMajority,
            leadJurisdiction = norm.leadJurisdiction,
            leadUnit = norm.leadUnit, participationType = norm.participationType, participationInstitution = norm.participationInstitution,
            documentTypeName = norm.documentTypeName, documentNormCategory = norm.documentNormCategory, documentTemplateName = norm.documentTemplateName,
            subjectFna = norm.subjectFna, subjectPreviousFna = norm.subjectPreviousFna, subjectGesta = norm.subjectGesta,
            subjectBgb3 = norm.subjectBgb3, unofficialTitle = norm.unofficialTitle, unofficialShortTitle = norm.unofficialShortTitle,
            unofficialAbbreviation = norm.unofficialAbbreviation, risAbbreviation = norm.risAbbreviation
        )
    }

    fun articlesToDto(articles: List<Article>, normId: Int, id: Int = 0): List<ArticleDto> {
        return articles.map { ArticleDto(id, it.guid, it.title, it.marker, normId) }
    }

    fun paragraphsToDto(paragraphs: List<Paragraph>, articleId: Int, id: Int = 0): List<ParagraphDto> {
        return paragraphs.map { ParagraphDto(id, it.guid, it.marker, it.text, articleId) }
    }
}
