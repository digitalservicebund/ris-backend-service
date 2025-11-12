package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.appeal.AppealStatusDTO;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DatabaseAppealStatusRepository extends JpaRepository<AppealStatusDTO, UUID> {}
