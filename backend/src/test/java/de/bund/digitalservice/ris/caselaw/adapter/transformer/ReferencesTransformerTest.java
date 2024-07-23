package de.bund.digitalservice.ris.caselaw.adapter.transformer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentationUnitDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.LegalPeriodicalDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.ReferenceDTO;
import de.bund.digitalservice.ris.caselaw.domain.DocumentUnit;
import de.bund.digitalservice.ris.caselaw.domain.Reference;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class ReferencesTransformerTest {

  private static Stream<Arguments> provideReferencesForTransformation() {
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
                .primaryReference(true)
                .referenceSupplement("Klammerzusatz")
                .legalPeriodicalId(UUID.fromString("33333333-2222-3333-4444-555555555555"))
                .legalPeriodicalTitle("Legal Periodical Title")
                .legalPeriodicalSubtitle("Legal Periodical Subtitle")
                .legalPeriodicalAbbreviation("LPA")
                .build()),
        // without legal periodical reference but with amtlich type, the type is primary
        Arguments.of(
            ReferenceDTO.builder().rank(1).citation("2024, 123").type("amtlich").build(),
            Reference.builder().citation("2024, 123").primaryReference(true).build()),
        // with unknown type, the default is non-primary
        Arguments.of(
            ReferenceDTO.builder().rank(1).citation("2024, 123").type("other").build(),
            Reference.builder().citation("2024, 123").primaryReference(false).build()),
        // without legalPeriodical id and type, the default is non-primary
        Arguments.of(
            ReferenceDTO.builder().rank(1).citation("2024, 123").build(),
            Reference.builder().citation("2024, 123").primaryReference(false).build()));
  }

  @ParameterizedTest
  @MethodSource("provideReferencesForTransformation")
  void testTransformToDomain_shouldTransformReferences(
      ReferenceDTO referenceDTO, Reference expectedReference) {
    assertThat(ReferenceTransformer.transformToDomain(referenceDTO)).isEqualTo(expectedReference);
  }

  private static Stream<Arguments> provideReferencesTestData() {
    var legalPeriodicalId = UUID.randomUUID();
    var referenceId = UUID.randomUUID();
    return Stream.of(
        // all fields set
        Arguments.of(
            Reference.builder()
                .id(referenceId)
                .legalPeriodicalTitle("Aa Bb Cc")
                .legalPeriodicalAbbreviation("ABC")
                .legalPeriodicalSubtitle("a test reference")
                .legalPeriodicalId(legalPeriodicalId)
                .citation("2024, S.5")
                .footnote("a footnote")
                .referenceSupplement("Klammerzusatz")
                .primaryReference(false)
                .build(),
            ReferenceDTO.builder()
                .id(referenceId)
                .rank(1)
                .type("nichtamtlich")
                .citation("2024, S.5")
                .footnote("a footnote")
                .referenceSupplement("Klammerzusatz")
                .legalPeriodical(LegalPeriodicalDTO.builder().id(legalPeriodicalId).build())
                .legalPeriodicalRawValue("ABC")
                .build()),
        // with primary flag, the type is amtlich
        Arguments.of(
            Reference.builder()
                .legalPeriodicalId(legalPeriodicalId)
                .legalPeriodicalAbbreviation("ABC")
                .citation("2024, S.5")
                .primaryReference(true)
                .build(),
            ReferenceDTO.builder()
                .rank(1)
                .citation("2024, S.5")
                .type("amtlich")
                .legalPeriodical(LegalPeriodicalDTO.builder().id(legalPeriodicalId).build())
                .legalPeriodicalRawValue("ABC")
                .build()),
        // with primary=false flag, the type is nichtamtlich
        Arguments.of(
            Reference.builder()
                .legalPeriodicalId(legalPeriodicalId)
                .legalPeriodicalAbbreviation("ABC")
                .citation("2024, S.5")
                .primaryReference(false)
                .build(),
            ReferenceDTO.builder()
                .rank(1)
                .citation("2024, S.5")
                .type("nichtamtlich")
                .legalPeriodical(LegalPeriodicalDTO.builder().id(legalPeriodicalId).build())
                .legalPeriodicalRawValue("ABC")
                .build()),
        // accept entries without legalPeriodical id. without primary flag, the type is nichtamtlich
        Arguments.of(
            Reference.builder().legalPeriodicalAbbreviation("ABC").citation("2024, S.5").build(),
            ReferenceDTO.builder()
                .rank(1)
                .citation("2024, S.5")
                .type("nichtamtlich")
                .legalPeriodicalRawValue("ABC")
                .build()));
  }

  @ParameterizedTest
  @MethodSource("provideReferencesTestData")
  void testTransformToDTO_shouldAddReferences(Reference reference, ReferenceDTO expected) {

    // we use the documentation unit transformer here because it adds a rank and sets the
    // documentation unit
    List<ReferenceDTO> referenceDTOS =
        DocumentationUnitTransformer.transformToDTO(
                DocumentationUnitDTO.builder().build(),
                DocumentUnit.builder().references(List.of(reference)).build())
            .getReferences();

    assertEquals(1, referenceDTOS.size());

    assertThat(referenceDTOS.get(0))
        .usingRecursiveComparison()
        .ignoringFields("documentationUnit")
        .isEqualTo(expected);

    assertNotNull(referenceDTOS.get(0).getDocumentationUnit());
  }
}
