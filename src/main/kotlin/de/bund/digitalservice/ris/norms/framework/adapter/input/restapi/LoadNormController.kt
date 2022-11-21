package de.bund.digitalservice.ris.norms.framework.adapter.input.restapi

import de.bund.digitalservice.ris.norms.application.port.input.LoadNormUseCase
import de.bund.digitalservice.ris.norms.domain.entity.Article
import de.bund.digitalservice.ris.norms.domain.entity.Norm
import de.bund.digitalservice.ris.norms.domain.entity.Paragraph
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono
import java.util.UUID

@RestController
@RequestMapping(ApiConfiguration.API_BASE_PATH)
class LoadNormController(private val loadNormService: LoadNormUseCase) {

    @GetMapping(path = ["/{guid}"])
    fun getNormByGuid(@PathVariable guid: String): Mono<ResponseEntity<NormResponseSchema>> {
        val query = LoadNormUseCase.Query(UUID.fromString(guid))

        return loadNormService
            .loadNorm(query)
            .map({ norm -> NormResponseSchema.from(norm) })
            .map({ normResponseSchema -> ResponseEntity.ok(normResponseSchema) })
            .defaultIfEmpty(ResponseEntity.notFound().build<NormResponseSchema>())
            .onErrorReturn(ResponseEntity.internalServerError().build())
    }

    data class NormResponseSchema
    private constructor(
        val guid: String,
        val longTitle: String,
        val articles: List<ArticleResponseSchema>
    ) {
        companion object {
            fun from(norm: Norm): NormResponseSchema {
                val articles = norm.articles.map { ArticleResponseSchema.from(it) }
                return NormResponseSchema(norm.guid.toString(), norm.longTitle, articles)
            }
        }
    }

    data class ArticleResponseSchema
    private constructor(
        val guid: String,
        val title: String,
        val marker: String,
        val paragraphs: List<ParagraphResponseSchema>
    ) {
        companion object {
            fun from(article: Article): ArticleResponseSchema {
                val paragraphs = article.paragraphs.map { ParagraphResponseSchema.from(it) }
                return ArticleResponseSchema(
                    article.guid.toString(),
                    article.title,
                    article.marker,
                    paragraphs
                )
            }
        }
    }

    data class ParagraphResponseSchema
    private constructor(val guid: String, val marker: String, val text: String) {
        companion object {
            fun from(paragraph: Paragraph): ParagraphResponseSchema {
                return ParagraphResponseSchema(paragraph.guid.toString(), paragraph.marker, paragraph.text)
            }
        }
    }
}
