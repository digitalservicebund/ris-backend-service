package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DatabaseLegalForceTypeRepository extends JpaRepository<LegalForceTypeDTO, UUID> {
  /**
   * Retrieves a list of legal force types whose abbreviations start with the provided search
   * string, ignoring case.
   *
   * @param searchString The search string used to filter legal force types by abbreviation.
   * @return A list of LegalForceTypeDTO entities whose abbreviations start with the given search
   *     string.
   */
  List<LegalForceTypeDTO> findAllByAbbreviationStartsWithIgnoreCase(String searchString);

  /**
   * Retrieves a list of all legal force types ordered by their abbreviations.
   *
   * @return A list of LegalForceTypeDTO entities ordered by abbreviation.
   */
  List<LegalForceTypeDTO> findAllByOrderByAbbreviation();
}
