package de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.lookuptable;

import java.util.UUID;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface DatabaseNormAbbreviationRepository
    extends R2dbcRepository<NormAbbreviationDTO, UUID> {

  @Query("select * from norm_abbreviation limit :size")
  Flux<NormAbbreviationDTO> findAll(int size);

  @Query(
      "select * from norm_abbreviation where abbreviation like :query||'%' order by abbreviation limit :size offset :pageOffset")
  Flux<NormAbbreviationDTO> findBySearchQuery(String query, Integer size, Integer pageOffset);

  @Query(
      "select *"
          + " from norm_abbreviation na"
          + " left outer join norm_abbreviation_region nar"
          + " on na.id = nar.norm_abbreviation_id"
          + " left outer join region r"
          + " on nar.region_id = r.id"
          + " where"
          + " to_tsvector('german',"
          + " coalesce(abbreviation, '') || ' ' ||"
          + " coalesce(official_long_title, '') || ' ' ||"
          + " coalesce(official_short_title, '') || ' ' ||"
          + " coalesce(official_letter_abbreviation, ''))"
          + " @@ to_tsquery('german', '' || :tsQuery || '')"
          + " limit :size offset :pageOffset")
  Flux<NormAbbreviationDTO> findByAwesomeSearchQuery(
      String tsQuery, Integer size, Integer pageOffset);

  Mono<NormAbbreviationDTO> findById(UUID normAbbreviationUuid);
}
