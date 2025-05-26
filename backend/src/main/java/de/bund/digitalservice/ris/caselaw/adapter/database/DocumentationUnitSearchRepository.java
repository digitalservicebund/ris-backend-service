package de.bund.digitalservice.ris.caselaw.adapter.database;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentationOfficeDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentationUnitListItemDTO;
import de.bund.digitalservice.ris.caselaw.domain.InboxStatus;
import de.bund.digitalservice.ris.caselaw.domain.PublicationStatus;
import java.time.LocalDate;
import java.util.Optional;
import lombok.Builder;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

public interface DocumentationUnitSearchRepository {
  /**
   * Searches for documentation units based on the provided parameters. Lazy relationships that are
   * needed for the ListItem are pre-fetched to avoid N+1 problems.
   *
   * @param parameters the search parameters
   * @param pageable pagination information
   * @return a slice of documentation units matching the search criteria
   */
  Slice<DocumentationUnitListItemDTO> search(SearchParameters parameters, Pageable pageable);

  @Builder
  record SearchParameters(
      Optional<String> courtType,
      Optional<String> courtLocation,
      Optional<String> documentNumber,
      Optional<String> fileNumber,
      Optional<LocalDate> decisionDate,
      Optional<LocalDate> decisionDateEnd,
      Optional<LocalDate> publicationDate,
      Optional<PublicationStatus> publicationStatus,
      boolean scheduledOnly,
      boolean withError,
      boolean myDocOfficeOnly,
      boolean withDuplicateWarning,
      Optional<InboxStatus> inboxStatus,
      DocumentationOfficeDTO documentationOfficeDTO) {}
}
