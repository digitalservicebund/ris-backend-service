package de.bund.digitalservice.ris.caselaw.adapter.transformer;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.HistoryLogDTO;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationOffice;
import de.bund.digitalservice.ris.caselaw.domain.HistoryLog;
import de.bund.digitalservice.ris.caselaw.domain.HistoryLogEventType;
import de.bund.digitalservice.ris.caselaw.domain.User;
import java.util.Optional;
import javax.annotation.Nullable;

/**
 * Utility class responsible for transforming {@link HistoryLogDTO} entities into their
 * corresponding domain model {@link HistoryLog}.
 *
 * <p>This class is not meant to be instantiated and provides only static transformation logic.
 */
public class HistoryLogTransformer {

  private HistoryLogTransformer() {}

  /**
   * Transforms a {@link HistoryLogDTO} and optional {@link User} context into a {@link HistoryLog}
   * domain object.
   *
   * <p>The transformation extracts relevant history log data, like who changed the documentation
   * unit (user or system, documentation office), the timestamp, description and the type of the
   * event. If the user is provided, additional visibility checks are applied to restrict sensitive
   * data.
   *
   * @param historyLogDTO the history log DTO containing information about change events of a
   *     documentation unit
   * @param currentUser the currently authenticated user, can be {@code null}
   * @param creatorUser the user, who created the log, can be {@code null}
   * @return a {@link HistoryLog} domain object built from the input DTO
   */
  public static HistoryLog transformToDomain(
      HistoryLogDTO historyLogDTO,
      @Nullable User currentUser,
      @Nullable User creatorUser,
      @Nullable User fromUser,
      @Nullable User toUser) {
    // The currently logged-in user's office.
    DocumentationOffice userDocumentationOffice =
        Optional.ofNullable(currentUser).map(User::documentationOffice).orElse(null);

    // The creator's office from the API call.
    DocumentationOffice creatorDocumentationOffice =
        Optional.ofNullable(creatorUser).map(User::documentationOffice).orElse(null);

    return HistoryLog.builder()
        .id(historyLogDTO.getId())
        .createdAt(historyLogDTO.getCreatedAt())
        .createdBy(
            transformCreatedBy(
                historyLogDTO, creatorUser, userDocumentationOffice, creatorDocumentationOffice))
        .documentationOffice(
            creatorDocumentationOffice != null ? creatorDocumentationOffice.abbreviation() : null)
        .description(
            transformDescription(
                historyLogDTO,
                userDocumentationOffice,
                creatorDocumentationOffice,
                fromUser,
                toUser))
        .eventType(historyLogDTO.getEventType())
        .build();
  }

  private static String transformCreatedBy(
      HistoryLogDTO historyLogDTO,
      @Nullable User creatorUser,
      DocumentationOffice currentUserDocumentationOffice,
      DocumentationOffice creatorDocumentationOffice) {

    String creatorUserName = Optional.ofNullable(creatorUser).map(User::name).orElse(null);

    if (creatorUserName != null
        && isUserAllowedToSeeUserName(currentUserDocumentationOffice, creatorDocumentationOffice)) {
      return creatorUserName;
    }

    return historyLogDTO.getSystemName();
  }

  private static String transformDescription(
      HistoryLogDTO historyLogDTO,
      DocumentationOffice currentUserDocumentationOffice,
      DocumentationOffice creatorDocumentationOffice,
      User fromUser,
      User toUser) {

    // If the description is already set, or no mapping exists, return the existing description
    if (historyLogDTO.getDescription() != null) {
      return historyLogDTO.getDescription();
    }

    // In case of PROCESS_STEP_USER we need to hydrate the description entry with dynamic user data
    if (historyLogDTO.getEventType() == HistoryLogEventType.PROCESS_STEP_USER) {
      boolean isSameOffice =
          isUserAllowedToSeeUserName(currentUserDocumentationOffice, creatorDocumentationOffice);

      if (isSameOffice) {
        String newPersonName = Optional.ofNullable(toUser).map(User::name).orElse(null);
        String oldPersonName = Optional.ofNullable(fromUser).map(User::name).orElse(null);

        if (oldPersonName == null && newPersonName != null) {
          return "Person gesetzt: " + newPersonName;
        } else if (oldPersonName != null && newPersonName == null) {
          return "Person entfernt: " + oldPersonName;
        } else if (oldPersonName != null
            && newPersonName != null
            && !oldPersonName.equals(newPersonName)) {
          return String.format("Person geändert: %s -> %s", oldPersonName, newPersonName);
        }
      } else {
        // Generic description for other offices
        if (fromUser == null) {
          return "Person gesetzt";
        } else {
          return "Person geändert";
        }
      }
    }

    return historyLogDTO.getDescription();
  }

  private static boolean isUserAllowedToSeeUserName(
      DocumentationOffice currentUserDocumentationOffice,
      DocumentationOffice creatorDocumentationOffice) {

    //    if (currentUserDocumentationOffice == null || creatorDocumentationOffice == null) {
    //      return false;
    //    }
    //
    //    return creatorDocumentationOffice.id().equals(currentUserDocumentationOffice.id());

    // Todo: uncomment the above code, as soon as the API returns the group path name for the
    // retrieved user
    return true;
  }
}
