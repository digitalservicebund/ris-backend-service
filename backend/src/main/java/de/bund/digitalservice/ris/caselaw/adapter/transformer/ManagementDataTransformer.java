package de.bund.digitalservice.ris.caselaw.adapter.transformer;

import static de.bund.digitalservice.ris.caselaw.adapter.transformer.DocumentableTransformer.extractBorderNumbers;
import static de.bund.digitalservice.ris.caselaw.adapter.transformer.DocumentableTransformer.transformDuplicateRelations;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DecisionDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentationOfficeDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.ManagementDataDTO;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationOffice;
import de.bund.digitalservice.ris.caselaw.domain.ManagementData;
import java.util.List;
import java.util.Optional;

public class ManagementDataTransformer {

  private ManagementDataTransformer() {
    // Private constructor to prevent instantiation of this utility class.
  }

  static ManagementData transformToDomain(
      DecisionDTO decisionDTO, DocumentationOffice userDocumentationOffice) {
    List<String> borderNumbers =
        extractBorderNumbers(
            decisionDTO.getTenor(),
            decisionDTO.getGrounds(),
            decisionDTO.getCaseFacts(),
            decisionDTO.getDecisionGrounds(),
            decisionDTO.getOtherLongText(),
            decisionDTO.getDissentingOpinion());

    return ManagementData.builder()
        .lastPublicationDateTime(decisionDTO.getLastPublicationDateTime())
        .scheduledPublicationDateTime(decisionDTO.getScheduledPublicationDateTime())
        .scheduledByEmail(decisionDTO.getScheduledByEmail())
        .borderNumbers(borderNumbers)
        .duplicateRelations(transformDuplicateRelations(decisionDTO))
        .lastUpdatedAtDateTime(
            Optional.ofNullable(decisionDTO.getManagementData())
                .map(ManagementDataDTO::getLastUpdatedAtDateTime)
                .orElse(null))
        .lastUpdatedByName(transformLastUpdatedByName(decisionDTO, userDocumentationOffice))
        .lastUpdatedByDocOffice(
            Optional.ofNullable(decisionDTO.getManagementData())
                .map(ManagementDataDTO::getLastUpdatedByDocumentationOffice)
                .map(DocumentationOfficeDTO::getAbbreviation)
                .orElse(null))
        .createdAtDateTime(
            Optional.ofNullable(decisionDTO.getManagementData())
                .map(ManagementDataDTO::getCreatedAtDateTime)
                .orElse(null))
        .createdByName(transformCreatedByName(decisionDTO, userDocumentationOffice))
        .createdByDocOffice(
            Optional.ofNullable(decisionDTO.getManagementData())
                .map(ManagementDataDTO::getCreatedByDocumentationOffice)
                .map(DocumentationOfficeDTO::getAbbreviation)
                .orElse(null))
        .firstPublishedAtDateTime(
            Optional.ofNullable(decisionDTO.getManagementData())
                .map(ManagementDataDTO::getFirstPublishedAtDateTime)
                .orElse(null))
        .build();
  }

  private static String transformLastUpdatedByName(
      DecisionDTO decisionDTO, DocumentationOffice userDocumentationOffice) {
    var lastUpdatedByUserName = getLastUpdatedByUserName(decisionDTO);
    var lastUpdatedByDocumentationOffice = getLastUpdatedByDocumentationOffice(decisionDTO);
    if (isUserAllowedToSeeName(userDocumentationOffice, lastUpdatedByDocumentationOffice)
        && lastUpdatedByUserName != null) {
      return lastUpdatedByUserName;
    }

    return Optional.ofNullable(decisionDTO.getManagementData())
        .map(ManagementDataDTO::getLastUpdatedBySystemName)
        .orElse(null);
  }

  private static boolean isUserAllowedToSeeName(
      DocumentationOffice userDocumentationOffice, DocumentationOfficeDTO documentationOfficeDTO) {
    return userDocumentationOffice != null
        && documentationOfficeDTO != null
        && documentationOfficeDTO.getId().equals(userDocumentationOffice.uuid());
  }

  private static String getLastUpdatedByUserName(DecisionDTO decisionDTO) {
    return Optional.ofNullable(decisionDTO.getManagementData())
        .map(ManagementDataDTO::getLastUpdatedByUserName)
        .orElse(null);
  }

  private static String transformCreatedByName(
      DecisionDTO decisionDTO, DocumentationOffice userDocumentationOffice) {
    var createdByUserName = getCreatedByUserName(decisionDTO);
    var createdByDocumentationOffice = getCreatedByDocumentationOffice(decisionDTO);
    if (isUserAllowedToSeeName(userDocumentationOffice, createdByDocumentationOffice)
        && createdByUserName != null) {
      return createdByUserName;
    }

    return Optional.ofNullable(decisionDTO.getManagementData())
        .map(ManagementDataDTO::getCreatedBySystemName)
        .orElse(null);
  }

  private static String getCreatedByUserName(DecisionDTO decisionDTO) {
    return Optional.ofNullable(decisionDTO.getManagementData())
        .map(ManagementDataDTO::getCreatedByUserName)
        .orElse(null);
  }

  private static DocumentationOfficeDTO getCreatedByDocumentationOffice(DecisionDTO decisionDTO) {
    return Optional.ofNullable(decisionDTO.getManagementData())
        .map(ManagementDataDTO::getCreatedByDocumentationOffice)
        .orElse(null);
  }

  private static DocumentationOfficeDTO getLastUpdatedByDocumentationOffice(
      DecisionDTO decisionDTO) {
    return Optional.ofNullable(decisionDTO.getManagementData())
        .map(ManagementDataDTO::getLastUpdatedByDocumentationOffice)
        .orElse(null);
  }
}
