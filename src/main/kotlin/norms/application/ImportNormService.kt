package de.bund.digitalservice.ris.norms.application

import de.bund.digitalservice.ris.norms.application.port.input.ImportNormUseCase
import de.bund.digitalservice.ris.norms.application.port.output.SaveNormOutputPort
import de.bund.digitalservice.ris.norms.domain.entity.Article
import de.bund.digitalservice.ris.norms.domain.entity.Norm
import de.bund.digitalservice.ris.norms.domain.entity.Paragraph
import de.bund.digitalservice.ris.norms.domain.value.Guid
import reactor.core.publisher.Mono

// TODO: Enable once all adapters are available
// @Component
class ImportNormService(private val saveNormPort: SaveNormOutputPort) : ImportNormUseCase {
    override fun importNorm(command: ImportNormUseCase.Command): Mono<Guid> {
        val guid = Guid.generateNew()
        val norm = createNorm(guid, command.data)
        return saveNormPort.saveNorm(norm).map { guid }
    }
}

private fun createNorm(guid: Guid, data: ImportNormUseCase.NormData): Norm {
    val articles = data.articles.map { createArticle(it) }
    return Norm(guid, data.longTitle, articles)
}

private fun createArticle(data: ImportNormUseCase.ArticleData): Article {
    val guid = Guid.generateNew()
    val paragraphs = data.paragraphs.map { createParagraph(it) }
    return Article(guid, data.title, data.marker, paragraphs)
}

private fun createParagraph(data: ImportNormUseCase.ParagraphData): Paragraph {
    val guid = Guid.generateNew()
    return Paragraph(guid, data.marker, data.text)
}
