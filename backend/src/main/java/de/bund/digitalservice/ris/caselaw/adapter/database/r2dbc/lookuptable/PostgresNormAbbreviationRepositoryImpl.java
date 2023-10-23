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
        .flatMapSequential(this::injectAdditionalInformation)
        .map(NormAbbreviationTransformer::transformDTO);
  }

  @Override
  public Mono<List<NormAbbreviation>> findByAwesomeSearchQuery(
      String query, Integer size, Integer page) {
    String cleanedQuery =
        query
            .trim()
            .replace(",", "")
            .replace(";", "")
            .replace("(", "")
            .replace(")", "")
            .replace("*", "")
            .replace("+", "")
            .replace("-", "")
            .replace(":", "")
            .replace("/", " ");
    String directInput = cleanedQuery.toLowerCase();
    String[] queryBlocks = cleanedQuery.split(" ");
    StringBuilder tsQuery = new StringBuilder();
    for (int i = 0; i < queryBlocks.length; i++) {
      if (queryBlocks[i].isBlank()) continue;

      if (i > 0) {
        tsQuery.append(" & ");
      }

      tsQuery.append(queryBlocks[i]).append(":*");
    }

    return repository
        .findByAwesomeSearchQuery_rank4(directInput, size)
        .collectList()
        .flatMap(
            list -> {
              if (list.size() < size) {
                return repository
                    .findByAwesomeSearchQuery_rank3(directInput, size - list.size())
                    .collectList()
                    .map(
                        rank3list -> {
                          rank3list.stream().filter(e -> !list.contains(e)).forEach(list::add);
                          return list;
                        });
              }
              return Mono.just(list);
            })
        .flatMap(
            list -> {
              if (list.size() < size) {
                return repository
                    .findByAwesomeSearchQuery_rank2(directInput, size - list.size())
                    .collectList()
                    .map(
                        rank2list -> {
                          rank2list.stream().filter(e -> !list.contains(e)).forEach(list::add);
                          return list;
                        });
              }
              return Mono.just(list);
            })
        .flatMap(
            list -> {
              if (list.size() < size) {
                return repository
                    .findByAwesomeSearchQuery_rank1(directInput, size - list.size())
                    .collectList()
                    .map(
                        rank1list -> {
                          rank1list.stream().filter(e -> !list.contains(e)).forEach(list::add);
                          return list;
                        });
              }
              return Mono.just(list);
            })
        .flatMap(
            list -> {
              if (list.size() < size) {
                return repository
                    .findByAwesomeSearchQuery_rankWeightedVector(
                        tsQuery.toString(), size - list.size())
                    .collectList()
                    .map(
                        rankWeightedList -> {
                          rankWeightedList.stream()
                              .filter(e -> !list.contains(e))
                              .forEach(list::add);
                          return list;
                        });
              }
              return Mono.just(list);
            })
        .flatMap(
            list ->
                Flux.fromIterable(list)
                    .flatMapSequential(
                        normAbbreviationDTO ->
                            injectAdditionalInformation(normAbbreviationDTO)
                                .map(NormAbbreviationTransformer::transformDTO))
                    .collectList());
  }

  @Override
  public Mono<Void> refreshMaterializedViews() {
    return repository.refreshMaterializedViews();
  }

  private Mono<NormAbbreviationDTO> injectAdditionalInformation(
      NormAbbreviationDTO normAbbreviationDTO) {
    Mono<List<DocumentTypeNewDTO>> documentTypes =
        normAbbreviationDocumentTypeRepository
            .findAllByNormAbbreviationId(normAbbreviationDTO.getId())
            .flatMapSequential(
                normAbbreviationDocumentTypeDTO ->
                    documentTypeRepository
                        .findById(normAbbreviationDocumentTypeDTO.getDocumentTypeId())
                        .flatMap(this::injectCategoryLabel))
            .collectList();
    Mono<List<RegionDTO>> regions =
        normAbbreviationRegionRepository
            .findAllByNormAbbreviationId(normAbbreviationDTO.getId())
            .flatMapSequential(
                normAbbreviationRegionDTO ->
                    regionRepository.findById(normAbbreviationRegionDTO.getRegionId()))
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
    if (documentTypeNewDTO == null || documentTypeNewDTO.getDocumentCategoryId() == null) {
      return Mono.just(documentTypeNewDTO);
    }

    return documentCategoryRepository
        .findById(documentTypeNewDTO.getDocumentCategoryId())
        .map(
            documentCategoryDTO -> {
              documentTypeNewDTO.setCategoryLabel(documentCategoryDTO.getLabel());
              return documentTypeNewDTO;
            });
  }
}
