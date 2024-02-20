package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DatabaseApiKeyRepository extends JpaRepository<ApiKeyDTO, UUID> {
  Optional<ApiKeyDTO> findByApiKey(String apiKey);

  Optional<ApiKeyDTO> findByUserAccountAndValidUntilAfter(String userAccount, Instant valid);

  Optional<ApiKeyDTO> findFirstByUserAccountOrderByValidUntilDesc(String userAccount);
}
