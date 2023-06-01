package de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc;

import java.util.UUID;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DocumentUnitStatusRepository
    extends R2dbcRepository<DocumentUnitStatusDTO, UUID> {}
