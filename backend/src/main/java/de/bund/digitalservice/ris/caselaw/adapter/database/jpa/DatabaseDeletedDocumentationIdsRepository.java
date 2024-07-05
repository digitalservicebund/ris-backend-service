package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import jakarta.persistence.LockModeType;
import java.time.Year;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Repository;

@Repository
public interface DatabaseDeletedDocumentationIdsRepository
    extends JpaRepository<DeletedDocumentationUnitDTO, String> {

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  Optional<DeletedDocumentationUnitDTO> findFirstByAbbreviationAndYear(
      String abbreviation, Year year);
}
