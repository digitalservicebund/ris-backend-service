package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import de.bund.digitalservice.ris.caselaw.domain.UserRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.stereotype.Repository;

/** Implementation of the DocumentationUnitProcessStepRepository for the Postgres database */
@Repository
@Slf4j
@Primary
public class PostgresUserRepositoryImpl implements UserRepository {

  private final DatabaseUserRepository repository;

  public PostgresUserRepositoryImpl(DatabaseUserRepository repository) {

    this.repository = repository;
  }

  @Override
  public Optional<UserDTO> getUser(UUID uuid) {
    return repository.findById(uuid);
  }

  @Override
  public List<UserDTO> getAllUsersForDocumentationOffice(
      DocumentationOfficeDTO documentationOffice) {
    return repository.findByDocumentationOffice(documentationOffice);
  }

  @Override
  public void saveAll(List<UserDTO> userDTOs) {
    try {
      repository.saveAll(userDTOs);
    } catch (JpaSystemException e) {
      if (e.getMessage().contains("name_and_docoffice_unique")) {
        log.debug(
            "Expected exception due to unique constraint on first name, last name, and documentation office",
            e);
      } else throw e;
    }
  }

  @Override
  public UserDTO findByFirstNameAndLastNameAndDocumentationOffice(
      String givenName, String familyName, DocumentationOfficeDTO documentationOfficeDTO) {
    return repository.findByFirstNameAndLastNameAndDocumentationOffice(
        givenName, familyName, documentationOfficeDTO);
  }
}
