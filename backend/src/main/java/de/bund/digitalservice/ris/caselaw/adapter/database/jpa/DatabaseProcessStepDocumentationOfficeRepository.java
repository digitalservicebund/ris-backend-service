package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import io.lettuce.core.dynamic.annotation.Param;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface DatabaseProcessStepDocumentationOfficeRepository
    extends JpaRepository<ProcessStepDocumentationOfficeDTO, UUID> {

  @Query(
      "SELECT psdo FROM ProcessStepDocumentationOfficeDTO psdo "
          + "WHERE psdo.documentationOfficeId = :documentationOfficeId "
          + "AND psdo.rank = ("
          + "    SELECT currentStep.rank + 1 FROM ProcessStepDocumentationOfficeDTO currentStep "
          + "    WHERE currentStep.processStepId = :processStepId "
          + "    AND currentStep.documentationOfficeId = :documentationOfficeId"
          + ")")
  Optional<ProcessStepDocumentationOfficeDTO> findNextByProcessStepIdAndDocumentationOfficeId(
      @Param("processStepId") UUID processStepId,
      @Param("documentationOfficeId") UUID documentationOfficeId);

  List<ProcessStepDocumentationOfficeDTO> findByDocumentationOfficeIdOrderByRankAsc(
      UUID docOfficeId);
}
