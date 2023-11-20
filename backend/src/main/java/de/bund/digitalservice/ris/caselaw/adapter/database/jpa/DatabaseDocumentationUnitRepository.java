package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface DatabaseDocumentationUnitRepository
    extends JpaRepository<DocumentationUnitDTO, UUID> {
  Optional<DocumentationUnitDTO> findByDocumentNumber(String documentNumber);

  Optional<DocumentationUnitMetadataDTO> findMetadataById(UUID documentUnitUuid);

  @Query(
      value =
          """
        SELECT documentationUnit FROM DocumentationUnitDTO documentationUnit
        WHERE (
            (:courtType IS NULL OR documentationUnit.court.type = :courtType)
        AND (:courtLocation IS NULL OR documentationUnit.court.location = :courtLocation)
        AND (:fileNumber is NULL OR EXISTS (
            SELECT 1
            FROM FileNumberDTO fileNumber
            WHERE fileNumber.documentationUnit.id = documentationUnit.id
            AND fileNumber.value = :fileNumber
            )
         )
        AND (cast(:decisionDate as date) IS NULL OR documentationUnit.decisionDate = :decisionDate)
        )
        ORDER BY documentationUnit.decisionDate DESC, documentationUnit.id DESC
        """)
  Page<DocumentationUnitSearchResultDTO> searchByDocumentUnitSearchInput(
      String courtType,
      String courtLocation,
      String fileNumber,
      LocalDate decisionDate,
      Pageable pageable);

  @Query(
      value =
          """
    SELECT documentationUnit FROM DocumentationUnitDTO documentationUnit
    LEFT JOIN documentationUnit.court court
    WHERE (
       (:documentNumberOrFileNumber IS NULL
         OR ( lower(documentationUnit.documentNumber) like lower(concat('%', :documentNumberOrFileNumber,'%'))
            OR EXISTS (
               SELECT 1
               FROM FileNumberDTO fileNumber
               WHERE fileNumber.documentationUnit.id = documentationUnit.id
               AND fileNumber.value = :documentNumberOrFileNumber
            )
            OR EXISTS (
               SELECT 1
               FROM DeviatingFileNumberDTO deviatingFileNumber
               WHERE deviatingFileNumber.documentationUnit.id = documentationUnit.id
               AND deviatingFileNumber.value = :documentNumberOrFileNumber
            )))
       AND (:courtType IS NULL OR court.type = :courtType)
       AND (:courtLocation IS NULL OR court.location = :courtLocation)
       AND (cast(:decisionDate as date) IS NULL
           OR (cast(:decisionDateEnd as date) IS NULL AND documentationUnit.decisionDate = :decisionDate)
           OR (cast(:decisionDateEnd as date) IS NOT NULL AND documentationUnit.decisionDate BETWEEN :decisionDate AND :decisionDateEnd))
       AND (:myDocOfficeOnly = FALSE OR (:myDocOfficeOnly = TRUE AND
    documentationUnit.documentationOffice.id = :documentationOfficeId))
       AND
         (
            (:status IS NULL AND ((documentationUnit.documentationOffice.id = :documentationOfficeId OR EXISTS (SELECT 1 FROM StatusDTO status WHERE status.documentationUnitDTO.id = documentationUnit.id AND status.publicationStatus IN (cast(de.bund.digitalservice.ris.caselaw.domain.PublicationStatus.PUBLISHED as string), cast(de.bund.digitalservice.ris.caselaw.domain.PublicationStatus.JURIS_PUBLISHED as string), cast(de.bund.digitalservice.ris.caselaw.domain.PublicationStatus.PUBLISHING as string))))))
         OR
            (:status IS NOT NULL AND EXISTS (SELECT 1 FROM StatusDTO status WHERE status.documentationUnitDTO.id = documentationUnit.id AND ((cast(status.publicationStatus as string) = :status) OR status.publicationStatus = cast(de.bund.digitalservice.ris.caselaw.domain.PublicationStatus.JURIS_PUBLISHED as string) AND :status = '1')))
         )
       AND (:withErrorOnly = FALSE OR documentationUnit.documentationOffice.id = :documentationOfficeId AND EXISTS (SELECT 1 FROM StatusDTO status WHERE status.documentationUnitDTO.id = documentationUnit.id AND status.withError = TRUE))
    )
    ORDER BY documentationUnit.documentNumber
""")
  Page<DocumentationUnitSearchResultDTO> searchByDocumentUnitSearchInput(
      UUID documentationOfficeId,
      String documentNumberOrFileNumber,
      String courtType,
      String courtLocation,
      LocalDate decisionDate,
      LocalDate decisionDateEnd,
      String status,
      Boolean withErrorOnly,
      Boolean myDocOfficeOnly,
      Pageable pageable);
}
