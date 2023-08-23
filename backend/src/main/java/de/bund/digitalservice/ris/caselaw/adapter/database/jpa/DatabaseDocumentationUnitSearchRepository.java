package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
@SuppressWarnings("java:S1192")
public interface DatabaseDocumentationUnitSearchRepository
    extends JpaRepository<DocumentationUnitSearchEntryDTO, UUID> {}
