package de.bund.digitalservice.ris.caselaw.adapter.transformer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.ActiveCitationDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.AppealDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.AttachmentDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.CaselawReferenceDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.CollectiveAgreementDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.CourtDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DecisionDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DefinitionDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DeviatingCourtDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DeviatingDateDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DeviatingDocumentNumberDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DeviatingEcliDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DeviatingFileNumberDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DismissalGroundsDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DismissalTypesDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentTypeDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentalistDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentationOfficeDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.EnsuingDecisionDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.ForeignLanguageVersionDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.InputTypeDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.JobProfileDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.LanguageCodeDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.LeadingDecisionNormReferenceDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.LegalEffectDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.LegalPeriodicalEditionDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.LiteratureReferenceDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.NormReferenceDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.OralHearingDateDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.ParticipatingJudgeDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.PendingDecisionDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.PreviousDecisionDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.ProcedureDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.SourceDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.YearOfDisputeDTO;
import de.bund.digitalservice.ris.caselaw.domain.ActiveCitation;
import de.bund.digitalservice.ris.caselaw.domain.AppealAdmission;
import de.bund.digitalservice.ris.caselaw.domain.AppealAdmitter;
import de.bund.digitalservice.ris.caselaw.domain.ContentRelatedIndexing;
import de.bund.digitalservice.ris.caselaw.domain.CoreData;
import de.bund.digitalservice.ris.caselaw.domain.CoreData.CoreDataBuilder;
import de.bund.digitalservice.ris.caselaw.domain.Decision;
import de.bund.digitalservice.ris.caselaw.domain.Definition;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationOffice;
import de.bund.digitalservice.ris.caselaw.domain.EnsuingDecision;
import de.bund.digitalservice.ris.caselaw.domain.ForeignLanguageVersion;
import de.bund.digitalservice.ris.caselaw.domain.LanguageCode;
import de.bund.digitalservice.ris.caselaw.domain.LegalForce;
import de.bund.digitalservice.ris.caselaw.domain.LongTexts;
import de.bund.digitalservice.ris.caselaw.domain.ManagementData;
import de.bund.digitalservice.ris.caselaw.domain.NormReference;
import de.bund.digitalservice.ris.caselaw.domain.PortalPublicationStatus;
import de.bund.digitalservice.ris.caselaw.domain.PreviousDecision;
import de.bund.digitalservice.ris.caselaw.domain.Reference;
import de.bund.digitalservice.ris.caselaw.domain.ReferenceType;
import de.bund.digitalservice.ris.caselaw.domain.ShortTexts;
import de.bund.digitalservice.ris.caselaw.domain.SingleNorm;
import de.bund.digitalservice.ris.caselaw.domain.Source;
import de.bund.digitalservice.ris.caselaw.domain.SourceValue;
import de.bund.digitalservice.ris.caselaw.domain.appeal.Appeal;
import de.bund.digitalservice.ris.caselaw.domain.appeal.AppealWithdrawal;
import de.bund.digitalservice.ris.caselaw.domain.appeal.PkhPlaintiff;
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
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

class DecisionTransformerTest {
  @Test
  void testTransformToDTO_withoutCoreData() {
    DecisionDTO currentDto = DecisionDTO.builder().build();
    Decision updatedDomainObject = Decision.builder().build();

    DecisionDTO decisionDTO = DecisionTransformer.transformToDTO(currentDto, updatedDomainObject);

    assertThat(decisionDTO.getProcedureHistory()).isEmpty();
    assertThat(decisionDTO.getProcedure()).isNull();
    assertThat(decisionDTO.getEcli()).isNull();
    assertThat(decisionDTO.getJudicialBody()).isNull();
    assertThat(decisionDTO.getDate()).isNull();
    assertThat(decisionDTO.getScheduledPublicationDateTime()).isNull();
    assertThat(decisionDTO.getLastHandoverDateTime()).isNull();
    assertThat(decisionDTO.getCourt()).isNull();
    assertThat(decisionDTO.getDocumentType()).isNull();
    assertThat(decisionDTO.getDocumentationOffice()).isNull();
  }

  @Test
  void testTransformToDTO_withoutDecisionNames() {
    DecisionDTO currentDto = DecisionDTO.builder().build();
    ShortTexts shortTexts = ShortTexts.builder().build();
    Decision updatedDomainObject = Decision.builder().shortTexts(shortTexts).build();

    DecisionDTO decisionDTO = DecisionTransformer.transformToDTO(currentDto, updatedDomainObject);

    assertThat(decisionDTO.getDecisionNames()).isEmpty();
  }

  @Test
  void testTransformToDTO_addLegalEffectWithCoreDataDeleted_shouldSetLegalEffectToNull() {
    DecisionDTO currentDto = DecisionDTO.builder().court(CourtDTO.builder().build()).build();
    Decision updatedDomainObject = Decision.builder().build();

    DecisionDTO decisionDTO = DecisionTransformer.transformToDTO(currentDto, updatedDomainObject);

    assertThat(decisionDTO.getLegalEffect()).isNull();
  }

  @Test
  void testTransformToDTO_withBlankString_shouldConvertToNull() {
    DecisionDTO currentDto = DecisionDTO.builder().note("before change").build();

    Decision updatedDomainObject = Decision.builder().note("  ").build();

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
    Decision updatedDomainObject =
        Decision.builder()
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
    Decision updatedDomainObject =
        Decision.builder()
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
    Decision updatedDomainObject =
        Decision.builder()
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
    Decision updatedDomainObject =
        Decision.builder()
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
    Decision updatedDomainObject =
        Decision.builder().coreData(CoreData.builder().inputTypes(inputTypes).build()).build();

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

    Decision updatedDomainObject =
        Decision.builder()
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

    Decision updatedDomainObject =
        Decision.builder()
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

    Decision updatedDomainObject =
        Decision.builder()
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

    Decision updatedDomainObject =
        Decision.builder()
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
    Decision updatedDomainObject =
        Decision.builder()
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

    Decision updatedDomainObject =
        Decision.builder()
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

    Decision updatedDomainObject =
        Decision.builder()
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
    Decision decision =
        generateSimpleDocumentationUnitBuilder()
            .contentRelatedIndexing(
                ContentRelatedIndexing.builder()
                    .jobProfiles(List.of("job profile", "job profile"))
                    .build())
            .build();

    DecisionDTO decisionDTO =
        DecisionTransformer.transformToDTO(DecisionDTO.builder().build(), decision);

    assertThat(decisionDTO.getJobProfiles()).extracting("value").containsExactly("job profile");
  }

  @Test
  void testTransformToDTO_withSameDismissalTypes_shouldMakeDismissalTypesDistinct() {
    Decision decision =
        generateSimpleDocumentationUnitBuilder()
            .contentRelatedIndexing(
                ContentRelatedIndexing.builder().dismissalTypes(List.of("type", "type")).build())
            .build();

    DecisionDTO decisionDTO =
        DecisionTransformer.transformToDTO(DecisionDTO.builder().build(), decision);

    assertThat(decisionDTO.getDismissalTypes()).extracting("value").containsExactly("type");
  }

  @Test
  void testTransformToDTO_withSameDismissalGrounds_shouldMakeDismissalGroundsDistinct() {
    Decision decision =
        generateSimpleDocumentationUnitBuilder()
            .contentRelatedIndexing(
                ContentRelatedIndexing.builder()
                    .dismissalGrounds(List.of("ground", "ground"))
                    .build())
            .build();

    DecisionDTO decisionDTO =
        DecisionTransformer.transformToDTO(DecisionDTO.builder().build(), decision);

    assertThat(decisionDTO.getDismissalGrounds()).extracting("value").containsExactly("ground");
  }

  @Test
  void testTransformToDTO_withSameCollectiveAgreements_shouldMakeCollectiveAgreementsDistinct() {
    Decision decision =
        generateSimpleDocumentationUnitBuilder()
            .contentRelatedIndexing(
                ContentRelatedIndexing.builder()
                    .collectiveAgreements(List.of("agreement", "agreement"))
                    .build())
            .build();

    DecisionDTO decisionDTO =
        DecisionTransformer.transformToDTO(DecisionDTO.builder().build(), decision);

    assertThat(decisionDTO.getCollectiveAgreements())
        .extracting("value")
        .containsExactly("agreement");
  }

  @Test
  void testTransformToDTO_withSameParticipatingJudges_shouldMakeJudgesDistinct() {
    // Arrange
    ParticipatingJudge participatingJudge = ParticipatingJudge.builder().name("Judge A").build();
    Decision decision =
        generateSimpleDocumentationUnitBuilder()
            .longTexts(
                LongTexts.builder()
                    .participatingJudges(List.of(participatingJudge, participatingJudge))
                    .build())
            .build();

    // Act
    DecisionDTO decisionDTO =
        DecisionTransformer.transformToDTO(DecisionDTO.builder().build(), decision);

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
    Decision updatedDomainObject = Decision.builder().caselawReferences(updatedReferences).build();

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
    Decision updatedDomainObject =
        Decision.builder().literatureReferences(updatedReferences).build();

    DecisionDTO decisionDTO = DecisionTransformer.transformToDTO(currentDto, updatedDomainObject);

    assertThat(decisionDTO.getLiteratureReferences().getFirst().getDocumentationUnitRank()).isOne();
    assertThat(decisionDTO.getLiteratureReferences().getFirst().getEditionRank()).isEqualTo(3);
    assertThat(decisionDTO.getLiteratureReferences().getFirst().getEdition().getName())
        .isEqualTo("Foo");
  }

  @Test
  void testTransformToDTO_withSource_withNoExistingSource() {
    DecisionDTO currentDto = DecisionDTO.builder().build();

    Decision updatedDomainObject =
        Decision.builder()
            .coreData(
                CoreData.builder()
                    .sources(List.of(Source.builder().value(SourceValue.E).build()))
                    .build())
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

    Decision updatedDomainObject =
        Decision.builder()
            .coreData(
                CoreData.builder()
                    .sources(List.of(Source.builder().value(SourceValue.E).build()))
                    .build())
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
    List<SourceDTO> existingSources =
        List.of(
            SourceDTO.builder().value(SourceValue.A).rank(1).build(),
            SourceDTO.builder().value(SourceValue.Z).sourceRawValue("z").rank(2).build());

    DecisionDTO currentDto = DecisionDTO.builder().source(existingSources).build();

    Decision updatedDomainObject =
        Decision.builder()
            .coreData(
                CoreData.builder()
                    .sources(
                        List.of(
                            Source.builder().value(SourceValue.A).build(),
                            Source.builder().value(SourceValue.Z).sourceRawValue("z").build()))
                    .build())
            .build();

    DecisionDTO decisionDTO = DecisionTransformer.transformToDTO(currentDto, updatedDomainObject);
    assertThat(decisionDTO.getSource()).hasSize(2);
    assertThat(decisionDTO.getSource().getFirst().getRank()).isEqualTo(1);
    assertThat(decisionDTO.getSource().getFirst().getValue()).isEqualTo(SourceValue.A);
    assertThat(decisionDTO.getSource().getFirst().getSourceRawValue()).isNull();
    assertThat(decisionDTO.getSource().getFirst().getReference()).isNull();
    assertThat(decisionDTO.getSource().getLast().getRank()).isEqualTo(2);
    assertThat(decisionDTO.getSource().getLast().getValue()).isEqualTo(SourceValue.Z);
    assertThat(decisionDTO.getSource().getLast().getSourceRawValue()).isEqualTo("z");
    assertThat(decisionDTO.getSource().getLast().getReference()).isNull();
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
    LocalDate deviatingDecisionDate = LocalDate.of(2025, 6, 13);
    LocalDate oralHearingDate = LocalDate.of(2023, 2, 3);
    boolean hasDeliveryDate = true;
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
            .hasDeliveryDate(hasDeliveryDate)
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
            .oralHearingDates(
                List.of(OralHearingDateDTO.builder().rank(0L).value(oralHearingDate).build()))
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
    Decision domainObject = DecisionTransformer.transformToDomain(decisionDTO);

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
    assertThat(coreData.hasDeliveryDate()).isEqualTo(hasDeliveryDate);
    assertThat(coreData.documentType().jurisShortcut()).isEqualTo("Urt");
    assertThat(coreData.court().label()).isEqualTo("BVerfG");
    assertThat(coreData.fileNumbers().getFirst()).isEqualTo(fileNumber);
    assertThat(coreData.deviatingFileNumbers().getFirst()).isEqualTo(deviatingFileNumber);
    assertThat(coreData.deviatingCourts().getFirst()).isEqualTo(courtLabel);
    assertThat(coreData.deviatingDecisionDates().getFirst()).isEqualTo(deviatingDecisionDate);
    assertThat(coreData.oralHearingDates().getFirst()).isEqualTo(oralHearingDate);
    assertThat(coreData.documentationOffice()).isNotNull();
    assertThat(coreData.documentationOffice().id()).isEqualTo(docOfficeId);
    assertThat(coreData.documentationOffice().abbreviation()).isEqualTo("DS");

    // --- Assert CoreData fields that are transformed from DecisionTransformer ---
    assertThat(coreData.ecli()).isEqualTo(ecli);
    assertThat(coreData.sources()).isNotNull();
    assertThat(coreData.sources()).hasSize(1);
    assertThat(coreData.sources().getFirst().value()).isEqualTo(SourceValue.E);
    assertThat(coreData.sources().getFirst().sourceRawValue()).isEqualTo("E");
    assertThat(coreData.sources().getFirst().reference()).isNull();
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

    Decision domainObject = DecisionTransformer.transformToDomain(decisionDTO);

    assertThat(domainObject.coreData().sources()).isEmpty();
  }

  @Test
  void testTransformToDomain_withOneValidSource() {
    DecisionDTO decisionDTO =
        DecisionDTO.builder()
            .source(List.of(SourceDTO.builder().value(SourceValue.A).rank(1).build()))
            .build();

    Decision domainObject = DecisionTransformer.transformToDomain(decisionDTO);

    assertThat(domainObject.coreData().sources()).hasSize(1);
    assertThat(domainObject.coreData().sources().getFirst().value()).isEqualTo(SourceValue.A);
    assertThat(domainObject.coreData().sources().getFirst().sourceRawValue()).isNull();
  }

  @Test
  void testTransformToDomain_withMultipleSources() {
    DecisionDTO decisionDTO =
        DecisionDTO.builder()
            .source(
                List.of(
                    SourceDTO.builder().value(SourceValue.O).rank(1).build(),
                    SourceDTO.builder().value(SourceValue.Z).rank(3).build(),
                    SourceDTO.builder().value(SourceValue.A).rank(2).build()))
            .build();

    Decision domainObject = DecisionTransformer.transformToDomain(decisionDTO);

    assertThat(domainObject.coreData().sources())
        .containsExactlyInAnyOrder(
            Source.builder().value(SourceValue.O).build(),
            Source.builder().value(SourceValue.A).build(),
            Source.builder().value(SourceValue.Z).build());
  }

  @Test
  void testTransformToDto_withEvsf_shouldAddEvsf() {
    DecisionDTO currentDto = DecisionDTO.builder().build();

    Decision updatedDomainObject =
        Decision.builder()
            .contentRelatedIndexing(ContentRelatedIndexing.builder().evsf("X 00 00-0-0").build())
            .build();

    DecisionDTO decisionDTO = DecisionTransformer.transformToDTO(currentDto, updatedDomainObject);
    assertThat(decisionDTO.getEvsf()).isEqualTo("X 00 00-0-0");
  }

  @Test
  void testTransformToDto_withoutEvsf_shouldNotAddEvsf() {
    DecisionDTO currentDto = DecisionDTO.builder().build();

    Decision updatedDomainObject =
        Decision.builder().contentRelatedIndexing(ContentRelatedIndexing.builder().build()).build();

    DecisionDTO decisionDTO = DecisionTransformer.transformToDTO(currentDto, updatedDomainObject);
    assertThat(decisionDTO.getEvsf()).isNull();
  }

  @Test
  void testTransformToDto_withDefinitions_shouldAddDefinitions() {
    DecisionDTO currentDto = DecisionDTO.builder().build();

    Decision updatedDomainObject =
        Decision.builder()
            .contentRelatedIndexing(
                ContentRelatedIndexing.builder()
                    .definitions(
                        List.of(
                            Definition.builder()
                                .definedTerm("Test")
                                .definingBorderNumber(1L)
                                .id(UUID.fromString("f73990a0-a9c2-4948-bc6e-8dea1d0d0b2d"))
                                .newEntry(true)
                                .build(),
                            Definition.builder()
                                .definedTerm("Zweiter")
                                .definingBorderNumber(3L)
                                .id(UUID.fromString("d9813057-195f-4735-b338-81b4408c2d52"))
                                .newEntry(false)
                                .build()))
                    .build())
            .build();

    DecisionDTO decisionDTO = DecisionTransformer.transformToDTO(currentDto, updatedDomainObject);
    assertThat(decisionDTO.getDefinitions())
        .usingRecursiveFieldByFieldElementComparator()
        .contains(
            DefinitionDTO.builder().value("Test").rank(1L).id(null).borderNumber(1L).build(),
            DefinitionDTO.builder()
                .value("Zweiter")
                .rank(2L)
                .borderNumber(3L)
                .id(UUID.fromString("d9813057-195f-4735-b338-81b4408c2d52"))
                .build());
  }

  @Test
  void testTransformToDto_withoutDefinitions_shouldNotAddDefinitions() {
    DecisionDTO currentDto = DecisionDTO.builder().build();

    Decision updatedDomainObject =
        Decision.builder().contentRelatedIndexing(ContentRelatedIndexing.builder().build()).build();

    DecisionDTO decisionDTO = DecisionTransformer.transformToDTO(currentDto, updatedDomainObject);
    assertThat(decisionDTO.getDefinitions()).isEmpty();
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

    Decision updatedDomainObject =
        Decision.builder()
            .coreData(
                CoreData.builder()
                    .sources(List.of(Source.builder().value(SourceValue.A).build()))
                    .build())
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

    Decision updatedDomainObject =
        Decision.builder()
            .coreData(
                CoreData.builder()
                    .sources(List.of(Source.builder().value(SourceValue.A).build()))
                    .build())
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
  void testTransformToDto_withAppealAdmitted_shouldAddAppealAdmitted() {
    DecisionDTO currentDto = DecisionDTO.builder().build();

    Decision updatedDomainObject =
        Decision.builder()
            .contentRelatedIndexing(
                ContentRelatedIndexing.builder()
                    .appealAdmission(
                        AppealAdmission.builder().admitted(true).by(AppealAdmitter.BFH).build())
                    .build())
            .build();

    DecisionDTO decisionDTO = DecisionTransformer.transformToDTO(currentDto, updatedDomainObject);
    assertThat(decisionDTO.getAppealAdmitted()).isTrue();
    assertThat(decisionDTO.getAppealAdmittedBy()).isEqualTo(AppealAdmitter.BFH);
  }

  @Test
  void testTransformToDto_withoutAppealAdmitted_shouldNotAddAppealAdmitted() {
    DecisionDTO currentDto = DecisionDTO.builder().build();

    Decision updatedDomainObject =
        Decision.builder().contentRelatedIndexing(ContentRelatedIndexing.builder().build()).build();

    DecisionDTO decisionDTO = DecisionTransformer.transformToDTO(currentDto, updatedDomainObject);
    assertThat(decisionDTO.getAppealAdmitted()).isNull();
    assertThat(decisionDTO.getAppealAdmittedBy()).isNull();
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
    Decision expected =
        generateSimpleDocumentationUnitBuilder()
            .coreData(generateSimpleCoreDataBuilder().legalEffect("Ja").build())
            .build();

    Decision decision = DecisionTransformer.transformToDomain(decisionDTO);

    assertThat(decision).isEqualTo(expected);
  }

  @Test
  void testTransformToDomain_withLegalEffectNo_shouldSetLegalEffectToNo() {
    DecisionDTO decisionDTO = generateSimpleDTOBuilder().legalEffect(LegalEffectDTO.NEIN).build();
    Decision expected =
        generateSimpleDocumentationUnitBuilder()
            .coreData(generateSimpleCoreDataBuilder().legalEffect("Nein").build())
            .build();

    Decision decision = DecisionTransformer.transformToDomain(decisionDTO);

    assertThat(decision).isEqualTo(expected);
  }

  @Test
  void testTransformToDomain_withLegalEffectNotSpecified_shouldSetLegalEffectToNotSpecified() {
    DecisionDTO decisionDTO =
        generateSimpleDTOBuilder().legalEffect(LegalEffectDTO.KEINE_ANGABE).build();
    Decision expected =
        generateSimpleDocumentationUnitBuilder()
            .coreData(generateSimpleCoreDataBuilder().legalEffect("Keine Angabe").build())
            .build();

    Decision decision = DecisionTransformer.transformToDomain(decisionDTO);

    assertThat(decision).isEqualTo(expected);
  }

  @Test
  void testTransformToDomain_withLegalEffectWrongValue_shouldSetLegalEffectToNull() {
    DecisionDTO decisionDTO =
        generateSimpleDTOBuilder().legalEffect(LegalEffectDTO.FALSCHE_ANGABE).build();
    Decision expected =
        generateSimpleDocumentationUnitBuilder()
            .coreData(generateSimpleCoreDataBuilder().build())
            .build();

    Decision decision = DecisionTransformer.transformToDomain(decisionDTO);

    assertThat(decision).isEqualTo(expected);
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
    Decision expected =
        generateSimpleDocumentationUnitBuilder()
            .coreData(generateSimpleCoreDataBuilder().build())
            .ensuingDecisions(
                List.of(
                    EnsuingDecision.builder().pending(false).note("ensuing with rank").build(),
                    EnsuingDecision.builder().pending(true).note("pending with rank").build(),
                    EnsuingDecision.builder().pending(false).note("ensuing without rank").build(),
                    EnsuingDecision.builder().pending(true).note("pending without rank").build()))
            .build();

    Decision decision = DecisionTransformer.transformToDomain(decisionDTO);

    assertThat(decision).isEqualTo(expected);
  }

  @Test
  void testTransformToDomain_textWithMultipleBorderNumberElements_shouldAddAllBorderNumbers() {
    DecisionDTO decisionDTO =
        generateSimpleDTOBuilder()
            .grounds(
                "lorem ipsum<border-number><number>1</number><content>foo</content></border-number> dolor sit amet <border-number><number>2</number><content>bar</content></border-number>")
            .build();

    Decision decision = DecisionTransformer.transformToDomain(decisionDTO);

    assertThat(decision.managementData().borderNumbers()).hasSize(2).containsExactly("1", "2");
  }

  @Test
  void testTransformScheduledPublicationDate_withDate_shouldAddScheduledPublicationDate() {
    DecisionDTO decisionDTO =
        generateSimpleDTOBuilder()
            .scheduledPublicationDateTime(LocalDateTime.parse("2022-01-23T18:25:14"))
            .build();

    Decision decision = DecisionTransformer.transformToDomain(decisionDTO);

    assertThat(decision.managementData().scheduledPublicationDateTime())
        .isEqualTo("2022-01-23T18:25:14");
  }

  @Test
  void testTransformScheduledPublicationDate_withoutDate_shouldNotAddScheduledPublicationDate() {
    DecisionDTO decisionDTO = generateSimpleDTOBuilder().scheduledPublicationDateTime(null).build();

    Decision decision = DecisionTransformer.transformToDomain(decisionDTO);

    assertThat(decision.managementData().scheduledPublicationDateTime()).isNull();
  }

  @Test
  void testTransformLastHandoverDate_withDate_shouldAddLastHandoverDate() {
    DecisionDTO decisionDTO =
        generateSimpleDTOBuilder()
            .lastHandoverDateTime(LocalDateTime.parse("2022-01-23T18:25:14"))
            .build();

    Decision decision = DecisionTransformer.transformToDomain(decisionDTO);

    assertThat(decision.managementData().lastHandoverDateTime()).isEqualTo("2022-01-23T18:25:14");
  }

  @Test
  void testTransformLastHandoverDate_withoutDate_shouldNotAddLastHandoverDate() {
    DecisionDTO decisionDTO = generateSimpleDTOBuilder().lastHandoverDateTime(null).build();

    Decision decision = DecisionTransformer.transformToDomain(decisionDTO);

    assertThat(decision.managementData().lastHandoverDateTime()).isNull();
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

    Decision decision = DecisionTransformer.transformToDomain(decisionDTO);

    assertThat(decision.managementData().borderNumbers())
        .hasSize(4)
        .containsExactly("1", "2", "3", "4");
  }

  @Test
  void testTransformToDomain_textWithoutBorderNumberElements_shouldNotAddBorderNumbers() {
    DecisionDTO decisionDTO =
        generateSimpleDTOBuilder().grounds("lorem ipsum dolor sit amet").build();

    Decision decision = DecisionTransformer.transformToDomain(decisionDTO);

    assertThat(decision.managementData().borderNumbers()).isEmpty();
  }

  @Test
  void
      testTransformToDomain_textWithMalformedBorderNumberElement_shouldOnlyAddValidBorderNumbers() {
    DecisionDTO decisionDTO =
        generateSimpleDTOBuilder()
            .grounds(
                "lorem ipsum<border-number><content>foo</content></border-number> dolor sit amet <border-number><number>2</number><content>bar</content></border-number>")
            .build();

    Decision decision = DecisionTransformer.transformToDomain(decisionDTO);

    assertThat(decision.managementData().borderNumbers()).hasSize(1).containsExactly("2");
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

    Decision decision = DecisionTransformer.transformToDomain(decisionDTO);

    assertThat(decision.coreData().leadingDecisionNormReferences())
        .hasSize(3)
        .containsExactly("BGB §1", "BGB §2", "BGB §3");
  }

  @Test
  void testTransformToDomain_withNote_shouldAddNote() {
    DecisionDTO decisionDTO = generateSimpleDTOBuilder().note("Beispiel Notiz").build();

    Decision decision = DecisionTransformer.transformToDomain(decisionDTO);

    assertThat(decision.note()).isEqualTo("Beispiel Notiz");
  }

  @Test
  void testTransformToDomain_withEmptyNote_shouldAddEmptyNote() {
    DecisionDTO decisionDTO = generateSimpleDTOBuilder().note("").build();

    Decision decision = DecisionTransformer.transformToDomain(decisionDTO);

    assertThat(decision.note()).isEmpty();
  }

  @Test
  void testTransformToDomain_withNullNote_shouldAddNullNote() {
    DecisionDTO decisionDTO = generateSimpleDTOBuilder().note(null).build();

    Decision decision = DecisionTransformer.transformToDomain(decisionDTO);

    assertThat(decision.note()).isNull();
  }

  @Test
  void testTransformToDomain_withoutNote_shouldAddNoNote() {
    DecisionDTO decisionDTO = generateSimpleDTOBuilder().build();

    Decision decision = DecisionTransformer.transformToDomain(decisionDTO);

    assertThat(decision.note()).isNull();
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

    Decision decision = DecisionTransformer.transformToDomain(decisionDTO);

    assertThat(decision.contentRelatedIndexing().jobProfiles()).containsExactly("job profile");
  }

  @Test
  void testTransformToDomain_withCollectiveAgreements_shouldAddCollectiveAgreements() {
    DecisionDTO decisionDTO =
        generateSimpleDTOBuilder()
            .collectiveAgreements(
                List.of(CollectiveAgreementDTO.builder().value("agreement").build()))
            .build();

    Decision decision = DecisionTransformer.transformToDomain(decisionDTO);

    assertThat(decision.contentRelatedIndexing().collectiveAgreements())
        .containsExactly("agreement");
  }

  @Test
  void testTransformToDomain_withDismissalTypes_shouldAddDismissalTypes() {
    DecisionDTO decisionDTO =
        generateSimpleDTOBuilder()
            .dismissalTypes(List.of(DismissalTypesDTO.builder().value("type").build()))
            .build();

    Decision decision = DecisionTransformer.transformToDomain(decisionDTO);

    assertThat(decision.contentRelatedIndexing().dismissalTypes()).containsExactly("type");
  }

  @Test
  void testTransformToDomain_withDismissalGrounds_shouldAddDismissalGrounds() {
    DecisionDTO decisionDTO =
        generateSimpleDTOBuilder()
            .dismissalGrounds(List.of(DismissalGroundsDTO.builder().value("ground").build()))
            .build();

    Decision decision = DecisionTransformer.transformToDomain(decisionDTO);

    assertThat(decision.contentRelatedIndexing().dismissalGrounds()).containsExactly("ground");
  }

  @Test
  void testTransformToDomain_withLegislativeMandate_shouldLegislativeMandateBeTrue() {
    DecisionDTO decisionDTO = generateSimpleDTOBuilder().hasLegislativeMandate(true).build();

    Decision decision = DecisionTransformer.transformToDomain(decisionDTO);

    assertThat(decision.contentRelatedIndexing().hasLegislativeMandate()).isTrue();
  }

  @Test
  void testTransformToDomain_withEvsf_shouldAddEvsf() {
    DecisionDTO decisionDTO = generateSimpleDTOBuilder().evsf("X 00 00-0-0").build();

    Decision decision = DecisionTransformer.transformToDomain(decisionDTO);

    assertThat(decision.contentRelatedIndexing().evsf()).isEqualTo("X 00 00-0-0");
  }

  @Test
  void testTransformToDomain_withoutEvsf_shouldNotAddEvsf() {
    DecisionDTO decisionDTO = generateSimpleDTOBuilder().build();

    Decision decision = DecisionTransformer.transformToDomain(decisionDTO);

    assertThat(decision.contentRelatedIndexing().evsf()).isNull();
  }

  @Test
  void testTransformToDomain_withDefinitions_shouldAddDefinitions() {
    DecisionDTO decisionDTO =
        generateSimpleDTOBuilder()
            .definitions(List.of(DefinitionDTO.builder().value("test").borderNumber(3L).build()))
            .build();

    Decision decision = DecisionTransformer.transformToDomain(decisionDTO);

    assertThat(decision.contentRelatedIndexing().definitions())
        .isEqualTo(
            List.of(Definition.builder().definedTerm("test").definingBorderNumber(3L).build()));
  }

  @Test
  void testTransformToDomain_withoutDefinitions_shouldNotAddDefinitions() {
    DecisionDTO decisionDTO = DecisionDTO.builder().build();

    Decision decision = DecisionTransformer.transformToDomain(decisionDTO);

    assertThat(decision.contentRelatedIndexing().definitions()).isEqualTo(List.of());
  }

  @Test
  void testTransformToDomain_withoutLegislativeMandate_shouldLegislativeMandateBeFalse() {
    DecisionDTO decisionDTO = generateSimpleDTOBuilder().hasLegislativeMandate(false).build();

    Decision decision = DecisionTransformer.transformToDomain(decisionDTO);

    assertThat(decision.contentRelatedIndexing().hasLegislativeMandate()).isFalse();
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
    Decision decision = DecisionTransformer.transformToDomain(decisionDTO);

    // Assert
    assertThat(decision.longTexts().participatingJudges()).hasSize(2);
    assertThat(decision.longTexts().participatingJudges().getFirst().id())
        .isEqualTo(participatingJudgeA.getId());
    assertThat(decision.longTexts().participatingJudges().getFirst().name())
        .isEqualTo(participatingJudgeA.getName());
    assertThat(decision.longTexts().participatingJudges().getFirst().referencedOpinions())
        .isEqualTo(participatingJudgeA.getReferencedOpinions());
    assertThat(decision.longTexts().participatingJudges().get(1).id())
        .isEqualTo(participatingJudgeB.getId());

    assertThat(decision.longTexts().participatingJudges().get(1).name())
        .isEqualTo(participatingJudgeB.getName());
    assertThat(decision.longTexts().participatingJudges().get(1).referencedOpinions())
        .isEqualTo(participatingJudgeB.getReferencedOpinions());
  }

  @Test
  void testTransformToDomain_withoutParticipatingJudges_shouldAddEmptyList() {
    // Act
    Decision decision = DecisionTransformer.transformToDomain(generateSimpleDTOBuilder().build());

    // Assert
    assertThat(decision.longTexts().participatingJudges()).isEmpty();
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

    Decision decision = DecisionTransformer.transformToDomain(decisionDTO);

    assertThat(decision.documentalists()).containsExactly("documentalist1", "documentalist2");
  }

  @Test
  void testTransformToDomain_withoutDocumentalists_shouldNotAddDocumentalists() {
    DecisionDTO decisionDTO = generateSimpleDTOBuilder().build();

    Decision decision = DecisionTransformer.transformToDomain(decisionDTO);

    assertThat(decision.documentalists()).isEmpty();
  }

  @Test
  void testTransformToDomain_withAttachments_shouldOnlyAddDocxAndFmxTypes() {
    DecisionDTO decisionDTO =
        generateSimpleDTOBuilder()
            .attachments(
                List.of(
                    AttachmentDTO.builder().filename("foo").format("fmx").build(),
                    AttachmentDTO.builder().filename("bar").format("docx").build(),
                    AttachmentDTO.builder().filename("baz").format("png").build(),
                    AttachmentDTO.builder().filename("qux").format("jpg").build(),
                    AttachmentDTO.builder().filename("quux").format("foo").build()))
            .build();

    Decision decision = DecisionTransformer.transformToDomain(decisionDTO);

    assertThat(decision.attachments())
        .hasSize(2)
        .satisfiesExactlyInAnyOrder(
            attachment -> assertThat(attachment.name()).isEqualTo("foo"),
            attachment -> assertThat(attachment.name()).isEqualTo("bar"));
  }

  @Test
  void testTransformToDomain_withDeviatingDocumentNumbers_shouldAdd() {
    DecisionDTO decisionDTO =
        generateSimpleDTOBuilder()
            .deviatingDocumentNumbers(
                List.of(
                    DeviatingDocumentNumberDTO.builder().value("XXRE123456789").rank(1L).build(),
                    DeviatingDocumentNumberDTO.builder().value("XXRE234567890").rank(2L).build()))
            .build();

    Decision decision = DecisionTransformer.transformToDomain(decisionDTO);

    assertThat(decision.coreData().deviatingDocumentNumbers())
        .containsExactly("XXRE123456789", "XXRE234567890");
  }

  @Test
  void testTransformToDomain_withOralHearingDates_shouldAdd() {
    DecisionDTO decisionDTO =
        generateSimpleDTOBuilder()
            .oralHearingDates(
                List.of(
                    OralHearingDateDTO.builder().value(LocalDate.of(2020, 1, 1)).rank(1L).build(),
                    OralHearingDateDTO.builder().value(LocalDate.of(2022, 2, 2)).rank(2L).build()))
            .build();

    Decision decision = DecisionTransformer.transformToDomain(decisionDTO);

    assertThat(decision.coreData().oralHearingDates())
        .containsExactly(LocalDate.of(2020, 1, 1), LocalDate.of(2022, 2, 2));
  }

  @Test
  void testTransformToDomain_withoutDeviatingDocumentNumbers_shouldNotAdd() {
    DecisionDTO decisionDTO = generateSimpleDTOBuilder().build();

    Decision decision = DecisionTransformer.transformToDomain(decisionDTO);

    assertThat(decision.coreData().deviatingDocumentNumbers()).isEmpty();
  }

  @Test
  void testTransformToDTO_withDeviatingDocumentNumbers_shouldAdd() {
    Decision decision =
        generateSimpleDocumentationUnitBuilder()
            .coreData(
                generateSimpleCoreDataBuilder()
                    .deviatingDocumentNumbers(List.of("XXRE123456789", "XXRE234567890"))
                    .build())
            .build();

    DecisionDTO decisionDTO =
        DecisionTransformer.transformToDTO(generateSimpleDTOBuilder().build(), decision);

    assertThat(decisionDTO.getDeviatingDocumentNumbers())
        .extracting("value")
        .containsExactly("XXRE123456789", "XXRE234567890");
  }

  @Test
  void testTransformToDTO_withOralHearingDates_shouldAdd() {
    Decision decision =
        generateSimpleDocumentationUnitBuilder()
            .coreData(
                generateSimpleCoreDataBuilder()
                    .oralHearingDates(List.of(LocalDate.of(2020, 1, 1), LocalDate.of(2020, 1, 2)))
                    .build())
            .build();

    DecisionDTO decisionDTO =
        DecisionTransformer.transformToDTO(generateSimpleDTOBuilder().build(), decision);

    assertThat(decisionDTO.getOralHearingDates())
        .extracting("value")
        .containsExactly(LocalDate.of(2020, 1, 1), LocalDate.of(2020, 1, 2));
  }

  @Test
  void testTransformToDTO_withoutDeviatingDocumentNumbers_shouldNotAdd() {
    Decision decision =
        generateSimpleDocumentationUnitBuilder()
            .coreData(generateSimpleCoreDataBuilder().build())
            .build();

    DecisionDTO decisionDTO =
        DecisionTransformer.transformToDTO(generateSimpleDTOBuilder().build(), decision);

    assertThat(decisionDTO.getDeviatingDocumentNumbers()).isEmpty();
  }

  @Nested
  class ForeignLanguageVersions {
    @Test
    void testTransformToDomain_withForeignLanguageVersions_shouldAddData() {
      // Arrange
      var englisch =
          ForeignLanguageVersionDTO.builder()
              .url("https://link-to-tranlsation.en")
              .languageCode(LanguageCodeDTO.builder().isoCode("en").value("Englisch").build())
              .rank(1L)
              .build();
      var french =
          ForeignLanguageVersionDTO.builder()
              .url("https://link-to-tranlsation.fr")
              .languageCode(LanguageCodeDTO.builder().isoCode("fr").value("Französisch").build())
              .rank(2L)
              .build();
      DecisionDTO decisionDTO =
          generateSimpleDTOBuilder().foreignLanguageVersions(List.of(englisch, french)).build();

      // Act
      Decision decision = DecisionTransformer.transformToDomain(decisionDTO);

      // Assert
      assertThat(decision.contentRelatedIndexing().foreignLanguageVersions()).hasSize(2);
      assertThat(decision.contentRelatedIndexing().foreignLanguageVersions().get(0))
          .isEqualTo(
              ForeignLanguageVersion.builder()
                  .link("https://link-to-tranlsation.en")
                  .languageCode(LanguageCode.builder().isoCode("en").label("Englisch").build())
                  .build());
      assertThat(decision.contentRelatedIndexing().foreignLanguageVersions().get(1))
          .isEqualTo(
              ForeignLanguageVersion.builder()
                  .link("https://link-to-tranlsation.fr")
                  .languageCode(LanguageCode.builder().isoCode("fr").label("Französisch").build())
                  .build());
    }

    @Test
    void testTransformToDomain_withoutForeignLanguageVersions_shouldNotAddData() {
      // Arrange
      DecisionDTO decisionDTO = generateSimpleDTOBuilder().build();

      // Act
      Decision decision = DecisionTransformer.transformToDomain(decisionDTO);

      // Assert
      assertThat(decision.contentRelatedIndexing().foreignLanguageVersions()).isEmpty();
    }

    @Test
    void testTransformToDTO_withForeignLanguageVersions_shouldAddData() {
      // Arrange
      var englisch =
          ForeignLanguageVersion.builder()
              .link("https://link-to-tranlsation.en")
              .languageCode(LanguageCode.builder().isoCode("en").label("Englisch").build())
              .build();
      var french =
          ForeignLanguageVersion.builder()
              .link("https://link-to-tranlsation.fr")
              .languageCode(LanguageCode.builder().isoCode("fr").label("Französisch").build())
              .build();
      Decision decision =
          Decision.builder()
              .contentRelatedIndexing(
                  ContentRelatedIndexing.builder()
                      .foreignLanguageVersions(List.of(englisch, french))
                      .build())
              .build();

      // Act
      DecisionDTO decisionDTO =
          DecisionTransformer.transformToDTO(generateSimpleDTOBuilder().build(), decision);

      // Assert
      assertThat(decisionDTO.getForeignLanguageVersions()).hasSize(2);
      assertThat(decisionDTO.getForeignLanguageVersions().get(0).getUrl())
          .isEqualTo("https://link-to-tranlsation.en");
      assertThat(decisionDTO.getForeignLanguageVersions().get(0).getRank()).isEqualTo(1L);
      assertThat(decisionDTO.getForeignLanguageVersions().get(0).getLanguageCode().getValue())
          .isEqualTo("Englisch");
      assertThat(decisionDTO.getForeignLanguageVersions().get(0).getLanguageCode().getIsoCode())
          .isEqualTo("en");
      assertThat(decisionDTO.getForeignLanguageVersions().get(1).getUrl())
          .isEqualTo("https://link-to-tranlsation.fr");
      assertThat(decisionDTO.getForeignLanguageVersions().get(1).getRank()).isEqualTo(2L);
      assertThat(decisionDTO.getForeignLanguageVersions().get(1).getLanguageCode().getValue())
          .isEqualTo("Französisch");
      assertThat(decisionDTO.getForeignLanguageVersions().get(1).getLanguageCode().getIsoCode())
          .isEqualTo("fr");
    }

    @Test
    void testTransformToDTO_withoutForeignLanguageVersions_shouldNotAddData() {
      // Arrange
      Decision decision = generateSimpleDocumentationUnitBuilder().build();

      // Act
      DecisionDTO decisionDTO =
          DecisionTransformer.transformToDTO(generateSimpleDTOBuilder().build(), decision);

      // Assert
      assertThat(decisionDTO.getForeignLanguageVersions()).isEmpty();
    }
  }

  @Test
  void testTransformToDomain_withCelex_resultShouldHaveCelex() {
    DecisionDTO decisionDTO = generateSimpleDTOBuilder().celexNumber("62023CJ0538").build();

    Decision decision = DecisionTransformer.transformToDomain(decisionDTO);

    assertThat(decision.coreData().celexNumber()).isEqualTo("62023CJ0538");
  }

  @Test
  void testTransformToDomain_withoutCelex_resultShouldNotHaveCelex() {
    DecisionDTO decisionDTO = generateSimpleDTOBuilder().build();

    Decision decision = DecisionTransformer.transformToDomain(decisionDTO);

    assertThat(decision.coreData().celexNumber()).isBlank();
  }

  @Test
  void testTransformToDTO_withCelex_resultShouldHaveCelex() {
    Decision decision =
        Decision.builder().coreData(CoreData.builder().celexNumber("62023CJ0538").build()).build();

    DecisionDTO decisionDTO =
        DecisionTransformer.transformToDTO(generateSimpleDTOBuilder().build(), decision);

    assertThat(decisionDTO.getCelexNumber()).isEqualTo("62023CJ0538");
  }

  @Test
  void testTransformToDTO_withoutCelex_resultShouldNotHaveCelex() {
    Decision decision = Decision.builder().coreData(CoreData.builder().build()).build();

    DecisionDTO decisionDTO =
        DecisionTransformer.transformToDTO(generateSimpleDTOBuilder().build(), decision);

    assertThat(decisionDTO.getCelexNumber()).isBlank();
  }

  @Test
  void testTransformToDomain_withAppealAdmitted_shouldAddAppealAdmitted() {
    DecisionDTO decisionDTO =
        generateSimpleDTOBuilder()
            .appealAdmitted(true)
            .appealAdmittedBy(AppealAdmitter.BFH)
            .build();

    Decision decision = DecisionTransformer.transformToDomain(decisionDTO);

    assertThat(decision.contentRelatedIndexing().appealAdmission().admitted()).isTrue();
    assertThat(decision.contentRelatedIndexing().appealAdmission().by())
        .isEqualTo(AppealAdmitter.BFH);
  }

  @Test
  void testTransformToDomain_withoutAppealAdmitted_shouldNotAddAppealAdmitted() {
    DecisionDTO decisionDTO = generateSimpleDTOBuilder().build();

    Decision decision = DecisionTransformer.transformToDomain(decisionDTO);

    assertThat(decision.contentRelatedIndexing().appealAdmission()).isNull();
  }

  @Test
  void transformToDomain_withAppeal_shouldAddAppeal() {
    DecisionDTO decisionDTO = generateSimpleDTOBuilder().build();

    decisionDTO.setAppeal(
        AppealDTO.builder()
            .appealWithdrawal(AppealWithdrawal.JA)
            .pkhPlaintiff(PkhPlaintiff.JA)
            .build());

    Decision decision = DecisionTransformer.transformToDomain(decisionDTO);

    assertThat(decision.contentRelatedIndexing().appeal()).isNotNull();
    assertThat(decision.contentRelatedIndexing().appeal().appealWithdrawal())
        .isEqualTo(AppealWithdrawal.JA);
    assertThat(decision.contentRelatedIndexing().appeal().pkhPlaintiff())
        .isEqualTo(PkhPlaintiff.JA);
  }

  @Test
  void transformToDomain_withoutAppeal_shouldNotAddAppeal() {
    DecisionDTO decisionDTO = generateSimpleDTOBuilder().build();

    Decision decision = DecisionTransformer.transformToDomain(decisionDTO);

    assertThat(decision.contentRelatedIndexing().appeal()).isNull();
  }

  @Test
  void transformToDTO_withAppeal_shouldAddAppeal() {
    Decision decision =
        Decision.builder()
            .contentRelatedIndexing(
                ContentRelatedIndexing.builder()
                    .appeal(Appeal.builder().appealWithdrawal(AppealWithdrawal.JA).build())
                    .build())
            .build();

    DecisionDTO decisionDTO =
        DecisionTransformer.transformToDTO(generateSimpleDTOBuilder().build(), decision);

    assertThat(decisionDTO.getAppeal()).isNotNull();
    assertThat(decisionDTO.getAppeal().getAppealWithdrawal()).isEqualTo(AppealWithdrawal.JA);
  }

  @Test
  void transformToDTO_withoutAppeal_shouldNotAddAppeal() {
    Decision decision = Decision.builder().build();
    DecisionDTO currentDTO = generateSimpleDTOBuilder().build();

    DecisionDTO decisionDTO = DecisionTransformer.transformToDTO(currentDTO, decision);

    assertThat(decisionDTO.getAppeal()).isNull();
  }

  private Decision.DecisionBuilder generateSimpleDocumentationUnitBuilder() {
    return Decision.builder()
        .portalPublicationStatus(PortalPublicationStatus.UNPUBLISHED)
        .previousDecisions(Collections.emptyList())
        .ensuingDecisions(Collections.emptyList())
        .shortTexts(ShortTexts.builder().decisionNames(Collections.emptyList()).build())
        .longTexts(LongTexts.builder().build())
        .managementData(
            ManagementData.builder()
                .scheduledPublicationDateTime(null)
                .lastHandoverDateTime(null)
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
                .definitions(Collections.emptyList())
                .hasLegislativeMandate(false)
                .evsf(null)
                .foreignLanguageVersions(Collections.emptyList())
                .build())
        .caselawReferences(Collections.emptyList())
        .literatureReferences(Collections.emptyList())
        .documentalists(Collections.emptyList())
        .processSteps(Collections.emptyList());
  }

  private CoreDataBuilder generateSimpleCoreDataBuilder() {
    return CoreData.builder()
        .documentationOffice(DocumentationOffice.builder().abbreviation("doc office").build())
        .deviatingDocumentNumbers(Collections.emptyList())
        .oralHearingDates(Collections.emptyList())
        .fileNumbers(Collections.emptyList())
        .deviatingFileNumbers(Collections.emptyList())
        .deviatingCourts(Collections.emptyList())
        .previousProcedures(Collections.emptyList())
        .deviatingEclis(Collections.emptyList())
        .deviatingDecisionDates(Collections.emptyList())
        .inputTypes(Collections.emptyList())
        .leadingDecisionNormReferences(Collections.emptyList())
        .yearsOfDispute(Collections.emptyList())
        .sources(Collections.emptyList());
  }
}
