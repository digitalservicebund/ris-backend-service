package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DatabaseLanguageCodeRepository extends JpaRepository<LanguageCodeDTO, UUID> {
  List<LanguageCodeDTO> findLanguageCodeDTOByValueContains(String searchString);
}
