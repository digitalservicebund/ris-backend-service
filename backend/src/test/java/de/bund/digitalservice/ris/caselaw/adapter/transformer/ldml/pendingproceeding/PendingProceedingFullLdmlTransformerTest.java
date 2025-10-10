package de.bund.digitalservice.ris.caselaw.adapter.transformer.ldml.pendingproceeding;

import static org.assertj.core.api.Assertions.assertThat;

import de.bund.digitalservice.ris.caselaw.adapter.XmlUtilService;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.CaseLawLdml;
import de.bund.digitalservice.ris.caselaw.adapter.transformer.ldml.TestUtils;
import de.bund.digitalservice.ris.caselaw.domain.ActiveCitation;
import de.bund.digitalservice.ris.caselaw.domain.ContentRelatedIndexing;
import de.bund.digitalservice.ris.caselaw.domain.CoreData;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationOffice;
import de.bund.digitalservice.ris.caselaw.domain.LegalForce;
import de.bund.digitalservice.ris.caselaw.domain.NormReference;
import de.bund.digitalservice.ris.caselaw.domain.PendingProceeding;
import de.bund.digitalservice.ris.caselaw.domain.PendingProceedingShortTexts;
import de.bund.digitalservice.ris.caselaw.domain.PreviousDecision;
import de.bund.digitalservice.ris.caselaw.domain.PublicationStatus;
import de.bund.digitalservice.ris.caselaw.domain.Reference;
import de.bund.digitalservice.ris.caselaw.domain.ReferenceType;
import de.bund.digitalservice.ris.caselaw.domain.SingleNorm;
import de.bund.digitalservice.ris.caselaw.domain.Status;
import de.bund.digitalservice.ris.caselaw.domain.court.Court;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.LegalForceType;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.LegalPeriodical;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.NormAbbreviation;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.citation.CitationType;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.documenttype.DocumentType;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.fieldoflaw.FieldOfLaw;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import javax.xml.parsers.DocumentBuilderFactory;
import net.sf.saxon.TransformerFactoryImpl;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.xmlunit.builder.DiffBuilder;
import org.xmlunit.diff.Diff;

@ExtendWith(MockitoExtension.class)
class PendingProceedingFullLdmlTransformerTest {

  private static PendingProceeding testDocumentUnit;
  static XmlUtilService xmlUtilService = new XmlUtilService(new TransformerFactoryImpl());

  private static PendingProceedingFullLdmlTransformer transformer;
  private static UUID documentationUnitId;

  @BeforeAll
  static void setUpBeforeClass() {
    DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();

    transformer = new PendingProceedingFullLdmlTransformer(documentBuilderFactory);

    documentationUnitId = UUID.randomUUID();

    PreviousDecision related1 =
        PreviousDecision.builder()
            .decisionDate(LocalDate.of(2020, 1, 1))
            .court(Court.builder().type("Test court type").build())
            .documentType(DocumentType.builder().label("Test decision type").build())
            .fileNumber("Test file number")
            .documentNumber("Test document number 1")
            .build();
    PreviousDecision related2 =
        related1.toBuilder().documentNumber("Test document number 2").build();

    testDocumentUnit =
        PendingProceeding.builder()
            .uuid(UUID.randomUUID())
            .coreData(
                CoreData.builder()
                    .court(
                        Court.builder()
                            .type("courtType")
                            .location("courtLocation")
                            .label("courtType courtLocation")
                            .build())
                    .documentType(DocumentType.builder().label("Anhängiges Verfahren").build())
                    .fileNumbers(List.of("Aktenzeichen"))
                    .decisionDate(LocalDate.of(2020, 1, 1))
                    .documentationOffice(DocumentationOffice.builder().abbreviation("BFH").build())
                    .isResolved(true)
                    .resolutionDate(LocalDate.of(2021, 10, 15))
                    .build())
            .documentNumber("Dokumentnummer")
            .shortTexts(
                PendingProceedingShortTexts.builder()
                    .legalIssue("Rechtsfrage")
                    .appellant("Rechtsmittelführer")
                    .admissionOfAppeal("Rechtsmittelzulassung")
                    .resolutionNote("Erledigungsmitteilung")
                    .build())
            .previousDecisions(List.of(related1, related2))
            .build();
  }

  @Test
  @DisplayName("Fulfilled pending proceeding")
  void shouldBeFulfilled() {
    String expected =
        """
     <akn:decision ris:domainTerm="Erledigungsvermerk">
        <akn:p alternativeTo="textWrapper">Erledigungsmitteilung</akn:p>
     </akn:decision>
     """;
    CaseLawLdml ldml = transformer.transformToLdml(testDocumentUnit);
    assertThat(ldml).isNotNull();
    Optional<String> fileContent = xmlUtilService.ldmlToString(ldml);
    assertThat(fileContent).isPresent();
    assertThat(StringUtils.deleteWhitespace(fileContent.get()))
        .contains(StringUtils.deleteWhitespace(expected));
  }

  @Test
  @DisplayName("Mixed text in header")
  void testTransform_mixedTextInHeader() {
    String expected =
        """
          <akn:header>
            <akn:p>Aktenzeichen: <akn:docNumber refersTo="#aktenzeichen">Aktenzeichen</akn:docNumber></akn:p>
            <akn:p>Entscheidungsdatum: <akn:docDate date="2020-01-01"refersTo="#entscheidungsdatum">01.01.2020</akn:docDate></akn:p>
            <akn:p>Gericht:<akn:courtType refersTo="#gericht">courtType courtLocation</akn:courtType></akn:p>
            <akn:p>Dokumenttyp:<akn:doc Type refersTo="#dokumenttyp">Anhängiges Verfahren</akn:docType></akn:p>
            <akn:p>Titelzeile:<akn:shortTitle refersTo="#titelzeile">
              <akn:embeddedStructure>
                <akn:p alternativeTo="textWrapper">Hello</akn:p><akn:p>paragraph</akn:p><akn:p alternativeTo="textWrapper">world!</akn:p>
              </akn:embeddedStructure>
            </akn:shortTitle>
            </akn:p>
          </akn:header>
        """;
    PendingProceeding otherLongTextCaseLaw =
        testDocumentUnit.toBuilder()
            .shortTexts(
                PendingProceedingShortTexts.builder()
                    .headline("Hello<p> paragraph</p> world!")
                    .legalIssue("legal issue")
                    .build())
            .build();

    CaseLawLdml ldml = transformer.transformToLdml(otherLongTextCaseLaw);

    assertThat(ldml).isNotNull();
    Optional<String> fileContent = xmlUtilService.ldmlToString(ldml);
    assertThat(fileContent).isPresent();
    assertThat(StringUtils.deleteWhitespace(fileContent.get()))
        .contains(StringUtils.deleteWhitespace(expected));
  }

  @Test
  @DisplayName("Keywords")
  void testTransform_keywords() {
    String expected =
        """
       <akn:keyword ris:domainTerm="Schlagwort"
           dictionary=""
           showAs="keyword 1"
           value="keyword 1"/>
       <akn:keyword ris:domainTerm="Schlagwort"
           dictionary=""
           showAs="Keyword 2"
           value="Keyword 2"/>
       """;
    PendingProceeding otherLongTextCaseLaw =
        testDocumentUnit.toBuilder()
            .contentRelatedIndexing(
                ContentRelatedIndexing.builder()
                    .keywords(List.of("keyword 1", "Keyword 2"))
                    .build())
            .build();

    CaseLawLdml ldml = transformer.transformToLdml(otherLongTextCaseLaw);

    assertThat(ldml).isNotNull();
    Optional<String> fileContent = xmlUtilService.ldmlToString(ldml);
    assertThat(fileContent).isPresent();
    assertThat(StringUtils.deleteWhitespace(fileContent.get()))
        .contains(StringUtils.deleteWhitespace(expected));
  }

  @Test
  @DisplayName("Entire LDML of unresolved pending proceeding")
  void testEntireLdml() throws IOException {
    // Arrange
    var documentationUnit = getEntireDocumentationUnit();
    Path expectedFilePath =
        Paths.get("src/test/resources/testdata/pending_proceeding_full_ldml.xml");
    String expected = Files.readString(expectedFilePath, StandardCharsets.UTF_8);

    // Act
    CaseLawLdml ldml = transformer.transformToLdml(documentationUnit);

    // Assert
    Assertions.assertNotNull(ldml);
    Optional<String> fileContent = xmlUtilService.ldmlToString(ldml);
    Assertions.assertTrue(fileContent.isPresent());

    Diff diff =
        DiffBuilder.compare(expected)
            .withTest(fileContent.get())
            .withDifferenceEvaluator(TestUtils.ignoreAttributeEvaluator)
            .ignoreWhitespace()
            .checkForIdentical()
            .build();

    if (diff.hasDifferences()) {
      StringBuilder differences = new StringBuilder();
      diff.getDifferences().forEach(d -> differences.append(d.toString()).append("\n"));
      Assertions.fail("XMLs differ:\n" + differences);
    }
  }

  PendingProceeding getEntireDocumentationUnit() {
    PreviousDecision previousDecision1 =
        PreviousDecision.builder()
            .decisionDate(LocalDate.of(2020, 1, 1))
            .court(Court.builder().type("previous decision court type").build())
            .documentType(DocumentType.builder().label("previous decision document type").build())
            .fileNumber("previous decision file number")
            .documentNumber("previous decision document number 1")
            .build();
    PreviousDecision previousDecision2 =
        previousDecision1.toBuilder().documentNumber("previous decision document number 2").build();

    return PendingProceeding.builder()
        .uuid(documentationUnitId)
        .status(Status.builder().publicationStatus(PublicationStatus.PUBLISHED).build())
        .coreData(
            CoreData.builder()
                .court(
                    Court.builder()
                        .type("courtType")
                        .location("courtLocation")
                        .label("courtType courtLocation")
                        .regions(List.of("NW"))
                        .build())
                .documentType(DocumentType.builder().label("Anhängiges Verfahren").build())
                .fileNumbers(List.of("Aktenzeichen"))
                .decisionDate(LocalDate.of(2020, 1, 1))
                .deviatingCourts(List.of("Abweichendes Gericht"))
                .deviatingFileNumbers(List.of("Abweichendes Aktenzeichen"))
                .deviatingDecisionDates(List.of(LocalDate.of(2010, 5, 12)))
                .deviatingDocumentNumbers(List.of("Abweichende Dokumentnummer"))
                .documentationOffice(DocumentationOffice.builder().abbreviation("BFH").build())
                .isResolved(false)
                .resolutionDate(null)
                .build())
        .status(Status.builder().publicationStatus(PublicationStatus.PUBLISHED).build())
        .documentNumber("YYTestDoc0013")
        .shortTexts(
            PendingProceedingShortTexts.builder()
                .headline("Titelzeile")
                .legalIssue("Rechtsfrage")
                .appellant("Rechtsmittelführer")
                .admissionOfAppeal("Rechtsmittelzulassung")
                .resolutionNote("<p>Erledigungsvermerk</p>")
                .build())
        .contentRelatedIndexing(
            ContentRelatedIndexing.builder()
                .activeCitations(
                    List.of(
                        ActiveCitation.builder()
                            .citationType(CitationType.builder().label("citation test").build())
                            .build()))
                .keywords(List.of("keyword test"))
                .fieldsOfLaw(List.of(FieldOfLaw.builder().text("Sachgebiet").build()))
                .norms(
                    List.of(
                        NormReference.builder()
                            .singleNorms(
                                List.of(
                                    SingleNorm.builder()
                                        .singleNorm("singleNorm test")
                                        .legalForce(
                                            LegalForce.builder()
                                                .type(
                                                    LegalForceType.builder()
                                                        .label("legalForce test")
                                                        .build())
                                                .build())
                                        .build()))
                            .normAbbreviation(
                                NormAbbreviation.builder()
                                    .abbreviation("normReference test")
                                    .build())
                            .build()))
                .jobProfiles(List.of("jobProfile test"))
                .dismissalGrounds(List.of("dismissalGround test"))
                .dismissalTypes(List.of("dismissalType test"))
                .collectiveAgreements(List.of("collectiveAgreement test"))
                .hasLegislativeMandate(true)
                .build())
        .previousDecisions(List.of(previousDecision1, previousDecision2))
        .caselawReferences(
            List.of(
                Reference.builder()
                    .referenceType(ReferenceType.CASELAW)
                    .citation("citation")
                    .referenceSupplement("reference supplement")
                    .legalPeriodical(
                        LegalPeriodical.builder().abbreviation("LegalPeriodical").build())
                    .legalPeriodicalRawValue("LegalPeriodical")
                    .build()))
        .literatureReferences(
            List.of(
                Reference.builder()
                    .referenceType(ReferenceType.LITERATURE)
                    .author("author")
                    .citation("citation")
                    .documentType(DocumentType.builder().label("doc type").build())
                    .build()))
        .build();
  }
}
