package de.bund.digitalservice.ris.caselaw.domain;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentationOfficeDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.UserDTO;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface UserRepository {

  Optional<UserDTO> getUser(UUID uuid);

  List<UserDTO> getAllUsersForDocumentationOffice(DocumentationOfficeDTO documentationOffice);

  void saveOrUpdate(List<UserDTO> userDTOs);

  Optional<UserDTO> saveOrUpdate(UserDTO userDTOs);

  Optional<UserDTO> findByExternalId(UUID externalId);
}
