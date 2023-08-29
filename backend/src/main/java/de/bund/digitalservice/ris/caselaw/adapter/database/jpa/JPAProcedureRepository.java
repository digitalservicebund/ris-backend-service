package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface JPAProcedureRepository extends JpaRepository<JPAProcedureDTO, UUID> {

  JPAProcedureDTO findByLabelAndDocumentationOfficeOrderByCreatedAtDesc(
      String label, JPADocumentationOfficeDTO documentationOfficeDTO);

  @Query(
      "SELECT p FROM procedure p WHERE (:label IS NULL OR p.label LIKE %:label%) AND p.documentationOffice = :documentationOffice")
  List<JPAProcedureDTO> findByLabelContainingAndDocumentationOffice(
      @Param("label") Optional<String> label,
      @Param("documentationOffice") JPADocumentationOfficeDTO documentationOfficeDTO,
      Pageable pageable);
}
