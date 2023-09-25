package de.bund.digitalservice.ris.norms.framework.adapter.output.database

import de.bund.digitalservice.ris.norms.application.port.output.*
import de.bund.digitalservice.ris.norms.domain.entity.*
import de.bund.digitalservice.ris.norms.domain.value.Eli
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType
import de.bund.digitalservice.ris.norms.framework.adapter.output.database.dto.*
import de.bund.digitalservice.ris.norms.framework.adapter.output.database.repository.*
import java.util.*
import java.util.Optional.of
import kotlin.jvm.optionals.getOrNull
import org.slf4j.LoggerFactory
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
    val recitalsRepository: RecitalsRepository,
    val formulaRepository: FormulaRepository,
    val documentSectionRepository: DocumentSectionRepository,
    val articleRepository: ArticleRepository,
    val paragraphRepository: ParagraphRepository,
    val conclusionRepository: ConclusionRepository,
) :
    NormsMapper,
    GetNormByGuidOutputPort,
    SaveNormOutputPort,
    EditNormOutputPort,
    SearchNormsOutputPort,
    GetNormByEliOutputPort,
    SaveFileReferenceOutputPort {

  companion object {
    private val logger = LoggerFactory.getLogger(NormsService::class.java)
  }

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

    val findRecitalsRequest: Mono<Optional<RecitalsDto>> =
        findNormRequest.flatMap { normDto ->
          normDto.recitals?.let { recitalsRepository.findByGuid(it).map { Optional.of(it) } }
              ?: Mono.just(Optional.empty())
        }

    val findFormulaRequest: Mono<Optional<FormulaDto>> =
        findNormRequest.flatMap { normDto ->
          normDto.formula?.let { formulaRepository.findByGuid(it).map { Optional.of(it) } }
              ?: Mono.just(Optional.empty())
        }

    val findConclusionRequest: Mono<Optional<ConclusionDto>> =
        findNormRequest.flatMap { normDto ->
          normDto.conclusion?.let { conclusionRepository.findByGuid(it).map { Optional.of(it) } }
              ?: Mono.just(Optional.empty())
        }

    return Mono.zip(
            findNormRequest,
            findFileReferencesRequest,
            findMetadataSectionsRequest,
            findMetadataRequest,
            findRecitalsRequest,
            findFormulaRequest,
            findDocumentationRequest,
            findConclusionRequest,
        )
        .map {
          normToEntity(
              it.t1,
              it.t2,
              it.t3,
              it.t4,
              it.t5.getOrNull(),
              it.t6.getOrNull(),
              it.t7,
              it.t8.getOrNull())
        }
        .doOnError { exception ->
          logger.error("Could not map norm to entity with query ${query}:", exception)
        }
  }

  @Transactional(transactionManager = "connectionFactoryTransactionManager")
  override fun saveNorm(command: SaveNormOutputPort.Command): Mono<Boolean> {

    val saveRecitalsRequest =
        command.norm.recitals?.let { recitals ->
          recitalsRepository.save(recitalsToDto(recitals)).map(::of)
        }
            ?: Mono.just(Optional.empty())

    val saveFormulaRequest =
        command.norm.formula?.let { formula ->
          formulaRepository.save(formulaToDto(formula)).map(::of)
        }
            ?: Mono.just(Optional.empty())

    val saveConclusionRequest =
        command.norm.conclusion?.let { conclusion ->
          conclusionRepository.save(conclusionToDto(conclusion)).map(::of)
        }
            ?: Mono.just(Optional.empty())

    return Mono.zip(
            saveRecitalsRequest,
            saveFormulaRequest,
            saveConclusionRequest,
        )
        .flatMap {
          val normDto =
              NormDto(
                  guid = command.norm.guid,
                  recitals = it.t1.getOrNull()?.guid,
                  formula = it.t2.getOrNull()?.guid,
                  conclusion = it.t3.getOrNull()?.guid,
                  eGesetzgebung = command.norm.eGesetzgebung,
              )
          normsRepository.save(normDto)
        }
        .flatMap {
          Mono.`when`(
                  saveDocumentation(command.norm.documentation, it.guid),
                  saveNormFiles(command.norm, it),
                  saveNormSectionsWithMetadata(command.norm, it),
              )
              .thenReturn(true)
        }
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
        .thenMany(saveDocumentation(documentSection.documentation, normGuid, documentSection.guid))
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
