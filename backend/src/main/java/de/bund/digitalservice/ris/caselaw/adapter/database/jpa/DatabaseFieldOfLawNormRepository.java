package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DatabaseFieldOfLawNormRepository extends JpaRepository<FieldOfLawNormDTO, UUID> {

  List<FieldOfLawNormDTO>
      findByAbbreviationContainingIgnoreCaseOrSingleNormDescriptionContainingIgnoreCase(
          String normStr, String description);
}
