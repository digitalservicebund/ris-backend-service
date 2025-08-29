package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DatabaseUserRepository extends JpaRepository<UserDTO, UUID> {

  List<UserDTO> findByDocumentationOffice(DocumentationOfficeDTO documentationOffice);

  UserDTO findByFirstNameAndLastNameAndDocumentationOffice(
      String givenName, String familyName, DocumentationOfficeDTO documentationOfficeDTO);
}
