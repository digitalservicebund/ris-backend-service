package de.bund.digitalservice.ris.caselaw.adapter.transformer;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DecisionDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentationOfficeDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentationUnitDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.ManagementDataDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.StatusDTO;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationOffice;
import de.bund.digitalservice.ris.caselaw.domain.DuplicateRelation;
import de.bund.digitalservice.ris.caselaw.domain.ManagementData;
import de.bund.digitalservice.ris.caselaw.domain.PublicationStatus;
import de.bund.digitalservice.ris.caselaw.domain.User;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import org.jetbrains.annotations.NotNull;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

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
   * Transforms a {@link DocumentationUnitDTO} and optional {@link User} context into a {@link
   * ManagementData} domain object.
   *
   * <p>The transformation extracts relevant metadata like publication dates, user info,
   * documentation office abbreviations, and access-controlled names. If the user is provided,
   * additional visibility checks are applied to restrict sensitive data.
   *
   * @param documentationUnitDTO the documentation unit DTO containing management data and other
   *     metadata
   * @param user the currently authenticated user, can be {@code null}
   * @return a {@link ManagementData} domain object built from the input DTO
   */
  public static ManagementData transformToDomain(
      DocumentationUnitDTO documentationUnitDTO, @Nullable User user) {

    List<String> borderNumbers = new ArrayList<>();
    if (documentationUnitDTO instanceof DecisionDTO decisionDTO) {
      borderNumbers =
          extractBorderNumbers(
              decisionDTO.getTenor(),
              decisionDTO.getGrounds(),
              decisionDTO.getCaseFacts(),
              decisionDTO.getDecisionGrounds(),
              decisionDTO.getOtherLongText(),
              decisionDTO.getDissentingOpinion());
    }

    DocumentationOffice userDocumentationOffice =
        Optional.ofNullable(user).map(User::documentationOffice).orElse(null);

    Optional<ManagementDataDTO> managementDataOptional =
        Optional.ofNullable(documentationUnitDTO.getManagementData());

    return ManagementData.builder()
        .lastHandoverDateTime(documentationUnitDTO.getLastHandoverDateTime())
        .scheduledPublicationDateTime(documentationUnitDTO.getScheduledPublicationDateTime())
        .scheduledByEmail(documentationUnitDTO.getScheduledByEmail())
        .borderNumbers(borderNumbers)
        .duplicateRelations(transformDuplicateRelations(documentationUnitDTO))
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
        .lastPublishedAtDateTime(
            managementDataOptional.map(ManagementDataDTO::getLastPublishedAtDateTime).orElse(null))
        .firstPublishedAtDateTime(
            managementDataOptional.map(ManagementDataDTO::getFirstPublishedAtDateTime).orElse(null))
        .build();
  }

  /**
   * Extracts all border numbers from the passed strings and returns them as a list based on the
   * following rules: For all <border-number> elements that contain a single <number> element with
   * non-blank content, that content will be added to the list of border numbers.
   *
   * @param input the strings to be searched for border numbers
   * @return a list of found border numbers or an empty list, if the input is null
   */
  private static List<String> extractBorderNumbers(String... input) {
    if (Objects.isNull(input)) {
      return new ArrayList<>();
    }
    List<String> borderNumbers = new ArrayList<>();
    Arrays.stream(input)
        .forEach(
            longText -> {
              if (Objects.isNull(longText)) {
                return;
              }
              Document doc = Jsoup.parse(longText);
              var borderNumberElements = doc.getElementsByTag("border-number");
              borderNumberElements.forEach(
                  element -> {
                    var numberElement = element.getElementsByTag("number");
                    if (numberElement.size() == 1) {
                      var number = numberElement.getFirst().text();
                      if (org.apache.commons.lang3.StringUtils.isNotBlank(number)) {
                        borderNumbers.add(numberElement.text());
                      }
                    }
                  });
            });
    return borderNumbers;
  }

  @NotNull
  private static List<DuplicateRelation> transformDuplicateRelations(
      DocumentationUnitDTO documentationUnitDTO) {
    if (documentationUnitDTO instanceof DecisionDTO decisionDTO) {
      return Stream.concat(
              decisionDTO.getDuplicateRelations1().stream()
                  .filter(
                      relation ->
                          isPublishedDuplicateOrSameDocOffice(
                              decisionDTO, relation.getDocumentationUnit2())),
              decisionDTO.getDuplicateRelations2().stream()
                  .filter(
                      relation ->
                          isPublishedDuplicateOrSameDocOffice(
                              decisionDTO, relation.getDocumentationUnit1())))
          .map(relation -> DuplicateRelationTransformer.transformToDomain(relation, decisionDTO))
          .sorted(
              Comparator.comparing(
                      (DuplicateRelation relation) ->
                          Optional.ofNullable(relation.decisionDate()).orElse(LocalDate.MIN),
                      Comparator.reverseOrder())
                  .thenComparing(DuplicateRelation::documentNumber))
          .toList();
    }
    return new ArrayList<>();
  }

  private static Boolean isPublishedDuplicateOrSameDocOffice(
      DocumentationUnitDTO original, DocumentationUnitDTO duplicate) {
    var duplicateStatus =
        Optional.ofNullable(duplicate.getStatus())
            .map(StatusDTO::getPublicationStatus)
            .orElse(null);
    return original.getDocumentationOffice().equals(duplicate.getDocumentationOffice())
        || PublicationStatus.PUBLISHED.equals(duplicateStatus)
        || PublicationStatus.PUBLISHING.equals(duplicateStatus);
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

    return documentationOffice.getId().equals(userDocumentationOffice.id());
  }

  private static String transformCreatedByUserName(
      ManagementDataDTO managementDataDTO, DocumentationOffice userDocumentationOffice) {
    var createdByUserName = managementDataDTO.getCreatedByUserName();
    var createdByDocumentationOffice = managementDataDTO.getCreatedByDocumentationOffice();
    if (createdByUserName != null
        && isUserAllowedToSeeUserName(userDocumentationOffice, createdByDocumentationOffice)) {
      return createdByUserName;
    }

    return managementDataDTO.getCreatedBySystemName();
  }
}
