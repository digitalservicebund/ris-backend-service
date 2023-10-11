package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import de.bund.digitalservice.ris.caselaw.adapter.transformer.NormAbbreviationTransformer;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.NormAbbreviation;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.NormAbbreviationRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public class PostgresNormAbbreviationRepositoryImpl implements NormAbbreviationRepository {

  private final DatabaseNormAbbreviationRepository repository;

  public PostgresNormAbbreviationRepositoryImpl(DatabaseNormAbbreviationRepository repository) {
    this.repository = repository;
  }

  @Override
  public NormAbbreviation findById(UUID id) {
    return NormAbbreviationTransformer.transformDTO(repository.findById(id).orElse(null));
  }

  @Override
  public Flux<NormAbbreviation> getNormAbbreviationsStartingWithExact(
      String query, Integer size, Integer page) {
    var list =
        repository.findByAbbreviationStartsWithOrderByAbbreviation(
            query, PageRequest.of(page, size));
    return Flux.fromIterable(list.stream().map(NormAbbreviationTransformer::transformDTO).toList());
  }

  @Override
  public Mono<List<NormAbbreviation>> findAllContainingOrderByAccuracy(
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

    List<NormAbbreviationDTO> results =
        repository.findByAbbreviationIgnoreCase(directInput, PageRequest.of(page, size));

    if (results.size() < size) {
      var officialLetterAbbreviationExact =
          repository.findByOfficialLetterAbbreviationIgnoreCase(
              directInput, PageRequest.of(page, size));
      officialLetterAbbreviationExact.stream()
          .filter(e -> results.size() < size && !results.contains(e))
          .forEach(results::add);
    }

    if (results.size() < size) {
      var abbreviationStartingWith =
          repository.findByAbbreviationStartsWithIgnoreCase(
              directInput, PageRequest.of(page, size));
      abbreviationStartingWith.stream()
          .filter(e -> results.size() < size && !results.contains(e))
          .forEach(results::add);
    }

    if (results.size() < size) {
      var officialLetterAbbreviationStartingWith =
          repository.findByOfficialLetterAbbreviationStartsWithIgnoreCase(
              directInput, PageRequest.of(page, size));
      officialLetterAbbreviationStartingWith.stream()
          .filter(e -> results.size() < size && !results.contains(e))
          .forEach(results::add);
    }

    if (results.size() < size) {
      var rankWeightedVector =
          repository.findByRankWeightedVector(tsQuery.toString(), size, page * size);
      rankWeightedVector.stream()
          .filter(e -> results.size() < size && !results.contains(e))
          .forEach(results::add);
    }

    return Mono.just(results.stream().map(NormAbbreviationTransformer::transformDTO).toList());
  }

  @Override
  public Mono<Void> refreshMaterializedViews() {
    repository.refreshMaterializedViews();
    return Mono.empty();
  }
}
