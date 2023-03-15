package de.bund.digitalservice.ris.norms.framework.adapter.output.database

import de.bund.digitalservice.ris.norms.application.port.output.EditNormOutputPort
import de.bund.digitalservice.ris.norms.application.port.output.GetNormByEliOutputPort
import de.bund.digitalservice.ris.norms.application.port.output.GetNormByGuidOutputPort
import de.bund.digitalservice.ris.norms.application.port.output.SaveFileReferenceOutputPort
import de.bund.digitalservice.ris.norms.application.port.output.SaveNormOutputPort
import de.bund.digitalservice.ris.norms.application.port.output.SearchNormsOutputPort
import de.bund.digitalservice.ris.norms.domain.entity.Article
import de.bund.digitalservice.ris.norms.domain.entity.Norm
import de.bund.digitalservice.ris.norms.framework.adapter.output.database.dto.ArticleDto
import de.bund.digitalservice.ris.norms.framework.adapter.output.database.dto.FileReferenceDto
import de.bund.digitalservice.ris.norms.framework.adapter.output.database.dto.MetadatumDto
import de.bund.digitalservice.ris.norms.framework.adapter.output.database.dto.NormDto
import de.bund.digitalservice.ris.norms.framework.adapter.output.database.dto.ParagraphDto
import de.bund.digitalservice.ris.norms.framework.adapter.output.database.repository.ArticlesRepository
import de.bund.digitalservice.ris.norms.framework.adapter.output.database.repository.FileReferenceRepository
import de.bund.digitalservice.ris.norms.framework.adapter.output.database.repository.MetadataRepository
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
    val fileReferenceRepository: FileReferenceRepository,
    val metadataRepository: MetadataRepository,
    client: DatabaseClient,
) : NormsMapper,
    GetNormByGuidOutputPort,
    SaveNormOutputPort,
    EditNormOutputPort,
    SearchNormsOutputPort,
    GetNormByEliOutputPort,
    SaveFileReferenceOutputPort {

    private val template: R2dbcEntityTemplate = R2dbcEntityTemplate(client, PostgresDialect.INSTANCE)
    private val criteria: NormsCriteriaBuilder = NormsCriteriaBuilder()

    override fun getNormByEli(query: GetNormByEliOutputPort.Query): Mono<Norm> {
        val selectQuery = Query.query(criteria.getEliCriteria(query.gazette, query.year, query.page))
        return template.select(NormDto::class.java)
            .matching(selectQuery)
            .first()
            .flatMap(::getNormWithArticles)
    }

    override fun searchNorms(
        query: SearchNormsOutputPort.Query,
    ): Flux<Norm> {
        val selectQuery = Query.query(criteria.getSearchCriteria(query))
        return template.select(NormDto::class.java)
            .matching(selectQuery)
            .all()
            .flatMap(::getNormWithArticles)
    }

    override fun getNormByGuid(query: GetNormByGuidOutputPort.Query): Mono<Norm> {
        val findNormRequest = normsRepository.findByGuid(query.guid).cache()
        val buildArticlesRequest = findNormRequest.flatMapMany { articlesRepository.findByNormId(it.id) }
            .flatMap(::getArticleWithParagraphs)
            .collectList()

        val findFileReferencesRequest = findNormRequest.flatMapMany { fileReferenceRepository.findByNormId(it.id) }
            .map(::fileReferenceToEntity)
            .collectList()

        val findMetadataRequest = findNormRequest.flatMapMany { metadataRepository.findByNormId(it.id) }
            .map(::metadatumToEntity)
            .collectList()

        return Mono.zip(
            findNormRequest,
            buildArticlesRequest,
            findFileReferencesRequest,
            findMetadataRequest,
        ).map {
            normToEntity(it.t1, it.t2, it.t3, it.t4)
        }
    }

    override fun saveNorm(command: SaveNormOutputPort.Command): Mono<Boolean> {
        val saveNormRequest = normsRepository.save(normToDto(command.norm)).cache()
        val saveArticlesRequest = saveNormRequest.flatMapMany { saveNormArticles(command.norm, it) }
        val saveFileReferencesRequest = saveNormRequest.flatMapMany { saveNormFiles(command.norm, it) }
        val saveMetadataRequest = saveNormRequest.flatMapMany { saveNormMetadata(command.norm, it) }

        return Mono.`when`(saveArticlesRequest, saveFileReferencesRequest, saveMetadataRequest).thenReturn(true)
    }

    override fun editNorm(command: EditNormOutputPort.Command): Mono<Boolean> {
        val findNormRequest = normsRepository.findByGuid(command.norm.guid).cache()
        val saveNormRequest = findNormRequest
            .map { normDto -> normToDto(command.norm, normDto.id) }
            .flatMap(normsRepository::save).cache()

        val updateMetadataRequest = saveNormRequest.flatMapMany { normDto -> saveNormMetadata(command.norm, normDto) }

        return Mono.`when`(saveNormRequest, updateMetadataRequest).thenReturn(true)
    }

    override fun saveFileReference(command: SaveFileReferenceOutputPort.Command): Mono<Boolean> {
        return normsRepository
            .findByGuid(command.norm.guid)
            .map { normDto -> fileReferenceToDto(command.fileReference, normDto.id) }
            .flatMap(fileReferenceRepository::save)
            .map { true }
    }

    private fun saveNormArticles(norm: Norm, normDto: NormDto): Flux<ParagraphDto> {
        return articlesRepository
            .saveAll(articlesToDto(norm.articles, normDto.id))
            .flatMap { article -> saveArticleParagraphs(norm, article) }
    }

    private fun saveNormFiles(norm: Norm, normDto: NormDto): Flux<FileReferenceDto> {
        return fileReferenceRepository.saveAll(fileReferencesToDto(norm.files, normDto.id))
    }

    private fun saveNormMetadata(norm: Norm, normDto: NormDto): Flux<MetadatumDto> {
        return metadataRepository.deleteByNormId(normDto.id)
            .thenMany(Flux.fromIterable(norm.metadata))
            .map { metadatumToDto(it, normDto.id) }
            .flatMap(metadataRepository::save)
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
        return articlesRepository.findByNormId(normDto.id)
            .flatMap(::getArticleWithParagraphs)
            .collectList()
            .map { articles -> normToEntity(normDto, articles, emptyList(), emptyList()) }
    }

    private fun getArticleWithParagraphs(articleDto: ArticleDto): Mono<Article> {
        return paragraphsRepository.findByArticleId(articleDto.id)
            .map(::paragraphToEntity)
            .collectList()
            .map { paragraphs -> articleToEntity(articleDto, paragraphs) }
    }
}
