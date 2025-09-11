package de.bund.digitalservice.ris.caselaw.adapter.transformer;

import static org.assertj.core.api.Assertions.assertThat;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentationOfficeDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.HistoryLogDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.UserDTO;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationOffice;
import de.bund.digitalservice.ris.caselaw.domain.HistoryLog;
import de.bund.digitalservice.ris.caselaw.domain.HistoryLogEventType;
import de.bund.digitalservice.ris.caselaw.domain.User;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class HistoryLogTransformerTest {

  private DocumentationOffice createDocOffice(UUID id, String abbreviation) {
    return DocumentationOffice.builder().id(id).abbreviation(abbreviation).build();
  }

  private User createUser(String name, DocumentationOffice office) {
    return User.builder().name(name).documentationOffice(office).build();
  }

  @Test
  void testTransformToDomain_withUserFromSameOffice_shouldShowUserName() {
    // Arrange
    UUID id = UUID.randomUUID();
    Instant now = Instant.now();
    String systemName = "testSystemName";
    String description = "HANDOVER";
    HistoryLogEventType eventType = HistoryLogEventType.HANDOVER;
    UUID docOfficeId = UUID.randomUUID();
    String officeAbbreviation = "BGH";

    HistoryLogDTO dto =
        HistoryLogDTO.builder()
            .id(id)
            .createdAt(now)
            .systemName(systemName)
            .user(
                UserDTO.builder()
                    .firstName("creatorUser")
                    .documentationOffice(
                        DocumentationOfficeDTO.builder()
                            .id(docOfficeId)
                            .abbreviation(officeAbbreviation)
                            .build())
                    .build())
            .description(description)
            .eventType(eventType)
            .documentationUnitId(UUID.randomUUID())
            .build();

    // Current user in the same office as the creator
    User currentUser = createUser("currentUser", createDocOffice(docOfficeId, officeAbbreviation));

    HistoryLog expected =
        HistoryLog.builder()
            .id(id)
            .createdAt(now)
            .createdBy("creatorUser")
            .documentationOffice(officeAbbreviation)
            .description(description)
            .eventType(eventType)
            .build();

    // Act
    HistoryLog result = HistoryLogTransformer.transformToDomain(dto, currentUser, null, null);

    // Assert
    assertThat(result).isEqualTo(expected);
  }

  @Test
  void testTransformToDomain_withUserFromDifferentOffice_shouldShowSystemName() {
    // Arrange
    UUID id = UUID.randomUUID();
    Instant now = Instant.now();
    String systemName = "testSystemName";
    String description = "UPDATE";
    HistoryLogEventType eventType = HistoryLogEventType.UPDATE;

    HistoryLogDTO dto =
        HistoryLogDTO.builder()
            .id(id)
            .createdAt(now)
            .systemName(systemName)
            .user(
                UserDTO.builder()
                    .firstName("creatorUser")
                    .documentationOffice(
                        DocumentationOfficeDTO.builder()
                            .id(UUID.randomUUID())
                            .abbreviation("BGH")
                            .build())
                    .build())
            .description(description)
            .eventType(eventType)
            .documentationUnitId(UUID.randomUUID())
            .build();

    // Current user in a different office than the creator
    User currentUser = createUser("currentUser", createDocOffice(UUID.randomUUID(), "DS"));

    HistoryLog expected =
        HistoryLog.builder()
            .id(id)
            .createdAt(now)
            .createdBy(systemName)
            .documentationOffice("BGH")
            .description(description)
            .eventType(eventType)
            .build();

    // Act
    HistoryLog result = HistoryLogTransformer.transformToDomain(dto, currentUser, null, null);

    // Assert
    assertThat(result).isEqualTo(expected);
  }

  @Test
  void
      testTransformToDomain_withUserFromDifferentOffice_withoutSystemName_shouldReturnNoCreatedBy() {
    // Arrange
    UUID id = UUID.randomUUID();
    Instant now = Instant.now();
    String description = "UPDATE";
    HistoryLogEventType eventType = HistoryLogEventType.UPDATE;
    String officeAbbreviation = "BGH";

    HistoryLogDTO dto =
        HistoryLogDTO.builder()
            .id(id)
            .createdAt(now)
            .description(description) // No system name set
            .user(
                UserDTO.builder()
                    .firstName("creatorUser")
                    .documentationOffice(
                        DocumentationOfficeDTO.builder()
                            .id(UUID.randomUUID())
                            .abbreviation(officeAbbreviation)
                            .build())
                    .build())
            .eventType(eventType)
            .documentationUnitId(UUID.randomUUID())
            .build();

    // Current user in a different office than the creator
    User currentUser = createUser("currentUser", createDocOffice(UUID.randomUUID(), "DS"));

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
    HistoryLog result = HistoryLogTransformer.transformToDomain(dto, currentUser, null, null);

    // Assert
    assertThat(result).isEqualTo(expected);
  }

  @Test
  void testTransformToDomain_withNullUser_shouldShowSystemName() {
    // Arrange
    UUID id = UUID.randomUUID();
    Instant now = Instant.now();
    String systemName = "testSystemName";
    String description = "UPDATE";
    HistoryLogEventType eventType = HistoryLogEventType.UPDATE;

    HistoryLogDTO dto =
        HistoryLogDTO.builder()
            .id(id)
            .createdAt(now)
            .systemName(systemName)
            .user(
                UserDTO.builder()
                    .firstName("creatorUser")
                    .documentationOffice(
                        DocumentationOfficeDTO.builder()
                            .id(UUID.randomUUID())
                            .abbreviation("DS")
                            .build())
                    .build())
            .description(description)
            .eventType(eventType)
            .documentationUnitId(UUID.randomUUID())
            .build();

    // No current user

    HistoryLog expectedLog =
        HistoryLog.builder()
            .id(id)
            .createdAt(now)
            .createdBy(systemName)
            .documentationOffice("DS")
            .description(description)
            .eventType(eventType)
            .build();

    // Act
    HistoryLog actualLog = HistoryLogTransformer.transformToDomain(dto, null, null, null);

    // Assert
    assertThat(actualLog).isEqualTo(expectedLog);
  }

  @Test
  void testTransformToDomain_withNullUser_withoutSystemName_shouldReturnNoCreatedBy() {
    // Arrange
    UUID id = UUID.randomUUID();
    Instant now = Instant.now();
    String description = "UPDATE";
    HistoryLogEventType eventType = HistoryLogEventType.UPDATE;

    HistoryLogDTO dto =
        HistoryLogDTO.builder()
            .id(id)
            .createdAt(now)
            .description(description)
            .user(
                UserDTO.builder()
                    .firstName("testUser")
                    .documentationOffice(
                        DocumentationOfficeDTO.builder()
                            .id(UUID.randomUUID())
                            .abbreviation("BGH")
                            .build())
                    .build())
            .eventType(eventType)
            .documentationUnitId(UUID.randomUUID())
            .build();

    // No current user, no system name

    HistoryLog expectedLog =
        HistoryLog.builder()
            .id(id)
            .createdAt(now)
            .createdBy(null) // Should be null
            .documentationOffice("BGH")
            .description(description)
            .eventType(eventType)
            .build();

    // Act
    HistoryLog actualLog = HistoryLogTransformer.transformToDomain(dto, null, null, null);

    // Assert
    assertThat(actualLog).isEqualTo(expectedLog);
  }

  @Test
  void testTransformToDomain_withNullCreatorUser_shouldShowSystemName() {
    // Arrange
    UUID id = UUID.randomUUID();
    Instant now = Instant.now();
    String systemName = "testSystemName";
    String description = "UPDATE";
    HistoryLogEventType eventType = HistoryLogEventType.UPDATE;

    HistoryLogDTO dto =
        HistoryLogDTO.builder()
            .id(id)
            .createdAt(now)
            .systemName(systemName)
            .user(null)
            .description(description)
            .eventType(eventType)
            .documentationUnitId(UUID.randomUUID())
            .build();

    User currentUser = createUser("currentUser", createDocOffice(UUID.randomUUID(), "DS"));

    HistoryLog expectedLog =
        HistoryLog.builder()
            .id(id)
            .createdAt(now)
            .createdBy(systemName) // System name is shown when creatorUser is null
            .documentationOffice(null) // Creator office is null if creatorUser is null
            .description(description)
            .eventType(eventType)
            .build();

    // Act
    HistoryLog actualLog = HistoryLogTransformer.transformToDomain(dto, currentUser, null, null);

    // Assert
    assertThat(actualLog).isEqualTo(expectedLog);
  }

  @Test
  void testTransformDescription_processStepUser_newPersonSet_sameOffice() {
    // Arrange
    UUID id = UUID.randomUUID();
    Instant now = Instant.now();
    HistoryLogEventType eventType = HistoryLogEventType.PROCESS_STEP_USER;
    UUID officeId = UUID.randomUUID();
    DocumentationOffice office = createDocOffice(officeId, "DS");

    HistoryLogDTO dto =
        HistoryLogDTO.builder()
            .id(id)
            .createdAt(now)
            .eventType(eventType)
            .user(
                UserDTO.builder()
                    .firstName("creatorUser")
                    .documentationOffice(
                        DocumentationOfficeDTO.builder().id(officeId).abbreviation("DS").build())
                    .build())
            .documentationUnitId(UUID.randomUUID())
            .build();

    User currentUser = createUser("currentUser", office);
    User toUser = createUser("New Person", office); // Same office as current user

    HistoryLog expected =
        HistoryLog.builder()
            .id(id)
            .createdAt(now)
            .createdBy("creatorUser")
            .documentationOffice(office.abbreviation())
            .description("Person gesetzt: New Person")
            .eventType(eventType)
            .build();

    // Act
    HistoryLog result = HistoryLogTransformer.transformToDomain(dto, currentUser, null, toUser);

    // Assert
    assertThat(result).isEqualTo(expected);
  }

  @Test
  void testTransformDescription_processStepUser_newPersonSet_differentOffice() {
    // Arrange
    UUID id = UUID.randomUUID();
    Instant now = Instant.now();
    HistoryLogEventType eventType = HistoryLogEventType.PROCESS_STEP_USER;

    HistoryLogDTO dto =
        HistoryLogDTO.builder()
            .id(id)
            .createdAt(now)
            .eventType(eventType)
            .user(
                UserDTO.builder()
                    .firstName("creatorUser")
                    .documentationOffice(
                        DocumentationOfficeDTO.builder()
                            .id(UUID.randomUUID())
                            .abbreviation("BGH")
                            .build())
                    .build())
            .documentationUnitId(UUID.randomUUID())
            .build(); // No initial description

    User currentUser = createUser("currentUser", createDocOffice(UUID.randomUUID(), "DS"));
    User toUser =
        createUser("New Person", createDocOffice(UUID.randomUUID(), "BGH")); // Different office

    HistoryLog expected =
        HistoryLog.builder()
            .id(id)
            .createdAt(now)
            .createdBy(null)
            .documentationOffice("BGH")
            .description("Person gesetzt") // Generic description
            .eventType(eventType)
            .build();

    // Act
    HistoryLog result = HistoryLogTransformer.transformToDomain(dto, currentUser, null, toUser);

    // Assert
    assertThat(result).isEqualTo(expected);
  }

  @Test
  void testTransformDescription_processStepUser_personRemoved_sameOffice() {
    // Arrange
    UUID id = UUID.randomUUID();
    Instant now = Instant.now();
    HistoryLogEventType eventType = HistoryLogEventType.PROCESS_STEP_USER;
    UUID officeId = UUID.randomUUID();
    DocumentationOffice office = createDocOffice(officeId, "DS");

    HistoryLogDTO dto =
        HistoryLogDTO.builder()
            .id(id)
            .createdAt(now)
            .eventType(eventType)
            .user(
                UserDTO.builder()
                    .firstName("creatorUser")
                    .documentationOffice(
                        DocumentationOfficeDTO.builder().id(officeId).abbreviation("DS").build())
                    .build())
            .documentationUnitId(UUID.randomUUID())
            .build(); // No initial description

    User currentUser = createUser("currentUser", office);
    User fromUser = createUser("Old Person", office);

    HistoryLog expected =
        HistoryLog.builder()
            .id(id)
            .createdAt(now)
            .createdBy("creatorUser")
            .documentationOffice(office.abbreviation())
            .description("Person entfernt: Old Person")
            .eventType(eventType)
            .build();

    // Act
    HistoryLog result = HistoryLogTransformer.transformToDomain(dto, currentUser, fromUser, null);

    // Assert
    assertThat(result).isEqualTo(expected);
  }

  @Test
  void testTransformDescription_processStepUser_personRemoved_differentOffice() {
    // Arrange
    UUID id = UUID.randomUUID();
    Instant now = Instant.now();
    HistoryLogEventType eventType = HistoryLogEventType.PROCESS_STEP_USER;
    DocumentationOffice otherDocOffice = createDocOffice(UUID.randomUUID(), "BGH");

    HistoryLogDTO dto =
        HistoryLogDTO.builder()
            .id(id)
            .createdAt(now)
            .eventType(eventType)
            .user(
                UserDTO.builder()
                    .firstName("creatorUser")
                    .documentationOffice(
                        DocumentationOfficeDTO.builder()
                            .id(UUID.randomUUID())
                            .abbreviation("BGH")
                            .build())
                    .build())
            .documentationUnitId(UUID.randomUUID())
            .build(); // No initial description

    User currentUser = createUser("currentUser", createDocOffice(UUID.randomUUID(), "DS"));
    User fromUser = createUser("Old Person", otherDocOffice);

    HistoryLog expected =
        HistoryLog.builder()
            .id(id)
            .createdAt(now)
            .createdBy(null)
            .documentationOffice(otherDocOffice.abbreviation())
            .description(
                "Person geändert") // Generic description ("entfernt" not in generic description)
            .eventType(eventType)
            .build();

    // Act
    HistoryLog result = HistoryLogTransformer.transformToDomain(dto, currentUser, fromUser, null);

    // Assert
    assertThat(result).isEqualTo(expected);
  }

  @Test
  void testTransformDescription_processStepUser_personChanged_sameOffice() {
    // Arrange
    UUID id = UUID.randomUUID();
    Instant now = Instant.now();
    HistoryLogEventType eventType = HistoryLogEventType.PROCESS_STEP_USER;
    UUID officeId = UUID.randomUUID();
    DocumentationOffice office = createDocOffice(officeId, "DS");

    HistoryLogDTO dto =
        HistoryLogDTO.builder()
            .id(id)
            .createdAt(now)
            .eventType(eventType)
            .user(
                UserDTO.builder()
                    .firstName("creatorUser")
                    .documentationOffice(
                        DocumentationOfficeDTO.builder().id(officeId).abbreviation("DS").build())
                    .build())
            .documentationUnitId(UUID.randomUUID())
            .build(); // No initial description

    User currentUser = createUser("currentUser", office);
    User fromUser = createUser("Old Person", office);
    User toUser = createUser("New Person", office);

    HistoryLog expected =
        HistoryLog.builder()
            .id(id)
            .createdAt(now)
            .createdBy("creatorUser")
            .documentationOffice(office.abbreviation())
            .description("Person geändert: Old Person → New Person")
            .eventType(eventType)
            .build();

    // Act
    HistoryLog result = HistoryLogTransformer.transformToDomain(dto, currentUser, fromUser, toUser);

    // Assert
    assertThat(result).isEqualTo(expected);
  }

  @Test
  void testTransformDescription_processStepUser_personChanged_differentOffice() {
    // Arrange
    UUID id = UUID.randomUUID();
    Instant now = Instant.now();
    HistoryLogEventType eventType = HistoryLogEventType.PROCESS_STEP_USER;
    DocumentationOffice otherDocOffice = createDocOffice(UUID.randomUUID(), "BGH");

    HistoryLogDTO dto =
        HistoryLogDTO.builder()
            .id(id)
            .createdAt(now)
            .eventType(eventType)
            .user(
                UserDTO.builder()
                    .firstName("creatorUser")
                    .documentationOffice(
                        DocumentationOfficeDTO.builder()
                            .id(otherDocOffice.id())
                            .abbreviation(otherDocOffice.abbreviation())
                            .build())
                    .build())
            .documentationUnitId(UUID.randomUUID())
            .build(); // No initial description

    User currentUser = createUser("currentUser", createDocOffice(UUID.randomUUID(), "DS"));
    User fromUser = createUser("Old Person", otherDocOffice);
    User toUser = createUser("New Person", otherDocOffice);

    HistoryLog expected =
        HistoryLog.builder()
            .id(id)
            .createdAt(now)
            .createdBy(null)
            .documentationOffice(otherDocOffice.abbreviation())
            .description("Person geändert") // Generic description
            .eventType(eventType)
            .build();

    // Act
    HistoryLog result = HistoryLogTransformer.transformToDomain(dto, currentUser, fromUser, toUser);

    // Assert
    assertThat(result).isEqualTo(expected);
  }

  @Test
  void testTransformDescription_processStepUser_personChanged_fromOfficeDifferentToSame() {
    // Arrange
    UUID id = UUID.randomUUID();
    Instant now = Instant.now();
    HistoryLogEventType eventType = HistoryLogEventType.PROCESS_STEP_USER;
    UUID currentUserOfficeId = UUID.randomUUID();
    UUID otherUserOfficeId = UUID.randomUUID();
    DocumentationOffice currentUserOffice = createDocOffice(currentUserOfficeId, "DS");
    DocumentationOffice otherUserOffice = createDocOffice(otherUserOfficeId, "DS");

    HistoryLogDTO dto =
        HistoryLogDTO.builder()
            .id(id)
            .createdAt(now)
            .eventType(eventType)
            .user(
                UserDTO.builder()
                    .firstName("creatorUser")
                    .documentationOffice(
                        DocumentationOfficeDTO.builder()
                            .id(otherUserOffice.id())
                            .abbreviation(otherUserOffice.abbreviation())
                            .build())
                    .build())
            .documentationUnitId(UUID.randomUUID())
            .build(); // No initial description

    User currentUser = createUser("currentUser", currentUserOffice);
    User fromUser = createUser("Old Person", otherUserOffice); // Different office
    User toUser = createUser("New Person", currentUserOffice); // Same office

    HistoryLog expected =
        HistoryLog.builder()
            .id(id)
            .createdAt(now)
            .createdBy(null)
            .documentationOffice(otherUserOffice.abbreviation())
            .description("Person geändert") // Generic because 'fromUser' office is different
            .eventType(eventType)
            .build();

    // Act
    HistoryLog result = HistoryLogTransformer.transformToDomain(dto, currentUser, fromUser, toUser);

    // Assert
    assertThat(result).isEqualTo(expected);
  }

  @Test
  void testTransformDescription_processStepUser_personChanged_fromOfficeSameToDifferent() {
    // Arrange
    UUID id = UUID.randomUUID();
    Instant now = Instant.now();
    HistoryLogEventType eventType = HistoryLogEventType.PROCESS_STEP_USER;
    UUID currentUserOfficeId = UUID.randomUUID();
    DocumentationOffice currentUserOffice = createDocOffice(currentUserOfficeId, "DS");

    HistoryLogDTO dto =
        HistoryLogDTO.builder()
            .id(id)
            .createdAt(now)
            .eventType(eventType)
            .user(
                UserDTO.builder()
                    .firstName("creatorUser")
                    .documentationOffice(
                        DocumentationOfficeDTO.builder()
                            .id(currentUserOffice.id())
                            .abbreviation(currentUserOffice.abbreviation())
                            .build())
                    .build())
            .documentationUnitId(UUID.randomUUID())
            .build(); // No initial description

    User currentUser = createUser("currentUser", currentUserOffice);
    User fromUser = createUser("Old Person", currentUserOffice); // Same office
    User toUser =
        createUser("New Person", createDocOffice(UUID.randomUUID(), "BGH")); // Different office

    HistoryLog expected =
        HistoryLog.builder()
            .id(id)
            .createdAt(now)
            .createdBy("creatorUser")
            .documentationOffice(currentUserOffice.abbreviation())
            .description("Person geändert") // Generic because 'toUser' office is different
            .eventType(eventType)
            .build();

    // Act
    HistoryLog result = HistoryLogTransformer.transformToDomain(dto, currentUser, fromUser, toUser);

    // Assert
    assertThat(result).isEqualTo(expected);
  }

  @Test
  void testTransformDescription_existingDescriptionTakesPrecedence() {
    // Arrange
    UUID id = UUID.randomUUID();
    Instant now = Instant.now();
    String systemName = "testSystemName";
    String preExistingDescription = "This is a custom description.";
    HistoryLogEventType eventType = HistoryLogEventType.PROCESS_STEP_USER; // Should be ignored

    HistoryLogDTO dto =
        HistoryLogDTO.builder()
            .id(id)
            .createdAt(now)
            .systemName(systemName)
            .user(null)
            .description(preExistingDescription) // Explicitly set description
            .eventType(eventType)
            .documentationUnitId(UUID.randomUUID())
            .build();

    User currentUser = createUser("currentUser", createDocOffice(UUID.randomUUID(), "DS"));
    User fromUser = createUser("Old Person", createDocOffice(UUID.randomUUID(), "BGH1"));
    User toUser = createUser("New Person", createDocOffice(UUID.randomUUID(), "BGH2"));

    HistoryLog expected =
        HistoryLog.builder()
            .id(id)
            .createdAt(now)
            .createdBy(systemName) // system name visible if creatorUser is null
            .documentationOffice(null) // creator office is null if creatorUser is null
            .description(preExistingDescription) // Should return the existing description
            .eventType(eventType)
            .build();

    // Act
    HistoryLog result = HistoryLogTransformer.transformToDomain(dto, currentUser, fromUser, toUser);

    // Assert
    assertThat(result).isEqualTo(expected);
  }
}
