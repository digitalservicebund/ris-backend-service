package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for accessing {@link IgnoredTextCheckWordDTO} entities in the database.
 * Extends {@link JpaRepository} to provide basic CRUD operations.
 */
@Repository
public interface DatabaseIgnoredTextCheckWordRepository
    extends JpaRepository<IgnoredTextCheckWordDTO, Long> {

  void deleteAllByWordAndDocumentationUnitId(String word, UUID documentationUnitId);

  @Query(
      """
                      SELECT i FROM IgnoredTextCheckWordDTO i
                      WHERE i.word = :word AND (
                          i.jurisId IS NOT NULL OR i.documentationUnitId = :documentationUnitId
                      )
                  """)
  List<IgnoredTextCheckWordDTO> findByWordAndDocumentationUnitIdAndExternal(
      String word, UUID documentationUnitId);
}
