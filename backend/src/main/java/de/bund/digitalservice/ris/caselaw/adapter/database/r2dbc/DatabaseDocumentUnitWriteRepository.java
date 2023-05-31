package de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DatabaseDocumentUnitWriteRepository
    extends R2dbcRepository<DocumentUnitWriteDTO, Long> {}
