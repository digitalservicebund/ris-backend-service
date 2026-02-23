package de.bund.digitalservice.ris.caselaw.adapter.publication.uli;

import de.bund.digitalservice.ris.caselaw.adapter.publication.uli.entities.UliRefView;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UliRefViewRepository extends JpaRepository<UliRefView, UUID> {
  Optional<UliRefView> findByDocumentNumber(String documentNumber);
}
