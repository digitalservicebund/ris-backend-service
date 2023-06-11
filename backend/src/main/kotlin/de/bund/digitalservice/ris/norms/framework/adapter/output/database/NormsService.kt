package de.bund.digitalservice.ris.norms.framework.adapter.output.database

import de.bund.digitalservice.ris.norms.application.port.output.EditNormOutputPort
import de.bund.digitalservice.ris.norms.application.port.output.GetNormByEliOutputPort
import de.bund.digitalservice.ris.norms.application.port.output.GetNormByGuidOutputPort
import de.bund.digitalservice.ris.norms.application.port.output.SaveFileReferenceOutputPort
import de.bund.digitalservice.ris.norms.application.port.output.SaveNormOutputPort
import de.bund.digitalservice.ris.norms.application.port.output.SearchNormsOutputPort
import de.bund.digitalservice.ris.norms.domain.entity.Article
import de.bund.digitalservice.ris.norms.domain.entity.Metadatum
import de.bund.digitalservice.ris.norms.domain.entity.Norm
import de.bund.digitalservice.ris.norms.domain.value.Eli
import de.bund.digitalservice.ris.norms.framework.adapter.output.database.dto.ArticleDto
import de.bund.digitalservice.ris.norms.framework.adapter.output.database.dto.FileReferenceDto
import de.bund.digitalservice.ris.norms.framework.adapter.output.database.dto.MetadataSectionDto
import de.bund.digitalservice.ris.norms.framework.adapter.output.database.dto.MetadatumDto
import de.bund.digitalservice.ris.norms.framework.adapter.output.database.dto.NormDto
import de.bund.digitalservice.ris.norms.framework.adapter.output.database.dto.ParagraphDto
import de.bund.digitalservice.ris.norms.framework.adapter.output.database.repository.ArticlesRepository
import de.bund.digitalservice.ris.norms.framework.adapter.output.database.repository.FileReferenceRepository
import de.bund.digitalservice.ris.norms.framework.adapter.output.database.repository.MetadataRepository
import de.bund.digitalservice.ris.norms.framework.adapter.output.database.repository.MetadataSectionsRepository
import de.bund.digitalservice.ris.norms.framework.adapter.output.database.repository.NormsRepository
import de.bund.digitalservice.ris.norms.framework.adapter.output.database.repository.ParagraphsRepository
import org.springframework.context.annotation.Primary
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.data.r2dbc.dialect.PostgresDialect
import org.springframework.data.relational.core.query.Query
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.*

@Component
@Primary
class NormsService(
    val normsRepository: NormsRepository,
    val articlesRepository: ArticlesRepository,
    val paragraphsRepository: ParagraphsRepository,
    val fileReferenceRepository: FileReferenceRepository,
    val metadataRepository: MetadataRepository,
    val metadataSectionsRepository: MetadataSectionsRepository,
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
        return normsRepository.findNormByEli(Eli.parseGazette(query.gazette), query.year, query.page)
            .flatMap { getNormByGuid(GetNormByGuidOutputPort.Query(UUID.fromString(it))) }
    }

    override fun searchNorms(
        query: SearchNormsOutputPort.Query,
    ): Flux<Norm> {
        val selectQuery = Query.query(criteria.getSearchCriteria(query))
        return template.select(NormDto::class.java)
            .matching(selectQuery)
            .all()
            .flatMap(::getNormWithMetadata)
    }

    override fun getNormByGuid(query: GetNormByGuidOutputPort.Query): Mono<Norm> {
        val findNormRequest = normsRepository.findByGuid(query.guid).cache()
        val buildArticlesRequest = findNormRequest.flatMapMany { articlesRepository.findByNormId(it.id) }
            .flatMap(::getArticleWithParagraphs)
            .collectList()

        val findFileReferencesRequest = findNormRequest.flatMapMany { fileReferenceRepository.findByNormId(it.id) }
            .map(::fileReferenceToEntity)
            .collectList()

        val findSectionsRequest = findNormRequest.flatMapMany { metadataSectionsRepository.findByNormId(it.id) }
            .collectList()

        val findMetadataRequest = findSectionsRequest.flatMapMany { metadataRepository.findBySectionIdIn(it.map { section -> section.id }) }
            .collectList()

        return Mono.zip(
            findNormRequest,
            buildArticlesRequest,
            findFileReferencesRequest,
            findSectionsRequest,
            findMetadataRequest,
        ).map {
            normToEntity(it.t1, it.t2, it.t3, it.t4, it.t5)
        }
    }

    @Transactional(transactionManager = "connectionFactoryTransactionManager")
    override fun saveNorm(command: SaveNormOutputPort.Command): Mono<Boolean> {
        val saveNormRequest = normsRepository.save(normToDto(command.norm)).cache()
        val saveArticlesRequest = saveNormRequest.flatMapMany { saveNormArticles(command.norm, it) }
        val saveFileReferencesRequest = saveNormRequest.flatMapMany { saveNormFiles(command.norm, it) }
        val saveSectionsRequest = saveNormRequest.flatMapMany { saveNormSectionsWithMetadata(command.norm, it) }

        return Mono.`when`(saveArticlesRequest, saveFileReferencesRequest, saveSectionsRequest).thenReturn(true)
    }

    @Transactional(transactionManager = "connectionFactoryTransactionManager")
    override fun editNorm(command: EditNormOutputPort.Command): Mono<Boolean> {
        val findNormRequest = normsRepository.findByGuid(command.norm.guid).cache()
        val saveNormRequest = findNormRequest
            .map { normDto -> normToDto(command.norm, normDto.id) }
            .flatMap(normsRepository::save).cache()

        val updateMetadataRequest = saveNormRequest.flatMapMany { normDto -> saveNormSectionsWithMetadata(command.norm, normDto) }

        return Mono.`when`(saveNormRequest, updateMetadataRequest).thenReturn(true)
    }

    @Transactional(transactionManager = "connectionFactoryTransactionManager")
    override fun saveFileReference(command: SaveFileReferenceOutputPort.Command): Mono<Boolean> {
        return normsRepository
            .findByGuid(command.norm.guid)
            .map { normDto -> fileReferenceToDto(fileReference = command.fileReference, normId = normDto.id, normGuid = normDto.guid) }
            .flatMap(fileReferenceRepository::save)
            .map { true }
    }

    private fun saveNormArticles(norm: Norm, normDto: NormDto): Flux<ParagraphDto> {
        return articlesRepository
            .saveAll(articlesToDto(articles = norm.articles, normId = normDto.id, normGuid = normDto.guid))
            .flatMap { article -> saveArticleParagraphs(norm, article) }
    }

    private fun saveNormSectionsWithMetadata(norm: Norm, normDto: NormDto): Flux<MetadataSectionDto> {
        return deleteOldMetadata(normDto.id)
            .thenMany(Flux.fromIterable(norm.metadataSections))
            .flatMap { metadataSectionsRepository.save(metadataSectionToDto(it, normDto.id)) }
            .flatMap { parentSectionDto ->
                val sectionDomain = norm.metadataSections.first { it.guid == parentSectionDto.guid }
                if (sectionDomain.sections.isNullOrEmpty()) {
                    saveSectionMetadata(
                        parentSectionDto,
                        norm.metadataSections
                            .filter { it.guid == parentSectionDto.guid }
                            .flatMap { it.metadata },
                    ).flatMap { Mono.just(parentSectionDto) }
                } else {
                    saveChildrenSections(norm, parentSectionDto)
                }
            }
    }

    private fun saveChildrenSections(norm: Norm, parentSection: MetadataSectionDto): Flux<MetadataSectionDto> {
        return metadataSectionsRepository.saveAll(
            metadataSectionsToDto(
                norm.metadataSections
                    .find { it.guid == parentSection.guid }
                    ?.sections ?: listOf(),
                parentSection.normId,
                parentSection.guid,
                parentSection.id,
            ),
        ).flatMap { childrenSectionDto ->
            val childrenSectionsDomain = norm.metadataSections
                .filter { !it.sections.isNullOrEmpty() }
                .mapNotNull { it.sections }
                .flatten()
            saveSectionMetadata(
                childrenSectionDto,
                childrenSectionsDomain
                    .filter { it.guid == childrenSectionDto.guid }
                    .flatMap { it.metadata },
            ).flatMap { Mono.just(childrenSectionDto) }
        }
    }

    private fun saveNormFiles(norm: Norm, normDto: NormDto): Flux<FileReferenceDto> {
        return fileReferenceRepository.saveAll(fileReferencesToDto(norm.files, normDto.id, normDto.guid))
    }

    private fun saveSectionMetadata(metadataSectionDto: MetadataSectionDto, metadata: List<Metadatum<*>>): Flux<MetadatumDto> {
        return metadataRepository.saveAll(metadataListToDto(metadata, metadataSectionDto.id, metadataSectionDto.guid))
    }

    private fun deleteOldMetadata(normId: Int): Mono<Void> = metadataSectionsRepository.findByNormId(normId)
        .flatMap {
            metadataRepository.deleteBySectionId(it.id)
        }.then(metadataSectionsRepository.deleteByNormId(normId))

    private fun saveArticleParagraphs(norm: Norm, article: ArticleDto): Flux<ParagraphDto> {
        return paragraphsRepository.saveAll(
            paragraphsToDto(
                paragraphs = norm.articles
                    .find { it.guid == article.guid }
                    ?.paragraphs ?: listOf(),
                articleId = article.id,
                articleGuid = article.guid,
            ),
        )
    }

    private fun getNormWithMetadata(normDto: NormDto): Mono<Norm> {
        val findSectionsRequest = metadataSectionsRepository.findByNormId(normDto.id).collectList()
        val findMetadataRequest = findSectionsRequest.flatMapMany { metadataRepository.findBySectionIdIn(it.map { section -> section.id }) }
            .collectList()

        return Mono.zip(
            findSectionsRequest,
            findMetadataRequest,
        ).map {
            normToEntity(normDto, emptyList(), emptyList(), it.t1, it.t2)
        }
    }

    private fun getArticleWithParagraphs(articleDto: ArticleDto): Mono<Article> {
        return paragraphsRepository.findByArticleId(articleDto.id)
            .map(::paragraphToEntity)
            .collectList()
            .map { paragraphs -> articleToEntity(articleDto, paragraphs) }
    }
}
