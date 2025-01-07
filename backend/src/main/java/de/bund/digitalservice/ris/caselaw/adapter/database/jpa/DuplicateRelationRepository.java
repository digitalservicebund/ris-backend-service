package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface DuplicateRelationRepository
    extends JpaRepository<DuplicateRelationDTO, DuplicateRelationDTO.DuplicateRelationId> {

  @Query(
      value =
          "SELECT duplicateRelation "
              + "FROM DuplicateRelationDTO duplicateRelation "
              + "WHERE duplicateRelation.id.documentationUnitId1 = :docUnitId "
              + "      OR duplicateRelation.id.documentationUnitId2 = :docUnitId")
  List<DuplicateRelationDTO> findAllByDocUnitId(UUID docUnitId);
}
