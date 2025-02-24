package de.bund.digitalservice.ris.caselaw.adapter.transformer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentationOfficeDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.NormAbbreviationDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.NormReferenceDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.PendingProceedingDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.StatusDTO;
import de.bund.digitalservice.ris.caselaw.domain.PendingProceeding;
import de.bund.digitalservice.ris.caselaw.domain.PublicationStatus;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class PendingProceedingTransformerTest {

  @Test
  void testTransformToDomain_withPendingProceedingDTOIsNull_shouldThrowException() {
    assertThatThrownBy(() -> PendingProceedingTransformer.transformToDomain(null))
        .isInstanceOf(DocumentationUnitTransformerException.class)
        .hasMessageContaining("Pending proceeding is null and won't transform");
  }

  @Test
  void testTransformToDomain_shouldTransformStatus() {
    PendingProceedingDTO pendingProceedingDTO =
        generateSimpleDTOBuilder()
            .status(
                StatusDTO.builder()
                    .createdAt(Instant.now())
                    .publicationStatus(PublicationStatus.UNPUBLISHED)
                    .withError(false)
                    .build())
            .build();

    PendingProceeding pendingProceeding =
        PendingProceedingTransformer.transformToDomain(pendingProceedingDTO);

    assertThat(pendingProceeding.status().publicationStatus())
        .isEqualTo(PublicationStatus.UNPUBLISHED);
    assertThat(pendingProceeding.status().withError()).isFalse();
  }

  @Test
  void
      testTransformToDomain_withMultipleNormReferences_withSameNormAbbreviation_shouldGroupNorms() {
    UUID normAbbreviationId = UUID.randomUUID();
    PendingProceedingDTO pendingProceedingDTO =
        generateSimpleDTOBuilder()
            .normReferences(
                List.of(
                    NormReferenceDTO.builder()
                        .normAbbreviation(
                            NormAbbreviationDTO.builder().id(normAbbreviationId).build())
                        .singleNorm("single norm 1")
                        .build(),
                    NormReferenceDTO.builder()
                        .normAbbreviation(
                            NormAbbreviationDTO.builder().id(normAbbreviationId).build())
                        .singleNorm("single norm 2")
                        .build()))
            .build();

    PendingProceeding pendingProceeding =
        PendingProceedingTransformer.transformToDomain(pendingProceedingDTO);

    assertThat(pendingProceeding.contentRelatedIndexing().norms()).hasSize(1);
    assertThat(
            pendingProceeding.contentRelatedIndexing().norms().getFirst().normAbbreviation().id())
        .isEqualTo(normAbbreviationId);
    assertThat(
            pendingProceeding
                .contentRelatedIndexing()
                .norms()
                .getFirst()
                .singleNorms()
                .getFirst()
                .singleNorm())
        .isEqualTo("single norm 1");
    assertThat(
            pendingProceeding
                .contentRelatedIndexing()
                .norms()
                .getFirst()
                .singleNorms()
                .get(1)
                .singleNorm())
        .isEqualTo("single norm 2");
  }

  @Test
  void
      testTransformToDomain_withMultipleNormReferences_withNoAbbreviation_withSameNAbbreviationRawValue_shouldGroupNorms() {
    PendingProceedingDTO pendingProceedingDTO =
        generateSimpleDTOBuilder()
            .normReferences(
                List.of(
                    NormReferenceDTO.builder()
                        .normAbbreviationRawValue("foo")
                        .singleNorm("single norm 1")
                        .build(),
                    NormReferenceDTO.builder()
                        .normAbbreviationRawValue("foo")
                        .singleNorm("single norm 2")
                        .build()))
            .build();

    PendingProceeding pendingProceeding =
        PendingProceedingTransformer.transformToDomain(pendingProceedingDTO);

    assertThat(pendingProceeding.contentRelatedIndexing().norms()).hasSize(1);
    assertThat(
            pendingProceeding
                .contentRelatedIndexing()
                .norms()
                .getFirst()
                .normAbbreviationRawValue())
        .isEqualTo("foo");
    assertThat(
            pendingProceeding
                .contentRelatedIndexing()
                .norms()
                .getFirst()
                .singleNorms()
                .getFirst()
                .singleNorm())
        .isEqualTo("single norm 1");
    assertThat(
            pendingProceeding
                .contentRelatedIndexing()
                .norms()
                .getFirst()
                .singleNorms()
                .get(1)
                .singleNorm())
        .isEqualTo("single norm 2");
  }

  @Test
  void testTransformToDomain_withMultipleNormReferences_withDifferentNormAbbreviation() {
    PendingProceedingDTO pendingProceedingDTO =
        generateSimpleDTOBuilder()
            .normReferences(
                List.of(
                    NormReferenceDTO.builder()
                        .normAbbreviation(
                            NormAbbreviationDTO.builder().id(UUID.randomUUID()).build())
                        .singleNorm("single norm 1")
                        .build(),
                    NormReferenceDTO.builder()
                        .normAbbreviation(
                            NormAbbreviationDTO.builder().id(UUID.randomUUID()).build())
                        .singleNorm("single norm 2")
                        .build()))
            .build();

    PendingProceeding pendingProceeding =
        PendingProceedingTransformer.transformToDomain(pendingProceedingDTO);

    assertThat(pendingProceeding.contentRelatedIndexing().norms()).hasSize(2);
    assertThat(
            pendingProceeding.contentRelatedIndexing().norms().getFirst().normAbbreviation().id())
        .isEqualTo(
            pendingProceedingDTO.getNormReferences().getFirst().getNormAbbreviation().getId());
    assertThat(pendingProceeding.contentRelatedIndexing().norms().get(1).normAbbreviation().id())
        .isEqualTo(pendingProceedingDTO.getNormReferences().get(1).getNormAbbreviation().getId());
    assertThat(
            pendingProceeding
                .contentRelatedIndexing()
                .norms()
                .getFirst()
                .singleNorms()
                .getFirst()
                .singleNorm())
        .isEqualTo("single norm 1");
    assertThat(
            pendingProceeding
                .contentRelatedIndexing()
                .norms()
                .get(1)
                .singleNorms()
                .getFirst()
                .singleNorm())
        .isEqualTo("single norm 2");
  }

  @Test
  void testTransformToDomain_withPendingProceedingFields() {
    PendingProceedingDTO decisionDTO =
        generateSimpleDTOBuilder()
            .appellant("appellant")
            .admissionOfAppeal("admission of appeal")
            .legalIssue("legal issue")
            .resolutionNote("resolution note")
            .isResolved(true)
            .build();
    PendingProceeding pendingProceeding =
        PendingProceedingTransformer.transformToDomain(decisionDTO);

    assertThat(pendingProceeding.appellant()).isEqualTo("appellant");
    assertThat(pendingProceeding.admissionOfAppeal()).isEqualTo("admission of appeal");
    assertThat(pendingProceeding.legalIssue()).isEqualTo("legal issue");
    assertThat(pendingProceeding.resolutionNote()).isEqualTo("resolution note");
    assertThat(pendingProceeding.isResolved()).isTrue();
  }

  private PendingProceedingDTO.PendingProceedingDTOBuilder<?, ?> generateSimpleDTOBuilder() {
    return PendingProceedingDTO.builder()
        .documentationOffice(DocumentationOfficeDTO.builder().abbreviation("doc office").build());
  }
}
