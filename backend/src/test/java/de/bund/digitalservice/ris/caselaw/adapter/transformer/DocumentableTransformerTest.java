package de.bund.digitalservice.ris.caselaw.adapter.transformer;

import static org.assertj.core.api.Assertions.assertThat;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DecisionDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentationOfficeDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentationUnitDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.NormAbbreviationDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.NormReferenceDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.PendingProceedingDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.StatusDTO;
import de.bund.digitalservice.ris.caselaw.domain.Documentable;
import de.bund.digitalservice.ris.caselaw.domain.PublicationStatus;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class DocumentableTransformerTest {

  static UUID normAbbreviationId = UUID.randomUUID();

  public static List<DocumentationUnitDTO> getDTOsWithStatus() {
    StatusDTO status =
        StatusDTO.builder()
            .createdAt(Instant.now())
            .publicationStatus(PublicationStatus.UNPUBLISHED)
            .withError(false)
            .build();
    return List.of(
        generateSimplePendingProceedingDTOBuilder().status(status).build(),
        generateSimpleDecisionDTOBuilder().status(status).build());
  }

  @ParameterizedTest
  @MethodSource("getDTOsWithStatus")
  void testTransformToDomain_shouldTransformStatus(DocumentationUnitDTO dto) {
    Documentable documentable =
        dto instanceof PendingProceedingDTO
            ? PendingProceedingTransformer.transformToDomain((PendingProceedingDTO) dto)
            : DecisionTransformer.transformToDomain((DecisionDTO) dto);

    assertThat(documentable.status().publicationStatus()).isEqualTo(PublicationStatus.UNPUBLISHED);
    assertThat(documentable.status().withError()).isFalse();
  }

  public static List<DocumentationUnitDTO> getDTOsWithSameNormAbbreviation() {
    var norms =
        List.of(
            NormReferenceDTO.builder()
                .normAbbreviation(NormAbbreviationDTO.builder().id(normAbbreviationId).build())
                .singleNorm("single norm 1")
                .build(),
            NormReferenceDTO.builder()
                .normAbbreviation(NormAbbreviationDTO.builder().id(normAbbreviationId).build())
                .singleNorm("single norm 2")
                .build());
    return List.of(
        generateSimplePendingProceedingDTOBuilder().normReferences(norms).build(),
        generateSimpleDecisionDTOBuilder().normReferences(norms).build());
  }

  @ParameterizedTest
  @MethodSource("getDTOsWithSameNormAbbreviation")
  void testTransformToDomain_withMultipleNormReferences_withSameNormAbbreviation_shouldGroupNorms(
      DocumentationUnitDTO dto) {

    Documentable documentable =
        dto instanceof PendingProceedingDTO
            ? PendingProceedingTransformer.transformToDomain((PendingProceedingDTO) dto)
            : DecisionTransformer.transformToDomain((DecisionDTO) dto);

    assertThat(documentable.contentRelatedIndexing().norms()).hasSize(1);
    assertThat(documentable.contentRelatedIndexing().norms().getFirst().normAbbreviation().id())
        .isEqualTo(normAbbreviationId);
    assertThat(
            documentable
                .contentRelatedIndexing()
                .norms()
                .getFirst()
                .singleNorms()
                .getFirst()
                .singleNorm())
        .isEqualTo("single norm 1");
    assertThat(
            documentable
                .contentRelatedIndexing()
                .norms()
                .getFirst()
                .singleNorms()
                .get(1)
                .singleNorm())
        .isEqualTo("single norm 2");
  }

  public static List<DocumentationUnitDTO> getDTOsWithNoAbbreviation() {
    var norms =
        List.of(
            NormReferenceDTO.builder()
                .normAbbreviationRawValue("foo")
                .singleNorm("single norm 1")
                .build(),
            NormReferenceDTO.builder()
                .normAbbreviationRawValue("foo")
                .singleNorm("single norm 2")
                .build());
    return List.of(
        generateSimplePendingProceedingDTOBuilder().normReferences(norms).build(),
        generateSimpleDecisionDTOBuilder().normReferences(norms).build());
  }

  @ParameterizedTest
  @MethodSource("getDTOsWithNoAbbreviation")
  void
      testTransformToDomain_withMultipleNormReferences_withNoAbbreviation_withSameNAbbreviationRawValue_shouldGroupNorms(
          DocumentationUnitDTO dto) {
    Documentable documentable =
        dto instanceof PendingProceedingDTO
            ? PendingProceedingTransformer.transformToDomain((PendingProceedingDTO) dto)
            : DecisionTransformer.transformToDomain((DecisionDTO) dto);

    assertThat(documentable.contentRelatedIndexing().norms()).hasSize(1);
    assertThat(documentable.contentRelatedIndexing().norms().getFirst().normAbbreviationRawValue())
        .isEqualTo("foo");
    assertThat(
            documentable
                .contentRelatedIndexing()
                .norms()
                .getFirst()
                .singleNorms()
                .getFirst()
                .singleNorm())
        .isEqualTo("single norm 1");
    assertThat(
            documentable
                .contentRelatedIndexing()
                .norms()
                .getFirst()
                .singleNorms()
                .get(1)
                .singleNorm())
        .isEqualTo("single norm 2");
  }

  public static List<DocumentationUnitDTO> getDTOsWithDifferentNormAbbreviation() {
    var norms =
        List.of(
            NormReferenceDTO.builder()
                .normAbbreviation(NormAbbreviationDTO.builder().id(UUID.randomUUID()).build())
                .singleNorm("single norm 1")
                .build(),
            NormReferenceDTO.builder()
                .normAbbreviation(NormAbbreviationDTO.builder().id(UUID.randomUUID()).build())
                .singleNorm("single norm 2")
                .build());
    return List.of(
        generateSimplePendingProceedingDTOBuilder().normReferences(norms).build(),
        generateSimpleDecisionDTOBuilder().normReferences(norms).build());
  }

  @ParameterizedTest
  @MethodSource("getDTOsWithDifferentNormAbbreviation")
  void testTransformToDomain_withMultipleNormReferences_withDifferentNormAbbreviation(
      DocumentationUnitDTO dto) {
    Documentable documentable =
        dto instanceof PendingProceedingDTO
            ? PendingProceedingTransformer.transformToDomain((PendingProceedingDTO) dto)
            : DecisionTransformer.transformToDomain((DecisionDTO) dto);

    assertThat(documentable.contentRelatedIndexing().norms()).hasSize(2);
    assertThat(documentable.contentRelatedIndexing().norms().getFirst().normAbbreviation().id())
        .isEqualTo(dto.getNormReferences().getFirst().getNormAbbreviation().getId());
    assertThat(documentable.contentRelatedIndexing().norms().get(1).normAbbreviation().id())
        .isEqualTo(dto.getNormReferences().get(1).getNormAbbreviation().getId());
    assertThat(
            documentable
                .contentRelatedIndexing()
                .norms()
                .getFirst()
                .singleNorms()
                .getFirst()
                .singleNorm())
        .isEqualTo("single norm 1");
    assertThat(
            documentable
                .contentRelatedIndexing()
                .norms()
                .get(1)
                .singleNorms()
                .getFirst()
                .singleNorm())
        .isEqualTo("single norm 2");
  }

  private static PendingProceedingDTO.PendingProceedingDTOBuilder<?, ?>
      generateSimplePendingProceedingDTOBuilder() {
    return PendingProceedingDTO.builder()
        .documentationOffice(DocumentationOfficeDTO.builder().abbreviation("doc office").build());
  }

  private static DecisionDTO.DecisionDTOBuilder<?, ?> generateSimpleDecisionDTOBuilder() {
    return DecisionDTO.builder()
        .documentationOffice(DocumentationOfficeDTO.builder().abbreviation("doc office").build());
  }
}
