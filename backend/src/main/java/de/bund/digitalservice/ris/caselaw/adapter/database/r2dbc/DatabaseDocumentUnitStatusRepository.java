package de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc;

import de.bund.digitalservice.ris.caselaw.domain.PublicationStatus;
import java.util.UUID;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface DatabaseDocumentUnitStatusRepository
    extends R2dbcRepository<DocumentUnitStatusDTO, UUID> {

  Mono<DocumentUnitStatusDTO> findFirstByDocumentUnitIdOrderByCreatedAtDesc(UUID documentUnitUuid);

  Mono<DocumentUnitStatusDTO> findFirstByDocumentUnitIdAndPublicationStatusOrderByCreatedAtDesc(
      UUID documentUnitUuid, PublicationStatus publicationStatus);
}
