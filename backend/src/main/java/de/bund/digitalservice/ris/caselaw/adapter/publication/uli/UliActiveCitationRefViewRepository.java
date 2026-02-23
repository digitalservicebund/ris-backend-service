package de.bund.digitalservice.ris.caselaw.adapter.publication.uli;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.UliActiveCitationRefView;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UliActiveCitationRefViewRepository
    extends JpaRepository<UliActiveCitationRefView, String> {
  List<UliActiveCitationRefView> findAllByTargetId(UUID targetId);

  List<UliActiveCitationRefView> findAll();
}
