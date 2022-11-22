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
        guid, data.longTitle, articles, data.officialShortTitle,
        data.officialAbbreviation, data.referenceNumber,
        if (data.publicationDate != null) LocalDate.parse(data.publicationDate) else null,
        if (data.announcementDate != null) LocalDate.parse(data.announcementDate) else null,
        if (data.citationDate != null) LocalDate.parse(data.citationDate) else null,
        data.frameKeywords, data.authorEntity,
        data.authorDecidingBody, data.authorIsResolutionMajority,
        data.leadJurisdiction,
        data.leadUnit, data.participationType, data.participationInstitution,
        data.documentTypeName, data.documentNormCategory, data.documentTemplateName,
        data.subjectFna, data.subjectPreviousFna, data.subjectGesta,
        data.subjectBgb3
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
