package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface DatabaseFieldOfLawNormRepository extends JpaRepository<FieldOfLawNormDTO, UUID> {

  @Query(
      "SELECT foln FROM FieldOfLawNormDTO foln "
          + "WHERE concat(foln.singleNormDescription, ' ' ,foln.abbreviation) ILIKE concat('%', :searchStr, '%') "
          + "OR concat(foln.abbreviation, ' ', foln.singleNormDescription) ILIKE concat('%', :searchStr, '%')")
  List<FieldOfLawNormDTO> findByAbbreviationAndSingleNormDescriptionContainingIgnoreCase(
      String searchStr);
}
