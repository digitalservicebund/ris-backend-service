package de.bund.digitalservice.ris.caselaw.adapter.transformer;

import static org.assertj.core.api.Assertions.assertThat;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentationOfficeDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.HistoryLogDTO;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationOffice;
import de.bund.digitalservice.ris.caselaw.domain.HistoryLog;
import de.bund.digitalservice.ris.caselaw.domain.HistoryLogEventType;
import de.bund.digitalservice.ris.caselaw.domain.User;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class HistoryLogTransformerTest {

  @Test
  void testTransformToDomain_withUserFromSameOffice_shouldShowUserName() {
    // Arrange
    UUID id = UUID.randomUUID();
    Instant now = Instant.now();
    String userName = "testUser";
    String systemName = "testSystemName";
    String description = "Unit changed";
    HistoryLogEventType eventType = HistoryLogEventType.HANDOVER;
    UUID docOfficeId = UUID.randomUUID();
    String officeAbbreviation = "BGH";
    DocumentationOfficeDTO documentationOfficeDTO =
        DocumentationOfficeDTO.builder().id(docOfficeId).abbreviation(officeAbbreviation).build();

    HistoryLogDTO dto =
        HistoryLogDTO.builder()
            .id(id)
            .createdAt(now)
            .userName(userName)
            .systemName(systemName)
            .description(description)
            .eventType(eventType)
            .documentationOffice(documentationOfficeDTO)
            .documentationUnitId(UUID.randomUUID())
            .build();

    User user =
        User.builder()
            .documentationOffice(
                DocumentationOffice.builder()
                    .id(docOfficeId)
                    .abbreviation(officeAbbreviation)
                    .build())
            .build();

    HistoryLog expected =
        HistoryLog.builder()
            .id(id)
            .createdAt(now)
            .createdBy(userName)
            .documentationOffice(officeAbbreviation)
            .description(description)
            .eventType(eventType)
            .build();

    // Act
    HistoryLog result = HistoryLogTransformer.transformToDomain(dto, user);

    // Assert
    assertThat(result).isEqualTo(expected);
  }

  @Test
  void testTransformToDomain_withUserFromDifferentOffice_shouldShowSystemName() {
    // Arrange
    UUID id = UUID.randomUUID();
    Instant now = Instant.now();
    String userName = "testUser";
    String systemName = "testSystemName";
    String description = "Unit changed";
    HistoryLogEventType eventType = HistoryLogEventType.UPDATE;
    UUID docOfficeId = UUID.randomUUID();
    String officeAbbreviation = "BGH";
    DocumentationOfficeDTO documentationOfficeDTO =
        DocumentationOfficeDTO.builder().id(docOfficeId).abbreviation(officeAbbreviation).build();

    HistoryLogDTO dto =
        HistoryLogDTO.builder()
            .id(id)
            .createdAt(now)
            .userName(userName)
            .systemName(systemName)
            .description(description)
            .eventType(eventType)
            .documentationOffice(documentationOfficeDTO)
            .documentationUnitId(UUID.randomUUID())
            .build();

    User user =
        User.builder()
            .documentationOffice(
                DocumentationOffice.builder().id(UUID.randomUUID()).abbreviation("DS").build())
            .build();

    HistoryLog expected =
        HistoryLog.builder()
            .id(id)
            .createdAt(now)
            .createdBy(systemName)
            .documentationOffice(officeAbbreviation)
            .description(description)
            .eventType(eventType)
            .build();

    // Act
    HistoryLog result = HistoryLogTransformer.transformToDomain(dto, user);

    // Assert
    assertThat(result).isEqualTo(expected);
  }

  @Test
  void
      testTransformToDomain_withUserFromDifferentOffice_withoutSystemName_shouldReturnNoCreatedBy() {
    // Arrange
    UUID id = UUID.randomUUID();
    Instant now = Instant.now();
    String userName = "testUser";
    String description = "Unit changed";
    HistoryLogEventType eventType = HistoryLogEventType.UPDATE;
    UUID docOfficeId = UUID.randomUUID();
    String officeAbbreviation = "BGH";
    DocumentationOfficeDTO documentationOfficeDTO =
        DocumentationOfficeDTO.builder().id(docOfficeId).abbreviation(officeAbbreviation).build();

    HistoryLogDTO dto =
        HistoryLogDTO.builder()
            .id(id)
            .createdAt(now)
            .userName(userName)
            .description(description)
            .eventType(eventType)
            .documentationOffice(documentationOfficeDTO)
            .documentationUnitId(UUID.randomUUID())
            .build();

    User user =
        User.builder()
            .documentationOffice(
                DocumentationOffice.builder().id(UUID.randomUUID()).abbreviation("DS").build())
            .build();

    HistoryLog expected =
        HistoryLog.builder()
            .id(id)
            .createdAt(now)
            .createdBy(null)
            .documentationOffice(officeAbbreviation)
            .description(description)
            .eventType(eventType)
            .build();

    // Act
    HistoryLog result = HistoryLogTransformer.transformToDomain(dto, user);

    // Assert
    assertThat(result).isEqualTo(expected);
  }

  @Test
  void testTransformToDomain_withNullUser_shouldShowSystemName() {
    // Arrange
    UUID id = UUID.randomUUID();
    Instant now = Instant.now();
    String userName = "testUser";
    String systemName = "testSystemName";
    String description = "Unit changed";
    HistoryLogEventType eventType = HistoryLogEventType.UPDATE;
    UUID docOfficeId = UUID.randomUUID();
    String officeAbbreviation = "BGH";
    DocumentationOfficeDTO documentationOfficeDTO =
        DocumentationOfficeDTO.builder().id(docOfficeId).abbreviation(officeAbbreviation).build();

    HistoryLogDTO dto =
        HistoryLogDTO.builder()
            .id(id)
            .createdAt(now)
            .userName(userName)
            .systemName(systemName)
            .description(description)
            .eventType(eventType)
            .documentationOffice(documentationOfficeDTO)
            .documentationUnitId(UUID.randomUUID())
            .build();

    HistoryLog expectedLog =
        HistoryLog.builder()
            .id(id)
            .createdAt(now)
            .createdBy(systemName)
            .documentationOffice(officeAbbreviation)
            .description(description)
            .eventType(eventType)
            .build();

    // Act
    HistoryLog actualLog = HistoryLogTransformer.transformToDomain(dto, null);

    // Assert
    assertThat(actualLog).isEqualTo(expectedLog);
  }

  @Test
  void testTransformToDomain_withNullUser_withoutSystemName_shouldReturnNoCreatedBy() {
    // Arrange
    UUID id = UUID.randomUUID();
    Instant now = Instant.now();
    String userName = "testUser";
    String description = "Unit changed";
    HistoryLogEventType eventType = HistoryLogEventType.UPDATE;
    UUID docOfficeId = UUID.randomUUID();
    String officeAbbreviation = "BGH";
    DocumentationOfficeDTO documentationOfficeDTO =
        DocumentationOfficeDTO.builder().id(docOfficeId).abbreviation(officeAbbreviation).build();

    HistoryLogDTO dto =
        HistoryLogDTO.builder()
            .id(id)
            .createdAt(now)
            .userName(userName)
            .description(description)
            .eventType(eventType)
            .documentationOffice(documentationOfficeDTO)
            .documentationUnitId(UUID.randomUUID())
            .build();

    HistoryLog expectedLog =
        HistoryLog.builder()
            .id(id)
            .createdAt(now)
            .createdBy(null)
            .documentationOffice(officeAbbreviation)
            .description(description)
            .eventType(eventType)
            .build();

    // Act
    HistoryLog actualLog = HistoryLogTransformer.transformToDomain(dto, null);

    // Assert
    assertThat(actualLog).isEqualTo(expectedLog);
  }
}
