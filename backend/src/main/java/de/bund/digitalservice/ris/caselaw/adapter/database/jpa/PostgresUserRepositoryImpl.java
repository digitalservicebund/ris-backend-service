package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import de.bund.digitalservice.ris.caselaw.adapter.transformer.DocumentationOfficeTransformer;
import de.bund.digitalservice.ris.caselaw.adapter.transformer.UserTransformer;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationOffice;
import de.bund.digitalservice.ris.caselaw.domain.User;
import de.bund.digitalservice.ris.caselaw.domain.UserRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

/** Implementation of the UserRepository for the Postgres database */
@Repository
@Slf4j
@Primary
public class PostgresUserRepositoryImpl implements UserRepository {

  private final DatabaseUserRepository repository;

  public PostgresUserRepositoryImpl(DatabaseUserRepository repository) {

    this.repository = repository;
  }

  @Override
  public Optional<User> getUser(UUID uuid) {
    if (uuid == null) return Optional.empty();
    return repository.findById(uuid).map(UserTransformer::transformToDomain);
  }

  @Override
  public List<User> getAllUsersForDocumentationOffice(DocumentationOffice documentationOffice) {
    return repository
        .findByDocumentationOffice(
            DocumentationOfficeTransformer.transformToDTO(documentationOffice))
        .stream()
        .map(UserTransformer::transformToDomain)
        .toList();
  }

  @Override
  public void saveOrUpdate(List<User> users) {
    users.forEach(this::saveOrUpdate);
  }

  @Override
  public Optional<User> saveOrUpdate(User user) {
    if (user == null) return Optional.empty();

    // TODO make sure to update the user's data (e.g. first name, last name) if they exist
    UserDTO existing = repository.findByExternalId(user.externalId()).get();

    return Optional.of(
        UserTransformer.transformToDomain(repository.save(UserTransformer.transformToDTO(user))));
  }

  @Override
  public Optional<User> findByExternalId(UUID externalId) {
    return repository.findByExternalId(externalId).map(UserTransformer::transformToDomain);
  }
}
