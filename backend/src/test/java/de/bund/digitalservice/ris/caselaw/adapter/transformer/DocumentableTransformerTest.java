package de.bund.digitalservice.ris.caselaw.adapter.transformer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DecisionDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.ManagementDataDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.PendingProceedingDTO;
import de.bund.digitalservice.ris.caselaw.domain.Decision;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnit;
import de.bund.digitalservice.ris.caselaw.domain.ManagementData;
import de.bund.digitalservice.ris.caselaw.domain.PendingProceeding;
import de.bund.digitalservice.ris.caselaw.domain.Reference;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class DocumentableTransformerTest {
  @Nested
  class DocumentableContainsReferenceWithId {
    @Test
    void test_withoutReferences_shouldReturnFalse() {
      // Arrange
      UUID id = UUID.randomUUID();
      DocumentationUnit documentationUnit = PendingProceeding.builder().build();

      // Act
      boolean result =
          DocumentableTransformer.documentableContainsReferenceWithId(documentationUnit, id);

      // Assert
      assertFalse(result);
    }

    @Test
    void test_withNullReferences_shouldReturnFalse() {
      // Arrange
      UUID id = UUID.randomUUID();
      DocumentationUnit documentationUnit =
          PendingProceeding.builder().caselawReferences(null).literatureReferences(null).build();

      // Act
      boolean result =
          DocumentableTransformer.documentableContainsReferenceWithId(documentationUnit, id);

      // Assert
      assertFalse(result);
    }

    @Test
    void test_withEmptyReferences_shouldReturnFalse() {
      // Arrange
      UUID id = UUID.randomUUID();
      DocumentationUnit documentationUnit =
          PendingProceeding.builder()
              .caselawReferences(List.of())
              .literatureReferences(List.of())
              .build();

      // Act
      boolean result =
          DocumentableTransformer.documentableContainsReferenceWithId(documentationUnit, id);

      // Assert
      assertFalse(result);
    }

    @Test
    void test_withOtherId_shouldReturnFalse() {
      // Arrange
      UUID id = UUID.randomUUID();
      DocumentationUnit documentationUnit =
          PendingProceeding.builder()
              .caselawReferences(List.of(Reference.builder().id(UUID.randomUUID()).build()))
              .literatureReferences(List.of(Reference.builder().id(UUID.randomUUID()).build()))
              .build();

      // Act
      boolean result =
          DocumentableTransformer.documentableContainsReferenceWithId(documentationUnit, id);

      // Assert
      assertFalse(result);
    }

    @Test
    void test_withCaselawReferenceWithId_shouldReturnTrue() {
      // Arrange
      UUID id = UUID.randomUUID();
      DocumentationUnit documentationUnit =
          PendingProceeding.builder()
              .caselawReferences(List.of(Reference.builder().id(id).build()))
              .literatureReferences(List.of(Reference.builder().id(UUID.randomUUID()).build()))
              .build();

      // Act
      boolean result =
          DocumentableTransformer.documentableContainsReferenceWithId(documentationUnit, id);

      // Assert
      assertTrue(result);
    }

    @Test
    void test_withLiteratureReferenceWithId_shouldReturnTrue() {
      // Arrange
      UUID id = UUID.randomUUID();
      DocumentationUnit documentationUnit =
          PendingProceeding.builder()
              .caselawReferences(List.of(Reference.builder().id(UUID.randomUUID()).build()))
              .literatureReferences(List.of(Reference.builder().id(id).build()))
              .build();

      // Act
      boolean result =
          DocumentableTransformer.documentableContainsReferenceWithId(documentationUnit, id);

      // Assert
      assertTrue(result);
    }
  }

  @Nested
  class AddManagementData {
    @Test
    void testDecision_withoutManagementData_shouldNotAddManagementData() {
      // Arrange
      Decision updatedDomainObject = Decision.builder().build();
      var builder = DecisionDTO.builder().build().toBuilder();

      // Act
      DocumentableTransformer.addManagementData(updatedDomainObject, builder);

      // Assert
      assertNull(builder.build().getScheduledPublicationDateTime());
      assertNull(builder.build().getLastHandoverDateTime());
      assertNull(builder.build().getScheduledByEmail());
    }

    @Test
    void testDecision_withManagementData_shouldAddManagementData() {
      // Arrange
      Decision updatedDomainObject =
          Decision.builder()
              .managementData(
                  ManagementData.builder()
                      .scheduledPublicationDateTime(LocalDateTime.now())
                      .scheduledByEmail("test@test.de")
                      .lastHandoverDateTime(LocalDateTime.now())
                      .build())
              .build();
      var builder = DecisionDTO.builder().build().toBuilder();

      // Act
      DocumentableTransformer.addManagementData(updatedDomainObject, builder);

      // Assert
      assertThat(builder.build().getScheduledPublicationDateTime()).isNotNull();
      assertThat(builder.build().getLastHandoverDateTime()).isNotNull();
      assertThat(builder.build().getScheduledByEmail()).isEqualTo("test@test.de");
    }

    @Test
    void testPendingProceeding_withoutManagementData_shouldNotAddManagementData() {
      // Arrange
      PendingProceeding updatedDomainObject = PendingProceeding.builder().build();
      var builder = PendingProceedingDTO.builder().build().toBuilder();

      // Act
      DocumentableTransformer.addManagementData(updatedDomainObject, builder);

      // Assert
      assertNull(builder.build().getScheduledPublicationDateTime());
      assertNull(builder.build().getLastHandoverDateTime());
      assertNull(builder.build().getScheduledByEmail());
    }
  }

  @Nested
  class PostProcessRelationShips {
    @Test
    void testDecision_withoutCaseLawReferences_shouldNotAddLink() {
      // Arrange
      var result = DecisionDTO.builder().build();
      var currentDTO = DecisionDTO.builder().build();

      // Act
      var actual = DocumentableTransformer.postProcessRelationships(result, currentDTO);

      // Assert
      assertThat(actual.getCaselawReferences()).isEmpty();
    }

    @Test
    void testPendingProceeding_withoutCaseLawReferences_shouldNotAddLink() {
      // Arrange
      var result = PendingProceedingDTO.builder().build();
      var currentDTO = PendingProceedingDTO.builder().build();

      // Act
      var actual = DocumentableTransformer.postProcessRelationships(result, currentDTO);

      // Assert
      assertThat(actual.getCaselawReferences()).isEmpty();
    }

    @Test
    void testDecision_withCaseLawReferences_shouldAddLink() {
      // Arrange
      var result =
          DecisionDTO.builder()
              .caselawReferences(
                  List.of(CaselawReferenceDTO.builder().id(UUID.randomUUID()).build()))
              .build();
      var currentDTO = DecisionDTO.builder().build();

      // Act
      var actual = DocumentableTransformer.postProcessRelationships(result, currentDTO);

      // Assert
      assertThat(actual.getCaselawReferences().get(0).getDocumentationUnit()).isEqualTo(result);
    }

    @Test
    void testPendingProceeding_withCaseLawReferences_shouldAddLink() {
      // Arrange
      var result =
          PendingProceedingDTO.builder()
              .caselawReferences(
                  List.of(CaselawReferenceDTO.builder().id(UUID.randomUUID()).build()))
              .build();
      var currentDTO = PendingProceedingDTO.builder().build();

      // Act
      var actual = DocumentableTransformer.postProcessRelationships(result, currentDTO);

      // Assert
      assertThat(actual.getCaselawReferences().get(0).getDocumentationUnit()).isEqualTo(result);
    }

    @Test
    void testDecision_withoutLiteratureReferences_shouldNotAddLink() {
      // Arrange
      var result = DecisionDTO.builder().build();
      var currentDTO = DecisionDTO.builder().build();

      // Act
      var actual = DocumentableTransformer.postProcessRelationships(result, currentDTO);

      // Assert
      assertThat(actual.getLiteratureReferences()).isEmpty();
    }

    @Test
    void testPendingProceeding_withoutLiteratureReferences_shouldNotAddLink() {
      // Arrange
      var result = PendingProceedingDTO.builder().build();
      var currentDTO = PendingProceedingDTO.builder().build();

      // Act
      var actual = DocumentableTransformer.postProcessRelationships(result, currentDTO);

      // Assert
      assertThat(actual.getLiteratureReferences()).isEmpty();
    }

    @Test
    void testDecision_withLiteratureReferences_shouldAddLink() {
      // Arrange
      var result =
          DecisionDTO.builder()
              .literatureReferences(
                  List.of(LiteratureReferenceDTO.builder().id(UUID.randomUUID()).build()))
              .build();
      var currentDTO = DecisionDTO.builder().build();

      // Act
      var actual = DocumentableTransformer.postProcessRelationships(result, currentDTO);

      // Assert
      assertThat(actual.getLiteratureReferences().get(0).getDocumentationUnit()).isEqualTo(result);
    }

    @Test
    void testPendingProceeding_withLiteratureReferences_shouldAddLink() {
      // Arrange
      var result =
          PendingProceedingDTO.builder()
              .literatureReferences(
                  List.of(LiteratureReferenceDTO.builder().id(UUID.randomUUID()).build()))
              .build();
      var currentDTO = PendingProceedingDTO.builder().build();

      // Act
      var actual = DocumentableTransformer.postProcessRelationships(result, currentDTO);

      // Assert
      assertThat(actual.getLiteratureReferences().get(0).getDocumentationUnit()).isEqualTo(result);
    }

    @Test
    void testDecision_withoutManagementData_shouldNotAddLink() {
      // Arrange
      var result = DecisionDTO.builder().build();
      var currentDTO = DecisionDTO.builder().build();

      // Act
      var actual = DocumentableTransformer.postProcessRelationships(result, currentDTO);

      // Assert
      assertNull(actual.getManagementData());
    }

    @Test
    void testPendingProceeding_withoutManagementData_shouldNotAddLink() {
      // Arrange
      var result = PendingProceedingDTO.builder().build();
      var currentDTO = PendingProceedingDTO.builder().build();

      // Act
      var actual = DocumentableTransformer.postProcessRelationships(result, currentDTO);

      // Assert
      assertNull(actual.getManagementData());
    }

    @Test
    void testDecision_withManagementData_shouldAddLink() {
      // Arrange
      ManagementDataDTO managementData =
          ManagementDataDTO.builder()
              .lastUpdatedAtDateTime(Instant.now())
              .lastUpdatedByUserName("Tester")
              .build();
      var result = DecisionDTO.builder().build();
      var currentDTO = DecisionDTO.builder().managementData(managementData).build();

      // Act
      var actual = DocumentableTransformer.postProcessRelationships(result, currentDTO);

      // Assert
      assertThat(actual.getManagementData().getDocumentationUnit()).isEqualTo(result);
    }

    @Test
    void testPendingProceeding_withManagementData_shouldAddLink() {
      // Arrange
      ManagementDataDTO managementData =
          ManagementDataDTO.builder()
              .lastUpdatedAtDateTime(Instant.now())
              .lastUpdatedByUserName("Tester")
              .build();
      var result = PendingProceedingDTO.builder().build();
      var currentDTO = PendingProceedingDTO.builder().managementData(managementData).build();

      // Act
      var actual = DocumentableTransformer.postProcessRelationships(result, currentDTO);

      // Assert
      assertThat(actual.getManagementData().getDocumentationUnit()).isEqualTo(result);
    }
  }
}
