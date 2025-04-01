package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import javax.annotation.Nullable;
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

  Optional<IgnoredTextCheckWordDTO> findByDocumentationOfficeAbbreviationAndWord(
      String abbreviation, String word);

  /**
   * @param documentationOfficeIds the documentation office ids to filter by
   * @param documentationUnitId optional documentation unit id to filter by
   * @param words to search for
   * @return a list of all ignored text check word that were found
   */
  @Query(
      "SELECT i FROM IgnoredTextCheckWordDTO i "
          + "WHERE i.documentationOffice.id IN :documentationOfficeIds "
          + "OR (:documentationUnitId IS NULL OR i.documentationUnit.id = :documentationUnitId) "
          + "AND (i.word IN :words)")
  List<IgnoredTextCheckWordDTO> findAllByDocumentationOfficesIdsOrUnitIdsAndWords(
      @Param("documentationOfficeIds") List<UUID> documentationOfficeIds,
      @Nullable @Param("documentationUnitId") UUID documentationUnitId,
      @Param("words") List<String> words);
}
