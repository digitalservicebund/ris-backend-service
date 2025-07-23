package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DatabaseProcessStepRepository extends JpaRepository<ProcessStepDTO, UUID> {}
