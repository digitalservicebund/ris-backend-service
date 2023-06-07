package de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc;

import de.bund.digitalservice.ris.caselaw.domain.DataSource;
import java.time.Instant;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface DatabaseDocumentUnitMetadataRepository
    extends R2dbcRepository<DocumentUnitMetadataDTO, Long> {

  String WHERE =
      "(:courtType IS NULL OR gerichtstyp = :courtType) AND "
          + "(:courtLocation IS NULL OR gerichtssitz = :courtLocation) AND"
          + "(:decisionDate IS NULL OR decision_date = :decisionDate) AND"
          + "(:docUnitIds IS NULL OR id IN (SELECT * FROM UNNEST(:docUnitIds))) AND "
          + "(:docTypeId IS NULL OR document_type_id = :docTypeId) AND "
          + "data_source != 'PROCEEDING_DECISION' ";

  Mono<DocumentUnitMetadataDTO> findByUuid(UUID documentUnitUuid);

  Flux<DocumentUnitMetadataDTO> findAllByDataSource(String dataSource, Pageable pageable);

  @Query(
      "SELECT docunit.*, status.* "
          + "FROM doc_unit AS docunit "
          + "LEFT JOIN document_unit_status AS status ON docunit.uuid = status.document_unit_id  "
          + "LEFT JOIN (  "
          + "  SELECT document_unit_id, MAX(created_at) as maxCreatedAt  "
          + "  FROM document_unit_status  "
          + "  GROUP BY document_unit_id  "
          + ") AS latest_status ON status.document_unit_id = latest_status.document_unit_id   "
          + "AND status.created_at = latest_status.maxCreatedAt  "
          + "WHERE docunit.data_source = :dataSource AND ( "
          + "  docunit.documentation_office_id = :documentationOffice OR "
          + "  status.status = 'PUBLISHED' OR "
          + "  status.status IS NULL"
          + ")")
  Flux<DocumentUnitMetadataDTO> findAllByDataSourceAndDocumentationOfficeId(
      String dataSource, Pageable pageable, UUID documentationOffice);

  @Query(
      "SELECT * FROM doc_unit WHERE "
          + WHERE
          + "ORDER BY decision_date DESC, id DESC "
          + "LIMIT :pageSize OFFSET :offset")
  Flux<DocumentUnitMetadataDTO> findByCourtDateFileNumberAndDocumentType(
      String courtType,
      String courtLocation,
      Instant decisionDate,
      Long[] docUnitIds,
      Long docTypeId,
      Integer pageSize,
      Long offset);

  @Query("SELECT COUNT(*) FROM doc_unit WHERE" + WHERE)
  Mono<Long> countByCourtDateFileNumberAndDocumentType(
      String courtType,
      String courtLocation,
      Instant decisionDate,
      Long[] docUnitIds,
      Long docTypeId);

  Mono<Long> countByDataSourceAndDocumentationOfficeId(
      DataSource dataSource, UUID documentationOfficeId);
}
