package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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
      "SELECT i FROM IgnoredTextCheckWordDTO i "
          + "WHERE i.documentationUnitId = :documentationUnitId "
          + "OR (i.jurisId IS NOT NULL AND i.word IN :words)")
  List<IgnoredTextCheckWordDTO> findByDocumentationUnitIdOrByGlobalWords(
      @Param("documentationUnitId") UUID documentationUnitId, @Param("words") List<String> words);

  int deleteByWordAndDocumentationUnitIdIsNullAndJurisIdIsNull(String word);
}
