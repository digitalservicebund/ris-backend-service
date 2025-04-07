package de.bund.digitalservice.ris.caselaw.adapter.transformer;

import static de.bund.digitalservice.ris.caselaw.adapter.transformer.DocumentableTransformer.extractBorderNumbers;
import static de.bund.digitalservice.ris.caselaw.adapter.transformer.DocumentableTransformer.transformDuplicateRelations;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DecisionDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentationOfficeDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.ManagementDataDTO;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationOffice;
import de.bund.digitalservice.ris.caselaw.domain.ManagementData;
import de.bund.digitalservice.ris.caselaw.domain.User;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;

/**
 * Utility class responsible for transforming {@link ManagementDataDTO} entities into their
 * corresponding domain model {@link ManagementData}.
 *
 * <p>This class is not meant to be instantiated and provides only static transformation logic.
 */
public class ManagementDataTransformer {

  private ManagementDataTransformer() {
    // Private constructor to prevent instantiation of this utility class.
  }

  /**
   * Transforms a {@link DecisionDTO} and optional {@link User} context into a {@link
   * ManagementData} domain object.
   *
   * <p>The transformation extracts relevant metadata like publication dates, user info,
   * documentation office abbreviations, and access-controlled names. If the user is provided,
   * additional visibility checks are applied to restrict sensitive data.
   *
   * @param decisionDTO the decision DTO containing management data and other metadata
   * @param user the currently authenticated user, can be {@code null}
   * @return a {@link ManagementData} domain object built from the input DTO
   */
  static ManagementData transformToDomain(DecisionDTO decisionDTO, @Nullable User user) {
    List<String> borderNumbers =
        extractBorderNumbers(
            decisionDTO.getTenor(),
            decisionDTO.getGrounds(),
            decisionDTO.getCaseFacts(),
            decisionDTO.getDecisionGrounds(),
            decisionDTO.getOtherLongText(),
            decisionDTO.getDissentingOpinion());

    DocumentationOffice userDocumentationOffice =
        Optional.ofNullable(user).map(User::documentationOffice).orElse(null);

    Optional<ManagementDataDTO> managementDataOptional =
        Optional.ofNullable(decisionDTO.getManagementData());

    return ManagementData.builder()
        .lastPublicationDateTime(decisionDTO.getLastPublicationDateTime())
        .scheduledPublicationDateTime(decisionDTO.getScheduledPublicationDateTime())
        .scheduledByEmail(decisionDTO.getScheduledByEmail())
        .borderNumbers(borderNumbers)
        .duplicateRelations(transformDuplicateRelations(decisionDTO))
        .lastUpdatedAtDateTime(
            managementDataOptional.map(ManagementDataDTO::getLastUpdatedAtDateTime).orElse(null))
        .lastUpdatedByName(
            managementDataOptional
                .map(
                    managementDataDTO ->
                        transformLastUpdatedByUserName(managementDataDTO, userDocumentationOffice))
                .orElse(null))
        .lastUpdatedByDocOffice(
            managementDataOptional
                .map(ManagementDataDTO::getLastUpdatedByDocumentationOffice)
                .map(DocumentationOfficeDTO::getAbbreviation)
                .orElse(null))
        .createdAtDateTime(
            managementDataOptional.map(ManagementDataDTO::getCreatedAtDateTime).orElse(null))
        .createdByName(
            managementDataOptional
                .map(
                    managementDataDTO ->
                        transformCreatedByUserName(managementDataDTO, userDocumentationOffice))
                .orElse(null))
        .createdByDocOffice(
            managementDataOptional
                .map(ManagementDataDTO::getCreatedByDocumentationOffice)
                .map(DocumentationOfficeDTO::getAbbreviation)
                .orElse(null))
        .firstPublishedAtDateTime(
            managementDataOptional.map(ManagementDataDTO::getFirstPublishedAtDateTime).orElse(null))
        .build();
  }

  private static String transformLastUpdatedByUserName(
      ManagementDataDTO managementDataDTO, DocumentationOffice userDocumentationOffice) {
    var lastUpdatedByUserName = managementDataDTO.getLastUpdatedByUserName();
    var lastUpdatedByDocumentationOffice = managementDataDTO.getLastUpdatedByDocumentationOffice();
    if (lastUpdatedByUserName != null
        && isUserAllowedToSeeUserName(userDocumentationOffice, lastUpdatedByDocumentationOffice)) {
      return lastUpdatedByUserName;
    }

    return managementDataDTO.getLastUpdatedBySystemName();
  }

  private static boolean isUserAllowedToSeeUserName(
      DocumentationOffice userDocumentationOffice, DocumentationOfficeDTO documentationOffice) {

    if (userDocumentationOffice == null || documentationOffice == null) {
      return false;
    }

    return documentationOffice.getId().equals(userDocumentationOffice.uuid());
  }

  private static String transformCreatedByUserName(
      ManagementDataDTO managementDataDTO, DocumentationOffice userDocumentationOffice) {
    var createdByUserName = managementDataDTO.getCreatedByUserName();
    var createdByDocumentationOffice = managementDataDTO.getCreatedByDocumentationOffice();
    if (createdByUserName != null
        && isUserAllowedToSeeUserName(userDocumentationOffice, createdByDocumentationOffice)) {
      return createdByUserName;
    }

    return managementDataDTO.getCreatedByUserName();
  }
}
