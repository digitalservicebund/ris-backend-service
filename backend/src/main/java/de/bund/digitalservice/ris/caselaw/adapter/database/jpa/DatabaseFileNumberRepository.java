package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DatabaseFileNumberRepository extends JpaRepository<FileNumberDTO, Long> {

  List<FileNumberDTO> findAllByDocumentationUnit(DocumentationUnitDTO documentationUnit);
}
