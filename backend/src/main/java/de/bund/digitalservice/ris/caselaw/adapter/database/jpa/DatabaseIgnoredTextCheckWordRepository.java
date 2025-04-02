package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for accessing {@link IgnoredTextCheckWordDTO} entities in the database.
 * Extends {@link JpaRepository} to provide basic CRUD operations.
 */
@Repository
public interface DatabaseIgnoredTextCheckWordRepository
    extends JpaRepository<IgnoredTextCheckWordDTO, Long> {

  List<IgnoredTextCheckWordDTO> findByWord(String word);
}
