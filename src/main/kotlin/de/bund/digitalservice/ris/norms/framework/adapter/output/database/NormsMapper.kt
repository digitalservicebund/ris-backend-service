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
            normDto.guid, articles, normDto.officialLongTitle, normDto.risAbbreviation, normDto.risAbbreviationInternationalLaw,
            normDto.documentNumber, normDto.divergentDocumentNumber, normDto.documentCategory, normDto.frameKeywords,
            normDto.documentTypeName, normDto.documentNormCategory, normDto.documentTemplateName, normDto.providerEntity,
            normDto.providerDecidingBody, normDto.providerIsResolutionMajority, normDto.participationType,
            normDto.participationInstitution, normDto.leadJurisdiction, normDto.leadUnit, normDto.subjectFna,
            normDto.subjectPreviousFna, normDto.subjectGesta, normDto.subjectBgb3,
            normDto.officialShortTitle, normDto.officialAbbreviation, normDto.unofficialLongTitle,
            normDto.unofficialShortTitle, normDto.unofficialAbbreviation,
            normDto.entryIntoForceDate, normDto.entryIntoForceDateState, normDto.principleEntryIntoForceDate,
            normDto.principleEntryIntoForceDateState, normDto.divergentEntryIntoForceDate, normDto.divergentEntryIntoForceDateState,
            normDto.expirationDate, normDto.expirationDateState, normDto.isExpirationDateTemp, normDto.principleExpirationDate,
            normDto.principleExpirationDateState, normDto.divergentExpirationDate, normDto.divergentExpirationDateState,
            normDto.expirationNormCategory, normDto.announcementDate, normDto.publicationDate, normDto.citationDate,
            normDto.printAnnouncementGazette, normDto.printAnnouncementYear, normDto.printAnnouncementNumber,
            normDto.printAnnouncementPage, normDto.printAnnouncementInfo, normDto.printAnnouncementExplanations,
            normDto.digitalAnnouncementMedium, normDto.digitalAccouncementDate, normDto.digitalAnnouncementEdition,
            normDto.digitalAnnouncementYear, normDto.digitalAnnouncementPage, normDto.digitalAnnouncementArea,
            normDto.digitalAnnouncementAreaNumber, normDto.digitalAnnouncementInfo, normDto.digitalAnnouncementExplanations,
            normDto.euAnnouncementGazette, normDto.euAnnouncementYear, normDto.euAnnouncementSeries,
            normDto.euAnnouncementNumber, normDto.euAnnouncementPage, normDto.euAnnouncementInfo,
            normDto.euAnnouncementExplanations, normDto.otherOfficialAnnouncement, normDto.unofficialReference,
            normDto.completeCitation, normDto.statusNote, normDto.statusDescription, normDto.statusDate,
            normDto.statusReference, normDto.repealNote, normDto.repealArticle, normDto.repealDate,
            normDto.repealReferences, normDto.reissueNote, normDto.reissueArticle, normDto.reissueDate,
            normDto.reissueReference, normDto.otherStatusNote, normDto.documentStatusWorkNote, normDto.documentStatusDescription,
            normDto.documentStatusDate, normDto.documentStatusReference, normDto.documentStatusEntryIntoForceDate,
            normDto.documentStatusProof, normDto.documentTextProof, normDto.otherDocumentNote, normDto.applicationScopeArea,
            normDto.applicationScopeStartDate, normDto.applicationScopeEndDate, normDto.categorizedReference,
            normDto.otherFootnote, normDto.validityRule, normDto.digitalEvidenceLink, normDto.digitalEvidenceRelatedData,
            normDto.digitalEvidenceExternalDataNote, normDto.digitalEvidenceAppendix, normDto.referenceNumber,
            normDto.europeanLegalIdentifier, normDto.celexNumber, normDto.ageIndicationStart, normDto.ageIndicationEnd,
            normDto.definition, normDto.ageOfMajorityIndication, normDto.text
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
            id, norm.guid, norm.officialLongTitle, norm.risAbbreviation, norm.risAbbreviationInternationalLaw,
            norm.documentNumber, norm.divergentDocumentNumber, norm.documentCategory, norm.frameKeywords,
            norm.documentTypeName, norm.documentNormCategory, norm.documentTemplateName, norm.providerEntity,
            norm.providerDecidingBody, norm.providerIsResolutionMajority, norm.participationType,
            norm.participationInstitution, norm.leadJurisdiction, norm.leadUnit, norm.subjectFna,
            norm.subjectPreviousFna, norm.subjectGesta, norm.subjectBgb3,
            norm.officialShortTitle, norm.officialAbbreviation, norm.unofficialLongTitle,
            norm.unofficialShortTitle, norm.unofficialAbbreviation,
            norm.entryIntoForceDate, norm.entryIntoForceDateState, norm.principleEntryIntoForceDate,
            norm.principleEntryIntoForceDateState, norm.divergentEntryIntoForceDate, norm.divergentEntryIntoForceDateState,
            norm.expirationDate, norm.expirationDateState, norm.isExpirationDateTemp, norm.principleExpirationDate,
            norm.principleExpirationDateState, norm.divergentExpirationDate, norm.divergentExpirationDateState,
            norm.expirationNormCategory, norm.announcementDate, norm.publicationDate, norm.citationDate,
            norm.printAnnouncementGazette, norm.printAnnouncementYear, norm.printAnnouncementNumber,
            norm.printAnnouncementPage, norm.printAnnouncementInfo, norm.printAnnouncementExplanations,
            norm.digitalAnnouncementMedium, norm.digitalAccouncementDate, norm.digitalAnnouncementEdition,
            norm.digitalAnnouncementYear, norm.digitalAnnouncementPage, norm.digitalAnnouncementArea,
            norm.digitalAnnouncementAreaNumber, norm.digitalAnnouncementInfo, norm.digitalAnnouncementExplanations,
            norm.euAnnouncementGazette, norm.euAnnouncementYear, norm.euAnnouncementSeries,
            norm.euAnnouncementNumber, norm.euAnnouncementPage, norm.euAnnouncementInfo,
            norm.euAnnouncementExplanations, norm.otherOfficialAnnouncement, norm.unofficialReference,
            norm.completeCitation, norm.statusNote, norm.statusDescription, norm.statusDate,
            norm.statusReference, norm.repealNote, norm.repealArticle, norm.repealDate,
            norm.repealReferences, norm.reissueNote, norm.reissueArticle, norm.reissueDate,
            norm.reissueReference, norm.otherStatusNote, norm.documentStatusWorkNote, norm.documentStatusDescription,
            norm.documentStatusDate, norm.documentStatusReference, norm.documentStatusEntryIntoForceDate,
            norm.documentStatusProof, norm.documentTextProof, norm.otherDocumentNote, norm.applicationScopeArea,
            norm.applicationScopeStartDate, norm.applicationScopeEndDate, norm.categorizedReference,
            norm.otherFootnote, norm.validityRule, norm.digitalEvidenceLink, norm.digitalEvidenceRelatedData,
            norm.digitalEvidenceExternalDataNote, norm.digitalEvidenceAppendix, norm.referenceNumber,
            norm.europeanLegalIdentifier, norm.celexNumber, norm.ageIndicationStart, norm.ageIndicationEnd,
            norm.definition, norm.ageOfMajorityIndication, norm.text
        )
    }

    fun articlesToDto(articles: List<Article>, normId: Int, id: Int = 0): List<ArticleDto> {
        return articles.map { ArticleDto(id, it.guid, it.title, it.marker, normId) }
    }

    fun paragraphsToDto(paragraphs: List<Paragraph>, articleId: Int, id: Int = 0): List<ParagraphDto> {
        return paragraphs.map { ParagraphDto(id, it.guid, it.marker, it.text, articleId) }
    }
}
