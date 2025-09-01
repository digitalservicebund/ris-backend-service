package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import de.bund.digitalservice.ris.caselaw.domain.UserRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
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
  public void saveOrUpdate(List<UserDTO> userDTOs) {
    userDTOs.forEach(this::saveOrUpdate);
  }

  @Override
  public Optional<UserDTO> saveOrUpdate(UserDTO user) {
    // make sure to update the user's data (e.g. first name, last name) if they exist
    repository
        .findByExternalId(user.getExternalId())
        .ifPresent(userDTO -> user.setId(userDTO.getId()));
    return Optional.of(repository.save(user));
  }

  @Override
  public Optional<UserDTO> findByExternalId(UUID externalId) {
    return repository.findByExternalId(externalId);
  }
}
