package de.bund.digitalservice.ris.caselaw.adapter;

import static org.assertj.core.api.Assertions.assertThat;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.CourtDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentationUnitDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DuplicateRelationDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.FileNumberDTO;
import de.bund.digitalservice.ris.caselaw.adapter.transformer.DuplicateRelationTransformer;
import de.bund.digitalservice.ris.caselaw.domain.DuplicateRelation;
import de.bund.digitalservice.ris.caselaw.domain.DuplicateRelationStatus;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.Test;

public class DuplicateRelationTransformerTest {

  @Test
  public void testTransformToDomain_withFullCoreData_shouldReturnCoreDataStrings() {
    // Arrange
    UUID uuid1 = UUID.randomUUID();
    UUID uuid2 = UUID.randomUUID();

    var decisionDate = LocalDate.of(2020, 1, 14);

    DocumentationUnitDTO original =
        DocumentationUnitDTO.builder().id(uuid1).documentNumber("documentNumber1").build();
    DocumentationUnitDTO duplicate =
        DocumentationUnitDTO.builder()
            .id(uuid2)
            .documentNumber("documentNumber2")
            .court(CourtDTO.builder().type("AG").location("Aachen").build())
            .decisionDate(decisionDate)
            .fileNumbers(List.of(FileNumberDTO.builder().value("my-file-number-1").build()))
            .build();

    DuplicateRelationDTO duplicateRelationDTO =
        DuplicateRelationDTO.builder()
            .status(DuplicateRelationStatus.IGNORED)
            .id(
                DuplicateRelationDTO.DuplicateRelationId.builder()
                    .documentationUnitId1(uuid1)
                    .documentationUnitId2(uuid2)
                    .build())
            .documentationUnit1(original)
            .documentationUnit2(duplicate)
            .build();

    // Act
    DuplicateRelation duplicateRelation =
        DuplicateRelationTransformer.transformToDomain(duplicateRelationDTO, original);

    // Assert
    var expected =
        DuplicateRelation.builder()
            .status(DuplicateRelationStatus.IGNORED)
            .documentNumber("documentNumber2")
            .fileNumber("my-file-number-1")
            .courtLabel("AG Aachen")
            .decisionDate(decisionDate)
            .isJdvDuplicateCheckActive(true)
            .build();
    assertThat(duplicateRelation).isEqualTo(expected);
  }

  @Test
  public void
      testTransformToDomain_withIsJdvDuplicateCheckActiveNotFalse_shouldReturnIsJdvDuplicateCheckActiveTrue() {
    // Arrange
    UUID uuid1 = UUID.randomUUID();
    UUID uuid2 = UUID.randomUUID();

    DocumentationUnitDTO documentationUnitDTO1 =
        DocumentationUnitDTO.builder()
            .id(uuid1)
            .documentNumber("documentNumber1")
            .isJdvDuplicateCheckActive(true)
            .build();
    DocumentationUnitDTO documentationUnitDTO2 =
        DocumentationUnitDTO.builder()
            .id(uuid2)
            .documentNumber("documentNumber2")
            .isJdvDuplicateCheckActive(null)
            .build();

    DuplicateRelationDTO duplicateRelationDTO =
        DuplicateRelationDTO.builder()
            .status(DuplicateRelationStatus.IGNORED)
            .id(
                DuplicateRelationDTO.DuplicateRelationId.builder()
                    .documentationUnitId1(uuid1)
                    .documentationUnitId2(uuid2)
                    .build())
            .documentationUnit1(documentationUnitDTO1)
            .documentationUnit2(documentationUnitDTO2)
            .build();

    // Act
    DuplicateRelation duplicateRelation =
        DuplicateRelationTransformer.transformToDomain(duplicateRelationDTO, documentationUnitDTO1);

    // Assert
    var expected =
        DuplicateRelation.builder()
            .status(DuplicateRelationStatus.IGNORED)
            .documentNumber("documentNumber2")
            .isJdvDuplicateCheckActive(true)
            .build();
    assertThat(duplicateRelation).isEqualTo(expected);
  }

  @Test
  public void
      testTransformToDomain_withIsJdvDuplicateCheckActiveFalse_shouldReturnIsJdvDuplicateCheckActiveFalse() {
    // Arrange
    UUID uuid1 = UUID.randomUUID();
    UUID uuid2 = UUID.randomUUID();

    DocumentationUnitDTO documentationUnitDTO1 =
        DocumentationUnitDTO.builder()
            .id(uuid1)
            .documentNumber("documentNumber1")
            .isJdvDuplicateCheckActive(false)
            .build();
    DocumentationUnitDTO documentationUnitDTO2 =
        DocumentationUnitDTO.builder()
            .id(uuid2)
            .documentNumber("documentNumber2")
            .isJdvDuplicateCheckActive(true)
            .build();

    DuplicateRelationDTO duplicateRelationDTO =
        DuplicateRelationDTO.builder()
            .status(DuplicateRelationStatus.IGNORED)
            .id(
                DuplicateRelationDTO.DuplicateRelationId.builder()
                    .documentationUnitId1(uuid1)
                    .documentationUnitId2(uuid2)
                    .build())
            .documentationUnit1(documentationUnitDTO1)
            .documentationUnit2(documentationUnitDTO2)
            .build();

    // Act
    DuplicateRelation duplicateRelation =
        DuplicateRelationTransformer.transformToDomain(duplicateRelationDTO, documentationUnitDTO1);

    // Assert
    var expected =
        DuplicateRelation.builder()
            .status(DuplicateRelationStatus.IGNORED)
            .documentNumber("documentNumber2")
            .isJdvDuplicateCheckActive(false)
            .build();
    assertThat(duplicateRelation).isEqualTo(expected);
  }
}
