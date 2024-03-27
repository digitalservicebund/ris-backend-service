package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import de.bund.digitalservice.ris.caselaw.adapter.transformer.NormAbbreviationTransformer;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.NormAbbreviation;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.NormAbbreviationRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

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
  public List<NormAbbreviation> getNormAbbreviationsStartingWithExact(
      String query, Integer size, Integer page) {
    var list =
        repository.findByAbbreviationStartsWithOrderByAbbreviation(
            query, PageRequest.of(page, size));
    return list.stream().map(NormAbbreviationTransformer::transformDTO).toList();
  }

  @Override
  public List<NormAbbreviation> findAllContainingOrderByAccuracy(
      String query, Integer size, Integer page) {

    List<NormAbbreviationDTO> results =
        repository.findByAbbreviationIgnoreCase(query, PageRequest.of(page, size));

    if (results.size() < size) {
      var officialLetterAbbreviationExact =
          repository.findByOfficialLetterAbbreviationIgnoreCase(query, PageRequest.of(page, size));
      officialLetterAbbreviationExact.stream()
          .filter(e -> results.size() < size && !results.contains(e))
          .forEach(results::add);
    }

    if (results.size() < size) {
      var abbreviationStartingWith =
          repository.findByAbbreviationStartsWithIgnoreCase(query, PageRequest.of(page, size));
      abbreviationStartingWith.stream()
          .filter(e -> results.size() < size && !results.contains(e))
          .forEach(results::add);
    }

    if (results.size() < size) {
      var officialLetterAbbreviationStartingWith =
          repository.findByOfficialLetterAbbreviationStartsWithIgnoreCase(
              query, PageRequest.of(page, size));
      officialLetterAbbreviationStartingWith.stream()
          .filter(e -> results.size() < size && !results.contains(e))
          .forEach(results::add);
    }

    if (results.size() < size) {
      var rankWeightedVector = repository.findByRankWeightedVector(query, size, page * size);
      rankWeightedVector.stream()
          .filter(e -> results.size() < size && !results.contains(e))
          .forEach(results::add);
    }

    return results.stream().map(NormAbbreviationTransformer::transformDTO).toList();
  }

  @Override
  public void refreshMaterializedViews() {
    repository.refreshMaterializedViews();
  }
}
