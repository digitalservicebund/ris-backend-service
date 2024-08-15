package de.bund.digitalservice.ris.caselaw.adapter.transformer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentationUnitDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.LegalPeriodicalDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.ReferenceDTO;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnit;
import de.bund.digitalservice.ris.caselaw.domain.Reference;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.LegalPeriodical;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class ReferencesTransformerTest {

  private static Stream<Arguments> provideReferencesTestData_toDomain() {
    return Stream.of(
        Arguments.of(
            // all fields set
            ReferenceDTO.builder()
                .rank(1)
                .citation("2024, 123")
                .footnote("footnote")
                .referenceSupplement("Klammerzusatz")
                .legalPeriodical(
                    LegalPeriodicalDTO.builder()
                        .id(UUID.fromString("33333333-2222-3333-4444-555555555555"))
                        .primaryReference(true)
                        .title("Legal Periodical Title")
                        .subtitle("Legal Periodical Subtitle")
                        .abbreviation("LPA")
                        .build())
                .build(),
            Reference.builder()
                .citation("2024, 123")
                .footnote("footnote")
                .referenceSupplement("Klammerzusatz")
                .legalPeriodical(
                    LegalPeriodical.builder()
                        .legalPeriodicalId(UUID.fromString("33333333-2222-3333-4444-555555555555"))
                        .legalPeriodicalTitle("Legal Periodical Title")
                        .legalPeriodicalSubtitle("Legal Periodical Subtitle")
                        .legalPeriodicalAbbreviation("LPA")
                        .primaryReference(true)
                        .build())
                .build()),
        // without legal periodical
        Arguments.of(
            ReferenceDTO.builder()
                .rank(1)
                .citation("2024, 123")
                .footnote("footnote")
                .referenceSupplement("Klammerzusatz")
                .legalPeriodicalRawValue("LPA")
                .build(),
            Reference.builder()
                .citation("2024, 123")
                .footnote("footnote")
                .referenceSupplement("Klammerzusatz")
                .legalPeriodicalRawValue("LPA")
                .build()));
  }

  @ParameterizedTest
  @MethodSource("provideReferencesTestData_toDomain")
  void testTransformToDomain_shouldTransformReferences(
      ReferenceDTO referenceDTO, Reference expectedReference) {
    assertThat(ReferenceTransformer.transformToDomain(referenceDTO)).isEqualTo(expectedReference);
  }

  private static Stream<Arguments> provideReferencesTestData_toDTO() {
    var legalPeriodicalId = UUID.randomUUID();
    var referenceId = UUID.randomUUID();
    return Stream.of(
        // all fields set
        Arguments.of(
            Reference.builder()
                .uuid(referenceId)
                .legalPeriodical(
                    LegalPeriodical.builder()
                        .legalPeriodicalTitle("Aa Bb Cc")
                        .legalPeriodicalAbbreviation("ABC")
                        .legalPeriodicalSubtitle("a test reference")
                        .legalPeriodicalId(legalPeriodicalId)
                        .primaryReference(false)
                        .build())
                .citation("2024, S.5")
                .footnote("a footnote")
                .referenceSupplement("Klammerzusatz")
                .build(),
            ReferenceDTO.builder()
                .id(referenceId)
                .rank(1)
                .citation("2024, S.5")
                .footnote("a footnote")
                .referenceSupplement("Klammerzusatz")
                .legalPeriodical(
                    LegalPeriodicalDTO.builder()
                        .id(legalPeriodicalId)
                        .title("Aa Bb Cc")
                        .abbreviation("ABC")
                        .subtitle("a test reference")
                        .primaryReference(false)
                        .build())
                .build()),
        // with primary flag, the type is amtlich
        Arguments.of(
            Reference.builder()
                .legalPeriodical(
                    LegalPeriodical.builder()
                        .legalPeriodicalId(legalPeriodicalId)
                        .legalPeriodicalAbbreviation("ABC")
                        .primaryReference(true)
                        .build())
                .citation("2024, S.5")
                .build(),
            ReferenceDTO.builder()
                .rank(1)
                .citation("2024, S.5")
                .legalPeriodical(
                    LegalPeriodicalDTO.builder()
                        .id(legalPeriodicalId)
                        .abbreviation("ABC")
                        .primaryReference(true)
                        .build())
                .build()),
        // with primary=false flag, the type is nichtamtlich
        Arguments.of(
            Reference.builder()
                .legalPeriodical(
                    LegalPeriodical.builder()
                        .legalPeriodicalId(legalPeriodicalId)
                        .legalPeriodicalAbbreviation("ABC")
                        .primaryReference(false)
                        .build())
                .citation("2024, S.5")
                .build(),
            ReferenceDTO.builder()
                .rank(1)
                .citation("2024, S.5")
                .legalPeriodical(
                    LegalPeriodicalDTO.builder()
                        .id(legalPeriodicalId)
                        .abbreviation("ABC")
                        .primaryReference(false)
                        .build())
                .build()),
        // possible with no legalPeriodical
        Arguments.of(
            Reference.builder().citation("2024, S.5").legalPeriodicalRawValue("ABC").build(),
            ReferenceDTO.builder()
                .rank(1)
                .citation("2024, S.5")
                .legalPeriodicalRawValue("ABC")
                .build()));
  }

  @ParameterizedTest
  @MethodSource("provideReferencesTestData_toDTO")
  void testTransformToDTO_shouldAddReferences(Reference reference, ReferenceDTO expected) {

    // we use the documentation unit transformer here because it adds a rank and sets the
    // documentation unit
    List<ReferenceDTO> referenceDTOS =
        DocumentationUnitTransformer.transformToDTO(
                DocumentationUnitDTO.builder().build(),
                DocumentationUnit.builder().references(List.of(reference)).build())
            .getReferences();

    assertEquals(1, referenceDTOS.size());

    assertThat(referenceDTOS.get(0))
        .usingRecursiveComparison()
        .ignoringFields("documentationUnit")
        .isEqualTo(expected);

    assertNotNull(referenceDTOS.get(0).getDocumentationUnit());
  }
}
