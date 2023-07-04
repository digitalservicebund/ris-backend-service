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
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType
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
) : NormsMapper,
    GetNormByGuidOutputPort,
    SaveNormOutputPort,
    EditNormOutputPort,
    SearchNormsOutputPort,
    GetNormByEliOutputPort,
    SaveFileReferenceOutputPort {

    override fun getNormByEli(query: GetNormByEliOutputPort.Query): Mono<Norm> {
        return normsRepository.findNormByEli(Eli.parseGazette(query.gazette), query.year, query.page)
            .flatMap { getNormByGuid(GetNormByGuidOutputPort.Query(UUID.fromString(it))) }
    }

    override fun searchNorms(
        query: SearchNormsOutputPort.Query,
    ): Flux<Norm> {
        if (query.searchTerm.isEmpty()) {
            return normsRepository.findAll().flatMap { getNormByGuid(GetNormByGuidOutputPort.Query(it.guid)) }
        }

        return metadataRepository.findByValueContainsAndTypeIn(
            query.searchTerm,
            listOf(
                MetadatumType.OFFICIAL_LONG_TITLE.name,
                MetadatumType.OFFICIAL_SHORT_TITLE.name,
                MetadatumType.UNOFFICIAL_LONG_TITLE.name,
                MetadatumType.UNOFFICIAL_SHORT_TITLE.name,
            ),
        ).collectList()
            .flatMapMany {
                metadataSectionsRepository.findByGuidIn(it.map { it.sectionGuid })
            }
            .collectList()
            .flatMapMany { Flux.fromIterable(it.map { it.normGuid }.distinct()) }
            .flatMap { getNormByGuid(GetNormByGuidOutputPort.Query(it)) }
    }

    override fun getNormByGuid(query: GetNormByGuidOutputPort.Query): Mono<Norm> {
        val findNormRequest = normsRepository.findByGuid(query.guid).cache()
        val buildArticlesRequest = findNormRequest.flatMapMany { articlesRepository.findByNormGuid(it.guid) }
            .flatMap(::getArticleWithParagraphs)
            .collectList()

        val findFileReferencesRequest = findNormRequest.flatMapMany { fileReferenceRepository.findByNormGuid(it.guid) }
            .map(::fileReferenceToEntity)
            .collectList()

        val findSectionsRequest = findNormRequest.flatMapMany { metadataSectionsRepository.findByNormGuid(it.guid) }
            .collectList()

        val findMetadataRequest = findSectionsRequest.flatMapMany { metadataRepository.findBySectionGuidIn(it.map { section -> section.guid }) }
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
        val normDto = normToDto(command.norm)
        return Mono.`when`(saveNormSectionsWithMetadata(command.norm, normDto)).thenReturn(true)
    }

    @Transactional(transactionManager = "connectionFactoryTransactionManager")
    override fun saveFileReference(command: SaveFileReferenceOutputPort.Command): Mono<Boolean> {
        return normsRepository
            .findByGuid(command.norm.guid)
            .map { normDto -> fileReferenceToDto(fileReference = command.fileReference, normGuid = normDto.guid) }
            .flatMap(fileReferenceRepository::save)
            .map { true }
    }

    private fun saveNormArticles(norm: Norm, normDto: NormDto): Flux<ParagraphDto> {
        return articlesRepository
            .saveAll(articlesToDto(articles = norm.articles, normGuid = normDto.guid))
            .flatMap { article -> saveArticleParagraphs(norm, article) }
    }

    private fun saveNormSectionsWithMetadata(norm: Norm, normDto: NormDto): Flux<MetadataSectionDto> {
        return metadataSectionsRepository.deleteByNormGuid(normDto.guid)
            .thenMany(Flux.fromIterable(norm.metadataSections))
            .flatMap { metadataSectionsRepository.save(metadataSectionToDto(it, normDto.guid)) }
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
                parentSection.guid,
                parentSection.normGuid,
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
        return fileReferenceRepository.saveAll(fileReferencesToDto(norm.files, normDto.guid))
    }

    private fun saveSectionMetadata(metadataSectionDto: MetadataSectionDto, metadata: List<Metadatum<*>>): Flux<MetadatumDto> {
        return metadataRepository.saveAll(metadataListToDto(metadata, metadataSectionDto.guid))
    }

    private fun saveArticleParagraphs(norm: Norm, article: ArticleDto): Flux<ParagraphDto> {
        return paragraphsRepository.saveAll(
            paragraphsToDto(
                paragraphs = norm.articles
                    .find { it.guid == article.guid }
                    ?.paragraphs ?: listOf(),
                articleGuid = article.guid,
            ),
        )
    }

    private fun getArticleWithParagraphs(articleDto: ArticleDto): Mono<Article> {
        return paragraphsRepository.findByArticleGuid(articleDto.guid)
            .map(::paragraphToEntity)
            .collectList()
            .map { paragraphs -> articleToEntity(articleDto, paragraphs) }
    }
}
