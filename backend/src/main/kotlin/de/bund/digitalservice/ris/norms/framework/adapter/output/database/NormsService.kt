package de.bund.digitalservice.ris.norms.framework.adapter.output.database

import de.bund.digitalservice.ris.norms.application.port.output.EditNormOutputPort
import de.bund.digitalservice.ris.norms.application.port.output.GetNormByEliOutputPort
import de.bund.digitalservice.ris.norms.application.port.output.GetNormByGuidOutputPort
import de.bund.digitalservice.ris.norms.application.port.output.SaveFileReferenceOutputPort
import de.bund.digitalservice.ris.norms.application.port.output.SaveNormOutputPort
import de.bund.digitalservice.ris.norms.application.port.output.SearchNormsOutputPort
import de.bund.digitalservice.ris.norms.domain.entity.Article
import de.bund.digitalservice.ris.norms.domain.entity.DocumentSection
import de.bund.digitalservice.ris.norms.domain.entity.Documentation
import de.bund.digitalservice.ris.norms.domain.entity.Metadatum
import de.bund.digitalservice.ris.norms.domain.entity.Norm
import de.bund.digitalservice.ris.norms.domain.value.Eli
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType
import de.bund.digitalservice.ris.norms.framework.adapter.output.database.dto.FileReferenceDto
import de.bund.digitalservice.ris.norms.framework.adapter.output.database.dto.MetadataSectionDto
import de.bund.digitalservice.ris.norms.framework.adapter.output.database.dto.MetadatumDto
import de.bund.digitalservice.ris.norms.framework.adapter.output.database.dto.NormDto
import de.bund.digitalservice.ris.norms.framework.adapter.output.database.repository.ArticleRepository
import de.bund.digitalservice.ris.norms.framework.adapter.output.database.repository.DocumentSectionRepository
import de.bund.digitalservice.ris.norms.framework.adapter.output.database.repository.FileReferenceRepository
import de.bund.digitalservice.ris.norms.framework.adapter.output.database.repository.MetadataRepository
import de.bund.digitalservice.ris.norms.framework.adapter.output.database.repository.MetadataSectionsRepository
import de.bund.digitalservice.ris.norms.framework.adapter.output.database.repository.NormsRepository
import de.bund.digitalservice.ris.norms.framework.adapter.output.database.repository.ParagraphRepository
import java.util.UUID
import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Component
@Primary
class NormsService(
    val normsRepository: NormsRepository,
    val fileReferenceRepository: FileReferenceRepository,
    val metadataRepository: MetadataRepository,
    val metadataSectionsRepository: MetadataSectionsRepository,
    val documentSectionRepository: DocumentSectionRepository,
    val articleRepository: ArticleRepository,
    val paragraphRepository: ParagraphRepository,
) :
    NormsMapper,
    GetNormByGuidOutputPort,
    SaveNormOutputPort,
    EditNormOutputPort,
    SearchNormsOutputPort,
    GetNormByEliOutputPort,
    SaveFileReferenceOutputPort {

  override fun getNormByEli(query: GetNormByEliOutputPort.Query): Mono<Norm> {
    return normsRepository
        .findNormByEli(Eli.parseGazette(query.gazette), query.year, query.page)
        .flatMap { getNormByGuid(GetNormByGuidOutputPort.Query(UUID.fromString(it))) }
  }

  override fun searchNorms(
      query: SearchNormsOutputPort.Query,
  ): Flux<Norm> {
    if (query.searchTerm.isEmpty()) {
      return normsRepository
          .findAll()
          .filter { it.eGesetzgebung == query.eGesetzgebung }
          .flatMap { getNormByGuid(GetNormByGuidOutputPort.Query(it.guid)) }
    }

    return metadataRepository
        .findByValueContainsAndTypeIn(
            query.searchTerm,
            listOf(
                MetadatumType.OFFICIAL_LONG_TITLE.name,
                MetadatumType.OFFICIAL_SHORT_TITLE.name,
                MetadatumType.UNOFFICIAL_LONG_TITLE.name,
                MetadatumType.UNOFFICIAL_SHORT_TITLE.name,
            ),
        )
        .collectList()
        .flatMapMany { metadataSectionsRepository.findByGuidIn(it.map { it.sectionGuid }) }
        .collectList()
        .flatMapMany { Flux.fromIterable(it.map { it.normGuid }.distinct()) }
        .flatMap { getNormByGuid(GetNormByGuidOutputPort.Query(it)) }
  }

  override fun getNormByGuid(query: GetNormByGuidOutputPort.Query): Mono<Norm> {
    val findNormRequest = normsRepository.findByGuid(query.guid).cache()

    val findFileReferencesRequest =
        findNormRequest
            .flatMapMany { fileReferenceRepository.findByNormGuid(it.guid) }
            .map(::fileReferenceToEntity)
            .collectList()

    val findMetadataSectionsRequest =
        findNormRequest
            .flatMapMany { metadataSectionsRepository.findByNormGuid(it.guid) }
            .collectList()

    val findMetadataRequest =
        findMetadataSectionsRequest
            .flatMapMany {
              metadataRepository.findBySectionGuidIn(it.map { section -> section.guid })
            }
            .collectList()

    val findDocumentationRequest =
        findNormRequest.flatMapMany { findDocumentation(it.guid, null) }.collectList()

    return Mono.zip(
            findNormRequest,
            findFileReferencesRequest,
            findMetadataSectionsRequest,
            findMetadataRequest,
            findDocumentationRequest,
        )
        .map { normToEntity(it.t1, it.t2, it.t3, it.t4, it.t5) }
  }

  @Transactional(transactionManager = "connectionFactoryTransactionManager")
  override fun saveNorm(command: SaveNormOutputPort.Command): Mono<Boolean> {
    val saveNormRequest = normsRepository.save(normToDto(command.norm)).cache()
    val saveDocumentationRequest =
        saveNormRequest.flatMapMany { saveDocumentation(command.norm.documentation, it.guid) }
    val saveFileReferencesRequest = saveNormRequest.flatMapMany { saveNormFiles(command.norm, it) }
    val saveMetadataSectionsRequest =
        saveNormRequest.flatMapMany { saveNormSectionsWithMetadata(command.norm, it) }

    return Mono.`when`(
            saveDocumentationRequest, saveFileReferencesRequest, saveMetadataSectionsRequest)
        .thenReturn(true)
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
        .map { normDto ->
          fileReferenceToDto(fileReference = command.fileReference, normGuid = normDto.guid)
        }
        .flatMap(fileReferenceRepository::save)
        .map { true }
  }

  // TODO: Maybe use `expand` and then save all?
  private fun saveDocumentation(
      documentation: Collection<Documentation>,
      normGuid: UUID,
      parentSectionGuid: UUID? = null,
  ): Flux<Boolean> {
    return Flux.fromIterable(documentation).flatMap {
      when (it) {
        is DocumentSection -> saveDocumentSection(it, normGuid, parentSectionGuid)
        is Article -> saveArticle(it, normGuid, parentSectionGuid)
      }
    }
  }

  private fun saveDocumentSection(
      documentSection: DocumentSection,
      normGuid: UUID,
      parentSectionGuid: UUID? = null
  ): Flux<Boolean> {
    return documentSectionRepository
        .save(documentSectionToDto(documentSection, normGuid, parentSectionGuid))
        .flatMapMany { saveDocumentation(documentSection.documentation, normGuid, it.guid) }
  }

  private fun saveArticle(
      article: Article,
      normGuid: UUID,
      documentSectionGuid: UUID? = null
  ): Flux<Boolean> {
    return articleRepository
        .save(articleToDto(article, normGuid, documentSectionGuid))
        .flatMapMany { articleDto ->
          paragraphRepository.saveAll(
              article.paragraphs.map { paragraphToDto(it, articleDto.guid) })
        }
        .map { true }
  }

  private fun saveNormSectionsWithMetadata(norm: Norm, normDto: NormDto): Flux<MetadataSectionDto> {
    return metadataSectionsRepository
        .deleteByNormGuid(normDto.guid)
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
                )
                .flatMap { Mono.just(parentSectionDto) }
          } else {
            saveChildrenSections(norm, parentSectionDto)
          }
        }
  }

  private fun saveChildrenSections(
      norm: Norm,
      parentSection: MetadataSectionDto
  ): Flux<MetadataSectionDto> {
    return metadataSectionsRepository
        .saveAll(
            metadataSectionsToDto(
                norm.metadataSections.find { it.guid == parentSection.guid }?.sections ?: listOf(),
                parentSection.guid,
                parentSection.normGuid,
            ),
        )
        .flatMap { childrenSectionDto ->
          val childrenSectionsDomain =
              norm.metadataSections
                  .filter { !it.sections.isNullOrEmpty() }
                  .mapNotNull { it.sections }
                  .flatten()
          saveSectionMetadata(
                  childrenSectionDto,
                  childrenSectionsDomain
                      .filter { it.guid == childrenSectionDto.guid }
                      .flatMap { it.metadata },
              )
              .flatMap { Mono.just(childrenSectionDto) }
        }
  }

  private fun saveNormFiles(norm: Norm, normDto: NormDto): Flux<FileReferenceDto> {
    return fileReferenceRepository.saveAll(fileReferencesToDto(norm.files, normDto.guid))
  }

  private fun saveSectionMetadata(
      metadataSectionDto: MetadataSectionDto,
      metadata: List<Metadatum<*>>
  ): Flux<MetadatumDto> {
    return metadataRepository.saveAll(metadataListToDto(metadata, metadataSectionDto.guid))
  }

  private fun findDocumentation(normGuid: UUID, parentSectionGuid: UUID?): Flux<Documentation> {
    val articles =
        articleRepository
            .findByNormGuidAndDocumentSectionGuid(normGuid, parentSectionGuid)
            .flatMap { articleDto ->
              paragraphRepository
                  .findByArticleGuid(articleDto.guid)
                  .map(::paragraphToEntity)
                  .collectList()
                  .map { articleToEntity(articleDto, it) }
            }

    val documentSections =
        documentSectionRepository
            .findByNormGuidAndParentSectionGuid(normGuid, parentSectionGuid)
            .flatMap { sectionDto ->
              findDocumentation(normGuid, sectionDto.guid).collectList().map {
                documentSectionToEntity(sectionDto, it)
              }
            }

    return Flux.merge(articles, documentSections)
  }
}
