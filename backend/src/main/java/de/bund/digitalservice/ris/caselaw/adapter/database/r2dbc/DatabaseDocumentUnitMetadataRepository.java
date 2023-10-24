package de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc;

import java.time.LocalDate;
import java.util.UUID;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
@SuppressWarnings("java:S1192")
public interface DatabaseDocumentUnitMetadataRepository
    extends R2dbcRepository<DocumentUnitMetadataDTO, Long> {

  String LINKEDDOC_QUERY =
      "LEFT JOIN ( "
          + "    SELECT DISTINCT ON (document_unit_id) document_unit_id, publication_status "
          + "    FROM public.status "
          + "    ORDER BY document_unit_id, created_at DESC "
          + ") status ON uuid = status.document_unit_id "
          + "WHERE "
          + "(:courtType IS NULL OR gerichtstyp = :courtType) AND "
          + "(:courtLocation IS NULL OR gerichtssitz = :courtLocation) AND"
          + "(:decisionDate IS NULL OR decision_date = :decisionDate) AND"
          + "(:docUnitIds IS NULL OR id = ANY(:docUnitIds)) AND "
          + "(:docTypeId IS NULL OR document_type_id = :docTypeId) AND "
          + "status.publication_status IN ('PUBLISHED', 'PUBLISHING', 'JURIS_PUBLISHED') AND "
          + "data_source in ('NEURIS', 'MIGRATION') ";

  Mono<DocumentUnitMetadataDTO> findByUuid(UUID documentUnitUuid);

  @Query(
      "SELECT * FROM doc_unit "
          + LINKEDDOC_QUERY
          + "ORDER BY decision_date DESC, id DESC "
          + "LIMIT :pageSize OFFSET :offset")
  Flux<DocumentUnitMetadataDTO> searchByLinkedDocumentationUnit(
      String courtType,
      String courtLocation,
      LocalDate decisionDate,
      Long[] docUnitIds,
      UUID docTypeId,
      Integer pageSize,
      Long offset);

  @Query("SELECT COUNT(*) FROM doc_unit " + LINKEDDOC_QUERY)
  Mono<Long> countSearchByLinkedDocumentationUnit(
      String courtType,
      String courtLocation,
      LocalDate decisionDate,
      Long[] docUnitIds,
      UUID docTypeId);
}
