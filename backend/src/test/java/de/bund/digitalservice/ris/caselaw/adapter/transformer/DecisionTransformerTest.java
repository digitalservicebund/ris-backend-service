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
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DeviatingEcliDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DeviatingFileNumberDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DismissalGroundsDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DismissalTypesDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentationOfficeDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DuplicateRelationDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.EnsuingDecisionDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.InputTypeDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.JobProfileDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.LeadingDecisionNormReferenceDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.LegalEffectDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.LegalPeriodicalEditionDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.LiteratureReferenceDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.NormAbbreviationDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.NormReferenceDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.ParticipatingJudgeDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.PendingDecisionDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.PreviousDecisionDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.SourceDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.StatusDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.YearOfDisputeDTO;
import de.bund.digitalservice.ris.caselaw.domain.ActiveCitation;
import de.bund.digitalservice.ris.caselaw.domain.ContentRelatedIndexing;
import de.bund.digitalservice.ris.caselaw.domain.CoreData;
import de.bund.digitalservice.ris.caselaw.domain.CoreData.CoreDataBuilder;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationOffice;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnit;
import de.bund.digitalservice.ris.caselaw.domain.DuplicateRelation;
import de.bund.digitalservice.ris.caselaw.domain.EnsuingDecision;
import de.bund.digitalservice.ris.caselaw.domain.LegalForce;
import de.bund.digitalservice.ris.caselaw.domain.LongTexts;
import de.bund.digitalservice.ris.caselaw.domain.ManagementData;
import de.bund.digitalservice.ris.caselaw.domain.NormReference;
import de.bund.digitalservice.ris.caselaw.domain.PreviousDecision;
import de.bund.digitalservice.ris.caselaw.domain.PublicationStatus;
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
import java.time.Instant;
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
import org.junit.jupiter.params.provider.ValueSource;

class DecisionTransformerTest {
  @Test
  void testTransformToDTO_withoutCoreData() {
    DecisionDTO currentDto = DecisionDTO.builder().build();
    DocumentationUnit updatedDomainObject = DocumentationUnit.builder().build();

    DecisionDTO documentationUnitDTO =
        (DecisionDTO) DecisionTransformer.transformToDTO(currentDto, updatedDomainObject);

    assertThat(documentationUnitDTO.getProcedureHistory()).isEmpty();
    assertThat(documentationUnitDTO.getProcedure()).isNull();
    assertThat(documentationUnitDTO.getEcli()).isNull();
    assertThat(documentationUnitDTO.getJudicialBody()).isNull();
    assertThat(documentationUnitDTO.getDate()).isNull();
    assertThat(documentationUnitDTO.getScheduledPublicationDateTime()).isNull();
    assertThat(documentationUnitDTO.getLastPublicationDateTime()).isNull();
    assertThat(documentationUnitDTO.getCourt()).isNull();
    assertThat(documentationUnitDTO.getDocumentType()).isNull();
    assertThat(documentationUnitDTO.getDocumentationOffice()).isNull();
  }

  @Test
  void testTransformToDTO_withoutDecisionNames() {
    DecisionDTO currentDto = DecisionDTO.builder().build();
    ShortTexts shortTexts = ShortTexts.builder().build();
    DocumentationUnit updatedDomainObject =
        DocumentationUnit.builder().shortTexts(shortTexts).build();

    DecisionDTO documentationUnitDTO =
        (DecisionDTO) DecisionTransformer.transformToDTO(currentDto, updatedDomainObject);

    assertThat(documentationUnitDTO.getDecisionNames()).isEmpty();
  }

  @Test
  void testTransformToDTO_addLegalEffectWithCoreDataDeleted_shouldSetLegalEffectToNull() {
    DecisionDTO currentDto = DecisionDTO.builder().court(CourtDTO.builder().build()).build();
    DocumentationUnit updatedDomainObject = DocumentationUnit.builder().build();

    DecisionDTO documentationUnitDTO =
        (DecisionDTO) DecisionTransformer.transformToDTO(currentDto, updatedDomainObject);

    assertThat(documentationUnitDTO.getLegalEffect()).isNull();
  }

  @Test
  void testTransformToDTO_withBlankString_shouldConvertToNull() {
    DecisionDTO currentDto = DecisionDTO.builder().note("before change").build();

    DocumentationUnit updatedDomainObject = DocumentationUnit.builder().note("  ").build();

    DecisionDTO documentationUnitDTO =
        DecisionTransformer.transformToDTO(currentDto, updatedDomainObject);

    assertNull(documentationUnitDTO.getNote());
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

    DecisionDTO documentationUnitDTO =
        (DecisionDTO) DecisionTransformer.transformToDTO(currentDto, updatedDomainObject);

    assertThat(documentationUnitDTO.getLegalEffect()).isEqualTo(LegalEffectDTO.NEIN);
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

    DecisionDTO documentationUnitDTO =
        DecisionTransformer.transformToDTO(currentDto, updatedDomainObject);

    assertThat(documentationUnitDTO.getLegalEffect()).isEqualTo(LegalEffectDTO.KEINE_ANGABE);
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

    DecisionDTO documentationUnitDTO =
        DecisionTransformer.transformToDTO(currentDto, updatedDomainObject);

    assertThat(documentationUnitDTO.getLegalEffect()).isEqualTo(LegalEffectDTO.JA);
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

    DecisionDTO documentationUnitDTO =
        (DecisionDTO) DecisionTransformer.transformToDTO(currentDto, updatedDomainObject);

    assertThat(documentationUnitDTO.getLegalEffect()).isEqualTo(LegalEffectDTO.JA);
  }

  @Test
  void testTransformToDTO_withInputTypes() {
    DecisionDTO currentDto = DecisionDTO.builder().build();
    List<String> inputTypes = List.of("input types 1", "input types 3", "input types 2");
    DocumentationUnit updatedDomainObject =
        DocumentationUnit.builder()
            .coreData(CoreData.builder().inputTypes(inputTypes).build())
            .build();

    DecisionDTO documentationUnitDTO =
        (DecisionDTO) DecisionTransformer.transformToDTO(currentDto, updatedDomainObject);

    assertThat(documentationUnitDTO.getInputTypes())
        .usingRecursiveFieldByFieldElementComparator()
        .containsExactly(
            InputTypeDTO.builder().value("input types 1").rank(1L).build(),
            InputTypeDTO.builder().value("input types 3").rank(2L).build(),
            InputTypeDTO.builder().value("input types 2").rank(3L).build());
  }

  @Test
  void testTransformToDTO_withOneNormReference() {
    DecisionDTO currentDto = DecisionDTO.builder().build();
    NormReference normReferenceInput =
        NormReference.builder()
            .normAbbreviation(
                NormAbbreviation.builder()
                    .id(UUID.fromString("33333333-2222-3333-4444-555555555555"))
                    .build())
            .singleNorms(List.of(SingleNorm.builder().singleNorm("single norm").build()))
            .build();

    DocumentationUnit updatedDomainObject =
        DocumentationUnit.builder()
            .contentRelatedIndexing(
                ContentRelatedIndexing.builder().norms(List.of(normReferenceInput)).build())
            .build();

    DecisionDTO documentationUnitDTO =
        DecisionTransformer.transformToDTO(currentDto, updatedDomainObject);

    assertThat(documentationUnitDTO.getNormReferences().get(0).getNormAbbreviation().getId())
        .isEqualTo(normReferenceInput.normAbbreviation().id());
    assertThat(documentationUnitDTO.getNormReferences().get(0).getSingleNorm())
        .isEqualTo(normReferenceInput.singleNorms().get(0).singleNorm());
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

    DecisionDTO documentationUnitDTO =
        DecisionTransformer.transformToDTO(currentDto, updatedDomainObject);

    assertThat(documentationUnitDTO.getNormReferences().get(0).getLegalForce()).isNotNull();
    assertThat(
            documentationUnitDTO
                .getNormReferences()
                .get(0)
                .getLegalForce()
                .getLegalForceType()
                .getId())
        .isEqualTo(normReferenceInput.singleNorms().get(0).legalForce().type().id());
    assertThat(documentationUnitDTO.getNormReferences().get(0).getLegalForce().getRegion().getId())
        .isEqualTo(normReferenceInput.singleNorms().get(0).legalForce().region().id());
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

    DecisionDTO documentationUnitDTO =
        DecisionTransformer.transformToDTO(currentDto, updatedDomainObject);

    assertThat(documentationUnitDTO.getNormReferences().get(0).getNormAbbreviation().getId())
        .isEqualTo(normReferenceInput.normAbbreviation().id());
    assertThat(documentationUnitDTO.getNormReferences().get(0).getSingleNorm())
        .isEqualTo(normReferenceInput.singleNorms().get(0).singleNorm());
    assertThat(documentationUnitDTO.getNormReferences().get(0).getLegalForce()).isNotNull();
    assertThat(
            documentationUnitDTO
                .getNormReferences()
                .get(0)
                .getLegalForce()
                .getLegalForceType()
                .getId())
        .isEqualTo(normReferenceInput.singleNorms().get(0).legalForce().type().id());
    assertThat(documentationUnitDTO.getNormReferences().get(0).getLegalForce().getRegion().getId())
        .isEqualTo(normReferenceInput.singleNorms().get(0).legalForce().region().id());

    assertThat(documentationUnitDTO.getNormReferences().get(1).getSingleNorm())
        .isEqualTo(normReferenceInput.singleNorms().get(1).singleNorm());
    assertThat(documentationUnitDTO.getNormReferences().get(1).getLegalForce()).isNotNull();
    assertThat(
            documentationUnitDTO
                .getNormReferences()
                .get(1)
                .getLegalForce()
                .getLegalForceType()
                .getId())
        .isEqualTo(normReferenceInput.singleNorms().get(1).legalForce().type().id());
    assertThat(documentationUnitDTO.getNormReferences().get(1).getLegalForce().getRegion().getId())
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

    DecisionDTO documentationUnitDTO =
        DecisionTransformer.transformToDTO(currentDto, updatedDomainObject);

    assertThat(documentationUnitDTO.getNormReferences().get(0).getNormAbbreviation().getId())
        .isEqualTo(normReferenceInput1.normAbbreviation().id());
    assertThat(documentationUnitDTO.getNormReferences().get(0).getSingleNorm())
        .isEqualTo(normReferenceInput1.singleNorms().get(0).singleNorm());

    assertThat(documentationUnitDTO.getNormReferences().get(1).getNormAbbreviation().getId())
        .isEqualTo(normReferenceInput2.normAbbreviation().id());
    assertThat(documentationUnitDTO.getNormReferences().get(1).getSingleNorm())
        .isEqualTo(normReferenceInput2.singleNorms().get(0).singleNorm());
    assertThat(documentationUnitDTO.getNormReferences().get(1).getLegalForce()).isNotNull();
    assertThat(
            documentationUnitDTO
                .getNormReferences()
                .get(1)
                .getLegalForce()
                .getLegalForceType()
                .getId())
        .isEqualTo(normReferenceInput2.singleNorms().get(0).legalForce().type().id());
    assertThat(documentationUnitDTO.getNormReferences().get(1).getLegalForce().getRegion().getId())
        .isEqualTo(normReferenceInput2.singleNorms().get(0).legalForce().region().id());
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

    DecisionDTO documentationUnitDTO =
        (DecisionDTO) DecisionTransformer.transformToDTO(currentDto, updatedDomainObject);

    assertThat(documentationUnitDTO.getLeadingDecisionNormReferences())
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

    DecisionDTO documentationUnitDTO =
        (DecisionDTO) DecisionTransformer.transformToDTO(currentDto, updatedDomainObject);

    assertThat(documentationUnitDTO.getLeadingDecisionNormReferences()).isEmpty();
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

    DecisionDTO documentationUnitDTO =
        (DecisionDTO) DecisionTransformer.transformToDTO(currentDto, updatedDomainObject);
    assertThat(documentationUnitDTO.getEcli()).isEqualTo("This is a test ecli with spaces");
    assertThat(documentationUnitDTO.getJudicialBody())
        .isEqualTo("This is a test appraisalBody with spaces");
    assertThat(documentationUnitDTO.getLeadingDecisionNormReferences())
        .usingRecursiveFieldByFieldElementComparator()
        .containsExactly(
            LeadingDecisionNormReferenceDTO.builder()
                .normReference("This is a test reference with spaces")
                .rank(1)
                .build());
    assertThat(
            documentationUnitDTO.getDeviatingEclis().stream()
                .map(DeviatingEcliDTO::getValue)
                .toList())
        .isEqualTo(List.of("This is a test deviatingEcli with spaces"));
    assertThat(
            documentationUnitDTO.getDeviatingFileNumbers().stream()
                .map(DeviatingFileNumberDTO::getValue)
                .toList())
        .isEqualTo(List.of("This is a test deviatingFileNumber with spaces"));
    assertThat(
            documentationUnitDTO.getDeviatingCourts().stream()
                .map(DeviatingCourtDTO::getValue)
                .toList())
        .isEqualTo(List.of("This is a test deviatingCourt with spaces"));
    assertThat(documentationUnitDTO.getInputTypes().stream().map(InputTypeDTO::getValue).toList())
        .isEqualTo(List.of("This is a test inputType with spaces"));

    assertThat(
            documentationUnitDTO.getEnsuingDecisions().stream()
                .map(EnsuingDecisionDTO::getFileNumber)
                .toList())
        .isEqualTo(List.of("This is a test fileNumber with spaces"));
    assertThat(
            documentationUnitDTO.getPreviousDecisions().stream()
                .map(PreviousDecisionDTO::getFileNumber)
                .toList())
        .isEqualTo(List.of("This is a test fileNumber with spaces"));
    assertThat(
            documentationUnitDTO.getPreviousDecisions().stream()
                .map(PreviousDecisionDTO::getDeviatingFileNumber)
                .toList())
        .isEqualTo(List.of("This is a test deviatingFileNumber with spaces"));
    assertThat(
            documentationUnitDTO.getActiveCitations().stream()
                .map(ActiveCitationDTO::getFileNumber)
                .toList())
        .isEqualTo(List.of("This is a test fileNumber with spaces"));
    assertThat(
            documentationUnitDTO.getNormReferences().stream()
                .map(NormReferenceDTO::getSingleNorm)
                .toList())
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

    DecisionDTO documentationUnitDTO =
        (DecisionDTO)
            DecisionTransformer.transformToDTO(DecisionDTO.builder().build(), documentationUnit);

    assertThat(documentationUnitDTO.getJobProfiles())
        .extracting("value")
        .containsExactly("job profile");
  }

  @Test
  void testTransformToDTO_withSameDismissalTypes_shouldMakeDismissalTypesDistinct() {
    DocumentationUnit documentationUnit =
        generateSimpleDocumentationUnitBuilder()
            .contentRelatedIndexing(
                ContentRelatedIndexing.builder().dismissalTypes(List.of("type", "type")).build())
            .build();

    DecisionDTO documentationUnitDTO =
        (DecisionDTO)
            DecisionTransformer.transformToDTO(DecisionDTO.builder().build(), documentationUnit);

    assertThat(documentationUnitDTO.getDismissalTypes())
        .extracting("value")
        .containsExactly("type");
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

    DecisionDTO documentationUnitDTO =
        (DecisionDTO)
            DecisionTransformer.transformToDTO(DecisionDTO.builder().build(), documentationUnit);

    assertThat(documentationUnitDTO.getDismissalGrounds())
        .extracting("value")
        .containsExactly("ground");
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

    DecisionDTO documentationUnitDTO =
        (DecisionDTO)
            DecisionTransformer.transformToDTO(DecisionDTO.builder().build(), documentationUnit);

    assertThat(documentationUnitDTO.getCollectiveAgreements())
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
    DecisionDTO documentationUnitDTO =
        (DecisionDTO)
            DecisionTransformer.transformToDTO(DecisionDTO.builder().build(), documentationUnit);

    // Assert
    assertThat(documentationUnitDTO.getParticipatingJudges()).hasSize(1);
    assertThat(documentationUnitDTO.getParticipatingJudges())
        .extracting("name")
        .containsExactly("Judge A");
    assertThat(documentationUnitDTO.getParticipatingJudges().get(0).getReferencedOpinions())
        .isNull();
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

    DecisionDTO documentationUnitDTO =
        DecisionTransformer.transformToDTO(currentDto, updatedDomainObject);

    assertThat(documentationUnitDTO.getCaselawReferences().getFirst().getDocumentationUnitRank())
        .isOne();
    assertThat(documentationUnitDTO.getCaselawReferences().getFirst().getEditionRank())
        .isEqualTo(3);
    assertThat(documentationUnitDTO.getCaselawReferences().getFirst().getEdition().getName())
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

    DecisionDTO documentationUnitDTO =
        DecisionTransformer.transformToDTO(currentDto, updatedDomainObject);

    assertThat(documentationUnitDTO.getLiteratureReferences().getFirst().getDocumentationUnitRank())
        .isOne();
    assertThat(documentationUnitDTO.getLiteratureReferences().getFirst().getEditionRank())
        .isEqualTo(3);
    assertThat(documentationUnitDTO.getLiteratureReferences().getFirst().getEdition().getName())
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

    DecisionDTO documentationUnitDTO =
        DecisionTransformer.transformToDTO(currentDto, updatedDomainObject);
    assertThat(documentationUnitDTO.getSource().getFirst().getRank()).isOne();
    assertThat(documentationUnitDTO.getSource().getFirst().getValue()).isEqualTo(SourceValue.E);
    assertThat(documentationUnitDTO.getSource().getFirst().getSourceRawValue()).isNull();
  }

  @Test
  void testTransformToDTO_withSource_withExistingSource() {
    List<SourceDTO> existingSources =
        List.of(SourceDTO.builder().value(SourceValue.A).rank(1).build()); // Example list

    DecisionDTO currentDto = DecisionDTO.builder().source(existingSources).build();

    DocumentationUnit updatedDomainObject =
        DocumentationUnit.builder()
            .coreData(
                CoreData.builder().source(Source.builder().value(SourceValue.E).build()).build())
            .build();

    DecisionDTO documentationUnitDTO =
        DecisionTransformer.transformToDTO(currentDto, updatedDomainObject);
    assertThat(documentationUnitDTO.getSource().getLast().getRank()).isEqualTo(2);
    assertThat(documentationUnitDTO.getSource().getLast().getValue()).isEqualTo(SourceValue.E);
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

  @Test
  void testTransformToDomain_withDocumentationUnitDTOIsNull_shouldReturnEmptyDocumentationUnit() {

    assertThatThrownBy(() -> DecisionTransformer.transformToDomain(null))
        .isInstanceOf(DocumentationUnitTransformerException.class)
        .hasMessageContaining("Document unit is null and won't transform");
  }

  @Test
  void testTransformToDomain_withLegalEffectYes_shouldSetLegalEffectToYes() {
    DecisionDTO documentationUnitDTO =
        generateSimpleDTOBuilder().legalEffect(LegalEffectDTO.JA).build();
    DocumentationUnit expected =
        generateSimpleDocumentationUnitBuilder()
            .coreData(generateSimpleCoreDataBuilder().legalEffect("Ja").build())
            .build();

    DocumentationUnit documentationUnit =
        DecisionTransformer.transformToDomain(documentationUnitDTO);

    assertThat(documentationUnit).isEqualTo(expected);
  }

  @Test
  void testTransformToDomain_withLegalEffectNo_shouldSetLegalEffectToNo() {
    DecisionDTO documentationUnitDTO =
        generateSimpleDTOBuilder().legalEffect(LegalEffectDTO.NEIN).build();
    DocumentationUnit expected =
        generateSimpleDocumentationUnitBuilder()
            .coreData(generateSimpleCoreDataBuilder().legalEffect("Nein").build())
            .build();

    DocumentationUnit documentationUnit =
        DecisionTransformer.transformToDomain(documentationUnitDTO);

    assertThat(documentationUnit).isEqualTo(expected);
  }

  @Test
  void testTransformToDomain_withLegalEffectNotSpecified_shouldSetLegalEffectToNotSpecified() {
    DecisionDTO documentationUnitDTO =
        generateSimpleDTOBuilder().legalEffect(LegalEffectDTO.KEINE_ANGABE).build();
    DocumentationUnit expected =
        generateSimpleDocumentationUnitBuilder()
            .coreData(generateSimpleCoreDataBuilder().legalEffect("Keine Angabe").build())
            .build();

    DocumentationUnit documentationUnit =
        DecisionTransformer.transformToDomain(documentationUnitDTO);

    assertThat(documentationUnit).isEqualTo(expected);
  }

  @Test
  void testTransformToDomain_withLegalEffectWrongValue_shouldSetLegalEffectToNull() {
    DecisionDTO documentationUnitDTO =
        generateSimpleDTOBuilder().legalEffect(LegalEffectDTO.FALSCHE_ANGABE).build();
    DocumentationUnit expected =
        generateSimpleDocumentationUnitBuilder()
            .coreData(generateSimpleCoreDataBuilder().build())
            .build();

    DocumentationUnit documentationUnit =
        DecisionTransformer.transformToDomain(documentationUnitDTO);

    assertThat(documentationUnit).isEqualTo(expected);
  }

  @Test
  void testTransformToDomain_withEnsuingAndPendingDecisionsWithoutARank_shouldAddItToTheEnd() {
    DecisionDTO documentationUnitDTO =
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

    DocumentationUnit documentationUnit =
        DecisionTransformer.transformToDomain(documentationUnitDTO);

    assertThat(documentationUnit).isEqualTo(expected);
  }

  @Test
  void testTransformToDomain_textWithMultipleBorderNumberElements_shouldAddAllBorderNumbers() {
    DecisionDTO documentationUnitDTO =
        generateSimpleDTOBuilder()
            .grounds(
                "lorem ipsum<border-number><number>1</number><content>foo</content></border-number> dolor sit amet <border-number><number>2</number><content>bar</content></border-number>")
            .build();

    DocumentationUnit documentationUnit =
        DecisionTransformer.transformToDomain(documentationUnitDTO);

    assertThat(documentationUnit.managementData().borderNumbers())
        .hasSize(2)
        .containsExactly("1", "2");
  }

  @Test
  void testTransformScheduledPublicationDate_withDate_shouldAddScheduledPublicationDate() {
    DecisionDTO documentationUnitDTO =
        generateSimpleDTOBuilder()
            .scheduledPublicationDateTime(LocalDateTime.parse("2022-01-23T18:25:14"))
            .build();

    DocumentationUnit documentationUnit =
        DecisionTransformer.transformToDomain(documentationUnitDTO);

    assertThat(documentationUnit.managementData().scheduledPublicationDateTime())
        .isEqualTo("2022-01-23T18:25:14");
  }

  @Test
  void testTransformScheduledPublicationDate_withoutDate_shouldNotAddScheduledPublicationDate() {
    DecisionDTO documentationUnitDTO =
        generateSimpleDTOBuilder().scheduledPublicationDateTime(null).build();

    DocumentationUnit documentationUnit =
        DecisionTransformer.transformToDomain(documentationUnitDTO);

    assertThat(documentationUnit.managementData().scheduledPublicationDateTime()).isNull();
  }

  @Test
  void testTransformLastPublicationDate_withDate_shouldAddLastPublicationDate() {
    DecisionDTO documentationUnitDTO =
        generateSimpleDTOBuilder()
            .lastPublicationDateTime(LocalDateTime.parse("2022-01-23T18:25:14"))
            .build();

    DocumentationUnit documentationUnit =
        DecisionTransformer.transformToDomain(documentationUnitDTO);

    assertThat(documentationUnit.managementData().lastPublicationDateTime())
        .isEqualTo("2022-01-23T18:25:14");
  }

  @Test
  void testTransformLastPublicationDate_withoutDate_shouldNotAddLastPublicationDate() {
    DecisionDTO documentationUnitDTO =
        generateSimpleDTOBuilder().lastPublicationDateTime(null).build();

    DocumentationUnit documentationUnit =
        DecisionTransformer.transformToDomain(documentationUnitDTO);

    assertThat(documentationUnit.managementData().lastPublicationDateTime()).isNull();
  }

  @Test
  void testTransformToDomain_multipleTextsWithBorderNumberElements_shouldAddAllBorderNumbers() {
    DecisionDTO documentationUnitDTO =
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

    DocumentationUnit documentationUnit =
        DecisionTransformer.transformToDomain(documentationUnitDTO);

    assertThat(documentationUnit.managementData().borderNumbers())
        .hasSize(4)
        .containsExactly("1", "2", "3", "4");
  }

  @Test
  void testTransformToDomain_textWithoutBorderNumberElements_shouldNotAddBorderNumbers() {
    DecisionDTO documentationUnitDTO =
        generateSimpleDTOBuilder().grounds("lorem ipsum dolor sit amet").build();

    DocumentationUnit documentationUnit =
        DecisionTransformer.transformToDomain(documentationUnitDTO);

    assertThat(documentationUnit.managementData().borderNumbers()).isEmpty();
  }

  @Test
  void
      testTransformToDomain_textWithMalformedBorderNumberElement_shouldOnlyAddValidBorderNumbers() {
    DecisionDTO documentationUnitDTO =
        generateSimpleDTOBuilder()
            .grounds(
                "lorem ipsum<border-number><content>foo</content></border-number> dolor sit amet <border-number><number>2</number><content>bar</content></border-number>")
            .build();

    DocumentationUnit documentationUnit =
        DecisionTransformer.transformToDomain(documentationUnitDTO);

    assertThat(documentationUnit.managementData().borderNumbers()).hasSize(1).containsExactly("2");
  }

  @Test
  void testTransformToDomain_shouldAddLeadingDecisionNormReferences() {
    DecisionDTO documentationUnitDTO =
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

    DocumentationUnit documentationUnit =
        DecisionTransformer.transformToDomain(documentationUnitDTO);

    assertThat(documentationUnit.coreData().leadingDecisionNormReferences())
        .hasSize(3)
        .containsExactly("BGB §1", "BGB §2", "BGB §3");
  }

  @Test
  void testTransformToDomain_shouldTransformStatus() {
    DecisionDTO documentationUnitDTO =
        generateSimpleDTOBuilder()
            .status(
                StatusDTO.builder()
                    .createdAt(Instant.now())
                    .publicationStatus(PublicationStatus.UNPUBLISHED)
                    .withError(false)
                    .build())
            .build();

    DocumentationUnit documentationUnit =
        DecisionTransformer.transformToDomain(documentationUnitDTO);

    assertThat(documentationUnit.status().publicationStatus())
        .isEqualTo(PublicationStatus.UNPUBLISHED);
    assertThat(documentationUnit.status().withError()).isFalse();
  }

  @Test
  void
      testTransformToDomain_withMultipleNormReferences_withSameNormAbbreviation_shouldGroupNorms() {
    UUID normAbbreviationId = UUID.randomUUID();
    DecisionDTO documentationUnitDTO =
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

    DocumentationUnit documentationUnit =
        DecisionTransformer.transformToDomain(documentationUnitDTO);

    assertThat(documentationUnit.contentRelatedIndexing().norms()).hasSize(1);
    assertThat(documentationUnit.contentRelatedIndexing().norms().get(0).normAbbreviation().id())
        .isEqualTo(normAbbreviationId);
    assertThat(
            documentationUnit
                .contentRelatedIndexing()
                .norms()
                .get(0)
                .singleNorms()
                .get(0)
                .singleNorm())
        .isEqualTo("single norm 1");
    assertThat(
            documentationUnit
                .contentRelatedIndexing()
                .norms()
                .get(0)
                .singleNorms()
                .get(1)
                .singleNorm())
        .isEqualTo("single norm 2");
  }

  @Test
  void
      testTransformToDomain_withMultipleNormReferences_withNoAbbreviation_withSameNAbbreviationRawValue_shouldGroupNorms() {
    DecisionDTO documentationUnitDTO =
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

    DocumentationUnit documentationUnit =
        DecisionTransformer.transformToDomain(documentationUnitDTO);

    assertThat(documentationUnit.contentRelatedIndexing().norms()).hasSize(1);
    assertThat(
            documentationUnit
                .contentRelatedIndexing()
                .norms()
                .getFirst()
                .normAbbreviationRawValue())
        .isEqualTo("foo");
    assertThat(
            documentationUnit
                .contentRelatedIndexing()
                .norms()
                .getFirst()
                .singleNorms()
                .getFirst()
                .singleNorm())
        .isEqualTo("single norm 1");
    assertThat(
            documentationUnit
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
    DecisionDTO documentationUnitDTO =
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

    DocumentationUnit documentationUnit =
        DecisionTransformer.transformToDomain(documentationUnitDTO);

    assertThat(documentationUnit.contentRelatedIndexing().norms()).hasSize(2);
    assertThat(documentationUnit.contentRelatedIndexing().norms().get(0).normAbbreviation().id())
        .isEqualTo(documentationUnitDTO.getNormReferences().get(0).getNormAbbreviation().getId());
    assertThat(documentationUnit.contentRelatedIndexing().norms().get(1).normAbbreviation().id())
        .isEqualTo(documentationUnitDTO.getNormReferences().get(1).getNormAbbreviation().getId());
    assertThat(
            documentationUnit
                .contentRelatedIndexing()
                .norms()
                .get(0)
                .singleNorms()
                .get(0)
                .singleNorm())
        .isEqualTo("single norm 1");
    assertThat(
            documentationUnit
                .contentRelatedIndexing()
                .norms()
                .get(1)
                .singleNorms()
                .get(0)
                .singleNorm())
        .isEqualTo("single norm 2");
  }

  @Test
  void testTransformToDomain_withNote_shouldAddNote() {
    DecisionDTO documentationUnitDTO = generateSimpleDTOBuilder().note("Beispiel Notiz").build();

    DocumentationUnit documentationUnit =
        DecisionTransformer.transformToDomain(documentationUnitDTO);

    assertThat(documentationUnit.note()).isEqualTo("Beispiel Notiz");
  }

  @Test
  void testTransformToDomain_withEmptyNote_shouldAddEmptyNote() {
    DecisionDTO documentationUnitDTO = generateSimpleDTOBuilder().note("").build();

    DocumentationUnit documentationUnit =
        DecisionTransformer.transformToDomain(documentationUnitDTO);

    assertThat(documentationUnit.note()).isEmpty();
  }

  @Test
  void testTransformToDomain_withNullNote_shouldAddNullNote() {
    DecisionDTO documentationUnitDTO = generateSimpleDTOBuilder().note(null).build();

    DocumentationUnit documentationUnit =
        DecisionTransformer.transformToDomain(documentationUnitDTO);

    assertThat(documentationUnit.note()).isNull();
  }

  @Test
  void testTransformToDomain_withoutNote_shouldAddNoNote() {
    DecisionDTO documentationUnitDTO = generateSimpleDTOBuilder().build();

    DocumentationUnit documentationUnit =
        DecisionTransformer.transformToDomain(documentationUnitDTO);

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
    DecisionDTO documentationUnitDTO =
        (DecisionDTO)
            DecisionTransformer.transformToDTO(
                currentDto,
                generateSimpleDocumentationUnitBuilder().coreData(coreDataBuilder.build()).build());

    assertEquals(
        documentationUnitDTO.getYearsOfDispute().stream().map(YearOfDisputeDTO::getRank).toList(),
        List.of(1, 2, 3));
  }

  @Test
  void testTransformToDomain_withJobProfiles_shouldAddJobProfiles() {
    DecisionDTO documentationUnitDTO =
        generateSimpleDTOBuilder()
            .jobProfiles(List.of(JobProfileDTO.builder().value("job profile").build()))
            .build();

    DocumentationUnit documentationUnit =
        DecisionTransformer.transformToDomain(documentationUnitDTO);

    assertThat(documentationUnit.contentRelatedIndexing().jobProfiles())
        .containsExactly("job profile");
  }

  @Test
  void testTransformToDomain_withCollectiveAgreements_shouldAddCollectiveAgreements() {
    DecisionDTO documentationUnitDTO =
        generateSimpleDTOBuilder()
            .collectiveAgreements(
                List.of(CollectiveAgreementDTO.builder().value("agreement").build()))
            .build();

    DocumentationUnit documentationUnit =
        DecisionTransformer.transformToDomain(documentationUnitDTO);

    assertThat(documentationUnit.contentRelatedIndexing().collectiveAgreements())
        .containsExactly("agreement");
  }

  @Test
  void testTransformToDomain_withDismissalTypes_shouldAddDismissalTypes() {
    DecisionDTO documentationUnitDTO =
        generateSimpleDTOBuilder()
            .dismissalTypes(List.of(DismissalTypesDTO.builder().value("type").build()))
            .build();

    DocumentationUnit documentationUnit =
        DecisionTransformer.transformToDomain(documentationUnitDTO);

    assertThat(documentationUnit.contentRelatedIndexing().dismissalTypes()).containsExactly("type");
  }

  @Test
  void testTransformToDomain_withDismissalGrounds_shouldAddDismissalGrounds() {
    DecisionDTO documentationUnitDTO =
        generateSimpleDTOBuilder()
            .dismissalGrounds(List.of(DismissalGroundsDTO.builder().value("ground").build()))
            .build();

    DocumentationUnit documentationUnit =
        DecisionTransformer.transformToDomain(documentationUnitDTO);

    assertThat(documentationUnit.contentRelatedIndexing().dismissalGrounds())
        .containsExactly("ground");
  }

  @Test
  void testTransformToDomain_withLegislativeMandate_shouldLegislativeMandateBeTrue() {
    DecisionDTO documentationUnitDTO =
        generateSimpleDTOBuilder().hasLegislativeMandate(true).build();

    DocumentationUnit documentationUnit =
        DecisionTransformer.transformToDomain(documentationUnitDTO);

    assertThat(documentationUnit.contentRelatedIndexing().hasLegislativeMandate()).isTrue();
  }

  @Test
  void testTransformToDomain_withoutLegislativeMandate_shouldLegislativeMandateBeFalse() {
    DecisionDTO documentationUnitDTO =
        generateSimpleDTOBuilder().hasLegislativeMandate(false).build();

    DocumentationUnit documentationUnit =
        DecisionTransformer.transformToDomain(documentationUnitDTO);

    assertThat(documentationUnit.contentRelatedIndexing().hasLegislativeMandate()).isFalse();
  }

  @Test
  void testTransformToDomain_withParticipatingJudges_shouldAddParticipatingJudges() {
    // Arrange
    ParticipatingJudgeDTO participatingJudgeA =
        ParticipatingJudgeDTO.builder().name("Judge A").referencedOpinions("Opinion A").build();
    ParticipatingJudgeDTO participatingJudgeB =
        ParticipatingJudgeDTO.builder().name("Judge B").referencedOpinions("Opinion B").build();

    DecisionDTO documentationUnitDTO =
        generateSimpleDTOBuilder()
            .participatingJudges(List.of(participatingJudgeA, participatingJudgeB))
            .build();

    // Act
    DocumentationUnit documentationUnit =
        DecisionTransformer.transformToDomain(documentationUnitDTO);

    // Assert
    assertThat(documentationUnit.longTexts().participatingJudges()).hasSize(2);
    assertThat(documentationUnit.longTexts().participatingJudges().get(0).id())
        .isEqualTo(participatingJudgeA.getId());
    assertThat(documentationUnit.longTexts().participatingJudges().get(0).name())
        .isEqualTo(participatingJudgeA.getName());
    assertThat(documentationUnit.longTexts().participatingJudges().get(0).referencedOpinions())
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
        .literatureReferences(Collections.emptyList());
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
  void
      testTransformToDomain_withUnpublishedDuplicateWarningFromOtherDocOffice_Relations1_shouldFilterOutWarning() {
    var original =
        generateSimpleDTOBuilder().documentNumber("original").id(UUID.randomUUID()).build();
    var unpublishedStatus =
        StatusDTO.builder().publicationStatus(PublicationStatus.UNPUBLISHED).build();
    var otherDocOffice = DocumentationOfficeDTO.builder().abbreviation("other office").build();
    var unpublishedDuplicateFromOtherDocOffice =
        generateSimpleDTOBuilder()
            .documentNumber("duplicate")
            .documentationOffice(otherDocOffice)
            .id(UUID.randomUUID())
            .status(unpublishedStatus)
            .build();
    var duplicateRelationship =
        DuplicateRelationDTO.builder()
            .documentationUnit1(original)
            .documentationUnit2(unpublishedDuplicateFromOtherDocOffice)
            .build();
    original = original.toBuilder().duplicateRelations1(Set.of(duplicateRelationship)).build();

    DocumentationUnit documentationUnit = DecisionTransformer.transformToDomain(original);

    assertThat(documentationUnit.managementData().duplicateRelations()).isEmpty();
  }

  @Test
  void
      testTransformToDomain_withUnpublishedDuplicateWarningFromOtherDocOffice_Relations2_shouldFilterOutWarning() {
    var original =
        generateSimpleDTOBuilder().documentNumber("original").id(UUID.randomUUID()).build();
    var unpublishedStatus =
        StatusDTO.builder().publicationStatus(PublicationStatus.UNPUBLISHED).build();
    var otherDocOffice = DocumentationOfficeDTO.builder().abbreviation("other office").build();
    var unpublishedDuplicateFromOtherDocOffice =
        generateSimpleDTOBuilder()
            .documentNumber("duplicate")
            .documentationOffice(otherDocOffice)
            .id(UUID.randomUUID())
            .status(unpublishedStatus)
            .build();
    var duplicateRelationship =
        DuplicateRelationDTO.builder()
            .documentationUnit1(unpublishedDuplicateFromOtherDocOffice)
            .documentationUnit2(original)
            .build();
    original = original.toBuilder().duplicateRelations2(Set.of(duplicateRelationship)).build();

    DocumentationUnit documentationUnit = DecisionTransformer.transformToDomain(original);

    assertThat(documentationUnit.managementData().duplicateRelations()).isEmpty();
  }

  @Test
  void
      testTransformToDomain_withUnpublishedDuplicateWarningFromSameDocOffice_shouldNotFilterOutWarning() {
    var original =
        generateSimpleDTOBuilder().documentNumber("original").id(UUID.randomUUID()).build();
    var unpublishedStatus =
        StatusDTO.builder().publicationStatus(PublicationStatus.UNPUBLISHED).build();
    var unpublishedDuplicateFromOtherDocOffice =
        generateSimpleDTOBuilder()
            .documentNumber("duplicate")
            .id(UUID.randomUUID())
            .status(unpublishedStatus)
            .build();
    var duplicateRelationship =
        DuplicateRelationDTO.builder()
            .documentationUnit1(original)
            .documentationUnit2(unpublishedDuplicateFromOtherDocOffice)
            .build();
    original = original.toBuilder().duplicateRelations1(Set.of(duplicateRelationship)).build();

    DocumentationUnit documentationUnit = DecisionTransformer.transformToDomain(original);

    assertThat(documentationUnit.managementData().duplicateRelations()).hasSize(1);
  }

  @Test
  void
      testTransformToDomain_withPublishedDuplicateWarningFromOtherDocOffice_shouldNotFilterOutWarning() {
    var original =
        generateSimpleDTOBuilder().documentNumber("original").id(UUID.randomUUID()).build();
    var publishedStatus =
        StatusDTO.builder().publicationStatus(PublicationStatus.PUBLISHED).build();
    var otherDocOffice = DocumentationOfficeDTO.builder().abbreviation("other office").build();
    var unpublishedDuplicateFromOtherDocOffice =
        generateSimpleDTOBuilder()
            .documentNumber("duplicate")
            .documentationOffice(otherDocOffice)
            .id(UUID.randomUUID())
            .status(publishedStatus)
            .build();
    var duplicateRelationship =
        DuplicateRelationDTO.builder()
            .documentationUnit1(unpublishedDuplicateFromOtherDocOffice)
            .documentationUnit2(original)
            .build();
    original = original.toBuilder().duplicateRelations2(Set.of(duplicateRelationship)).build();

    DocumentationUnit documentationUnit = DecisionTransformer.transformToDomain(original);

    assertThat(documentationUnit.managementData().duplicateRelations()).hasSize(1);
    assertThat(
            documentationUnit.managementData().duplicateRelations().stream()
                .findFirst()
                .get()
                .documentNumber())
        .isEqualTo("duplicate");
  }

  @Test
  void
      testTransformToDomain_withPublishingDuplicateWarningFromOtherDocOffice_shouldNotFilterOutWarning() {
    var original =
        generateSimpleDTOBuilder().documentNumber("original").id(UUID.randomUUID()).build();
    var publishedStatus =
        StatusDTO.builder().publicationStatus(PublicationStatus.PUBLISHING).build();
    var otherDocOffice = DocumentationOfficeDTO.builder().abbreviation("other office").build();
    var unpublishedDuplicateFromOtherDocOffice =
        generateSimpleDTOBuilder()
            .documentNumber("duplicate")
            .documentationOffice(otherDocOffice)
            .id(UUID.randomUUID())
            .status(publishedStatus)
            .build();
    var duplicateRelationship =
        DuplicateRelationDTO.builder()
            .documentationUnit1(unpublishedDuplicateFromOtherDocOffice)
            .documentationUnit2(original)
            .build();
    original = original.toBuilder().duplicateRelations2(Set.of(duplicateRelationship)).build();

    DocumentationUnit documentationUnit = DecisionTransformer.transformToDomain(original);

    assertThat(documentationUnit.managementData().duplicateRelations()).hasSize(1);
    assertThat(
            documentationUnit.managementData().duplicateRelations().stream()
                .findFirst()
                .get()
                .documentNumber())
        .isEqualTo("duplicate");
  }

  @Test
  void testTransformToDomain_withMultipleDuplicateWarnings_shouldSortByDecisionDateAndDocNumber() {
    var original =
        generateSimpleDTOBuilder().documentNumber("original").id(UUID.randomUUID()).build();
    var duplicate1 =
        generateSimpleDTOBuilder()
            .documentNumber("duplicate1")
            .date(LocalDate.of(2020, 1, 1))
            .id(UUID.randomUUID())
            .build();
    var duplicate2 =
        generateSimpleDTOBuilder().documentNumber("duplicate2").id(UUID.randomUUID()).build();
    var duplicate3 =
        generateSimpleDTOBuilder()
            .documentNumber("duplicate3")
            .date(LocalDate.of(2021, 1, 1))
            .id(UUID.randomUUID())
            .build();
    var duplicate4 =
        generateSimpleDTOBuilder()
            .documentNumber("duplicate4")
            .date(LocalDate.of(2021, 1, 1))
            .id(UUID.randomUUID())
            .build();
    var duplicate5 =
        generateSimpleDTOBuilder().documentNumber("duplicate05").id(UUID.randomUUID()).build();
    var duplicateRelationship1 =
        DuplicateRelationDTO.builder()
            .documentationUnit1(duplicate1)
            .documentationUnit2(original)
            .build();
    var duplicateRelationship2 =
        DuplicateRelationDTO.builder()
            .documentationUnit1(duplicate2)
            .documentationUnit2(original)
            .build();
    var duplicateRelationship3 =
        DuplicateRelationDTO.builder()
            .documentationUnit1(duplicate3)
            .documentationUnit2(original)
            .build();
    var duplicateRelationship4 =
        DuplicateRelationDTO.builder()
            .documentationUnit1(duplicate4)
            .documentationUnit2(original)
            .build();
    var duplicateRelationship5 =
        DuplicateRelationDTO.builder()
            .documentationUnit1(duplicate5)
            .documentationUnit2(original)
            .build();
    original =
        original.toBuilder()
            .duplicateRelations2(
                Set.of(duplicateRelationship1, duplicateRelationship2, duplicateRelationship5))
            .duplicateRelations1(Set.of(duplicateRelationship4, duplicateRelationship3))
            .build();

    DocumentationUnit documentationUnit = DecisionTransformer.transformToDomain(original);

    var transformedRelations = documentationUnit.managementData().duplicateRelations();
    assertThat(transformedRelations).hasSize(5);
    assertThat(transformedRelations.stream().map(DuplicateRelation::documentNumber))
        .containsExactly("duplicate3", "duplicate4", "duplicate1", "duplicate05", "duplicate2");
  }
}
