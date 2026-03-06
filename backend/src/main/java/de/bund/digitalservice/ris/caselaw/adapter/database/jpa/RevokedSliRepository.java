package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RevokedSliRepository extends JpaRepository<RevokedSli, UUID> {
  List<RevokedSli> findAllByRevokedAtAfter(Instant lastCheck);
}
