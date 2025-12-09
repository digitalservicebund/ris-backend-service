package de.bund.digitalservice.ris.caselaw.adapter.transformer;

import static org.assertj.core.api.Assertions.assertThat;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DecisionDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentationOfficeDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.NonApplicationNormDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.NormAbbreviationDTO;
import de.bund.digitalservice.ris.caselaw.domain.ContentRelatedIndexing;
import de.bund.digitalservice.ris.caselaw.domain.Decision;
import de.bund.digitalservice.ris.caselaw.domain.NormReference;
import de.bund.digitalservice.ris.caselaw.domain.SingleNorm;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.NormAbbreviation;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class NonApplicationNormTransformerTest {
  private static final UUID NORM_ABBREVIATION_UUID_1 = UUID.randomUUID();
  private static final UUID NORM_ABBREVIATION_UUID_2 = UUID.randomUUID();

  @Test
  void testTransformToDTO_addingANewSingleNormToTheExistingNormAbbreviation() {
    Decision decision = generateDecision();
    DecisionDTO documentationUnitDTO = generateDecisionDTO();
    NonApplicationNormDTO nonApplicationNormDTO1 =
        NonApplicationNormDTO.builder()
            .normAbbreviation(NormAbbreviationDTO.builder().id(NORM_ABBREVIATION_UUID_1).build())
            .singleNorm("single norm 1")
            .rank(1)
            .build();
    NonApplicationNormDTO nonApplicationNormDTO2 =
        NonApplicationNormDTO.builder()
            .normAbbreviation(NormAbbreviationDTO.builder().id(NORM_ABBREVIATION_UUID_1).build())
            .singleNorm("single norm 2")
            .rank(2)
            .build();
    addNonApplicationNormToDomain(
        decision, NORM_ABBREVIATION_UUID_1, "single norm 1", "single norm 2");
    addNonApplicationNormToDTO(documentationUnitDTO, NORM_ABBREVIATION_UUID_1, 1, "single norm 1");

    DecisionDTO result = DecisionTransformer.transformToDTO(documentationUnitDTO, decision);

    assertThat(result.getNonApplicationNorms())
        .usingRecursiveFieldByFieldElementComparator()
        .containsExactly(nonApplicationNormDTO1, nonApplicationNormDTO2);
  }

  @Test
  void testTransformToDTO_addingANewSingleNormWithANewNormAbbreviation() {
    Decision decision = generateDecision();
    DecisionDTO documentationUnitDTO = generateDecisionDTO();
    NonApplicationNormDTO nonApplicationNormDTO1 =
        NonApplicationNormDTO.builder()
            .normAbbreviation(NormAbbreviationDTO.builder().id(NORM_ABBREVIATION_UUID_1).build())
            .singleNorm("single norm 1")
            .rank(1)
            .build();
    NonApplicationNormDTO nonApplicationNormDTO2 =
        NonApplicationNormDTO.builder()
            .normAbbreviation(NormAbbreviationDTO.builder().id(NORM_ABBREVIATION_UUID_2).build())
            .singleNorm("single norm 2")
            .rank(2)
            .build();
    addNonApplicationNormToDomain(decision, NORM_ABBREVIATION_UUID_1, "single norm 1");
    addNonApplicationNormToDomain(decision, NORM_ABBREVIATION_UUID_2, "single norm 2");
    addNonApplicationNormToDTO(documentationUnitDTO, NORM_ABBREVIATION_UUID_1, 1, "single norm 1");

    DecisionDTO result = DecisionTransformer.transformToDTO(documentationUnitDTO, decision);

    assertThat(result.getNonApplicationNorms())
        .usingRecursiveFieldByFieldElementComparator()
        .containsExactly(nonApplicationNormDTO1, nonApplicationNormDTO2);
  }

  @Test
  void
      testTransformToDTO_withTwoNormsWithDifferentNormAbbreviationAddingANewSingleNormToTheFirstNormAbbreviation() {
    Decision decision = generateDecision();
    DecisionDTO documentationUnitDTO =
        DecisionDTO.builder().nonApplicationNorms(new ArrayList<>()).build();
    NonApplicationNormDTO nonApplicationNormDTO1 =
        NonApplicationNormDTO.builder()
            .normAbbreviation(NormAbbreviationDTO.builder().id(NORM_ABBREVIATION_UUID_1).build())
            .singleNorm("single norm 1")
            .rank(1)
            .build();
    // rank change because third one added to first reference
    NonApplicationNormDTO nonApplicationNormDTO2 =
        NonApplicationNormDTO.builder()
            .normAbbreviation(NormAbbreviationDTO.builder().id(NORM_ABBREVIATION_UUID_2).build())
            .singleNorm("single norm 2")
            .rank(3)
            .build();
    NonApplicationNormDTO nonApplicationNormDTO3 =
        NonApplicationNormDTO.builder()
            .normAbbreviation(NormAbbreviationDTO.builder().id(NORM_ABBREVIATION_UUID_1).build())
            .singleNorm("single norm 3")
            .rank(2)
            .build();
    addNonApplicationNormToDomain(
        decision, NORM_ABBREVIATION_UUID_1, "single norm 1", "single norm 3");
    addNonApplicationNormToDomain(decision, NORM_ABBREVIATION_UUID_2, "single norm 2");
    addNonApplicationNormToDTO(documentationUnitDTO, NORM_ABBREVIATION_UUID_1, 1, "single norm 1");
    addNonApplicationNormToDTO(documentationUnitDTO, NORM_ABBREVIATION_UUID_2, 2, "single norm 2");

    DecisionDTO result = DecisionTransformer.transformToDTO(documentationUnitDTO, decision);

    // sequence change. second norm go to the end
    assertThat(result.getNonApplicationNorms())
        .usingRecursiveFieldByFieldElementComparator()
        .containsExactly(nonApplicationNormDTO1, nonApplicationNormDTO3, nonApplicationNormDTO2);
  }

  @Test
  void testTransformToDTO_removeAExistingSingleNorm() {
    Decision decision = generateDecision();
    DecisionDTO documentationUnitDTO =
        DecisionDTO.builder().nonApplicationNorms(new ArrayList<>()).build();
    NonApplicationNormDTO nonApplicationNormDTO1 =
        NonApplicationNormDTO.builder()
            .normAbbreviation(NormAbbreviationDTO.builder().id(NORM_ABBREVIATION_UUID_1).build())
            .singleNorm("single norm 1")
            .rank(1)
            .build();
    addNonApplicationNormToDomain(decision, NORM_ABBREVIATION_UUID_1, "single norm 1");
    addNonApplicationNormToDTO(
        documentationUnitDTO, NORM_ABBREVIATION_UUID_1, 1, "single norm 1", "single norm 2");

    DecisionDTO result = DecisionTransformer.transformToDTO(documentationUnitDTO, decision);

    assertThat(result.getNonApplicationNorms())
        .usingRecursiveFieldByFieldElementComparator()
        .containsExactly(nonApplicationNormDTO1);
  }

  @Test
  void testTransformToDTO_removeAllExistingSingleNorm() {
    Decision decision = generateDecision();
    DecisionDTO documentationUnitDTO =
        DecisionDTO.builder().nonApplicationNorms(new ArrayList<>()).build();
    NonApplicationNormDTO nonApplicationNormDTO =
        NonApplicationNormDTO.builder()
            .normAbbreviation(NormAbbreviationDTO.builder().id(NORM_ABBREVIATION_UUID_1).build())
            .singleNorm(null)
            .rank(1)
            .build();
    addNonApplicationNormToDomain(decision, NORM_ABBREVIATION_UUID_1);
    addNonApplicationNormToDTO(
        documentationUnitDTO, NORM_ABBREVIATION_UUID_1, 1, "single norm 1", "single norm 2");

    DecisionDTO result = DecisionTransformer.transformToDTO(documentationUnitDTO, decision);

    assertThat(result.getNonApplicationNorms())
        .usingRecursiveFieldByFieldElementComparator()
        .containsExactly(nonApplicationNormDTO);
  }

  @Test
  void testTransformToDTO_removeExistingNormAbbreviation() {
    Decision decision = generateDecision();
    DecisionDTO documentationUnitDTO =
        DecisionDTO.builder().nonApplicationNorms(new ArrayList<>()).build();
    addNonApplicationNormToDTO(
        documentationUnitDTO, NORM_ABBREVIATION_UUID_1, 1, "single norm 1", "single norm 2");

    DecisionDTO result = DecisionTransformer.transformToDTO(documentationUnitDTO, decision);

    assertThat(result.getNonApplicationNorms()).isEmpty();
  }

  @Test
  void testTransformToDomain_twoSingleNormsForTwoDifferentAbbreviations() {
    DecisionDTO documentationUnitDTO = generateDecisionDTO();
    addNonApplicationNormToDTO(documentationUnitDTO, NORM_ABBREVIATION_UUID_1, 1, "single norm 1");
    addNonApplicationNormToDTO(documentationUnitDTO, NORM_ABBREVIATION_UUID_2, 2, "single norm 2");
    NormReference normReference1 =
        NormReference.builder()
            .normAbbreviation(
                NormAbbreviation.builder()
                    .id(NORM_ABBREVIATION_UUID_1)
                    .documentTypes(Collections.emptyList())
                    .build())
            .singleNorms(List.of(SingleNorm.builder().singleNorm("single norm 1").build()))
            .build();
    NormReference normReference2 =
        NormReference.builder()
            .normAbbreviation(
                NormAbbreviation.builder()
                    .id(NORM_ABBREVIATION_UUID_2)
                    .documentTypes(Collections.emptyList())
                    .build())
            .singleNorms(List.of(SingleNorm.builder().singleNorm("single norm 2").build()))
            .build();

    Decision result = DecisionTransformer.transformToDomain(documentationUnitDTO);

    assertThat(result.contentRelatedIndexing().nonApplicationNorms())
        .containsExactly(normReference1, normReference2);
  }

  @Test
  void testTransformToDomain_multipleSingleNormsForOneAbbreviation() {
    DecisionDTO decisionDTO = generateDecisionDTO();
    NormReference expected1 =
        NormReference.builder()
            .normAbbreviation(
                NormAbbreviation.builder()
                    .id(NORM_ABBREVIATION_UUID_1)
                    .documentTypes(Collections.emptyList())
                    .build())
            .singleNorms(
                List.of(
                    SingleNorm.builder().singleNorm("single norm 1").build(),
                    SingleNorm.builder().singleNorm("single norm 2").build()))
            .build();
    NormReference expected2 =
        NormReference.builder()
            .normAbbreviation(
                NormAbbreviation.builder()
                    .id(NORM_ABBREVIATION_UUID_2)
                    .documentTypes(Collections.emptyList())
                    .build())
            .singleNorms(List.of(SingleNorm.builder().singleNorm("single norm 2").build()))
            .build();

    addNonApplicationNormToDTO(decisionDTO, NORM_ABBREVIATION_UUID_1, 1, "single norm 1");
    addNonApplicationNormToDTO(decisionDTO, NORM_ABBREVIATION_UUID_1, 3, "single norm 2");
    addNonApplicationNormToDTO(decisionDTO, NORM_ABBREVIATION_UUID_2, 2, "single norm 2");

    Decision result = DecisionTransformer.transformToDomain(decisionDTO);

    assertThat(result.contentRelatedIndexing().nonApplicationNorms())
        .containsExactly(expected1, expected2);
  }

  private Decision generateDecision() {
    ContentRelatedIndexing contentRelatedIndexing =
        ContentRelatedIndexing.builder().nonApplicationNorms(new ArrayList<>()).build();
    return Decision.builder().contentRelatedIndexing(contentRelatedIndexing).build();
  }

  private DecisionDTO generateDecisionDTO() {
    return DecisionDTO.builder()
        .documentationOffice(DocumentationOfficeDTO.builder().build())
        .nonApplicationNorms(new ArrayList<>())
        .build();
  }

  private void addNonApplicationNormToDomain(
      Decision decision, UUID normAbbreviationId, String... singleNormTexts) {
    NormAbbreviation normAbbreviation = NormAbbreviation.builder().id(normAbbreviationId).build();
    List<SingleNorm> singleNorms = new ArrayList<>();
    for (String singleNormText : singleNormTexts) {
      singleNorms.add(SingleNorm.builder().singleNorm(singleNormText).build());
    }
    NormReference normReference =
        NormReference.builder().normAbbreviation(normAbbreviation).singleNorms(singleNorms).build();
    decision.contentRelatedIndexing().nonApplicationNorms().add(normReference);
  }

  private void addNonApplicationNormToDTO(
      DecisionDTO documentationUnitDTO,
      UUID normAbbreviationId,
      int rank,
      String... singleNormTexts) {

    NormAbbreviationDTO normAbbreviationDTO =
        NormAbbreviationDTO.builder().id(normAbbreviationId).build();
    for (String singleNormText : singleNormTexts) {
      documentationUnitDTO
          .getNonApplicationNorms()
          .add(
              NonApplicationNormDTO.builder()
                  .normAbbreviation(normAbbreviationDTO)
                  .singleNorm(singleNormText)
                  .rank(rank)
                  .build());
    }
  }
}
