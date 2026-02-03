package de.bund.digitalservice.ris.caselaw.adapter.transformer;

import static de.bund.digitalservice.ris.caselaw.EntityBuilderTestUtil.createTestDocumentationUnitDTO;
import static de.bund.digitalservice.ris.caselaw.EntityBuilderTestUtil.createTestLegalPeriodical;
import static de.bund.digitalservice.ris.caselaw.EntityBuilderTestUtil.createTestLegalPeriodicalDTO;
import static de.bund.digitalservice.ris.caselaw.EntityBuilderTestUtil.createTestRelatedDocument;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DecisionDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.LegalPeriodicalDTO;
import de.bund.digitalservice.ris.caselaw.domain.Reference;
import de.bund.digitalservice.ris.caselaw.domain.ReferenceType;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.LegalPeriodical;
import java.util.UUID;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class ReferenceTransformerTest {

  private static Stream<Arguments> provideReferencesTestData_toDomain() {
    return Stream.of(
        Arguments.of(
            // all fields set
            CaselawReferenceDTO.builder()
                .documentationUnitRank(1)
                .citation("2024, 123")
                .footnote("footnote")
                .referenceSupplement("Klammerzusatz")
                .documentationUnit(createTestDocumentationUnitDTO())
                .legalPeriodical(createTestLegalPeriodicalDTO())
                .build(),
            Reference.builder()
                .citation("2024, 123")
                .footnote("footnote")
                .referenceSupplement("Klammerzusatz")
                .referenceType(ReferenceType.CASELAW)
                .documentationUnit(createTestRelatedDocument())
                .legalPeriodical(createTestLegalPeriodical())
                .legalPeriodicalRawValue("LPA")
                .primaryReference(true)
                .build()),
        // without legal periodical, with editionRank
        Arguments.of(
            CaselawReferenceDTO.builder()
                .editionRank(3)
                .documentationUnitRank(1)
                .citation("2024, 123")
                .footnote("footnote")
                .referenceSupplement("Klammerzusatz")
                .legalPeriodicalRawValue("LPA")
                .type("amtlich")
                .documentationUnit(createTestDocumentationUnitDTO())
                .build(),
            Reference.builder()
                .citation("2024, 123")
                .footnote("footnote")
                .referenceSupplement("Klammerzusatz")
                .referenceType(ReferenceType.CASELAW)
                .legalPeriodicalRawValue("LPA")
                .primaryReference(true)
                .documentationUnit(createTestRelatedDocument())
                .build()));
  }

  @ParameterizedTest
  @MethodSource("provideReferencesTestData_toDomain")
  void testTransformToDomain_shouldTransformReferences(
      CaselawReferenceDTO caselawReferenceDTO, Reference expectedReference) {
    assertThat(ReferenceTransformer.transformToDomain(caselawReferenceDTO))
        .isEqualTo(expectedReference);
  }

  @Test
  void testTransformToDomainWithoutType_shouldThrowException() {
    assertThrows(
        IllegalArgumentException.class,
        () ->
            ReferenceTransformer.transformToDomain(
                CaselawReferenceDTO.builder()
                    .documentationUnitRank(1)
                    .citation("2024, 123")
                    .referenceSupplement("Klammerzusatz")
                    .documentationUnit(createTestDocumentationUnitDTO())
                    .build()));
  }

  private static Stream<Arguments> provideReferencesTestData_toDTO() {
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
                .footnote("a footnote")
                .referenceSupplement("Klammerzusatz")
                .referenceType(ReferenceType.CASELAW)
                .build(),
            CaselawReferenceDTO.builder()
                .id(referenceId)
                .citation("2024, S.5")
                .footnote("a footnote")
                .referenceSupplement("Klammerzusatz")
                .documentationUnit(
                    DecisionDTO.builder()
                        .id(UUID.fromString("e8c6f756-d6b2-4fa4-b751-e88c7c53bde4"))
                        .build())
                .legalPeriodical(
                    LegalPeriodicalDTO.builder()
                        .id(legalPeriodicalId)
                        .title("Aa Bb Cc")
                        .abbreviation("ABC")
                        .subtitle("a test reference")
                        .primaryReference(false)
                        .build())
                .legalPeriodicalRawValue("ABC")
                .type("nichtamtlich")
                .build()),
        // with primary flag, the type is amtlich
        Arguments.of(
            Reference.builder()
                .legalPeriodical(
                    LegalPeriodical.builder()
                        .uuid(legalPeriodicalId)
                        .abbreviation("ABC")
                        .primaryReference(true)
                        .build())
                .citation("2024, S.5")
                .referenceType(ReferenceType.CASELAW)
                .build(),
            CaselawReferenceDTO.builder()
                .citation("2024, S.5")
                .legalPeriodical(
                    LegalPeriodicalDTO.builder()
                        .id(legalPeriodicalId)
                        .abbreviation("ABC")
                        .primaryReference(true)
                        .build())
                .legalPeriodicalRawValue("ABC")
                .type("amtlich")
                .build()),
        // with primary=false flag, the type is nichtamtlich
        Arguments.of(
            Reference.builder()
                .legalPeriodical(
                    LegalPeriodical.builder()
                        .uuid(legalPeriodicalId)
                        .abbreviation("ABC")
                        .primaryReference(false)
                        .build())
                .citation("2024, S.5")
                .referenceType(ReferenceType.CASELAW)
                .build(),
            CaselawReferenceDTO.builder()
                .citation("2024, S.5")
                .legalPeriodical(
                    LegalPeriodicalDTO.builder()
                        .id(legalPeriodicalId)
                        .abbreviation("ABC")
                        .primaryReference(false)
                        .build())
                .legalPeriodicalRawValue("ABC")
                .type("nichtamtlich")
                .build()),
        // possible with no legalPeriodical object
        Arguments.of(
            Reference.builder()
                .citation("2024, S.5")
                .legalPeriodicalRawValue("ABC")
                .primaryReference(true)
                .referenceType(ReferenceType.CASELAW)
                .build(),
            CaselawReferenceDTO.builder()
                .citation("2024, S.5")
                .legalPeriodicalRawValue("ABC")
                .type("amtlich")
                .build()));
  }

  @ParameterizedTest
  @MethodSource("provideReferencesTestData_toDTO")
  void testTransformToDTO_shouldAddReferences(Reference reference, CaselawReferenceDTO expected) {

    var referenceDTO = ReferenceTransformer.transformToDTO(reference);
    assertThat(referenceDTO).usingRecursiveComparison().isEqualTo(expected);
  }

  @Test
  void testTransformToDTOWithoutLegalPeriodicalType_shouldThrowException() {
    assertThrows(
        IllegalArgumentException.class,
        () ->
            ReferenceTransformer.transformToDTO(
                Reference.builder()
                    .citation("2024, S.5")
                    .referenceType(ReferenceType.CASELAW)
                    .build()));
  }
}
