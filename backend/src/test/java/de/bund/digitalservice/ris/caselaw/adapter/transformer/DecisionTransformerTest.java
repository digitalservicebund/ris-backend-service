package de.bund.digitalservice.ris.caselaw.adapter.transformer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.ActiveCitationDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.CaselawReferenceDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.CollectiveAgreementDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.CourtDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DecisionDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DeviatingCourtDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DeviatingDateDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DeviatingEcliDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DeviatingFileNumberDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DismissalGroundsDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DismissalTypesDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentTypeDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentalistDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentationOfficeDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.EnsuingDecisionDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.InputTypeDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.JobProfileDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.LeadingDecisionNormReferenceDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.LegalEffectDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.LegalPeriodicalEditionDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.LiteratureReferenceDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.NormReferenceDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.ParticipatingJudgeDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.PendingDecisionDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.PreviousDecisionDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.ProcedureDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.SourceDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.YearOfDisputeDTO;
import de.bund.digitalservice.ris.caselaw.domain.ActiveCitation;
import de.bund.digitalservice.ris.caselaw.domain.ContentRelatedIndexing;
import de.bund.digitalservice.ris.caselaw.domain.CoreData;
import de.bund.digitalservice.ris.caselaw.domain.CoreData.CoreDataBuilder;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationOffice;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnit;
import de.bund.digitalservice.ris.caselaw.domain.EnsuingDecision;
import de.bund.digitalservice.ris.caselaw.domain.LegalForce;
import de.bund.digitalservice.ris.caselaw.domain.LongTexts;
import de.bund.digitalservice.ris.caselaw.domain.ManagementData;
import de.bund.digitalservice.ris.caselaw.domain.NormReference;
import de.bund.digitalservice.ris.caselaw.domain.PreviousDecision;
import de.bund.digitalservice.ris.caselaw.domain.Reference;
import de.bund.digitalservice.ris.caselaw.domain.ReferenceType;
import de.bund.digitalservice.ris.caselaw.domain.ShortTexts;
import de.bund.digitalservice.ris.caselaw.domain.SingleNorm;
import de.bund.digitalservice.ris.caselaw.domain.Source;
import de.bund.digitalservice.ris.caselaw.domain.SourceValue;
import de.bund.digitalservice.ris.caselaw.domain.court.Court;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.LegalForceType;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.NormAbbreviation;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.ParticipatingJudge;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.Region;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Year;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

class DecisionTransformerTest {
  @Test
  void testTransformToDTO_withoutCoreData() {
    DecisionDTO currentDto = DecisionDTO.builder().build();
    DocumentationUnit updatedDomainObject = DocumentationUnit.builder().build();

    DecisionDTO decisionDTO = DecisionTransformer.transformToDTO(currentDto, updatedDomainObject);

    assertThat(decisionDTO.getProcedureHistory()).isEmpty();
    assertThat(decisionDTO.getProcedure()).isNull();
    assertThat(decisionDTO.getEcli()).isNull();
    assertThat(decisionDTO.getJudicialBody()).isNull();
    assertThat(decisionDTO.getDate()).isNull();
    assertThat(decisionDTO.getScheduledPublicationDateTime()).isNull();
    assertThat(decisionDTO.getLastPublicationDateTime()).isNull();
    assertThat(decisionDTO.getCourt()).isNull();
    assertThat(decisionDTO.getDocumentType()).isNull();
    assertThat(decisionDTO.getDocumentationOffice()).isNull();
  }

  @Test
  void testTransformToDTO_withoutDecisionNames() {
    DecisionDTO currentDto = DecisionDTO.builder().build();
    ShortTexts shortTexts = ShortTexts.builder().build();
    DocumentationUnit updatedDomainObject =
        DocumentationUnit.builder().shortTexts(shortTexts).build();

    DecisionDTO decisionDTO = DecisionTransformer.transformToDTO(currentDto, updatedDomainObject);

    assertThat(decisionDTO.getDecisionNames()).isEmpty();
  }

  @Test
  void testTransformToDTO_addLegalEffectWithCoreDataDeleted_shouldSetLegalEffectToNull() {
    DecisionDTO currentDto = DecisionDTO.builder().court(CourtDTO.builder().build()).build();
    DocumentationUnit updatedDomainObject = DocumentationUnit.builder().build();

    DecisionDTO decisionDTO = DecisionTransformer.transformToDTO(currentDto, updatedDomainObject);

    assertThat(decisionDTO.getLegalEffect()).isNull();
  }

  @Test
  void testTransformToDTO_withBlankString_shouldConvertToNull() {
    DecisionDTO currentDto = DecisionDTO.builder().note("before change").build();

    DocumentationUnit updatedDomainObject = DocumentationUnit.builder().note("  ").build();

    DecisionDTO decisionDTO = DecisionTransformer.transformToDTO(currentDto, updatedDomainObject);

    assertNull(decisionDTO.getNote());
  }

  @Test
  void
      testTransformToDTO_addLegalEffectWithCourtChangedAndNotSuperiorCourtAndLegalEffectNo_shouldSetLegalEffectToNo() {
    DecisionDTO currentDto =
        DecisionDTO.builder()
            .court(
                CourtDTO.builder()
                    .id(UUID.fromString("CCCCCCCC-1111-2222-3333-444444444444"))
                    .build())
            .build();
    DocumentationUnit updatedDomainObject =
        DocumentationUnit.builder()
            .coreData(
                CoreData.builder()
                    .court(
                        Court.builder()
                            .id(UUID.fromString("CCCCCCCC-2222-3333-4444-55555555555"))
                            .type("not superior")
                            .build())
                    .legalEffect("Nein")
                    .build())
            .build();

    DecisionDTO decisionDTO = DecisionTransformer.transformToDTO(currentDto, updatedDomainObject);

    assertThat(decisionDTO.getLegalEffect()).isEqualTo(LegalEffectDTO.NEIN);
  }

  @Test
  void
      testTransformToDTO_addLegalEffectWithCourtChangedAndNotSuperiorCourtAndLegalEffectNotSpecified_shouldSetLegalEffectToNotSpecified() {
    DecisionDTO currentDto =
        DecisionDTO.builder()
            .court(
                CourtDTO.builder()
                    .id(UUID.fromString("CCCCCCCC-1111-2222-3333-444444444444"))
                    .build())
            .build();
    DocumentationUnit updatedDomainObject =
        DocumentationUnit.builder()
            .coreData(
                CoreData.builder()
                    .court(
                        Court.builder()
                            .id(UUID.fromString("CCCCCCCC-2222-3333-4444-55555555555"))
                            .type("not superior")
                            .build())
                    .legalEffect("Keine Angabe")
                    .build())
            .build();

    DecisionDTO decisionDTO = DecisionTransformer.transformToDTO(currentDto, updatedDomainObject);

    assertThat(decisionDTO.getLegalEffect()).isEqualTo(LegalEffectDTO.KEINE_ANGABE);
  }

  @Test
  void
      testTransformToDTO_addLegalEffectWithCourtChangedAndNotSuperiorCourtAndLegalEffectYes_shouldSetLegalEffectToYes() {
    DecisionDTO currentDto =
        DecisionDTO.builder()
            .court(
                CourtDTO.builder()
                    .id(UUID.fromString("CCCCCCCC-1111-2222-3333-444444444444"))
                    .build())
            .build();
    DocumentationUnit updatedDomainObject =
        DocumentationUnit.builder()
            .coreData(
                CoreData.builder()
                    .court(
                        Court.builder()
                            .id(UUID.fromString("CCCCCCCC-2222-3333-4444-55555555555"))
                            .type("not superior")
                            .build())
                    .legalEffect("Ja")
                    .build())
            .build();

    DecisionDTO decisionDTO = DecisionTransformer.transformToDTO(currentDto, updatedDomainObject);

    assertThat(decisionDTO.getLegalEffect()).isEqualTo(LegalEffectDTO.JA);
  }

  @ParameterizedTest
  @ValueSource(strings = {"BGH", "BVerwG", "BFH", "BVerfG", "BAG", "BSG"})
  void testTransformToDTO_addLegalEffectWithCourtChangedAndSuperiorCourt_shouldSetLegalEffectToYes(
      String courtType) {
    DecisionDTO currentDto =
        DecisionDTO.builder()
            .court(
                CourtDTO.builder()
                    .id(UUID.fromString("CCCCCCCC-1111-2222-3333-444444444444"))
                    .build())
            .build();
    DocumentationUnit updatedDomainObject =
        DocumentationUnit.builder()
            .coreData(
                CoreData.builder()
                    .court(
                        Court.builder()
                            .id(UUID.fromString("CCCCCCCC-2222-3333-4444-55555555555"))
                            .type(courtType)
                            .build())
                    .legalEffect("Nein")
                    .build())
            .build();

    DecisionDTO decisionDTO = DecisionTransformer.transformToDTO(currentDto, updatedDomainObject);

    assertThat(decisionDTO.getLegalEffect()).isEqualTo(LegalEffectDTO.JA);
  }

  @Test
  void testTransformToDTO_withInputTypes() {
    DecisionDTO currentDto = DecisionDTO.builder().build();
    List<String> inputTypes = List.of("input types 1", "input types 3", "input types 2");
    DocumentationUnit updatedDomainObject =
        DocumentationUnit.builder()
            .coreData(CoreData.builder().inputTypes(inputTypes).build())
            .build();

    DecisionDTO decisionDTO = DecisionTransformer.transformToDTO(currentDto, updatedDomainObject);

    assertThat(decisionDTO.getInputTypes())
        .usingRecursiveFieldByFieldElementComparator()
        .containsExactly(
            InputTypeDTO.builder().value("input types 1").rank(1L).build(),
            InputTypeDTO.builder().value("input types 3").rank(2L).build(),
            InputTypeDTO.builder().value("input types 2").rank(3L).build());
  }

  @Test
  void testTransformToDTO_withOneNormReference_withLegalForce() {
    DecisionDTO currentDto = DecisionDTO.builder().build();
    NormReference normReferenceInput =
        NormReference.builder()
            .normAbbreviation(
                NormAbbreviation.builder()
                    .id(UUID.fromString("33333333-2222-3333-4444-555555555555"))
                    .build())
            .singleNorms(
                List.of(
                    SingleNorm.builder()
                        .singleNorm("single norm")
                        .legalForce(
                            LegalForce.builder()
                                .type(LegalForceType.builder().id(UUID.randomUUID()).build())
                                .region(Region.builder().id(UUID.randomUUID()).build())
                                .build())
                        .build()))
            .build();

    DocumentationUnit updatedDomainObject =
        DocumentationUnit.builder()
            .contentRelatedIndexing(
                ContentRelatedIndexing.builder().norms(List.of(normReferenceInput)).build())
            .build();

    DecisionDTO decisionDTO = DecisionTransformer.transformToDTO(currentDto, updatedDomainObject);

    assertThat(decisionDTO.getNormReferences().getFirst().getLegalForce()).isNotNull();
    assertThat(
            decisionDTO.getNormReferences().getFirst().getLegalForce().getLegalForceType().getId())
        .isEqualTo(normReferenceInput.singleNorms().getFirst().legalForce().type().id());
    assertThat(decisionDTO.getNormReferences().getFirst().getLegalForce().getRegion().getId())
        .isEqualTo(normReferenceInput.singleNorms().getFirst().legalForce().region().id());
  }

  @Test
  void testTransformToDTO_withOneNormReference_withMultipleSingleNorms_withLegalForce() {
    DecisionDTO currentDto = DecisionDTO.builder().build();
    NormReference normReferenceInput =
        NormReference.builder()
            .normAbbreviation(
                NormAbbreviation.builder()
                    .id(UUID.fromString("33333333-2222-3333-4444-555555555555"))
                    .build())
            .singleNorms(
                List.of(
                    SingleNorm.builder()
                        .singleNorm("single norm 1")
                        .legalForce(
                            LegalForce.builder()
                                .type(LegalForceType.builder().id(UUID.randomUUID()).build())
                                .region(Region.builder().id(UUID.randomUUID()).build())
                                .build())
                        .build(),
                    SingleNorm.builder()
                        .singleNorm("single norm 2")
                        .legalForce(
                            LegalForce.builder()
                                .type(LegalForceType.builder().id(UUID.randomUUID()).build())
                                .region(Region.builder().id(UUID.randomUUID()).build())
                                .build())
                        .build()))
            .build();

    DocumentationUnit updatedDomainObject =
        DocumentationUnit.builder()
            .contentRelatedIndexing(
                ContentRelatedIndexing.builder().norms(List.of(normReferenceInput)).build())
            .build();

    DecisionDTO decisionDTO = DecisionTransformer.transformToDTO(currentDto, updatedDomainObject);

    assertThat(decisionDTO.getNormReferences().getFirst().getNormAbbreviation().getId())
        .isEqualTo(normReferenceInput.normAbbreviation().id());
    assertThat(decisionDTO.getNormReferences().getFirst().getSingleNorm())
        .isEqualTo(normReferenceInput.singleNorms().getFirst().singleNorm());
    assertThat(decisionDTO.getNormReferences().getFirst().getLegalForce()).isNotNull();
    assertThat(
            decisionDTO.getNormReferences().getFirst().getLegalForce().getLegalForceType().getId())
        .isEqualTo(normReferenceInput.singleNorms().getFirst().legalForce().type().id());
    assertThat(decisionDTO.getNormReferences().getFirst().getLegalForce().getRegion().getId())
        .isEqualTo(normReferenceInput.singleNorms().getFirst().legalForce().region().id());

    assertThat(decisionDTO.getNormReferences().get(1).getSingleNorm())
        .isEqualTo(normReferenceInput.singleNorms().get(1).singleNorm());
    assertThat(decisionDTO.getNormReferences().get(1).getLegalForce()).isNotNull();
    assertThat(decisionDTO.getNormReferences().get(1).getLegalForce().getLegalForceType().getId())
        .isEqualTo(normReferenceInput.singleNorms().get(1).legalForce().type().id());
    assertThat(decisionDTO.getNormReferences().get(1).getLegalForce().getRegion().getId())
        .isEqualTo(normReferenceInput.singleNorms().get(1).legalForce().region().id());
  }

  @Test
  void testTransformToDTO_withMultipleNormReferences() {
    DecisionDTO currentDto = DecisionDTO.builder().build();
    NormReference normReferenceInput1 =
        NormReference.builder()
            .normAbbreviation(
                NormAbbreviation.builder()
                    .id(UUID.fromString("33333333-2222-3333-4444-555555555555"))
                    .build())
            .singleNorms(List.of(SingleNorm.builder().singleNorm("single norm 1").build()))
            .build();

    NormReference normReferenceInput2 =
        NormReference.builder()
            .normAbbreviation(
                NormAbbreviation.builder()
                    .id(UUID.fromString("33333333-2222-3333-4444-555555555555"))
                    .build())
            .singleNorms(
                List.of(
                    SingleNorm.builder()
                        .singleNorm("single norm 2")
                        .legalForce(
                            LegalForce.builder()
                                .type(LegalForceType.builder().id(UUID.randomUUID()).build())
                                .region(Region.builder().id(UUID.randomUUID()).build())
                                .build())
                        .build()))
            .build();

    DocumentationUnit updatedDomainObject =
        DocumentationUnit.builder()
            .contentRelatedIndexing(
                ContentRelatedIndexing.builder()
                    .norms(List.of(normReferenceInput1, normReferenceInput2))
                    .build())
            .build();

    DecisionDTO decisionDTO = DecisionTransformer.transformToDTO(currentDto, updatedDomainObject);

    assertThat(decisionDTO.getNormReferences().getFirst().getNormAbbreviation().getId())
        .isEqualTo(normReferenceInput1.normAbbreviation().id());
    assertThat(decisionDTO.getNormReferences().getFirst().getSingleNorm())
        .isEqualTo(normReferenceInput1.singleNorms().getFirst().singleNorm());

    assertThat(decisionDTO.getNormReferences().get(1).getNormAbbreviation().getId())
        .isEqualTo(normReferenceInput2.normAbbreviation().id());
    assertThat(decisionDTO.getNormReferences().get(1).getSingleNorm())
        .isEqualTo(normReferenceInput2.singleNorms().getFirst().singleNorm());
    assertThat(decisionDTO.getNormReferences().get(1).getLegalForce()).isNotNull();
    assertThat(decisionDTO.getNormReferences().get(1).getLegalForce().getLegalForceType().getId())
        .isEqualTo(normReferenceInput2.singleNorms().getFirst().legalForce().type().id());
    assertThat(decisionDTO.getNormReferences().get(1).getLegalForce().getRegion().getId())
        .isEqualTo(normReferenceInput2.singleNorms().getFirst().legalForce().region().id());
  }

  @Test
  void testTransformToDTO_withNormReference_withoutNormAbbreviation_throwsException() {
    DecisionDTO currentDto = DecisionDTO.builder().build();
    NormReference normReferenceInput =
        NormReference.builder()
            .singleNorms(List.of(SingleNorm.builder().singleNorm("single norm 1").build()))
            .build();

    DocumentationUnit updatedDomainObject =
        DocumentationUnit.builder()
            .contentRelatedIndexing(
                ContentRelatedIndexing.builder().norms(List.of(normReferenceInput)).build())
            .build();

    Exception exception =
        assertThrows(
            DocumentationUnitTransformerException.class,
            () -> DecisionTransformer.transformToDTO(currentDto, updatedDomainObject));

    String expectedMessage = "Norm reference has no norm abbreviation, but is required.";
    String actualMessage = exception.getMessage();

    Assertions.assertTrue(actualMessage.contains(expectedMessage));
  }

  @Test
  void testTransformToDTO_withLeadingDecisionNormReferences() {
    DecisionDTO currentDto = DecisionDTO.builder().build();
    List<String> leadingDecisionNormReferences = List.of("BGB §1", "BGB §2", "BGB §3");
    DocumentationUnit updatedDomainObject =
        DocumentationUnit.builder()
            .coreData(
                CoreData.builder()
                    .court(
                        Court.builder()
                            .id(UUID.fromString("CCCCCCCC-2222-3333-4444-55555555555"))
                            .type("BGH")
                            .build())
                    .leadingDecisionNormReferences(leadingDecisionNormReferences)
                    .build())
            .build();

    DecisionDTO decisionDTO = DecisionTransformer.transformToDTO(currentDto, updatedDomainObject);

    assertThat(decisionDTO.getLeadingDecisionNormReferences())
        .usingRecursiveFieldByFieldElementComparator()
        .containsExactly(
            LeadingDecisionNormReferenceDTO.builder().normReference("BGB §1").rank(1).build(),
            LeadingDecisionNormReferenceDTO.builder().normReference("BGB §2").rank(2).build(),
            LeadingDecisionNormReferenceDTO.builder().normReference("BGB §3").rank(3).build());
  }

  @Test
  void testTransformToDTO_deletedLeadingDecisionNormReferences() {
    DecisionDTO currentDto =
        DecisionDTO.builder()
            .court(
                CourtDTO.builder()
                    .id(UUID.fromString("CCCCCCCC-2222-3333-4444-55555555555"))
                    .type("BGH")
                    .build())
            .leadingDecisionNormReferences(
                List.of(
                    LeadingDecisionNormReferenceDTO.builder()
                        .id(UUID.fromString("CCCCCCCC-1111-3333-4444-55555555555"))
                        .normReference("BGB §1")
                        .rank(1)
                        .build()))
            .build();

    DocumentationUnit updatedDomainObject =
        DocumentationUnit.builder()
            .coreData(
                CoreData.builder()
                    .court(
                        Court.builder()
                            .id(UUID.fromString("CCCCCCCC-2222-3333-4444-55555555555"))
                            .type("BGH")
                            .build())
                    .leadingDecisionNormReferences(List.of())
                    .build())
            .build();

    DecisionDTO decisionDTO = DecisionTransformer.transformToDTO(currentDto, updatedDomainObject);

    assertThat(decisionDTO.getLeadingDecisionNormReferences()).isEmpty();
  }

  @Test
  void testTransformToDTO_normalizesNonBreakingSpaces() {
    DecisionDTO currentDto = DecisionDTO.builder().build();

    DocumentationUnit updatedDomainObject =
        DocumentationUnit.builder()
            .coreData(
                CoreData.builder()
                    .ecli("This\u00A0is\u202Fa\uFEFFtest\u2007ecli\u180Ewith\u2060spaces")
                    .appraisalBody(
                        "This\u00A0is\u202Fa\uFEFFtest\u2007appraisalBody\u180Ewith\u2060spaces")
                    .leadingDecisionNormReferences(
                        List.of(
                            "This\u00A0is\u202Fa\uFEFFtest\u2007reference\u180Ewith\u2060spaces"))
                    .deviatingEclis(
                        List.of(
                            "This\u00A0is\u202Fa\uFEFFtest\u2007deviatingEcli\u180Ewith\u2060spaces"))
                    .deviatingFileNumbers(
                        List.of(
                            "This\u00A0is\u202Fa\uFEFFtest\u2007deviatingFileNumber\u180Ewith\u2060spaces"))
                    .deviatingCourts(
                        List.of(
                            "This\u00A0is\u202Fa\uFEFFtest\u2007deviatingCourt\u180Ewith\u2060spaces"))
                    .inputTypes(
                        List.of(
                            "This\u00A0is\u202Fa\uFEFFtest\u2007inputType\u180Ewith\u2060spaces"))
                    .build())
            .ensuingDecisions(
                List.of(
                    EnsuingDecision.builder()
                        .fileNumber(
                            "This\u00A0is\u202Fa\uFEFFtest\u2007fileNumber\u180Ewith\u2060spaces")
                        .note("This\u00A0is\u202Fa\uFEFFtest\u2007note\u180Ewith\u2060spaces")
                        .build()))
            .previousDecisions(
                List.of(
                    PreviousDecision.builder()
                        .fileNumber(
                            "This\u00A0is\u202Fa\uFEFFtest\u2007fileNumber\u180Ewith\u2060spaces")
                        .deviatingFileNumber(
                            "This\u00A0is\u202Fa\uFEFFtest\u2007deviatingFileNumber\u180Ewith\u2060spaces")
                        .build()))
            .contentRelatedIndexing(
                ContentRelatedIndexing.builder()
                    .activeCitations(
                        List.of(
                            ActiveCitation.builder()
                                .fileNumber(
                                    "This\u00A0is\u202Fa\uFEFFtest\u2007fileNumber\u180Ewith\u2060spaces")
                                .build()))
                    .norms(
                        List.of(
                            NormReference.builder()
                                .normAbbreviation(
                                    NormAbbreviation.builder()
                                        .id(UUID.fromString("33333333-2222-3333-4444-555555555555"))
                                        .build())
                                .singleNorms(
                                    List.of(
                                        SingleNorm.builder()
                                            .singleNorm(
                                                "This\u00A0is\u202Fa\uFEFFtest\u2007singlenorm\u180Ewith\u2060spaces")
                                            .build()))
                                .build()))
                    .build())
            .build();

    DecisionDTO decisionDTO = DecisionTransformer.transformToDTO(currentDto, updatedDomainObject);
    assertThat(decisionDTO.getEcli()).isEqualTo("This is a test ecli with spaces");
    assertThat(decisionDTO.getJudicialBody()).isEqualTo("This is a test appraisalBody with spaces");
    assertThat(decisionDTO.getLeadingDecisionNormReferences())
        .usingRecursiveFieldByFieldElementComparator()
        .containsExactly(
            LeadingDecisionNormReferenceDTO.builder()
                .normReference("This is a test reference with spaces")
                .rank(1)
                .build());
    assertThat(decisionDTO.getDeviatingEclis().stream().map(DeviatingEcliDTO::getValue).toList())
        .isEqualTo(List.of("This is a test deviatingEcli with spaces"));
    assertThat(
            decisionDTO.getDeviatingFileNumbers().stream()
                .map(DeviatingFileNumberDTO::getValue)
                .toList())
        .isEqualTo(List.of("This is a test deviatingFileNumber with spaces"));
    assertThat(decisionDTO.getDeviatingCourts().stream().map(DeviatingCourtDTO::getValue).toList())
        .isEqualTo(List.of("This is a test deviatingCourt with spaces"));
    assertThat(decisionDTO.getInputTypes().stream().map(InputTypeDTO::getValue).toList())
        .isEqualTo(List.of("This is a test inputType with spaces"));

    assertThat(
            decisionDTO.getEnsuingDecisions().stream()
                .map(EnsuingDecisionDTO::getFileNumber)
                .toList())
        .isEqualTo(List.of("This is a test fileNumber with spaces"));
    assertThat(
            decisionDTO.getPreviousDecisions().stream()
                .map(PreviousDecisionDTO::getFileNumber)
                .toList())
        .isEqualTo(List.of("This is a test fileNumber with spaces"));
    assertThat(
            decisionDTO.getPreviousDecisions().stream()
                .map(PreviousDecisionDTO::getDeviatingFileNumber)
                .toList())
        .isEqualTo(List.of("This is a test deviatingFileNumber with spaces"));
    assertThat(
            decisionDTO.getActiveCitations().stream()
                .map(ActiveCitationDTO::getFileNumber)
                .toList())
        .isEqualTo(List.of("This is a test fileNumber with spaces"));
    assertThat(
            decisionDTO.getNormReferences().stream().map(NormReferenceDTO::getSingleNorm).toList())
        .isEqualTo(List.of("This is a test singlenorm with spaces"));
  }

  @Test
  void testTransformToDTO_withSameJobProfiles_shouldMakeJobProfilesDistinct() {
    DocumentationUnit documentationUnit =
        generateSimpleDocumentationUnitBuilder()
            .contentRelatedIndexing(
                ContentRelatedIndexing.builder()
                    .jobProfiles(List.of("job profile", "job profile"))
                    .build())
            .build();

    DecisionDTO decisionDTO =
        DecisionTransformer.transformToDTO(DecisionDTO.builder().build(), documentationUnit);

    assertThat(decisionDTO.getJobProfiles()).extracting("value").containsExactly("job profile");
  }

  @Test
  void testTransformToDTO_withSameDismissalTypes_shouldMakeDismissalTypesDistinct() {
    DocumentationUnit documentationUnit =
        generateSimpleDocumentationUnitBuilder()
            .contentRelatedIndexing(
                ContentRelatedIndexing.builder().dismissalTypes(List.of("type", "type")).build())
            .build();

    DecisionDTO decisionDTO =
        DecisionTransformer.transformToDTO(DecisionDTO.builder().build(), documentationUnit);

    assertThat(decisionDTO.getDismissalTypes()).extracting("value").containsExactly("type");
  }

  @Test
  void testTransformToDTO_withSameDismissalGrounds_shouldMakeDismissalGroundsDistinct() {
    DocumentationUnit documentationUnit =
        generateSimpleDocumentationUnitBuilder()
            .contentRelatedIndexing(
                ContentRelatedIndexing.builder()
                    .dismissalGrounds(List.of("ground", "ground"))
                    .build())
            .build();

    DecisionDTO decisionDTO =
        DecisionTransformer.transformToDTO(DecisionDTO.builder().build(), documentationUnit);

    assertThat(decisionDTO.getDismissalGrounds()).extracting("value").containsExactly("ground");
  }

  @Test
  void testTransformToDTO_withSameCollectiveAgreements_shouldMakeCollectiveAgreementsDistinct() {
    DocumentationUnit documentationUnit =
        generateSimpleDocumentationUnitBuilder()
            .contentRelatedIndexing(
                ContentRelatedIndexing.builder()
                    .collectiveAgreements(List.of("agreement", "agreement"))
                    .build())
            .build();

    DecisionDTO decisionDTO =
        DecisionTransformer.transformToDTO(DecisionDTO.builder().build(), documentationUnit);

    assertThat(decisionDTO.getCollectiveAgreements())
        .extracting("value")
        .containsExactly("agreement");
  }

  @Test
  void testTransformToDTO_withSameParticipatingJudges_shouldMakeJudgesDistinct() {
    // Arrange
    ParticipatingJudge participatingJudge = ParticipatingJudge.builder().name("Judge A").build();
    DocumentationUnit documentationUnit =
        generateSimpleDocumentationUnitBuilder()
            .longTexts(
                LongTexts.builder()
                    .participatingJudges(List.of(participatingJudge, participatingJudge))
                    .build())
            .build();

    // Act
    DecisionDTO decisionDTO =
        DecisionTransformer.transformToDTO(DecisionDTO.builder().build(), documentationUnit);

    // Assert
    assertThat(decisionDTO.getParticipatingJudges()).hasSize(1);
    assertThat(decisionDTO.getParticipatingJudges()).extracting("name").containsExactly("Judge A");
    assertThat(decisionDTO.getParticipatingJudges().getFirst().getReferencedOpinions()).isNull();
  }

  @Test
  void testTransformToDTO_withCaselawReferences() {
    var uuid = UUID.randomUUID();
    DecisionDTO currentDto =
        DecisionDTO.builder()
            .caselawReferences(
                List.of(
                    CaselawReferenceDTO.builder()
                        .id(uuid)
                        .edition(LegalPeriodicalEditionDTO.builder().name("Foo").build())
                        .editionRank(3)
                        .documentationUnitRank(3)
                        .build()))
            .build();

    var updatedReferences =
        List.of(
            Reference.builder()
                .id(uuid)
                .referenceType(ReferenceType.CASELAW)
                .primaryReference(true)
                .build());
    DocumentationUnit updatedDomainObject =
        DocumentationUnit.builder().caselawReferences(updatedReferences).build();

    DecisionDTO decisionDTO = DecisionTransformer.transformToDTO(currentDto, updatedDomainObject);

    assertThat(decisionDTO.getCaselawReferences().getFirst().getDocumentationUnitRank()).isOne();
    assertThat(decisionDTO.getCaselawReferences().getFirst().getEditionRank()).isEqualTo(3);
    assertThat(decisionDTO.getCaselawReferences().getFirst().getEdition().getName())
        .isEqualTo("Foo");
  }

  @Test
  void testTransformToDTO_withLiteratureReferences() {
    var uuid = UUID.randomUUID();
    DecisionDTO currentDto =
        DecisionDTO.builder()
            .literatureReferences(
                List.of(
                    LiteratureReferenceDTO.builder()
                        .id(uuid)
                        .edition(LegalPeriodicalEditionDTO.builder().name("Foo").build())
                        .editionRank(3)
                        .documentationUnitRank(3)
                        .build()))
            .build();

    var updatedReferences =
        List.of(Reference.builder().id(uuid).referenceType(ReferenceType.LITERATURE).build());
    DocumentationUnit updatedDomainObject =
        DocumentationUnit.builder().literatureReferences(updatedReferences).build();

    DecisionDTO decisionDTO = DecisionTransformer.transformToDTO(currentDto, updatedDomainObject);

    assertThat(decisionDTO.getLiteratureReferences().getFirst().getDocumentationUnitRank()).isOne();
    assertThat(decisionDTO.getLiteratureReferences().getFirst().getEditionRank()).isEqualTo(3);
    assertThat(decisionDTO.getLiteratureReferences().getFirst().getEdition().getName())
        .isEqualTo("Foo");
  }

  @Test
  void testTransformToDTO_withSource_withNoExistingSource() {
    DecisionDTO currentDto = DecisionDTO.builder().build();

    DocumentationUnit updatedDomainObject =
        DocumentationUnit.builder()
            .coreData(
                CoreData.builder().source(Source.builder().value(SourceValue.E).build()).build())
            .build();

    DecisionDTO decisionDTO = DecisionTransformer.transformToDTO(currentDto, updatedDomainObject);
    assertThat(decisionDTO.getSource().getFirst().getRank()).isOne();
    assertThat(decisionDTO.getSource().getFirst().getValue()).isEqualTo(SourceValue.E);
    assertThat(decisionDTO.getSource().getFirst().getSourceRawValue()).isNull();
  }

  @Test
  void testTransformToDTO_withSource_withUpdatedExistingSource() {
    var reference = CaselawReferenceDTO.builder().build();
    List<SourceDTO> existingSources =
        List.of(
            SourceDTO.builder()
                .value(SourceValue.Z)
                .sourceRawValue("z")
                .reference(reference)
                .rank(1)
                .build());

    DecisionDTO currentDto = DecisionDTO.builder().source(existingSources).build();

    DocumentationUnit updatedDomainObject =
        DocumentationUnit.builder()
            .coreData(
                CoreData.builder().source(Source.builder().value(SourceValue.E).build()).build())
            .build();

    DecisionDTO decisionDTO = DecisionTransformer.transformToDTO(currentDto, updatedDomainObject);
    assertThat(decisionDTO.getSource()).hasSize(1);
    assertThat(decisionDTO.getSource().getLast().getRank()).isEqualTo(1);
    assertThat(decisionDTO.getSource().getLast().getValue()).isEqualTo(SourceValue.E);
    assertThat(decisionDTO.getSource().getLast().getReference()).isNull();
    assertThat(decisionDTO.getSource().getLast().getSourceRawValue()).isNull();
  }

  @Test
  void testTransformToDTO_withSource_withUnchangedExistingSources() {
    var reference = CaselawReferenceDTO.builder().build();
    List<SourceDTO> existingSources =
        List.of(
            SourceDTO.builder().value(SourceValue.A).rank(1).build(),
            SourceDTO.builder()
                .value(SourceValue.Z)
                .sourceRawValue("z")
                .reference(reference)
                .rank(2)
                .build());

    DecisionDTO currentDto = DecisionDTO.builder().source(existingSources).build();

    DocumentationUnit updatedDomainObject =
        DocumentationUnit.builder()
            .coreData(
                CoreData.builder().source(Source.builder().value(SourceValue.Z).build()).build())
            .build();

    DecisionDTO decisionDTO = DecisionTransformer.transformToDTO(currentDto, updatedDomainObject);
    assertThat(decisionDTO.getSource()).hasSize(2);
    assertThat(decisionDTO.getSource().getLast().getRank()).isEqualTo(2);
    assertThat(decisionDTO.getSource().getLast().getValue()).isEqualTo(SourceValue.Z);
    assertThat(decisionDTO.getSource().getLast().getSourceRawValue()).isEqualTo("z");
    assertThat(decisionDTO.getSource().getLast().getReference()).isEqualTo(reference);
  }

  @Test
  void testTransformToDomain_shouldTransformAllCoreDataRelevantForDecision() {
    // Arrange
    UUID decisionId = UUID.randomUUID();
    UUID documentTypeId = UUID.randomUUID();
    UUID courtId = UUID.randomUUID();
    UUID docOfficeId = UUID.randomUUID();
    UUID creatingDocOfficeId = UUID.randomUUID();
    UUID procedureId = UUID.randomUUID();
    String documentNumber = "DOCNUMBER1234";

    LocalDate decisionDate = LocalDate.of(2024, 5, 10);
    LocalDate deviatingDecisionDate = LocalDate.of(2024, 5, 10);
    String celexNumber = "CELEX-XYZ";
    String ecli = "ECLI:DE:BVerfG:2024:051024u100124";
    String deviatingEcli = "ECLI:DE:BVerfG:2024:051024u100125";
    String judicialBody = "Bundesverfassungsgericht";
    String fileNumber = "AZ-456";
    String deviatingFileNumber = "1B23/24";
    String courtLabel = "LG Berlin";

    DecisionDTO decisionDTO =
        DecisionDTO.builder()
            // --- fields from DocumentationUnitDTO parent (for builder) ---
            .id(decisionId)
            .documentNumber(documentNumber)
            .version(1L)
            .date(decisionDate)
            .documentType(DocumentTypeDTO.builder().id(documentTypeId).abbreviation("Urt").build())
            .court(CourtDTO.builder().id(courtId).type("BVerfG").build())
            .celexNumber(celexNumber)
            .judicialBody(judicialBody) // Mapped to coreData.appraisalBody
            .fileNumbers(
                List.of(
                    de.bund.digitalservice.ris.caselaw.adapter.database.jpa.FileNumberDTO.builder()
                        .rank(0L)
                        .value(fileNumber)
                        .build()))
            .deviatingFileNumbers(
                List.of(
                    DeviatingFileNumberDTO.builder().rank(0L).value(deviatingFileNumber).build()))
            .deviatingDates(
                List.of(DeviatingDateDTO.builder().rank(0L).value(deviatingDecisionDate).build()))
            .deviatingCourts(
                List.of(
                    de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DeviatingCourtDTO
                        .builder()
                        .rank(0L)
                        .value(courtLabel)
                        .build()))
            .documentationOffice(
                DocumentationOfficeDTO.builder().id(docOfficeId).abbreviation("DS").build())

            // --- Fields specifically from DecisionDTO that contribute to CoreData ---
            .procedure(ProcedureDTO.builder().id(procedureId).label("Vorgang").build())
            .ecli(ecli)
            .source(
                List.of(
                    SourceDTO.builder().rank(1).value(SourceValue.E).sourceRawValue("E").build()))
            .legalEffect(LegalEffectDTO.JA)
            .inputTypes(List.of(InputTypeDTO.builder().rank(1L).value("Email").build()))
            .leadingDecisionNormReferences(
                List.of(
                    LeadingDecisionNormReferenceDTO.builder()
                        .rank(0)
                        .normReference("NormAbk")
                        .build()))
            .yearsOfDispute(Set.of(YearOfDisputeDTO.builder().value("2022").rank(0).build()))
            .deviatingEclis(
                List.of(DeviatingEcliDTO.builder().value(deviatingEcli).rank(1L).build()))
            .creatingDocumentationOffice(
                DocumentationOfficeDTO.builder()
                    .id(creatingDocOfficeId)
                    .abbreviation("BGH")
                    .build())
            .build();

    // Act
    DocumentationUnit domainObject = DecisionTransformer.transformToDomain(decisionDTO);

    // Assert general DocumentationUnit fields
    assertThat(domainObject).isNotNull();
    assertThat(domainObject.uuid()).isEqualTo(decisionId);
    assertThat(domainObject.documentNumber()).isEqualTo(documentNumber);
    assertThat(domainObject.version()).isEqualTo(1L);

    // Assert CoreData object itself
    CoreData coreData = domainObject.coreData();
    assertThat(coreData).isNotNull();

    // --- Assert mutual CoreData fields that are transformed from DocumentableTransformer ---
    assertThat(coreData.celexNumber()).isEqualTo(celexNumber);
    assertThat(coreData.appraisalBody()).isEqualTo(judicialBody);
    assertThat(coreData.decisionDate()).isEqualTo(decisionDate);
    assertThat(coreData.documentType().jurisShortcut()).isEqualTo("Urt");
    assertThat(coreData.court().label()).isEqualTo("BVerfG");
    assertThat(coreData.fileNumbers().getFirst()).isEqualTo(fileNumber);
    assertThat(coreData.deviatingFileNumbers().getFirst()).isEqualTo(deviatingFileNumber);
    assertThat(coreData.deviatingCourts().getFirst()).isEqualTo(courtLabel);
    assertThat(coreData.deviatingDecisionDates().getFirst()).isEqualTo(deviatingDecisionDate);
    assertThat(coreData.documentationOffice()).isNotNull();
    assertThat(coreData.documentationOffice().id()).isEqualTo(docOfficeId);
    assertThat(coreData.documentationOffice().abbreviation()).isEqualTo("DS");

    // --- Assert CoreData fields that are transformed from DecisionTransformer ---
    assertThat(coreData.ecli()).isEqualTo(ecli);
    assertThat(coreData.source()).isNotNull();
    assertThat(coreData.source().value()).isEqualTo(SourceValue.E);
    assertThat(coreData.source().sourceRawValue()).isEqualTo("E");
    assertThat(coreData.source().reference()).isNull();
    assertThat(coreData.legalEffect()).isEqualTo("Ja");
    assertThat(coreData.inputTypes()).containsExactly("Email");
    assertThat(coreData.leadingDecisionNormReferences()).containsExactly("NormAbk");
    assertThat(coreData.yearsOfDispute()).containsExactly(Year.of(2022));
    assertThat(coreData.deviatingEclis()).containsExactly(deviatingEcli);
    assertThat(coreData.procedure()).isNotNull();
    assertThat(coreData.procedure().id()).isEqualTo(procedureId);
    assertThat(coreData.procedure().label()).isEqualTo("Vorgang");
    assertThat(coreData.creatingDocOffice()).isNotNull();
    assertThat(coreData.creatingDocOffice().id()).isEqualTo(creatingDocOfficeId);
    assertThat(coreData.creatingDocOffice().abbreviation()).isEqualTo("BGH");
  }

  @Test
  void testTransformToDomain_withNoSources() {
    DecisionDTO decisionDTO = DecisionDTO.builder().build();

    DocumentationUnit domainObject = DecisionTransformer.transformToDomain(decisionDTO);

    assertThat(domainObject.coreData().source()).isNull();
  }

  @Test
  void testTransformToDomain_withOneValidSource() {
    DecisionDTO decisionDTO =
        DecisionDTO.builder()
            .source(List.of(SourceDTO.builder().value(SourceValue.A).rank(1).build()))
            .build();

    DocumentationUnit domainObject = DecisionTransformer.transformToDomain(decisionDTO);

    assertThat(domainObject.coreData().source().value()).isEqualTo(SourceValue.A);
    assertThat(domainObject.coreData().source().sourceRawValue()).isNull();
  }

  @Test
  void testTransformToDomain_withMultipleSources_shouldPickHighestRank() {
    DecisionDTO decisionDTO =
        DecisionDTO.builder()
            .source(
                List.of(
                    SourceDTO.builder().value(SourceValue.O).rank(1).build(),
                    SourceDTO.builder().value(SourceValue.Z).rank(3).build(),
                    SourceDTO.builder().value(SourceValue.A).rank(2).build()))
            .build();

    DocumentationUnit domainObject = DecisionTransformer.transformToDomain(decisionDTO);

    assertThat(domainObject.coreData().source().value()).isEqualTo(SourceValue.Z);
    assertThat(domainObject.coreData().source().sourceRawValue()).isNull();
  }

  @ParameterizedTest
  @EnumSource(
      value = ReferenceType.class,
      names = {"CASELAW", "LITERATURE"})
  void testTransformToDTO_withSourceReferenceIsNotIncludedInReferences_shouldRemoveLinkToReference(
      ReferenceType referenceType) {
    var referenceId = UUID.randomUUID();
    List<SourceDTO> existingSourcesWithReference =
        List.of(
            SourceDTO.builder()
                .value(SourceValue.A)
                .rank(1)
                .reference(
                    referenceType.equals(ReferenceType.CASELAW)
                        ? CaselawReferenceDTO.builder().id(referenceId).build()
                        : LiteratureReferenceDTO.builder().id(referenceId).build())
                .build());

    DecisionDTO currentDto = DecisionDTO.builder().source(existingSourcesWithReference).build();

    DocumentationUnit updatedDomainObject =
        DocumentationUnit.builder()
            .coreData(
                CoreData.builder().source(Source.builder().value(SourceValue.A).build()).build())
            .build();

    DecisionDTO decisionDTO = DecisionTransformer.transformToDTO(currentDto, updatedDomainObject);
    assertThat(decisionDTO.getSource().getFirst().getReference()).isNull();
    assertThat(decisionDTO.getCaselawReferences()).isEmpty();
    assertThat(decisionDTO.getLiteratureReferences()).isEmpty();
  }

  @ParameterizedTest
  @EnumSource(
      value = ReferenceType.class,
      names = {"CASELAW", "LITERATURE"})
  void testTransformToDTO_withSourceReferenceIsIncludedInReferences_shouldKeepLinkToReference(
      ReferenceType referenceType) {
    var referenceId = UUID.randomUUID();
    List<SourceDTO> existingSourcesWithReference =
        List.of(
            SourceDTO.builder()
                .value(SourceValue.A)
                .rank(1)
                .reference(
                    referenceType.equals(ReferenceType.CASELAW)
                        ? CaselawReferenceDTO.builder()
                            .id(referenceId)
                            .type("amtlich")
                            .documentationUnitRank(1)
                            .build()
                        : LiteratureReferenceDTO.builder()
                            .id(referenceId)
                            .documentationUnitRank(1)
                            .build())
                .build());

    DecisionDTO currentDto = DecisionDTO.builder().source(existingSourcesWithReference).build();

    var references =
        List.of(
            Reference.builder()
                .id(referenceId)
                .referenceType(referenceType)
                .primaryReference(true)
                .build());

    DocumentationUnit updatedDomainObject =
        DocumentationUnit.builder()
            .coreData(
                CoreData.builder().source(Source.builder().value(SourceValue.A).build()).build())
            .caselawReferences(referenceType.equals(ReferenceType.CASELAW) ? references : null)
            .literatureReferences(
                referenceType.equals(ReferenceType.LITERATURE) ? references : null)
            .build();

    DecisionDTO decisionDTO = DecisionTransformer.transformToDTO(currentDto, updatedDomainObject);
    assertThat(decisionDTO.getSource().getFirst().getReference().getId()).isEqualTo(referenceId);
    if (referenceType.equals(ReferenceType.CASELAW))
      assertThat(decisionDTO.getCaselawReferences()).isNotEmpty();
    else if (referenceType.equals(ReferenceType.LITERATURE))
      assertThat(decisionDTO.getLiteratureReferences()).isNotEmpty();
  }

  @Test
  void testTransformToDomain_withDocumentationUnitDTOIsNull_shouldReturnEmptyDocumentationUnit() {

    assertThatThrownBy(() -> DecisionTransformer.transformToDomain(null))
        .isInstanceOf(DocumentationUnitTransformerException.class)
        .hasMessageContaining("Document unit is null and won't transform");
  }

  @Test
  void testTransformToDomain_withLegalEffectYes_shouldSetLegalEffectToYes() {
    DecisionDTO decisionDTO = generateSimpleDTOBuilder().legalEffect(LegalEffectDTO.JA).build();
    DocumentationUnit expected =
        generateSimpleDocumentationUnitBuilder()
            .coreData(generateSimpleCoreDataBuilder().legalEffect("Ja").build())
            .build();

    DocumentationUnit documentationUnit = DecisionTransformer.transformToDomain(decisionDTO);

    assertThat(documentationUnit).isEqualTo(expected);
  }

  @Test
  void testTransformToDomain_withLegalEffectNo_shouldSetLegalEffectToNo() {
    DecisionDTO decisionDTO = generateSimpleDTOBuilder().legalEffect(LegalEffectDTO.NEIN).build();
    DocumentationUnit expected =
        generateSimpleDocumentationUnitBuilder()
            .coreData(generateSimpleCoreDataBuilder().legalEffect("Nein").build())
            .build();

    DocumentationUnit documentationUnit = DecisionTransformer.transformToDomain(decisionDTO);

    assertThat(documentationUnit).isEqualTo(expected);
  }

  @Test
  void testTransformToDomain_withLegalEffectNotSpecified_shouldSetLegalEffectToNotSpecified() {
    DecisionDTO decisionDTO =
        generateSimpleDTOBuilder().legalEffect(LegalEffectDTO.KEINE_ANGABE).build();
    DocumentationUnit expected =
        generateSimpleDocumentationUnitBuilder()
            .coreData(generateSimpleCoreDataBuilder().legalEffect("Keine Angabe").build())
            .build();

    DocumentationUnit documentationUnit = DecisionTransformer.transformToDomain(decisionDTO);

    assertThat(documentationUnit).isEqualTo(expected);
  }

  @Test
  void testTransformToDomain_withLegalEffectWrongValue_shouldSetLegalEffectToNull() {
    DecisionDTO decisionDTO =
        generateSimpleDTOBuilder().legalEffect(LegalEffectDTO.FALSCHE_ANGABE).build();
    DocumentationUnit expected =
        generateSimpleDocumentationUnitBuilder()
            .coreData(generateSimpleCoreDataBuilder().build())
            .build();

    DocumentationUnit documentationUnit = DecisionTransformer.transformToDomain(decisionDTO);

    assertThat(documentationUnit).isEqualTo(expected);
  }

  @Test
  void testTransformToDomain_withEnsuingAndPendingDecisionsWithoutARank_shouldAddItToTheEnd() {
    DecisionDTO decisionDTO =
        generateSimpleDTOBuilder()
            .ensuingDecisions(
                List.of(
                    EnsuingDecisionDTO.builder().note("ensuing with rank").rank(1).build(),
                    EnsuingDecisionDTO.builder().note("ensuing without rank").rank(0).build()))
            .pendingDecisions(
                List.of(
                    PendingDecisionDTO.builder().note("pending with rank").rank(2).build(),
                    PendingDecisionDTO.builder().note("pending without rank").rank(0).build()))
            .build();
    DocumentationUnit expected =
        generateSimpleDocumentationUnitBuilder()
            .coreData(generateSimpleCoreDataBuilder().build())
            .ensuingDecisions(
                List.of(
                    EnsuingDecision.builder().pending(false).note("ensuing with rank").build(),
                    EnsuingDecision.builder().pending(true).note("pending with rank").build(),
                    EnsuingDecision.builder().pending(false).note("ensuing without rank").build(),
                    EnsuingDecision.builder().pending(true).note("pending without rank").build()))
            .build();

    DocumentationUnit documentationUnit = DecisionTransformer.transformToDomain(decisionDTO);

    assertThat(documentationUnit).isEqualTo(expected);
  }

  @Test
  void testTransformToDomain_textWithMultipleBorderNumberElements_shouldAddAllBorderNumbers() {
    DecisionDTO decisionDTO =
        generateSimpleDTOBuilder()
            .grounds(
                "lorem ipsum<border-number><number>1</number><content>foo</content></border-number> dolor sit amet <border-number><number>2</number><content>bar</content></border-number>")
            .build();

    DocumentationUnit documentationUnit = DecisionTransformer.transformToDomain(decisionDTO);

    assertThat(documentationUnit.managementData().borderNumbers())
        .hasSize(2)
        .containsExactly("1", "2");
  }

  @Test
  void testTransformScheduledPublicationDate_withDate_shouldAddScheduledPublicationDate() {
    DecisionDTO decisionDTO =
        generateSimpleDTOBuilder()
            .scheduledPublicationDateTime(LocalDateTime.parse("2022-01-23T18:25:14"))
            .build();

    DocumentationUnit documentationUnit = DecisionTransformer.transformToDomain(decisionDTO);

    assertThat(documentationUnit.managementData().scheduledPublicationDateTime())
        .isEqualTo("2022-01-23T18:25:14");
  }

  @Test
  void testTransformScheduledPublicationDate_withoutDate_shouldNotAddScheduledPublicationDate() {
    DecisionDTO decisionDTO = generateSimpleDTOBuilder().scheduledPublicationDateTime(null).build();

    DocumentationUnit documentationUnit = DecisionTransformer.transformToDomain(decisionDTO);

    assertThat(documentationUnit.managementData().scheduledPublicationDateTime()).isNull();
  }

  @Test
  void testTransformLastPublicationDate_withDate_shouldAddLastPublicationDate() {
    DecisionDTO decisionDTO =
        generateSimpleDTOBuilder()
            .lastPublicationDateTime(LocalDateTime.parse("2022-01-23T18:25:14"))
            .build();

    DocumentationUnit documentationUnit = DecisionTransformer.transformToDomain(decisionDTO);

    assertThat(documentationUnit.managementData().lastPublicationDateTime())
        .isEqualTo("2022-01-23T18:25:14");
  }

  @Test
  void testTransformLastPublicationDate_withoutDate_shouldNotAddLastPublicationDate() {
    DecisionDTO decisionDTO = generateSimpleDTOBuilder().lastPublicationDateTime(null).build();

    DocumentationUnit documentationUnit = DecisionTransformer.transformToDomain(decisionDTO);

    assertThat(documentationUnit.managementData().lastPublicationDateTime()).isNull();
  }

  @Test
  void testTransformToDomain_multipleTextsWithBorderNumberElements_shouldAddAllBorderNumbers() {
    DecisionDTO decisionDTO =
        generateSimpleDTOBuilder()
            .tenor(
                "lorem ipsum <border-number><number>1</number><content>foo</content></border-number>")
            .grounds(
                "dolor sit amet <border-number><number>2</number><content>bar</content></border-number>")
            .caseFacts(
                "consectetur <border-number><number>3</number><content>baz</content></border-number>")
            .decisionGrounds(
                "adipiscing <border-number><number>4</number><content>qux</content></border-number>")
            .build();

    DocumentationUnit documentationUnit = DecisionTransformer.transformToDomain(decisionDTO);

    assertThat(documentationUnit.managementData().borderNumbers())
        .hasSize(4)
        .containsExactly("1", "2", "3", "4");
  }

  @Test
  void testTransformToDomain_textWithoutBorderNumberElements_shouldNotAddBorderNumbers() {
    DecisionDTO decisionDTO =
        generateSimpleDTOBuilder().grounds("lorem ipsum dolor sit amet").build();

    DocumentationUnit documentationUnit = DecisionTransformer.transformToDomain(decisionDTO);

    assertThat(documentationUnit.managementData().borderNumbers()).isEmpty();
  }

  @Test
  void
      testTransformToDomain_textWithMalformedBorderNumberElement_shouldOnlyAddValidBorderNumbers() {
    DecisionDTO decisionDTO =
        generateSimpleDTOBuilder()
            .grounds(
                "lorem ipsum<border-number><content>foo</content></border-number> dolor sit amet <border-number><number>2</number><content>bar</content></border-number>")
            .build();

    DocumentationUnit documentationUnit = DecisionTransformer.transformToDomain(decisionDTO);

    assertThat(documentationUnit.managementData().borderNumbers()).hasSize(1).containsExactly("2");
  }

  @Test
  void testTransformToDomain_shouldAddLeadingDecisionNormReferences() {
    DecisionDTO decisionDTO =
        generateSimpleDTOBuilder()
            .leadingDecisionNormReferences(
                List.of(
                    LeadingDecisionNormReferenceDTO.builder()
                        .normReference("BGB §1")
                        .rank(1)
                        .build(),
                    LeadingDecisionNormReferenceDTO.builder()
                        .normReference("BGB §2")
                        .rank(2)
                        .build(),
                    LeadingDecisionNormReferenceDTO.builder()
                        .normReference("BGB §3")
                        .rank(3)
                        .build()))
            .build();

    DocumentationUnit documentationUnit = DecisionTransformer.transformToDomain(decisionDTO);

    assertThat(documentationUnit.coreData().leadingDecisionNormReferences())
        .hasSize(3)
        .containsExactly("BGB §1", "BGB §2", "BGB §3");
  }

  @Test
  void testTransformToDomain_withNote_shouldAddNote() {
    DecisionDTO decisionDTO = generateSimpleDTOBuilder().note("Beispiel Notiz").build();

    DocumentationUnit documentationUnit = DecisionTransformer.transformToDomain(decisionDTO);

    assertThat(documentationUnit.note()).isEqualTo("Beispiel Notiz");
  }

  @Test
  void testTransformToDomain_withEmptyNote_shouldAddEmptyNote() {
    DecisionDTO decisionDTO = generateSimpleDTOBuilder().note("").build();

    DocumentationUnit documentationUnit = DecisionTransformer.transformToDomain(decisionDTO);

    assertThat(documentationUnit.note()).isEmpty();
  }

  @Test
  void testTransformToDomain_withNullNote_shouldAddNullNote() {
    DecisionDTO decisionDTO = generateSimpleDTOBuilder().note(null).build();

    DocumentationUnit documentationUnit = DecisionTransformer.transformToDomain(decisionDTO);

    assertThat(documentationUnit.note()).isNull();
  }

  @Test
  void testTransformToDomain_withoutNote_shouldAddNoNote() {
    DecisionDTO decisionDTO = generateSimpleDTOBuilder().build();

    DocumentationUnit documentationUnit = DecisionTransformer.transformToDomain(decisionDTO);

    assertThat(documentationUnit.note()).isNull();
  }

  private DecisionDTO.DecisionDTOBuilder<?, ?> generateSimpleDTOBuilder() {
    return DecisionDTO.builder()
        .documentationOffice(DocumentationOfficeDTO.builder().abbreviation("doc office").build());
  }

  @Test
  void testAddYearsOfDisputeToDTORanking() {
    DecisionDTO currentDto = DecisionDTO.builder().build();

    CoreData.CoreDataBuilder coreDataBuilder = generateSimpleCoreDataBuilder();
    coreDataBuilder.yearsOfDispute(
        List.of(Year.now().minusYears(2), Year.now(), Year.now().minusYears(4)));
    DecisionDTO decisionDTO =
        DecisionTransformer.transformToDTO(
            currentDto,
            generateSimpleDocumentationUnitBuilder().coreData(coreDataBuilder.build()).build());

    assertEquals(
        decisionDTO.getYearsOfDispute().stream().map(YearOfDisputeDTO::getRank).toList(),
        List.of(1, 2, 3));
  }

  @Test
  void testTransformToDomain_withJobProfiles_shouldAddJobProfiles() {
    DecisionDTO decisionDTO =
        generateSimpleDTOBuilder()
            .jobProfiles(List.of(JobProfileDTO.builder().value("job profile").build()))
            .build();

    DocumentationUnit documentationUnit = DecisionTransformer.transformToDomain(decisionDTO);

    assertThat(documentationUnit.contentRelatedIndexing().jobProfiles())
        .containsExactly("job profile");
  }

  @Test
  void testTransformToDomain_withCollectiveAgreements_shouldAddCollectiveAgreements() {
    DecisionDTO decisionDTO =
        generateSimpleDTOBuilder()
            .collectiveAgreements(
                List.of(CollectiveAgreementDTO.builder().value("agreement").build()))
            .build();

    DocumentationUnit documentationUnit = DecisionTransformer.transformToDomain(decisionDTO);

    assertThat(documentationUnit.contentRelatedIndexing().collectiveAgreements())
        .containsExactly("agreement");
  }

  @Test
  void testTransformToDomain_withDismissalTypes_shouldAddDismissalTypes() {
    DecisionDTO decisionDTO =
        generateSimpleDTOBuilder()
            .dismissalTypes(List.of(DismissalTypesDTO.builder().value("type").build()))
            .build();

    DocumentationUnit documentationUnit = DecisionTransformer.transformToDomain(decisionDTO);

    assertThat(documentationUnit.contentRelatedIndexing().dismissalTypes()).containsExactly("type");
  }

  @Test
  void testTransformToDomain_withDismissalGrounds_shouldAddDismissalGrounds() {
    DecisionDTO decisionDTO =
        generateSimpleDTOBuilder()
            .dismissalGrounds(List.of(DismissalGroundsDTO.builder().value("ground").build()))
            .build();

    DocumentationUnit documentationUnit = DecisionTransformer.transformToDomain(decisionDTO);

    assertThat(documentationUnit.contentRelatedIndexing().dismissalGrounds())
        .containsExactly("ground");
  }

  @Test
  void testTransformToDomain_withLegislativeMandate_shouldLegislativeMandateBeTrue() {
    DecisionDTO decisionDTO = generateSimpleDTOBuilder().hasLegislativeMandate(true).build();

    DocumentationUnit documentationUnit = DecisionTransformer.transformToDomain(decisionDTO);

    assertThat(documentationUnit.contentRelatedIndexing().hasLegislativeMandate()).isTrue();
  }

  @Test
  void testTransformToDomain_withoutLegislativeMandate_shouldLegislativeMandateBeFalse() {
    DecisionDTO decisionDTO = generateSimpleDTOBuilder().hasLegislativeMandate(false).build();

    DocumentationUnit documentationUnit = DecisionTransformer.transformToDomain(decisionDTO);

    assertThat(documentationUnit.contentRelatedIndexing().hasLegislativeMandate()).isFalse();
  }

  @Test
  void testTransformToDomain_withParticipatingJudges_shouldAddParticipatingJudges() {
    // Arrange
    ParticipatingJudgeDTO participatingJudgeA =
        ParticipatingJudgeDTO.builder().name("Judge A").referencedOpinions("Opinion A").build();
    ParticipatingJudgeDTO participatingJudgeB =
        ParticipatingJudgeDTO.builder().name("Judge B").referencedOpinions("Opinion B").build();

    DecisionDTO decisionDTO =
        generateSimpleDTOBuilder()
            .participatingJudges(List.of(participatingJudgeA, participatingJudgeB))
            .build();

    // Act
    DocumentationUnit documentationUnit = DecisionTransformer.transformToDomain(decisionDTO);

    // Assert
    assertThat(documentationUnit.longTexts().participatingJudges()).hasSize(2);
    assertThat(documentationUnit.longTexts().participatingJudges().getFirst().id())
        .isEqualTo(participatingJudgeA.getId());
    assertThat(documentationUnit.longTexts().participatingJudges().getFirst().name())
        .isEqualTo(participatingJudgeA.getName());
    assertThat(documentationUnit.longTexts().participatingJudges().getFirst().referencedOpinions())
        .isEqualTo(participatingJudgeA.getReferencedOpinions());
    assertThat(documentationUnit.longTexts().participatingJudges().get(1).id())
        .isEqualTo(participatingJudgeB.getId());

    assertThat(documentationUnit.longTexts().participatingJudges().get(1).name())
        .isEqualTo(participatingJudgeB.getName());
    assertThat(documentationUnit.longTexts().participatingJudges().get(1).referencedOpinions())
        .isEqualTo(participatingJudgeB.getReferencedOpinions());
  }

  @Test
  void testTransformToDomain_withoutParticipatingJudges_shouldAddEmptyList() {
    // Act
    DocumentationUnit documentationUnit =
        DecisionTransformer.transformToDomain(generateSimpleDTOBuilder().build());

    // Assert
    assertThat(documentationUnit.longTexts().participatingJudges()).isEmpty();
  }

  private DocumentationUnit.DocumentationUnitBuilder generateSimpleDocumentationUnitBuilder() {
    return DocumentationUnit.builder()
        .previousDecisions(Collections.emptyList())
        .ensuingDecisions(Collections.emptyList())
        .shortTexts(ShortTexts.builder().build())
        .longTexts(LongTexts.builder().build())
        .managementData(
            ManagementData.builder()
                .scheduledPublicationDateTime(null)
                .lastPublicationDateTime(null)
                .borderNumbers(Collections.emptyList())
                .duplicateRelations(List.of())
                .build())
        .attachments(Collections.emptyList())
        .contentRelatedIndexing(
            ContentRelatedIndexing.builder()
                .keywords(Collections.emptyList())
                .fieldsOfLaw(Collections.emptyList())
                .norms(Collections.emptyList())
                .activeCitations(Collections.emptyList())
                .jobProfiles(Collections.emptyList())
                .dismissalGrounds(Collections.emptyList())
                .dismissalTypes(Collections.emptyList())
                .collectiveAgreements(Collections.emptyList())
                .hasLegislativeMandate(false)
                .build())
        .caselawReferences(Collections.emptyList())
        .literatureReferences(Collections.emptyList())
        .documentalists(Collections.emptyList());
  }

  private CoreDataBuilder generateSimpleCoreDataBuilder() {
    return CoreData.builder()
        .documentationOffice(DocumentationOffice.builder().abbreviation("doc office").build())
        .fileNumbers(Collections.emptyList())
        .deviatingFileNumbers(Collections.emptyList())
        .deviatingCourts(Collections.emptyList())
        .previousProcedures(Collections.emptyList())
        .deviatingEclis(Collections.emptyList())
        .deviatingDecisionDates(Collections.emptyList())
        .inputTypes(Collections.emptyList())
        .leadingDecisionNormReferences(Collections.emptyList())
        .yearsOfDispute(Collections.emptyList());
  }

  @Test
  void testTransformToDomain_withDocumentalists_shouldAddDocumentalists() {
    DecisionDTO decisionDTO =
        generateSimpleDTOBuilder()
            .documentalists(
                List.of(
                    DocumentalistDTO.builder().value("documentalist1").build(),
                    DocumentalistDTO.builder().value("documentalist2").build()))
            .build();

    DocumentationUnit documentationUnit = DecisionTransformer.transformToDomain(decisionDTO);

    assertThat(documentationUnit.documentalists())
        .containsExactly("documentalist1", "documentalist2");
  }

  @Test
  void testTransformToDomain_withoutDocumentalists_shouldNotAddDocumentalists() {
    DecisionDTO decisionDTO = generateSimpleDTOBuilder().build();

    DocumentationUnit documentationUnit = DecisionTransformer.transformToDomain(decisionDTO);

    assertThat(documentationUnit.documentalists()).isEmpty();
  }
}
