package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import de.bund.digitalservice.ris.caselaw.adapter.DatabaseDocumentUnitConsistencyService;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface DatabaseDocUnitConsistencyRepository
    extends JpaRepository<DocumentationUnitDTO, UUID> {

  @Query(
      nativeQuery = true,
      value =
"""
SELECT documentation_unit.id, documentation_unit.document_number
FROM incremental_migration.documentation_unit documentation_unit
         LEFT JOIN incremental_migration.decision decision ON documentation_unit.id = decision.id
         LEFT JOIN incremental_migration.pending_proceeding pending_proceeding ON documentation_unit.id = pending_proceeding.id
WHERE decision.id IS NULL AND pending_proceeding.id IS NULL
""")
  List<DatabaseDocumentUnitConsistencyService.DocumentationUnitIdentifier>
      findDocUnitsWithoutDecisionOrPendingProceeding();
}
