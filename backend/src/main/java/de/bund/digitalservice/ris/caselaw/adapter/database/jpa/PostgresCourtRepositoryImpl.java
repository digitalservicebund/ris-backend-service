package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import de.bund.digitalservice.ris.caselaw.adapter.transformer.CourtTransformer;
import de.bund.digitalservice.ris.caselaw.domain.court.Court;
import de.bund.digitalservice.ris.caselaw.domain.court.CourtRepository;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Repository;

@Repository
public class PostgresCourtRepositoryImpl implements CourtRepository {

  private final DatabaseCourtRepository repository;

  public PostgresCourtRepositoryImpl(DatabaseCourtRepository repository) {
    this.repository = repository;
  }

  @Override
  public List<Court> findBySearchStr(String searchString) {
    return repository.findBySearchStr(searchString).stream()
        .map(CourtTransformer::transformToDomain)
        .toList();
  }

  @Override
  public Optional<Court> findByTypeAndLocation(String type, String location) {
    if (type == null) {
      return Optional.empty();
    }

    Map<String, List<CourtDTO>> courtMap =
        repository.findAll().stream().collect(Collectors.groupingBy(CourtDTO::getType));

    Optional<Court> court = Optional.empty();
    if (courtMap.containsKey(type)) {
      List<CourtDTO> foundCourts = courtMap.get(type);
      if (foundCourts.size() == 1) {
        court = Optional.of(CourtTransformer.transformToDomain(foundCourts.get(0)));
      } else if (location != null) {
        List<CourtDTO> filteredCourts =
            foundCourts.stream()
                .filter(courtDTO -> courtDTO.getLocation().equals(location))
                .toList();
        if (filteredCourts.size() == 1) {
          court = Optional.of(CourtTransformer.transformToDomain(filteredCourts.get(0)));
        }
      }
    }

    return court;
  }

  @Override
  public Optional<Court> findUniqueBySearchString(String searchString) {
    Map<String, List<CourtDTO>> foundCourts =
        repository.findAll().stream()
            .collect(
                Collectors.groupingBy(
                    courtDTO -> courtDTO.getType() + " " + courtDTO.getLocation()));

    Optional<Court> court = Optional.empty();
    if (foundCourts.containsKey(searchString)) {
      List<CourtDTO> courtDTOS = foundCourts.get(searchString);
      if (courtDTOS.size() == 1) {
        court = Optional.of(CourtTransformer.transformToDomain(courtDTOS.get(0)));
      }
    }

    return court;
  }

  @Override
  public List<Court> findAllByOrderByTypeAscLocationAsc() {
    return repository.findAllByOrderByTypeAscLocationAsc().stream()
        .map(CourtTransformer::transformToDomain)
        .toList();
  }
}
