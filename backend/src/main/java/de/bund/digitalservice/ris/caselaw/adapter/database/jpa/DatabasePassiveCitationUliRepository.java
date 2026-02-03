package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DatabasePassiveCitationUliRepository
    extends JpaRepository<PassiveCitationUliDTO, UUID> {}
