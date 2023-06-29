package de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.lookuptable;

import java.util.UUID;
import org.springframework.data.r2dbc.repository.R2dbcRepository;

public interface DatabaseNormElementRepository extends R2dbcRepository<NormElementDTO, UUID> {}
