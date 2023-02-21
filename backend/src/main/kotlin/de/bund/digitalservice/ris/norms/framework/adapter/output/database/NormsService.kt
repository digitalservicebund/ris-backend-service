package de.bund.digitalservice.ris.norms.framework.adapter.output.database

import de.bund.digitalservice.ris.norms.application.port.output.EditNormOutputPort
import de.bund.digitalservice.ris.norms.application.port.output.GetNormByEliOutputPort
import de.bund.digitalservice.ris.norms.application.port.output.GetNormByGuidOutputPort
import de.bund.digitalservice.ris.norms.application.port.output.SaveNormOutputPort
import de.bund.digitalservice.ris.norms.application.port.output.SearchNormsOutputPort
import de.bund.digitalservice.ris.norms.domain.entity.Article
import de.bund.digitalservice.ris.norms.domain.entity.Norm
import de.bund.digitalservice.ris.norms.framework.adapter.output.database.dto.ArticleDto
import de.bund.digitalservice.ris.norms.framework.adapter.output.database.dto.FileReferenceDto
import de.bund.digitalservice.ris.norms.framework.adapter.output.database.dto.NormDto
import de.bund.digitalservice.ris.norms.framework.adapter.output.database.dto.ParagraphDto
import de.bund.digitalservice.ris.norms.framework.adapter.output.database.repository.ArticlesRepository
import de.bund.digitalservice.ris.norms.framework.adapter.output.database.repository.FilesRepository
import de.bund.digitalservice.ris.norms.framework.adapter.output.database.repository.NormsRepository
import de.bund.digitalservice.ris.norms.framework.adapter.output.database.repository.ParagraphsRepository
import org.springframework.context.annotation.Primary
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.data.r2dbc.dialect.PostgresDialect
import org.springframework.data.relational.core.query.Query
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Component
@Primary
class NormsService(
    val normsRepository: NormsRepository,
    val articlesRepository: ArticlesRepository,
    val paragraphsRepository: ParagraphsRepository,
    val filesRepository: FilesRepository,
    client: DatabaseClient,
) : NormsMapper,
    GetNormByGuidOutputPort,
    SaveNormOutputPort,
    EditNormOutputPort,
    SearchNormsOutputPort,
    GetNormByEliOutputPort {

    private val template: R2dbcEntityTemplate = R2dbcEntityTemplate(client, PostgresDialect.INSTANCE)
    private val criteria: NormsCriteriaBuilder = NormsCriteriaBuilder()

    override fun getNormByEli(query: GetNormByEliOutputPort.Query): Mono<Norm> {
        return template.select(NormDto::class.java)
            .matching(Query.query(criteria.getEliCriteria(query.gazette, query.year, query.page)))
            .first()
            .flatMap { normDto: NormDto -> getNormWithArticles(normDto) }
    }

    override fun searchNorms(
        query: SearchNormsOutputPort.Query,
    ): Flux<Norm> {
        return template.select(NormDto::class.java)
            .matching(Query.query(criteria.getSearchCriteria(query)))
            .all()
            .flatMap { normDto: NormDto -> getNormWithArticles(normDto) }
    }

    override fun getNormByGuid(query: GetNormByGuidOutputPort.Query): Mono<Norm> {
        return normsRepository
            .findByGuid(query.guid)
            .flatMap { normDto: NormDto -> getNormWithArticles(normDto) }
    }

    override fun saveNorm(command: SaveNormOutputPort.Command): Mono<Boolean> {
        return normsRepository
            .save(normToDto(command.norm))
            .flatMap { normDto ->
                saveNormFiles(command.norm, normDto).then(
                    saveNormArticles(command.norm, normDto)
                        .then(Mono.just(true)),
                )
            }
    }

    override fun editNorm(command: EditNormOutputPort.Command): Mono<Boolean> {
        return normsRepository
            .findByGuid(command.norm.guid)
            .flatMap { normDto ->
                normsRepository
                    .save(normToDto(command.norm, normDto.id))
                    .flatMap { Mono.just(true) }
            }
    }

    private fun saveNormArticles(norm: Norm, normDto: NormDto): Flux<ParagraphDto> {
        return articlesRepository
            .saveAll(articlesToDto(norm.articles, normDto.id))
            .flatMap { article -> saveArticleParagraphs(norm, article) }
    }

    private fun saveNormFiles(norm: Norm, normDto: NormDto): Flux<FileReferenceDto> {
        return filesRepository
            .saveAll(filesToDto(norm.files, normDto.id))
    }

    private fun saveArticleParagraphs(norm: Norm, article: ArticleDto): Flux<ParagraphDto> {
        return paragraphsRepository.saveAll(
            paragraphsToDto(
                norm.articles
                    .find { it.guid == article.guid }
                    ?.paragraphs ?: listOf(),
                article.id,
            ),
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
