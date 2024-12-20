package de.bund.digitalservice.ris.caselaw.adapter.transformer;

import static de.bund.digitalservice.ris.caselaw.EntityBuilderTestUtil.createTestDocumentType;
import static de.bund.digitalservice.ris.caselaw.EntityBuilderTestUtil.createTestDocumentTypeDTO;
import static de.bund.digitalservice.ris.caselaw.EntityBuilderTestUtil.createTestDocumentationUnitDTO;
import static de.bund.digitalservice.ris.caselaw.EntityBuilderTestUtil.createTestLegalPeriodical;
import static de.bund.digitalservice.ris.caselaw.EntityBuilderTestUtil.createTestLegalPeriodicalDTO;
import static de.bund.digitalservice.ris.caselaw.EntityBuilderTestUtil.createTestRelatedDocument;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DependentLiteratureCitationDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.LegalPeriodicalDTO;
import de.bund.digitalservice.ris.caselaw.domain.DependentLiteratureCitationType;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnit;
import de.bund.digitalservice.ris.caselaw.domain.Reference;
import de.bund.digitalservice.ris.caselaw.domain.ReferenceType;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.LegalPeriodical;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class DependentLiteratureTransformerTest {

  private static Stream<Arguments> provideLiteratureCitationsTestData_toDomain() {
    return Stream.of(
        Arguments.of(
            // all fields set
            DependentLiteratureCitationDTO.builder()
                .rank(1)
                .citation("2024, 123")
                .author("author")
                .documentType(createTestDocumentTypeDTO())
                .documentTypeRawValue("Auf")
                .documentationUnit(createTestDocumentationUnitDTO())
                .legalPeriodical(createTestLegalPeriodicalDTO())
                .build(),
            Reference.builder()
                .rank(1)
                .citation("2024, 123")
                .referenceType(ReferenceType.LITERATURE)
                .documentType(createTestDocumentType())
                .author("author")
                .documentationUnit(createTestRelatedDocument())
                .legalPeriodical(createTestLegalPeriodical())
                .legalPeriodicalRawValue("LPA")
                .primaryReference(true)
                .build()),
        // without legal periodical, with editionRank
        Arguments.of(
            DependentLiteratureCitationDTO.builder()
                .editionRank(3)
                .rank(1)
                .citation("2024, 123")
                .author("Einstein, Albert")
                .legalPeriodicalRawValue("LPA")
                .documentationUnit(createTestDocumentationUnitDTO())
                .build(),
            Reference.builder()
                .rank(3)
                .citation("2024, 123")
                .author("Einstein, Albert")
                .referenceType(ReferenceType.LITERATURE)
                .legalPeriodicalRawValue("LPA")
                .documentationUnit(createTestRelatedDocument())
                .build()),
        // without any legal periodical
        Arguments.of(
            DependentLiteratureCitationDTO.builder()
                .rank(1)
                .citation("2024, 123")
                .author("Curie, Marie")
                .documentationUnit(createTestDocumentationUnitDTO())
                .build(),
            Reference.builder()
                .rank(1)
                .citation("2024, 123")
                .author("Curie, Marie")
                .referenceType(ReferenceType.LITERATURE)
                .documentationUnit(createTestRelatedDocument())
                .build()));
  }

  @ParameterizedTest
  @MethodSource("provideLiteratureCitationsTestData_toDomain")
  void testTransformToDomain_shouldTransformLiteratureCitation(
      DependentLiteratureCitationDTO referenceDTO, Reference expectedReference) {
    assertThat(DependentLiteratureTransformer.transformToDomain(referenceDTO))
        .isEqualTo(expectedReference);
  }

  private static Stream<Arguments> provideLiteratureCitationsTestData_toDTO() {
    var legalPeriodicalId = UUID.randomUUID();
    var referenceId = UUID.randomUUID();
    return Stream.of(
        // all fields set
        Arguments.of(
            Reference.builder()
                .id(referenceId)
                .legalPeriodical(
                    LegalPeriodical.builder()
                        .uuid(legalPeriodicalId)
                        .title("Aa Bb Cc")
                        .abbreviation("ABC")
                        .subtitle("a test reference")
                        .primaryReference(false)
                        .build())
                .documentationUnit(createTestRelatedDocument())
                .citation("2024, S.5")
                .author("Chomsky, Noam")
                .documentType(createTestDocumentType())
                .referenceType(ReferenceType.LITERATURE)
                .build(),
            DependentLiteratureCitationDTO.builder()
                .id(referenceId)
                .rank(1)
                .citation("2024, S.5")
                .author("Chomsky, Noam")
                .documentTypeRawValue("Auf")
                .documentType(createTestDocumentTypeDTO())
                .type(DependentLiteratureCitationType.PASSIVE)
                .legalPeriodical(
                    LegalPeriodicalDTO.builder()
                        .id(legalPeriodicalId)
                        .title("Aa Bb Cc")
                        .abbreviation("ABC")
                        .subtitle("a test reference")
                        .primaryReference(false)
                        .build())
                .legalPeriodicalRawValue("ABC")
                .build()),
        // possible with no legalPeriodical object
        Arguments.of(
            Reference.builder()
                .citation("2024, S.5")
                .author("Luhmann, Niklas")
                .legalPeriodicalRawValue("ABC")
                .primaryReference(true)
                .documentType(createTestDocumentType())
                .referenceType(ReferenceType.LITERATURE)
                .build(),
            DependentLiteratureCitationDTO.builder()
                .rank(1)
                .citation("2024, S.5")
                .legalPeriodicalRawValue("ABC")
                .documentTypeRawValue("Auf")
                .documentType(createTestDocumentTypeDTO())
                .author("Luhmann, Niklas")
                .type(DependentLiteratureCitationType.PASSIVE)
                .build()),
        // possible with no legalPeriodical raw values, should default to nichtamtlich
        Arguments.of(
            Reference.builder()
                .citation("2024, S.5")
                .documentType(createTestDocumentType())
                .referenceType(ReferenceType.LITERATURE)
                .build(),
            DependentLiteratureCitationDTO.builder()
                .rank(1)
                .citation("2024, S.5")
                .documentTypeRawValue("Auf")
                .documentType(createTestDocumentTypeDTO())
                .type(DependentLiteratureCitationType.PASSIVE)
                .build()));
  }

  @ParameterizedTest
  @MethodSource("provideLiteratureCitationsTestData_toDTO")
  void testTransformToDTO_shouldAddCitations(
      Reference reference, DependentLiteratureCitationDTO expected) {

    // we use the documentation unit transformer here because it adds a rank and sets the
    // documentation unit
    List<DependentLiteratureCitationDTO> referenceDTOS =
        DocumentationUnitTransformer.transformToDTO(
                createTestDocumentationUnitDTO(),
                DocumentationUnit.builder().literatureReferences(List.of(reference)).build())
            .getDependentLiteratureCitations();

    assertEquals(1, referenceDTOS.size());

    assertThat(referenceDTOS.getFirst())
        .usingRecursiveComparison()
        .ignoringFields("documentationUnit")
        .isEqualTo(expected);

    assertNotNull(referenceDTOS.get(0).getDocumentationUnit());
  }
}
