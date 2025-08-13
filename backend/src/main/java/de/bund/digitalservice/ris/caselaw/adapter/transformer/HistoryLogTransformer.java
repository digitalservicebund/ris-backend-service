package de.bund.digitalservice.ris.caselaw.adapter.transformer;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentationUnitProcessStepDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.HistoryLogDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.HistoryLogDocumentationUnitProcessStepDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.ProcessStepDTO;
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
      @Nullable HistoryLogDocumentationUnitProcessStepDTO historyLogDocumentationUnitProcessStepDTO,
      @Nullable User fromUser,
      @Nullable User toUser) {
    // The currently logged-in user's office.
    DocumentationOffice userDocumentationOffice =
        Optional.ofNullable(currentUser).map(User::documentationOffice).orElse(null);

    // The creator's office from the API call.
    DocumentationOffice creatorDocumentationOffice =
        Optional.ofNullable(creatorUser).map(User::documentationOffice).orElse(null);

    String description =
        getDescription(
            historyLogDTO,
            historyLogDocumentationUnitProcessStepDTO,
            userDocumentationOffice,
            creatorDocumentationOffice,
            fromUser,
            toUser);

    return HistoryLog.builder()
        .id(historyLogDTO.getId())
        .createdAt(historyLogDTO.getCreatedAt())
        .createdBy(
            transformCreatedBy(
                historyLogDTO, creatorUser, userDocumentationOffice, creatorDocumentationOffice))
        .documentationOffice(
            creatorDocumentationOffice != null ? creatorDocumentationOffice.abbreviation() : null)
        .description(description)
        .eventType(historyLogDTO.getEventType())
        .build();
  }

  private static String transformCreatedBy(
      HistoryLogDTO historyLogDTO,
      @Nullable User creatorUser,
      DocumentationOffice currentUserDocumentationOffice,
      DocumentationOffice creatorDocumentationOffice) {

    // Todo: Add return for inactive /deleted users
    String creatorUserName = Optional.ofNullable(creatorUser).map(User::name).orElse(null);

    if (creatorUserName != null
        && isUserAllowedToSeeUserName(currentUserDocumentationOffice, creatorDocumentationOffice)) {
      return creatorUserName;
    }

    return historyLogDTO.getSystemName();
  }

  private static String getDescription(
      HistoryLogDTO historyLogDTO,
      @Nullable HistoryLogDocumentationUnitProcessStepDTO historyLogDocumentationUnitProcessStepDTO,
      DocumentationOffice currentUserDocumentationOffice,
      DocumentationOffice creatorDocumentationOffice,
      User fromUser,
      User toUser) {

    // If the description is already set, or no mapping exists, return the existing description
    if (historyLogDTO.getDescription() != null
        || historyLogDocumentationUnitProcessStepDTO == null) {
      return historyLogDTO.getDescription();
    }

    // Logic for a process step change
    if (historyLogDTO.getEventType() == HistoryLogEventType.PROCESS_STEP) {
      String newStepName =
          Optional.ofNullable(
                  historyLogDocumentationUnitProcessStepDTO.getToDocumentationUnitProcessStep())
              .map(DocumentationUnitProcessStepDTO::getProcessStep)
              .map(ProcessStepDTO::getName)
              .orElse("Unbekannt");

      if (historyLogDocumentationUnitProcessStepDTO.getFromDocumentationUnitProcessStep() == null) {
        // Case for "Schritt gesetzt"
        return "Schritt gesetzt: " + newStepName;
      } else {
        // Case for "Schritt geändert"
        String oldStepName =
            Optional.of(
                    historyLogDocumentationUnitProcessStepDTO.getFromDocumentationUnitProcessStep())
                .map(DocumentationUnitProcessStepDTO::getProcessStep)
                .map(ProcessStepDTO::getName)
                .orElse("Unbekannt");
        return String.format("Schritt geändert: %s -> %s", oldStepName, newStepName);
      }
    }

    // Logic for a process step user change
    if (historyLogDTO.getEventType() == HistoryLogEventType.PROCESS_STEP_USER) {
      // Determine if the current user is in the same office as the person in the log
      boolean isSameOffice =
          isUserAllowedToSeeUserName(currentUserDocumentationOffice, creatorDocumentationOffice);

      if (isSameOffice) {
        // Get the names, they will be null if the user is null
        String newPersonName = Optional.ofNullable(toUser).map(User::name).orElse(null);
        String oldPersonName = Optional.ofNullable(fromUser).map(User::name).orElse(null);

        // Case 1: A person was newly assigned (from null to a name)
        if (oldPersonName == null && newPersonName != null) {
          return "Person gesetzt: " + newPersonName;
        }
        // Case 2: A person was removed (from a name to null)
        else if (oldPersonName != null && newPersonName == null) {
          return "Person geändert: " + oldPersonName + " -> "; // Or your desired string for removal
        }
        // Case 3: A person was changed (from one name to another)
        else if (oldPersonName != null
            && newPersonName != null
            && !oldPersonName.equals(newPersonName)) {
          return String.format("Person geändert: %s -> %s", oldPersonName, newPersonName);
        }
      } else {
        // Generic description for other offices
        if (Optional.ofNullable(
                historyLogDocumentationUnitProcessStepDTO.getFromDocumentationUnitProcessStep())
            .isEmpty()) {
          return "Person gesetzt";
        } else {
          return "Person geändert";
        }
      }
    } else {
      // Generic description for other offices
      if (historyLogDocumentationUnitProcessStepDTO.getFromDocumentationUnitProcessStep() == null) {
        return "Person gesetzt";
      } else {
        return "Person geändert";
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
