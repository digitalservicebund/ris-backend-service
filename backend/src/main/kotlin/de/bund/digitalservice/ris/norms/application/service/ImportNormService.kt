package de.bund.digitalservice.ris.norms.application.service

import de.bund.digitalservice.ris.norms.application.port.input.ImportNormUseCase
import de.bund.digitalservice.ris.norms.application.port.output.SaveNormOutputPort
import de.bund.digitalservice.ris.norms.domain.entity.Article
import de.bund.digitalservice.ris.norms.domain.entity.Norm
import de.bund.digitalservice.ris.norms.domain.entity.Paragraph
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import java.util.*

@Service
class ImportNormService(private val saveNormPort: SaveNormOutputPort) : ImportNormUseCase {
    override fun importNorm(command: ImportNormUseCase.Command): Mono<UUID> {
        val guid = UUID.randomUUID()
        val norm = createNorm(guid, command.data)
        return saveNormPort.saveNorm(norm).map { guid }
    }
}

private fun createNorm(guid: UUID, data: ImportNormUseCase.NormData): Norm {
    val articles = data.articles.map { createArticle(it) }
    return Norm(
        guid = guid,
        officialLongTitle = data.officialLongTitle,
        articles = articles,
        officialShortTitle = data.officialShortTitle,
        officialAbbreviation = data.officialAbbreviation,
        referenceNumber = data.referenceNumber,
        announcementDate = data.announcementDate,
        citationDate = data.citationDate,
        frameKeywords = data.frameKeywords,
        providerEntity = data.providerEntity,
        providerDecidingBody = data.providerDecidingBody,
        providerIsResolutionMajority = data.providerIsResolutionMajority,
        leadJurisdiction = data.leadJurisdiction,
        leadUnit = data.leadUnit,
        participationType = data.participationType,
        participationInstitution = data.participationInstitution,
        subjectFna = data.subjectFna,
        subjectGesta = data.subjectGesta,
        documentNumber = data.documentNumber,
        documentCategory = data.documentCategory,
        risAbbreviationInternationalLaw = data.risAbbreviationInternationalLaw,
        unofficialReference = data.unofficialReference,
        applicationScopeArea = data.applicationScopeArea,
        applicationScopeStartDate = data.applicationScopeStartDate,
        applicationScopeEndDate = data.applicationScopeEndDate,
        validityRule = data.validityRule,
        celexNumber = data.celexNumber,
        definition = data.definition,
        categorizedReference = data.categorizedReference,
        otherFootnote = data.otherFootnote,
        expirationDate = data.expirationDate,
        entryIntoForceDate = data.entryIntoForceDate,
        unofficialLongTitle = data.unofficialLongTitle,
        unofficialShortTitle = data.unofficialShortTitle,
        unofficialAbbreviation = data.unofficialAbbreviation,
        risAbbreviation = data.risAbbreviation
    )
}

private fun createArticle(data: ImportNormUseCase.ArticleData): Article {
    val guid = UUID.randomUUID()
    val paragraphs = data.paragraphs.map { createParagraph(it) }
    return Article(guid, data.title, data.marker, paragraphs)
}

private fun createParagraph(data: ImportNormUseCase.ParagraphData): Paragraph {
    val guid = UUID.randomUUID()
    return Paragraph(guid, data.marker, data.text)
}
