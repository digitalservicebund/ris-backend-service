package de.bund.digitalservice.ris.norms.framework.adapter.output.database

import de.bund.digitalservice.ris.norms.application.port.output.EditNormOutputPort
import de.bund.digitalservice.ris.norms.application.port.output.GetNormByEliOutputPort
import de.bund.digitalservice.ris.norms.application.port.output.GetNormByGuidOutputPort
import de.bund.digitalservice.ris.norms.application.port.output.SaveFileReferenceOutputPort
import de.bund.digitalservice.ris.norms.application.port.output.SaveNormOutputPort
import de.bund.digitalservice.ris.norms.application.port.output.SearchNormsOutputPort
import de.bund.digitalservice.ris.norms.domain.entity.Article
import de.bund.digitalservice.ris.norms.domain.entity.MetadataSection
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

        val findSectionsWithoutParentRequest = findNormRequest.flatMapMany { metadataSectionsRepository.findByNormIdAndSectionIdIsNull(it.id) }
            .flatMap(::getSectionMetadata)
            .collectList()

        return Mono.zip(
            findNormRequest,
            buildArticlesRequest,
            findFileReferencesRequest,
            findSectionsWithoutParentRequest,
        ).map {
            normToEntity(it.t1, it.t2, it.t3, it.t4)
        }
    }

    @Transactional(transactionManager = "connectionFactoryTransactionManager")
    override fun saveNorm(command: SaveNormOutputPort.Command): Mono<Boolean> {
        val saveNormRequest = normsRepository.save(normToDto(command.norm)).cache()
        val saveArticlesRequest = saveNormRequest.flatMapMany { saveNormArticles(command.norm, it) }
        val saveFileReferencesRequest = saveNormRequest.flatMapMany { saveNormFiles(command.norm, it) }
        val deleteOldMetadataRequest = saveNormRequest.flatMapMany { deleteOldMetadata(it.id) }
        val saveMetadataOfFirstLevelSectionsRequest = saveNormRequest.flatMapMany { saveNormMetadataOfFirstLevelSections(command.norm, it) }
        val saveMetadataOfSecondLevelSectionsRequest = saveNormRequest.flatMapMany { saveNormMetadataOfSecondLevelSections(command.norm, it) }

        return Mono.`when`(saveArticlesRequest, saveFileReferencesRequest, deleteOldMetadataRequest, saveMetadataOfFirstLevelSectionsRequest, saveMetadataOfSecondLevelSectionsRequest).thenReturn(true)
    }

    @Transactional(transactionManager = "connectionFactoryTransactionManager")
    override fun editNorm(command: EditNormOutputPort.Command): Mono<Boolean> {
        val findNormRequest = normsRepository.findByGuid(command.norm.guid).cache()
        val saveNormRequest = findNormRequest
            .map { normDto -> normToDto(command.norm, normDto.id) }
            .flatMap(normsRepository::save).cache()

        val updateMetadataRequest = saveNormRequest.flatMapMany { normDto -> saveNormMetadataOfFirstLevelSections(command.norm, normDto) }

        return Mono.`when`(saveNormRequest, updateMetadataRequest).thenReturn(true)
    }

    @Transactional(transactionManager = "connectionFactoryTransactionManager")
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

    private fun saveNormMetadataOfFirstLevelSections(norm: Norm, normDto: NormDto): Flux<MetadatumDto> = Flux
        .fromIterable(norm.metadataSections.filter { it.sections == null })
        .map { metadataSectionToDto(it, normDto.id) }
        .flatMap(metadataSectionsRepository::save)
        .flatMap { metadataSectionDto ->
            saveSectionMetadata(
                metadataSectionDto,
                norm.metadataSections
                    .filter { it.name == metadataSectionDto.name && it.order == metadataSectionDto.order }
                    .flatMap { it.metadata },
            )
        }

    private fun saveNormMetadataOfSecondLevelSections(norm: Norm, normDto: NormDto): Mono<Boolean> {
        val sections = norm.metadataSections.filter { it.sections != null }
        return metadataSectionsToDto(sections, normDto.id)?.let {
            metadataSectionsRepository
                .saveAll(it)
                .collectList()
                .flatMap { childSections ->
                    childSections.mapIndexed { index, parentSectionDto ->
                        saveChildSections(sections[index], parentSectionDto)
                    }
                    Mono.just(true)
                }
        } ?: Mono.just(false)
    }

    private fun saveChildSections(metadataSection: MetadataSection, parentSectionDto: MetadataSectionDto): Mono<Boolean> = metadataSectionsRepository
        .saveAll(metadataSectionsToDto(metadataSection.sections, parentSectionDto.normId, parentSectionDto.id) ?: listOf())
        .flatMap { saveSectionMetadata(it, metadataSection.metadata) }
        .then(Mono.just(true))

    private fun saveSectionMetadata(metadataSectionDto: MetadataSectionDto, metadata: List<Metadatum<*>>): Flux<MetadatumDto> {
        return metadataRepository.saveAll(metadataListToDto(metadata, metadataSectionDto.id))
    }

    private fun deleteOldMetadata(normId: Int): Mono<Void> = metadataSectionsRepository.findByNormId(normId)
        .flatMap {
            metadataRepository.deleteBySectionId(it.id)
        }.then(metadataSectionsRepository.deleteByNormId(normId))

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

    private fun getSectionMetadata(metadataSectionDto: MetadataSectionDto): Mono<MetadataSection> {
        return metadataRepository.findBySectionId(metadataSectionDto.id)
            .map(::metadatumToEntity)
            .collectList()
            .map { metadata -> metadataSectionToEntity(metadataSectionDto, metadata) }
    }

    private fun getArticleWithParagraphs(articleDto: ArticleDto): Mono<Article> {
        return paragraphsRepository.findByArticleId(articleDto.id)
            .map(::paragraphToEntity)
            .collectList()
            .map { paragraphs -> articleToEntity(articleDto, paragraphs) }
    }
}
