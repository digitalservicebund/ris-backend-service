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
   * Find all history logs for a documentation unit by its id
   *
   * @param documentationUnitId the id of the documentation unit
   * @param user logged-in user for checking view permissions
   * @return a list of history logs for this documentation unit
   */
  List<HistoryLog> findByDocumentationUnitId(UUID documentationUnitId, User user);

  /**
   * Find all history logs for a documentation unit by its id for a certain duration
   *
   * @param documentationUnitId the id of the documentation unit
   * @param user logged-in user for checking view permissions
   * @param start timestamp for the start
   * @param end timestamp for the end
   * @return an optional history log, which is the latest history log, that was created by the same
   *     user at the same date for this documentation unit and is of type "UPDATE"
   */
  Optional<HistoryLog> findUpdateLogForDuration(
      UUID documentationUnitId, User user, Instant start, Instant end);

  /**
   * Saves a new or updates an existing history log with event typ 'UPDATE'
   *
   * @param id the id of the history log to be updated (can be null, if it is the first update log
   *     of the day, for the user
   * @param documentationUnitId the id of the documentation unit
   * @param user logged-in user for checking view permissions
   * @return
   */
  void saveHistoryLog(
      UUID id,
      UUID documentationUnitId,
      User user,
      HistoryLogEventType eventType,
      String description);

  void saveProcessStepHistoryLog(
      UUID documentationUnitId,
      User user,
      HistoryLogEventType eventType,
      String description,
      DocumentationUnitProcessStep fromStep,
      DocumentationUnitProcessStep toStep);
}
