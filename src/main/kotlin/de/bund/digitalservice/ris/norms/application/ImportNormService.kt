package de.bund.digitalservice.ris.norms.application

import de.bund.digitalservice.ris.norms.application.port.input.ImportNormUseCase
import de.bund.digitalservice.ris.norms.application.port.output.SaveNormOutputPort
import de.bund.digitalservice.ris.norms.domain.entity.Article
import de.bund.digitalservice.ris.norms.domain.entity.Norm
import de.bund.digitalservice.ris.norms.domain.entity.Paragraph
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import java.time.LocalDate
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
        longTitle = data.longTitle,
        articles = articles,
        officialShortTitle = data.officialShortTitle,
        officialAbbreviation = data.officialAbbreviation,
        referenceNumber = data.referenceNumber,
        publicationDate = decodeLocalDate(data.publicationDate),
        announcementDate = decodeLocalDate(data.announcementDate),
        citationDate = decodeLocalDate(data.citationDate),
        frameKeywords = data.frameKeywords,
        authorEntity = data.authorEntity,
        authorDecidingBody = data.authorDecidingBody,
        authorIsResolutionMajority = data.authorIsResolutionMajority,
        leadJurisdiction = data.leadJurisdiction,
        leadUnit = data.leadUnit,
        participationType = data.participationType,
        participationInstitution = data.participationInstitution,
        documentTypeName = data.documentTypeName,
        documentNormCategory = data.documentNormCategory,
        documentTemplateName = data.documentTemplateName,
        subjectFna = data.subjectFna,
        subjectPreviousFna = data.subjectPreviousFna,
        subjectGesta = data.subjectGesta,
        subjectBgb3 = data.subjectBgb3
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

private fun decodeLocalDate(dateString: String?): LocalDate? =
    if (dateString != null) LocalDate.parse(dateString) else null
