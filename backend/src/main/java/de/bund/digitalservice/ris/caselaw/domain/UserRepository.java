package de.bund.digitalservice.ris.caselaw.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface UserRepository {

  Optional<User> getUser(UUID uuid);

  long getCount();

  List<User> getAllUsersForDocumentationOffice(DocumentationOffice documentationOffice);

  void saveOrUpdate(List<User> userDTOs);

  Optional<User> saveOrUpdate(User userDTOs);

  Optional<User> findByExternalId(UUID externalId);
}
