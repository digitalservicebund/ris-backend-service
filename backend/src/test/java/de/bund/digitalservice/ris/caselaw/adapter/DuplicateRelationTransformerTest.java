package de.bund.digitalservice.ris.caselaw.adapter;

import static org.assertj.core.api.Assertions.assertThat;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentationUnitDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DuplicateRelationDTO;
import de.bund.digitalservice.ris.caselaw.adapter.transformer.DuplicateRelationTransformer;
import de.bund.digitalservice.ris.caselaw.domain.DuplicateRelation;
import de.bund.digitalservice.ris.caselaw.domain.DuplicateRelationStatus;
import java.util.UUID;
import org.junit.Test;

public class DuplicateRelationTransformerTest {

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
