package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface DatabaseDocumentationOfficeRepository
    extends JpaRepository<DocumentationOfficeDTO, UUID> {

  DocumentationOfficeDTO findByAbbreviation(String abbreviation);

  List<DocumentationOfficeDTO> findByAbbreviationStartsWithIgnoreCase(String abbreviation);

  List<DocumentationOfficeDTO> findAllByOrderByAbbreviationAsc();

  @Query(
      value =
          "SELECT ps.* FROM incremental_migration.process_step ps "
              + "JOIN incremental_migration.process_step_documentation_office psdo "
              + "ON ps.id = psdo.process_step_id "
              + "WHERE psdo.documentation_office_id = :documentationOfficeId "
              + "ORDER BY psdo.rank",
      nativeQuery = true)
  List<ProcessStepDTO> findOrderedProcessStepsByDocumentationOfficeId(
      @Param("documentationOfficeId") UUID documentationOfficeId);
}
