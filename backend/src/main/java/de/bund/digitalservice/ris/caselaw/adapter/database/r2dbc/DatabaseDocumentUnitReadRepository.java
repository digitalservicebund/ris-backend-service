package de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc;

import java.util.UUID;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface DatabaseDocumentUnitReadRepository
    extends R2dbcRepository<DocumentUnitReadDTO, Long> {
  @Query("SELECT * FROM document_unit_with_latest_status WHERE documentnumber = $1")
  Mono<DocumentUnitReadDTO> findByDocumentnumber(String documentnumber);

  @Query("SELECT * FROM document_unit_with_latest_status WHERE uuid = $1")
  Mono<DocumentUnitReadDTO> findByUuid(UUID uuid);
}
