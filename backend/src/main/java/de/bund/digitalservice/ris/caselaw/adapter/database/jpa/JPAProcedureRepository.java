package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JPAProcedureRepository extends JpaRepository<JPAProcedureDTO, UUID> {

  JPAProcedureDTO findByNameAndDocumentationOffice(
      String name, JPADocumentationOfficeDTO documentationOfficeDTO);
}
