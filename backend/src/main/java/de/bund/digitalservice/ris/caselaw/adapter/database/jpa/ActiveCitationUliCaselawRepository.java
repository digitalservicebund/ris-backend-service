package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ActiveCitationUliCaselawRepository
    extends JpaRepository<ActiveCitationUliCaselaw, String> {
  List<ActiveCitationUliCaselaw> findAllByTargetId(UUID targetId);

  List<ActiveCitationUliCaselaw> findAll();
}
