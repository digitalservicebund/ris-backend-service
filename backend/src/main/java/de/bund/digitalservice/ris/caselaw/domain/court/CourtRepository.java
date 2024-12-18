package de.bund.digitalservice.ris.caselaw.domain.court;

import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface CourtRepository {
  List<Court> findBySearchStr(String searchString, Integer size);

  Optional<Court> findByTypeAndLocation(String type, String location);

  Optional<Court> findUniqueBySearchString(String searchString);

  List<Court> findAllByOrderByTypeAscLocationAsc(Integer size);
}
