package de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.lookuptable;

import de.bund.digitalservice.ris.caselaw.adapter.transformer.NormAbbreviationTransformer;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.NormAbbreviation;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.NormAbbreviationRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public class PostgresNormAbbreviationRepositoryImpl implements NormAbbreviationRepository {
  private final DatabaseNormAbbreviationRepository repository;
  private final DatabaseDocumentTypeNewRepository documentTypeRepository;
  private final DatabaseNormAbbreviationDocumentTypeRepository
      normAbbreviationDocumentTypeRepository;
  private final DatabaseRegionRepository regionRepository;
  private final DatabaseNormAbbreviationRegionRepository normAbbreviationRegionRepository;
  private final DatabaseDocumentCategoryRepository documentCategoryRepository;

  public PostgresNormAbbreviationRepositoryImpl(
      DatabaseNormAbbreviationRepository repository,
      DatabaseDocumentTypeNewRepository documentTypeRepository,
      DatabaseNormAbbreviationDocumentTypeRepository normAbbreviationDocumentTypeRepository,
      DatabaseRegionRepository regionRepository,
      DatabaseNormAbbreviationRegionRepository normAbbreviationRegionRepository,
      DatabaseDocumentCategoryRepository documentCategoryRepository) {

    this.repository = repository;
    this.documentTypeRepository = documentTypeRepository;
    this.normAbbreviationDocumentTypeRepository = normAbbreviationDocumentTypeRepository;
    this.regionRepository = regionRepository;
    this.normAbbreviationRegionRepository = normAbbreviationRegionRepository;
    this.documentCategoryRepository = documentCategoryRepository;
  }

  @Override
  public Mono<NormAbbreviation> findById(UUID id) {
    return repository
        .findById(id)
        .flatMap(this::injectAdditionalInformation)
        .map(NormAbbreviationTransformer::transformDTO);
  }

  @Override
  public Flux<NormAbbreviation> findBySearchQuery(String query, Integer size, Integer pageOffset) {
    return repository
        .findBySearchQuery(query, size, pageOffset)
        .flatMap(this::injectAdditionalInformation)
        .map(NormAbbreviationTransformer::transformDTO);
  }

  private Mono<NormAbbreviationDTO> injectAdditionalInformation(
      NormAbbreviationDTO normAbbreviationDTO) {
    Mono<List<DocumentTypeNewDTO>> documentTypes =
        normAbbreviationDocumentTypeRepository
            .findAllByNormAbbreviationId(normAbbreviationDTO.getId())
            .flatMap(
                normAbbreviationDocumentTypeDTO ->
                    documentTypeRepository
                        .findById(normAbbreviationDocumentTypeDTO.documentTypeId())
                        .flatMap(this::injectCategoryLabel))
            .collectList();
    Mono<List<RegionDTO>> regions =
        normAbbreviationRegionRepository
            .findAllByNormAbbreviationId(normAbbreviationDTO.getId())
            .flatMap(
                normAbbreviationRegionDTO ->
                    regionRepository.findById(normAbbreviationRegionDTO.regionId()))
            .collectList();

    return Mono.zip(documentTypes, regions)
        .map(
            tuple -> {
              normAbbreviationDTO.setDocumentTypes(tuple.getT1());
              normAbbreviationDTO.setRegions(tuple.getT2());

              return normAbbreviationDTO;
            });
  }

  private Mono<DocumentTypeNewDTO> injectCategoryLabel(DocumentTypeNewDTO documentTypeNewDTO) {
    if (documentTypeNewDTO == null || documentTypeNewDTO.documentCategoryId == null) {
      return Mono.just(documentTypeNewDTO);
    }

    return documentCategoryRepository
        .findById(documentTypeNewDTO.documentCategoryId)
        .map(
            documentCategoryDTO -> {
              documentTypeNewDTO.setCategoryLabel(documentCategoryDTO.label());
              return documentTypeNewDTO;
            });
  }
}
