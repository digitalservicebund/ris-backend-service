package de.bund.digitalservice.ris.caselaw.domain;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.repository.NoRepositoryBean;

/** Domain repository for documentation units */
@NoRepositoryBean
public interface DocumentationUnitHistoryLogRepository {

  /**
   * Find all history logs for a documentation unit by its uuid
   *
   * @param documentationUnitId the uuid of the documentation unit
   * @param user logged-in user for checking view permissions
   * @return a list of history logs for this documentation unit
   */
  List<HistoryLog> findByDocumentationUnitId(UUID documentationUnitId, User user);

  /**
   * Find all history logs for a documentation unit by its uuid
   *
   * @param documentationUnitId the uuid of the documentation unit
   * @param user logged-in user for checking view permissions
   * @param start timestamp for the start of current day
   * @param end timestamp for the end of current day
   * @return an optional history log, which is the latest history log, that was created by the same
   *     user at the same date for this documentation unit and is of type "UPDATE"
   */
  Optional<HistoryLog> findUpdateLogForToday(
      UUID documentationUnitId, User user, Instant start, Instant end);

  /**
   * Saves a new or updates an existing history log with event typ 'UPDATE'
   *
   * @param id the id of the history log to be updated (can be null, if it is the first update log
   *     of the day, for the user
   * @param documentationUnitId the uuid of the documentation unit
   * @param user logged-in user for checking view permissions
   * @return a domain representation of the saved history log
   */
  HistoryLog saveUpdateLog(UUID id, UUID documentationUnitId, User user);
}
