package de.bund.digitalservice.ris.caselaw.adapter.transformer;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentationOfficeDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.HistoryLogDTO;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationOffice;
import de.bund.digitalservice.ris.caselaw.domain.HistoryLog;
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
   * @param user the currently authenticated user, can be {@code null}
   * @return a {@link HistoryLog} domain object built from the input DTO
   */
  public static HistoryLog transformToDomain(HistoryLogDTO historyLogDTO, @Nullable User user) {
    DocumentationOffice userDocumentationOffice =
        Optional.ofNullable(user).map(User::documentationOffice).orElse(null);

    return HistoryLog.builder()
        .id(historyLogDTO.getId())
        .createdAt(historyLogDTO.getCreatedAt())
        .createdBy(transformCreatedBy(historyLogDTO, userDocumentationOffice))
        .documentationOffice(
            Optional.ofNullable(historyLogDTO.getDocumentationOffice())
                .map(DocumentationOfficeDTO::getAbbreviation)
                .orElse(null))
        .description(historyLogDTO.getDescription())
        .eventType(historyLogDTO.getEventType())
        .build();
  }

  private static String transformCreatedBy(
      HistoryLogDTO historyLogDTO, DocumentationOffice userDocumentationOffice) {
    var userName = historyLogDTO.getUserName();
    var documentationOffice = historyLogDTO.getDocumentationOffice();
    if (userName != null
        && isUserAllowedToSeeUserName(userDocumentationOffice, documentationOffice)) {
      return userName;
    }

    return historyLogDTO.getSystemName();
  }

  private static boolean isUserAllowedToSeeUserName(
      DocumentationOffice userDocumentationOffice, DocumentationOfficeDTO documentationOffice) {

    if (userDocumentationOffice == null || documentationOffice == null) {
      return false;
    }

    return documentationOffice.getId().equals(userDocumentationOffice.id());
  }
}
