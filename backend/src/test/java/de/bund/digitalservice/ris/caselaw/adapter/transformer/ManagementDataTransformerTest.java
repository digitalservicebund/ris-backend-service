package de.bund.digitalservice.ris.caselaw.adapter.transformer;

import static org.assertj.core.api.Assertions.assertThat;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DecisionDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentationOfficeDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DuplicateRelationDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.ManagementDataDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.StatusDTO;
import de.bund.digitalservice.ris.caselaw.domain.Decision;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationOffice;
import de.bund.digitalservice.ris.caselaw.domain.DuplicateRelation;
import de.bund.digitalservice.ris.caselaw.domain.ManagementData;
import de.bund.digitalservice.ris.caselaw.domain.PublicationStatus;
import de.bund.digitalservice.ris.caselaw.domain.User;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import lombok.Builder;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class ManagementDataTransformerTest {
  @Test
  void testTransformToDomain_withoutManagementData_shouldTransformToEmptyManagementData() {
    // Arrange
    DecisionDTO decisionDTO = DecisionDTO.builder().build();
    ManagementData expected =
        generateManagementData(Optional.of(CreationParameters.builder().build()));

    // Act
    ManagementData managementData = ManagementDataTransformer.transformToDomain(decisionDTO, null);

    // Assert
    assertThat(managementData).isEqualTo(expected);
  }

  @Test
  void testTransformToDomain_withBorderNumbers_shouldExtractOnlyRelevantBorderNumbers() {
    // Arrange
    DecisionDTO decisionDTO =
        DecisionDTO.builder()
            .tenor("<border-number><number>1</number><content>hello</content></border-number>")
            .grounds("<border-number><number>2</number><content>hello</content></border-number>")
            .caseFacts("<border-number><number>3</number><content>hello</content></border-number>")
            .decisionGrounds(
                "<border-number><number>4</number><content>hello</content></border-number>")
            .otherLongText(
                "<border-number><number>5</number><content>hello</content></border-number>")
            .dissentingOpinion(
                "<border-number><number>6</number><content>hello</content></border-number>")
            .headnote("<border-number><number>7</number><content>hello</content></border-number>")
            .guidingPrinciple(
                "<border-number><number>8</number><content>hello</content></border-number>")
            .headline("<border-number><number>9</number><content>hello</content></border-number>")
            .otherHeadnote(
                "<border-number><number>10</number><content>hello</content></border-number>")
            .outline("<border-number><number>11</number><content>hello</content></border-number>")
            .build();
    ManagementData expected =
        generateManagementData(
            Optional.of(
                CreationParameters.builder()
                    .borderNumbers(List.of("1", "2", "3", "4", "5", "6"))
                    .build()));

    // Act
    ManagementData managementData = ManagementDataTransformer.transformToDomain(decisionDTO, null);

    // Assert
    assertThat(managementData).isEqualTo(expected);
  }

  @Nested
  class DuplicateRelations {
    @Test
    void
        testTransformToDomain_withUnpublishedDuplicateWarningFromOtherDocOffice_Relations1_shouldFilterOutWarning() {
      // Arrange
      var original =
          generateSimpleDTOBuilder().documentNumber("original").id(UUID.randomUUID()).build();
      var unpublishedStatus =
          StatusDTO.builder().publicationStatus(PublicationStatus.UNPUBLISHED).build();
      var otherDocOffice = DocumentationOfficeDTO.builder().abbreviation("other office").build();
      var unpublishedDuplicateFromOtherDocOffice =
          generateSimpleDTOBuilder()
              .documentNumber("duplicate")
              .documentationOffice(otherDocOffice)
              .id(UUID.randomUUID())
              .status(unpublishedStatus)
              .build();
      var duplicateRelationship =
          DuplicateRelationDTO.builder()
              .documentationUnit1(original)
              .documentationUnit2(unpublishedDuplicateFromOtherDocOffice)
              .build();
      original = original.toBuilder().duplicateRelations1(Set.of(duplicateRelationship)).build();

      // Act
      Decision decision = DecisionTransformer.transformToDomain(original);

      // Assert
      assertThat(decision.managementData().duplicateRelations()).isEmpty();
    }

    @Test
    void
        testTransformToDomain_withUnpublishedDuplicateWarningFromOtherDocOffice_Relations2_shouldFilterOutWarning() {
      // Arrange
      var original =
          generateSimpleDTOBuilder().documentNumber("original").id(UUID.randomUUID()).build();
      var unpublishedStatus =
          StatusDTO.builder().publicationStatus(PublicationStatus.UNPUBLISHED).build();
      var otherDocOffice = DocumentationOfficeDTO.builder().abbreviation("other office").build();
      var unpublishedDuplicateFromOtherDocOffice =
          generateSimpleDTOBuilder()
              .documentNumber("duplicate")
              .documentationOffice(otherDocOffice)
              .id(UUID.randomUUID())
              .status(unpublishedStatus)
              .build();
      var duplicateRelationship =
          DuplicateRelationDTO.builder()
              .documentationUnit1(unpublishedDuplicateFromOtherDocOffice)
              .documentationUnit2(original)
              .build();
      original = original.toBuilder().duplicateRelations2(Set.of(duplicateRelationship)).build();

      // Act
      Decision decision = DecisionTransformer.transformToDomain(original);

      // Assert
      assertThat(decision.managementData().duplicateRelations()).isEmpty();
    }

    @Test
    void
        testTransformToDomain_withUnpublishedDuplicateWarningFromSameDocOffice_shouldNotFilterOutWarning() {
      // Arrange
      var original =
          generateSimpleDTOBuilder().documentNumber("original").id(UUID.randomUUID()).build();
      var unpublishedStatus =
          StatusDTO.builder().publicationStatus(PublicationStatus.UNPUBLISHED).build();
      var unpublishedDuplicateFromOtherDocOffice =
          generateSimpleDTOBuilder()
              .documentNumber("duplicate")
              .id(UUID.randomUUID())
              .status(unpublishedStatus)
              .build();
      var duplicateRelationship =
          DuplicateRelationDTO.builder()
              .documentationUnit1(original)
              .documentationUnit2(unpublishedDuplicateFromOtherDocOffice)
              .build();
      original = original.toBuilder().duplicateRelations1(Set.of(duplicateRelationship)).build();

      // Act
      Decision decision = DecisionTransformer.transformToDomain(original);

      // Assert
      assertThat(decision.managementData().duplicateRelations()).hasSize(1);
    }

    @Test
    void
        testTransformToDomain_withPublishedDuplicateWarningFromOtherDocOffice_shouldNotFilterOutWarning() {
      // Arrange
      var original =
          generateSimpleDTOBuilder().documentNumber("original").id(UUID.randomUUID()).build();
      var publishedStatus =
          StatusDTO.builder().publicationStatus(PublicationStatus.PUBLISHED).build();
      var otherDocOffice = DocumentationOfficeDTO.builder().abbreviation("other office").build();
      var unpublishedDuplicateFromOtherDocOffice =
          generateSimpleDTOBuilder()
              .documentNumber("duplicate")
              .documentationOffice(otherDocOffice)
              .id(UUID.randomUUID())
              .status(publishedStatus)
              .build();
      var duplicateRelationship =
          DuplicateRelationDTO.builder()
              .documentationUnit1(unpublishedDuplicateFromOtherDocOffice)
              .documentationUnit2(original)
              .build();
      original = original.toBuilder().duplicateRelations2(Set.of(duplicateRelationship)).build();

      // Act
      Decision decision = DecisionTransformer.transformToDomain(original);

      // Assert
      assertThat(decision.managementData().duplicateRelations()).hasSize(1);
      assertThat(
              decision.managementData().duplicateRelations().stream()
                  .findFirst()
                  .get()
                  .documentNumber())
          .isEqualTo("duplicate");
    }

    @Test
    void
        testTransformToDomain_withPublishingDuplicateWarningFromOtherDocOffice_shouldNotFilterOutWarning() {
      // Arrange
      var original =
          generateSimpleDTOBuilder().documentNumber("original").id(UUID.randomUUID()).build();
      var publishedStatus =
          StatusDTO.builder().publicationStatus(PublicationStatus.PUBLISHING).build();
      var otherDocOffice = DocumentationOfficeDTO.builder().abbreviation("other office").build();
      var unpublishedDuplicateFromOtherDocOffice =
          generateSimpleDTOBuilder()
              .documentNumber("duplicate")
              .documentationOffice(otherDocOffice)
              .id(UUID.randomUUID())
              .status(publishedStatus)
              .build();
      var duplicateRelationship =
          DuplicateRelationDTO.builder()
              .documentationUnit1(unpublishedDuplicateFromOtherDocOffice)
              .documentationUnit2(original)
              .build();
      original = original.toBuilder().duplicateRelations2(Set.of(duplicateRelationship)).build();

      // Act
      Decision decision = DecisionTransformer.transformToDomain(original);

      // Assert
      assertThat(decision.managementData().duplicateRelations()).hasSize(1);
      assertThat(
              decision.managementData().duplicateRelations().stream()
                  .findFirst()
                  .get()
                  .documentNumber())
          .isEqualTo("duplicate");
    }

    @Test
    void
        testTransformToDomain_withMultipleDuplicateWarnings_shouldSortByDecisionDateAndDocNumber() {
      // Arrange
      var original =
          generateSimpleDTOBuilder().documentNumber("original").id(UUID.randomUUID()).build();
      var duplicate1 =
          generateSimpleDTOBuilder()
              .documentNumber("duplicate1")
              .date(LocalDate.of(2020, 1, 1))
              .id(UUID.randomUUID())
              .build();
      var duplicate2 =
          generateSimpleDTOBuilder().documentNumber("duplicate2").id(UUID.randomUUID()).build();
      var duplicate3 =
          generateSimpleDTOBuilder()
              .documentNumber("duplicate3")
              .date(LocalDate.of(2021, 1, 1))
              .id(UUID.randomUUID())
              .build();
      var duplicate4 =
          generateSimpleDTOBuilder()
              .documentNumber("duplicate4")
              .date(LocalDate.of(2021, 1, 1))
              .id(UUID.randomUUID())
              .build();
      var duplicate5 =
          generateSimpleDTOBuilder().documentNumber("duplicate05").id(UUID.randomUUID()).build();
      var duplicateRelationship1 =
          DuplicateRelationDTO.builder()
              .documentationUnit1(duplicate1)
              .documentationUnit2(original)
              .build();
      var duplicateRelationship2 =
          DuplicateRelationDTO.builder()
              .documentationUnit1(duplicate2)
              .documentationUnit2(original)
              .build();
      var duplicateRelationship3 =
          DuplicateRelationDTO.builder()
              .documentationUnit1(duplicate3)
              .documentationUnit2(original)
              .build();
      var duplicateRelationship4 =
          DuplicateRelationDTO.builder()
              .documentationUnit1(duplicate4)
              .documentationUnit2(original)
              .build();
      var duplicateRelationship5 =
          DuplicateRelationDTO.builder()
              .documentationUnit1(duplicate5)
              .documentationUnit2(original)
              .build();
      original =
          original.toBuilder()
              .duplicateRelations2(
                  Set.of(duplicateRelationship1, duplicateRelationship2, duplicateRelationship5))
              .duplicateRelations1(Set.of(duplicateRelationship4, duplicateRelationship3))
              .build();

      // Act
      Decision decision = DecisionTransformer.transformToDomain(original);

      // Assert
      var transformedRelations = decision.managementData().duplicateRelations();
      assertThat(transformedRelations).hasSize(5);
      assertThat(transformedRelations.stream().map(DuplicateRelation::documentNumber))
          .containsExactly("duplicate3", "duplicate4", "duplicate1", "duplicate05", "duplicate2");
    }
  }

  @Nested
  class MetaData {
    @Test
    void testTransformToDomain_withAllMetaDataAndUser_shouldTransformAll() {
      // Arrange
      Instant lastUpdatedAtDateTime = Instant.now();
      DocumentationOfficeDTO creatingAndUpdatingDocOffice =
          DocumentationOfficeDTO.builder().id(UUID.randomUUID()).abbreviation("BGH").build();
      String lastUpdatedByName = "Winnie Puuh";
      Instant createdAtDateTime = Instant.now();
      String createdByName = "I Aah";
      Instant firstPublishedAtDateTime = Instant.now();

      ManagementDataDTO managementDataDTO =
          ManagementDataDTO.builder()
              .lastUpdatedAtDateTime(lastUpdatedAtDateTime)
              .lastUpdatedByUserName(lastUpdatedByName)
              .lastUpdatedByDocumentationOffice(creatingAndUpdatingDocOffice)
              .createdAtDateTime(createdAtDateTime)
              .createdByUserName(createdByName)
              .createdByDocumentationOffice(creatingAndUpdatingDocOffice)
              .firstPublishedAtDateTime(firstPublishedAtDateTime)
              .build();
      DecisionDTO decisionDTO = DecisionDTO.builder().managementData(managementDataDTO).build();
      CreationParameters parameters =
          CreationParameters.builder()
              .lastUpdatedAtDateTime(lastUpdatedAtDateTime)
              .lastUpdatedByName(lastUpdatedByName)
              .lastUpdatedByDocumentationOffice(creatingAndUpdatingDocOffice.getAbbreviation())
              .createdAtDateTime(createdAtDateTime)
              .createdByName(createdByName)
              .createdByDocumentationOffice(creatingAndUpdatingDocOffice.getAbbreviation())
              .firstPublishedAtDateTime(firstPublishedAtDateTime)
              .build();
      ManagementData expected = generateManagementData(Optional.of(parameters));
      User user =
          User.builder()
              .documentationOffice(
                  DocumentationOffice.builder()
                      .id(creatingAndUpdatingDocOffice.getId())
                      .abbreviation(creatingAndUpdatingDocOffice.getAbbreviation())
                      .build())
              .build();

      // Act
      ManagementData managementData =
          ManagementDataTransformer.transformToDomain(decisionDTO, user);

      // Assert
      assertThat(managementData).isEqualTo(expected);
    }

    @Test
    void testTransformToDomain_withAllowedUserDocOffice_shouldTransformUserName() {
      // Arrange
      UUID docOfficeId = UUID.randomUUID();
      DocumentationOfficeDTO documentationOfficeDTO =
          DocumentationOfficeDTO.builder().id(docOfficeId).abbreviation("BGH").build();
      ManagementDataDTO managementDataDTO =
          ManagementDataDTO.builder()
              .lastUpdatedByUserName("Winnie Puuh")
              .lastUpdatedByDocumentationOffice(documentationOfficeDTO)
              .build();
      DecisionDTO decisionDTO = DecisionDTO.builder().managementData(managementDataDTO).build();
      ManagementData expected =
          generateManagementData(
              Optional.of(
                  CreationParameters.builder()
                      .lastUpdatedByName("Winnie Puuh")
                      .lastUpdatedByDocumentationOffice("BGH")
                      .build()));
      User user =
          User.builder()
              .documentationOffice(
                  DocumentationOffice.builder().id(docOfficeId).abbreviation("BGH").build())
              .build();

      // Act
      ManagementData managementData =
          ManagementDataTransformer.transformToDomain(decisionDTO, user);

      // Assert
      assertThat(managementData).isEqualTo(expected);
    }

    @Test
    void
        testTransformToDomain_withOtherUserDocOfficeAndSystemName_shouldTransformLastUpdatedByNameToSystemName() {
      // Arrange
      ManagementDataDTO managementDataDTO =
          ManagementDataDTO.builder()
              .lastUpdatedByUserName("Winnie Puuh")
              .lastUpdatedBySystemName("NeuRIS")
              .lastUpdatedByDocumentationOffice(
                  DocumentationOfficeDTO.builder()
                      .id(UUID.randomUUID())
                      .abbreviation("BGH")
                      .build())
              .build();
      DecisionDTO decisionDTO = DecisionDTO.builder().managementData(managementDataDTO).build();
      ManagementData expected =
          generateManagementData(
              Optional.of(
                  CreationParameters.builder()
                      .lastUpdatedByName("NeuRIS")
                      .lastUpdatedByDocumentationOffice("BGH")
                      .build()));
      User user =
          User.builder()
              .documentationOffice(
                  DocumentationOffice.builder().id(UUID.randomUUID()).abbreviation("DS").build())
              .build();

      // Act
      ManagementData managementData =
          ManagementDataTransformer.transformToDomain(decisionDTO, user);

      // Assert
      assertThat(managementData).isEqualTo(expected);
    }

    @Test
    void testTransformToDomain_withOtherUserDocOffice_shouldTransformLastUpdatedByNameToNull() {
      // Arrange
      ManagementDataDTO managementDataDTO =
          ManagementDataDTO.builder()
              .lastUpdatedByUserName("Winnie Puuh")
              .lastUpdatedByDocumentationOffice(
                  DocumentationOfficeDTO.builder()
                      .id(UUID.randomUUID())
                      .abbreviation("BGH")
                      .build())
              .build();
      DecisionDTO decisionDTO = DecisionDTO.builder().managementData(managementDataDTO).build();
      ManagementData expected =
          generateManagementData(
              Optional.of(
                  CreationParameters.builder()
                      .lastUpdatedByDocumentationOffice("BGH")
                      .lastUpdatedByName(null)
                      .build()));
      User user =
          User.builder()
              .documentationOffice(
                  DocumentationOffice.builder().id(UUID.randomUUID()).abbreviation("DS").build())
              .build();

      // Act
      ManagementData managementData =
          ManagementDataTransformer.transformToDomain(decisionDTO, user);

      // Assert
      assertThat(managementData).isEqualTo(expected);
    }

    @Test
    void testTransformToDomain_withoutDocOffice_shouldTransformLastUpdatedByNameToNull() {
      // Arrange
      ManagementDataDTO managementDataDTO =
          ManagementDataDTO.builder().lastUpdatedByUserName("Winnie Puuh").build();
      DecisionDTO decisionDTO = DecisionDTO.builder().managementData(managementDataDTO).build();
      ManagementData expected =
          generateManagementData(
              Optional.of(
                  CreationParameters.builder()
                      .lastUpdatedByDocumentationOffice(null)
                      .lastUpdatedByName(null)
                      .build()));
      User user =
          User.builder()
              .documentationOffice(
                  DocumentationOffice.builder().id(UUID.randomUUID()).abbreviation("DS").build())
              .build();

      // Act
      ManagementData managementData =
          ManagementDataTransformer.transformToDomain(decisionDTO, user);

      // Assert
      assertThat(managementData).isEqualTo(expected);
    }

    @Test
    void testTransformToDomain_withoutUser_shouldTransformLastUpdatedByNameToNull() {
      // Arrange
      ManagementDataDTO managementDataDTO =
          ManagementDataDTO.builder()
              .lastUpdatedByUserName("Winnie Puuh")
              .lastUpdatedByDocumentationOffice(
                  DocumentationOfficeDTO.builder()
                      .id(UUID.randomUUID())
                      .abbreviation("BGH")
                      .build())
              .build();
      DecisionDTO decisionDTO = DecisionDTO.builder().managementData(managementDataDTO).build();
      ManagementData expected =
          generateManagementData(
              Optional.of(
                  CreationParameters.builder()
                      .lastUpdatedByDocumentationOffice("BGH")
                      .lastUpdatedByName(null)
                      .build()));

      // Act
      ManagementData managementData =
          ManagementDataTransformer.transformToDomain(decisionDTO, null);

      // Assert
      assertThat(managementData).isEqualTo(expected);
    }

    @Test
    void testTransformToDomain_withoutUserDocOffice_shouldTransformLastUpdatedByNameToNull() {
      // Arrange
      ManagementDataDTO managementDataDTO =
          ManagementDataDTO.builder()
              .lastUpdatedByUserName("Winnie Puuh")
              .lastUpdatedByDocumentationOffice(
                  DocumentationOfficeDTO.builder()
                      .id(UUID.randomUUID())
                      .abbreviation("BGH")
                      .build())
              .build();
      DecisionDTO decisionDTO = DecisionDTO.builder().managementData(managementDataDTO).build();
      ManagementData expected =
          generateManagementData(
              Optional.of(
                  CreationParameters.builder()
                      .lastUpdatedByDocumentationOffice("BGH")
                      .lastUpdatedByName(null)
                      .build()));

      // Act
      ManagementData managementData =
          ManagementDataTransformer.transformToDomain(decisionDTO, User.builder().build());

      // Assert
      assertThat(managementData).isEqualTo(expected);
    }

    @Test
    void
        testTransformToDomain_withOtherUserDocOfficeAndSystemName_shouldTransformCreatedByNameToSystemName() {
      // Arrange
      ManagementDataDTO managementDataDTO =
          ManagementDataDTO.builder()
              .createdByUserName("Winnie Puuh")
              .createdBySystemName("NeuRIS")
              .createdByDocumentationOffice(
                  DocumentationOfficeDTO.builder()
                      .id(UUID.randomUUID())
                      .abbreviation("BGH")
                      .build())
              .build();
      DecisionDTO decisionDTO = DecisionDTO.builder().managementData(managementDataDTO).build();
      ManagementData expected =
          generateManagementData(
              Optional.of(
                  CreationParameters.builder()
                      .createdByName("NeuRIS")
                      .createdByDocumentationOffice("BGH")
                      .build()));
      User user =
          User.builder()
              .documentationOffice(
                  DocumentationOffice.builder().id(UUID.randomUUID()).abbreviation("DS").build())
              .build();

      // Act
      ManagementData managementData =
          ManagementDataTransformer.transformToDomain(decisionDTO, user);

      // Assert
      assertThat(managementData).isEqualTo(expected);
    }

    @Test
    void testTransformToDomain_withoutUser_shouldTransformCreatedByNameToNull() {
      // Arrange
      ManagementDataDTO managementDataDTO =
          ManagementDataDTO.builder()
              .createdByUserName("Winnie Puuh")
              .createdByDocumentationOffice(
                  DocumentationOfficeDTO.builder()
                      .id(UUID.randomUUID())
                      .abbreviation("BGH")
                      .build())
              .build();
      DecisionDTO decisionDTO = DecisionDTO.builder().managementData(managementDataDTO).build();
      ManagementData expected =
          generateManagementData(
              Optional.of(
                  CreationParameters.builder()
                      .createdByDocumentationOffice("BGH")
                      .createdByName(null)
                      .build()));

      // Act
      ManagementData managementData =
          ManagementDataTransformer.transformToDomain(decisionDTO, null);

      // Assert
      assertThat(managementData).isEqualTo(expected);
    }

    @Test
    void testTransformToDomain_withoutUserDocOffice_shouldTransformCreatedByNameToNull() {
      // Arrange
      ManagementDataDTO managementDataDTO =
          ManagementDataDTO.builder()
              .createdByUserName("Winnie Puuh")
              .createdByDocumentationOffice(
                  DocumentationOfficeDTO.builder()
                      .id(UUID.randomUUID())
                      .abbreviation("BGH")
                      .build())
              .build();
      DecisionDTO decisionDTO = DecisionDTO.builder().managementData(managementDataDTO).build();
      ManagementData expected =
          generateManagementData(
              Optional.of(
                  CreationParameters.builder()
                      .createdByDocumentationOffice("BGH")
                      .createdByName(null)
                      .build()));

      // Act
      ManagementData managementData =
          ManagementDataTransformer.transformToDomain(decisionDTO, User.builder().build());

      // Assert
      assertThat(managementData).isEqualTo(expected);
    }

    @Test
    void testTransformToDomain_withoutDocOffice_shouldTransformCreatedByNameToNull() {
      // Arrange
      ManagementDataDTO managementDataDTO =
          ManagementDataDTO.builder().createdByUserName("Winnie Puuh").build();
      DecisionDTO decisionDTO = DecisionDTO.builder().managementData(managementDataDTO).build();
      ManagementData expected =
          generateManagementData(
              Optional.of(
                  CreationParameters.builder()
                      .createdByDocumentationOffice(null)
                      .createdByName(null)
                      .build()));
      User user =
          User.builder()
              .documentationOffice(
                  DocumentationOffice.builder().id(UUID.randomUUID()).abbreviation("DS").build())
              .build();

      // Act
      ManagementData managementData =
          ManagementDataTransformer.transformToDomain(decisionDTO, user);

      // Assert
      assertThat(managementData).isEqualTo(expected);
    }

    @Test
    void testTransformToDomain_withOtherUserDocOffice_shouldTransformCreatedByNameToNull() {
      // Arrange
      ManagementDataDTO managementDataDTO =
          ManagementDataDTO.builder()
              .createdByUserName("Winnie Puuh")
              .createdByDocumentationOffice(
                  DocumentationOfficeDTO.builder()
                      .id(UUID.randomUUID())
                      .abbreviation("BGH")
                      .build())
              .build();
      DecisionDTO decisionDTO = DecisionDTO.builder().managementData(managementDataDTO).build();
      ManagementData expected =
          generateManagementData(
              Optional.of(
                  CreationParameters.builder()
                      .createdByDocumentationOffice("BGH")
                      .createdByName(null)
                      .build()));
      User user =
          User.builder()
              .documentationOffice(
                  DocumentationOffice.builder().id(UUID.randomUUID()).abbreviation("DS").build())
              .build();

      // Act
      ManagementData managementData =
          ManagementDataTransformer.transformToDomain(decisionDTO, user);

      // Assert
      assertThat(managementData).isEqualTo(expected);
    }
  }

  private DecisionDTO.DecisionDTOBuilder<?, ?> generateSimpleDTOBuilder() {
    return DecisionDTO.builder()
        .documentationOffice(DocumentationOfficeDTO.builder().abbreviation("doc office").build());
  }

  @Builder(toBuilder = true)
  public record CreationParameters(
      LocalDateTime lastPublicationDateTime,
      LocalDateTime scheduledPublicationDateTime,
      String scheduledByEmail,
      List<DuplicateRelation> duplicateRelations,
      List<String> borderNumbers,
      String createdByDocumentationOffice,
      UUID createdByUserId,
      Instant createdAtDateTime,
      String createdByName,
      String lastUpdatedByDocumentationOffice,
      UUID lastUpdatedByUserId,
      Instant lastUpdatedAtDateTime,
      String lastUpdatedByName,
      Instant firstPublishedAtDateTime) {}

  private ManagementData generateManagementData(Optional<CreationParameters> parameters) {
    return ManagementData.builder()
        .lastPublicationDateTime(parameters.get().lastPublicationDateTime)
        .scheduledPublicationDateTime(parameters.get().scheduledPublicationDateTime)
        .scheduledByEmail(parameters.get().scheduledByEmail)
        .borderNumbers(Optional.ofNullable(parameters.get().borderNumbers).orElse(List.of()))
        .duplicateRelations(
            Optional.ofNullable(parameters.get().duplicateRelations).orElse(List.of()))
        .lastUpdatedAtDateTime(parameters.get().lastUpdatedAtDateTime)
        .lastUpdatedByName(parameters.get().lastUpdatedByName)
        .lastUpdatedByDocOffice(parameters.get().lastUpdatedByDocumentationOffice)
        .createdAtDateTime(parameters.get().createdAtDateTime)
        .createdByName(parameters.get().createdByName)
        .createdByDocOffice(parameters.get().createdByDocumentationOffice)
        .firstPublishedAtDateTime(parameters.get().firstPublishedAtDateTime)
        .build();
  }
}
