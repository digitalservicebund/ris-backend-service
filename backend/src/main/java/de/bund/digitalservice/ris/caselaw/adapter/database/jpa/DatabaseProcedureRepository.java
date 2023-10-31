package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface DatabaseProcedureRepository extends JpaRepository<ProcedureDTO, UUID> {

  ProcedureDTO findByLabelAndDocumentationOffice(
      String label, DocumentationOfficeDTO documentationOfficeDTO);

  @Query(
      "SELECT p FROM ProcedureDTO p WHERE (:label IS NULL OR p.label LIKE %:label%) AND p.documentationOffice = :documentationOffice")
  Page<ProcedureDTO> findByLabelContainingAndDocumentationOffice(
      @Param("label") Optional<String> label,
      @Param("documentationOffice") DocumentationOfficeDTO documentationOfficeDTO,
      Pageable pageable);

  void deleteByLabelAndDocumentationOffice(
      String label, DocumentationOfficeDTO documentationOfficeDTO);
}
