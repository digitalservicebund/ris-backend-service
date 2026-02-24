package de.bund.digitalservice.ris.caselaw.adapter.publication.uli;

import de.bund.digitalservice.ris.caselaw.adapter.publication.uli.entities.ActiveCitationUliCaselaw;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UliActiveCitationRefViewRepository
    extends JpaRepository<ActiveCitationUliCaselaw, String> {
  List<ActiveCitationUliCaselaw> findAllByTargetId(UUID targetId);

  List<ActiveCitationUliCaselaw> findAll();
}
