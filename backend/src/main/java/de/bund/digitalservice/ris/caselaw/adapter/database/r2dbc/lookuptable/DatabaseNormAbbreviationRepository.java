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
      "select"
          + " id,"
          + " abbreviation,"
          + " decision_date,"
          + " document_id,"
          + " document_number,"
          + " official_letter_abbreviation,"
          + " official_long_title,"
          + " official_short_title,"
          + " source,"
          + " case"
          + "  when lower(abbreviation) like '' || :directInput || '%' then 2"
          + "  when lower(official_letter_abbreviation) like '' || :directInput || '%' then 1"
          + "  else 0"
          + " end +"
          + " ts_rank_cd(weighted_vector, to_tsquery('german', '' || :tsQuery || '')) rank"
          + " from norm_abbreviation_search"
          + " where weighted_vector @@ to_tsquery('german', '' || :tsQuery || '')"
          + " order by rank desc"
          + " limit :size offset :pageOffset")
  Flux<NormAbbreviationDTO> findByAwesomeSearchQuery(
      String directInput, String tsQuery, Integer size, Integer pageOffset);

  Mono<NormAbbreviationDTO> findById(UUID normAbbreviationUuid);
}
