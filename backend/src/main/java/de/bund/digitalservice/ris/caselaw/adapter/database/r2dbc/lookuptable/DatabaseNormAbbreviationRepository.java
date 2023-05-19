package de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.lookuptable;

import java.util.UUID;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;

public interface DatabaseNormAbbreviationRepository
    extends R2dbcRepository<NormAbbreviationDTO, UUID> {

  @Query("select * from norm_abbreviation limit :size")
  Flux<NormAbbreviationDTO> findAll(int size);

  @Query(
      "select * from norm_abbreviation where abbreviation like :query||'%' limit :size offset :pageOffset")
  Flux<NormAbbreviationDTO> findBySearchQuery(String query, Integer size, Integer pageOffset);
}
