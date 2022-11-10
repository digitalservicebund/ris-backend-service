package de.bund.digitalservice.ris.norms.framework.adapter.output.database

import de.bund.digitalservice.ris.norms.application.port.output.GetAllNormsOutputPort
import de.bund.digitalservice.ris.norms.application.port.output.GetNormByGuidOutputPort
import de.bund.digitalservice.ris.norms.application.port.output.SaveNormOutputPort
import de.bund.digitalservice.ris.norms.domain.entity.Article
import de.bund.digitalservice.ris.norms.domain.entity.Norm
import de.bund.digitalservice.ris.norms.framework.adapter.output.database.dto.ArticleDto
import de.bund.digitalservice.ris.norms.framework.adapter.output.database.dto.NormDto
import de.bund.digitalservice.ris.norms.framework.adapter.output.database.dto.ParagraphDto
import de.bund.digitalservice.ris.norms.framework.adapter.output.database.repository.ArticlesRepository
import de.bund.digitalservice.ris.norms.framework.adapter.output.database.repository.NormsRepository
import de.bund.digitalservice.ris.norms.framework.adapter.output.database.repository.ParagraphsRepository
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.UUID

@Component
class NormsService(
    val normsRepository: NormsRepository,
    val articlesRepository: ArticlesRepository,
    val paragraphsRepository: ParagraphsRepository
) : NormsMapper, GetAllNormsOutputPort, GetNormByGuidOutputPort, SaveNormOutputPort {

    override fun getNormByGuid(guid: UUID): Mono<Norm> {
        return normsRepository
            .findByGuid(guid)
            .flatMap { normDto: NormDto -> getNormWithArticles(normDto) }
    }

    override fun getAllNorms(): Flux<Norm> {
        return normsRepository
            .findAll()
            .flatMap { normDto: NormDto -> getNormWithArticles(normDto) }
    }

    override fun saveNorm(norm: Norm): Mono<Boolean> {
        return normsRepository
            .save(normToDto(norm))
            .flatMap { normDto ->
                saveNormArticles(norm, normDto)
                    .then(Mono.just(true))
            }
    }

    private fun saveNormArticles(norm: Norm, normDto: NormDto): Flux<ParagraphDto> {
        return articlesRepository
            .saveAll(articlesToDto(norm.articles, normDto.id))
            .flatMap { article -> saveArticleParagraphs(norm, article) }
    }

    private fun saveArticleParagraphs(norm: Norm, article: ArticleDto): Flux<ParagraphDto> {
        return paragraphsRepository.saveAll(
            paragraphsToDto(
                norm.articles
                    .find { it.guid == article.guid }
                    ?.paragraphs ?: listOf(),
                article.id
            )
        )
    }

    private fun getNormWithArticles(normDto: NormDto): Mono<Norm> {
        return articlesRepository
            .findByNormId(normDto.id)
            .flatMap { articleDto: ArticleDto ->
                getArticleWithParagraphs(articleDto)
            }
            .collectList()
            .flatMap { articles: List<Article> ->
                Mono.just(normToEntity(normDto, articles))
            }
    }

    private fun getArticleWithParagraphs(articleDto: ArticleDto): Mono<Article> {
        return paragraphsRepository
            .findByArticleId(articleDto.id)
            .collectList()
            .map { paragraphs: List<ParagraphDto> ->
                articleToEntity(articleDto, paragraphs.map { paragraphToEntity(it) })
            }
    }
}
