package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DatabaseKeywordRepository extends JpaRepository<KeywordDTO, UUID> {

  Optional<KeywordDTO> findByValue(String value);
}
