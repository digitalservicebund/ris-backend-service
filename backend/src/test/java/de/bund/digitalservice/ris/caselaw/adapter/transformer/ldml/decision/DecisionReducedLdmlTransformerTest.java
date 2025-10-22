package de.bund.digitalservice.ris.caselaw.adapter.transformer.ldml.decision;

import de.bund.digitalservice.ris.caselaw.adapter.XmlUtilService;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.CaseLawLdml;
import de.bund.digitalservice.ris.caselaw.adapter.transformer.ldml.TestUtils;
import de.bund.digitalservice.ris.caselaw.domain.ActiveCitation;
import de.bund.digitalservice.ris.caselaw.domain.ContentRelatedIndexing;
import de.bund.digitalservice.ris.caselaw.domain.CoreData;
import de.bund.digitalservice.ris.caselaw.domain.Decision;
import de.bund.digitalservice.ris.caselaw.domain.Definition;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationOffice;
import de.bund.digitalservice.ris.caselaw.domain.EnsuingDecision;
import de.bund.digitalservice.ris.caselaw.domain.ForeignLanguageVersion;
import de.bund.digitalservice.ris.caselaw.domain.LanguageCode;
import de.bund.digitalservice.ris.caselaw.domain.LegalForce;
import de.bund.digitalservice.ris.caselaw.domain.LongTexts;
import de.bund.digitalservice.ris.caselaw.domain.NormReference;
import de.bund.digitalservice.ris.caselaw.domain.PreviousDecision;
import de.bund.digitalservice.ris.caselaw.domain.Procedure;
import de.bund.digitalservice.ris.caselaw.domain.PublicationStatus;
import de.bund.digitalservice.ris.caselaw.domain.Reference;
import de.bund.digitalservice.ris.caselaw.domain.ReferenceType;
import de.bund.digitalservice.ris.caselaw.domain.ShortTexts;
import de.bund.digitalservice.ris.caselaw.domain.SingleNorm;
import de.bund.digitalservice.ris.caselaw.domain.Source;
import de.bund.digitalservice.ris.caselaw.domain.SourceValue;
import de.bund.digitalservice.ris.caselaw.domain.Status;
import de.bund.digitalservice.ris.caselaw.domain.court.Court;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.LegalForceType;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.LegalPeriodical;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.NormAbbreviation;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.ParticipatingJudge;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.citation.CitationType;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.documenttype.DocumentType;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.fieldoflaw.FieldOfLaw;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.Year;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;
import javax.xml.parsers.DocumentBuilderFactory;
import net.sf.saxon.TransformerFactoryImpl;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.junit.jupiter.MockitoExtension;
import org.xmlunit.builder.DiffBuilder;
import org.xmlunit.diff.Diff;

@ExtendWith(MockitoExtension.class)
class DecisionReducedLdmlTransformerTest {

  private static Decision testDocumentUnit;
  static XmlUtilService xmlUtilService = new XmlUtilService(new TransformerFactoryImpl());

  private static DecisionReducedLdmlTransformer subject;
  private static UUID documentationUnitId;

  @BeforeAll
  static void setUpBeforeClass() {
    DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();

    subject = new DecisionReducedLdmlTransformer(documentBuilderFactory);
    documentationUnitId = UUID.randomUUID();
    createTestDocumentationUnit();
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("testCasesForIncludedAttributes")
  void transformIncludedAttributes(String testName, String expected) {
    // Act
    CaseLawLdml ldml = subject.transformToLdml(testDocumentUnit);

    // Assert
    Assertions.assertNotNull(ldml);
    Optional<String> fileContent = xmlUtilService.ldmlToString(ldml);
    Assertions.assertTrue(fileContent.isPresent());
    Assertions.assertTrue(
        StringUtils.deleteWhitespace(fileContent.get())
            .contains(StringUtils.deleteWhitespace(expected)));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("testCasesForExcludedAttributes")
  void transformExcludedAttributes(String testName, String expected) {
    // Act
    CaseLawLdml ldml = subject.transformToLdml(testDocumentUnit);

    // Assert
    Assertions.assertNotNull(ldml);
    Optional<String> fileContent = xmlUtilService.ldmlToString(ldml);
    Assertions.assertTrue(fileContent.isPresent());
    Assertions.assertFalse(
        StringUtils.deleteWhitespace(fileContent.get())
            .contains(StringUtils.deleteWhitespace(expected)));
  }

  @Test
  void testEntireLdml() throws Exception {
    // Arrange
    Path expectedFilePath = Paths.get("src/test/resources/testdata/decision_reduced_ldml.xml");
    String expected = Files.readString(expectedFilePath, StandardCharsets.UTF_8);

    // Act
    CaseLawLdml ldml = subject.transformToLdml(testDocumentUnit);

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
      Assertions.fail("XMLs differ:\n" + differences.toString());
    }
  }

  private static void createTestDocumentationUnit() {
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

    EnsuingDecision ensuingDecision1 =
        EnsuingDecision.builder()
            .decisionDate(LocalDate.of(2022, 10, 1))
            .court(Court.builder().type("ensuing decision court type").build())
            .documentType(DocumentType.builder().label("ensuing decision document type").build())
            .fileNumber("ensuing decision file number")
            .documentNumber("ensuing decision document number 1")
            .pending(true)
            .build();
    EnsuingDecision ensuingDecision2 =
        ensuingDecision1.toBuilder().documentNumber("previous decision document number 2").build();

    testDocumentUnit =
        Decision.builder()
            .uuid(documentationUnitId)
            .status(Status.builder().publicationStatus(PublicationStatus.PUBLISHED).build())
            .note("note test")
            .coreData(
                CoreData.builder()
                    .ecli("ecli test")
                    .celexNumber("celex test")
                    .documentationOffice(
                        DocumentationOffice.builder().abbreviation("documentationOffice").build())
                    .creatingDocOffice(
                        DocumentationOffice.builder()
                            .abbreviation("creatingDocumentationOffice")
                            .build())
                    .court(
                        Court.builder()
                            .label("courtLabel test")
                            .type("courtType")
                            .location("courtLocation")
                            .regions(List.of("NW"))
                            .build())
                    .source(
                        Source.builder()
                            .sourceRawValue("sourceRawValue test")
                            .value(SourceValue.S)
                            .build())
                    .documentType(DocumentType.builder().label("documentType test").build())
                    .legalEffect("ja")
                    .fileNumbers(List.of("fileNumber test"))
                    .decisionDate(LocalDate.of(2020, 1, 1))
                    .appraisalBody("appraisalBody test")
                    .inputTypes(List.of("inputType test"))
                    .procedure(Procedure.builder().label("procedure test").build())
                    .previousProcedures(List.of("previous procedure test"))
                    .documentationOffice(
                        DocumentationOffice.builder().abbreviation("documentationOffice").build())
                    .creatingDocOffice(
                        DocumentationOffice.builder()
                            .abbreviation("creatingDocumentationOffice")
                            .build())
                    .previousProcedures(List.of("previous procedure test"))
                    .procedure(Procedure.builder().label("procedure test").build())
                    .inputTypes(List.of("inputType test"))
                    .deviatingEclis(List.of("deviating ecli test"))
                    .deviatingCourts(List.of("deviating court"))
                    .deviatingFileNumbers(List.of("deviating fileNumber"))
                    .deviatingDecisionDates(List.of(LocalDate.of(2010, 5, 12)))
                    .yearsOfDispute(List.of(Year.now()))
                    .leadingDecisionNormReferences(List.of("leadingDecisionNormReference test"))
                    .build())
            .status(Status.builder().publicationStatus(PublicationStatus.PUBLISHED).build())
            .documentNumber("YYTestDoc0013")
            .longTexts(
                LongTexts.builder()
                    .caseFacts("<p>caseFacts test</p>")
                    .decisionReasons("<p>decisionGrounds test</p>")
                    .reasons("<p>grounds test</p>")
                    .dissentingOpinion("<p>dissenting test</p>")
                    .otherLongText("<p>otherLongText test</p>")
                    .tenor("<p>tenor test</p>")
                    .outline("<p>outline test</p>")
                    .participatingJudges(
                        List.of(
                            ParticipatingJudge.builder()
                                .name("Dr. Phil. Max Mustermann")
                                .referencedOpinions("referenced opinions test 1")
                                .build(),
                            ParticipatingJudge.builder()
                                .name("Richterin Maxima Mustermann")
                                .referencedOpinions("referenced opinions test 2")
                                .build()))
                    .build())
            .shortTexts(
                ShortTexts.builder()
                    .guidingPrinciple("<p>guidingPrinciple test</p>")
                    .headline("<p>headline test</p>")
                    .decisionNames(List.of("decisionNames test"))
                    .headnote("<p>headNote test</p>")
                    .otherHeadnote("<p>otherHeadNote test</p>")
                    .build())
            .contentRelatedIndexing(
                ContentRelatedIndexing.builder()
                    .activeCitations(
                        List.of(
                            ActiveCitation.builder()
                                .citationType(CitationType.builder().label("citation test").build())
                                .build()))
                    .keywords(List.of("keyword test"))
                    .fieldsOfLaw(
                        List.of(FieldOfLaw.builder().identifier("fieldOfLaw test").build()))
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
                    .foreignLanguageVersions(
                        List.of(
                            ForeignLanguageVersion.builder()
                                .id(UUID.randomUUID())
                                .languageCode(
                                    LanguageCode.builder()
                                        .id(UUID.randomUUID())
                                        .label("Englisch")
                                        .isoCode("en")
                                        .isoCode3Letters("eng")
                                        .build())
                                .link("https://ihre-url-zur-englischen-übersetzung")
                                .build(),
                            ForeignLanguageVersion.builder()
                                .id(UUID.randomUUID())
                                .languageCode(
                                    LanguageCode.builder()
                                        .id(UUID.randomUUID())
                                        .label("Französisch")
                                        .isoCode("fr")
                                        .isoCode3Letters("fra")
                                        .build())
                                .link("https://ihre-url-zur-französischen-übersetzung")
                                .build()))
                    .definitions(
                        List.of(
                            Definition.builder()
                                .definedTerm("indirekte Steuern")
                                .definingBorderNumber(2L)
                                .build(),
                            Definition.builder().definedTerm("Sachgesamtheit").build()))
                    .evsf("evsf test")
                    .build())
            .previousDecisions(List.of(previousDecision1, previousDecision2))
            .ensuingDecisions(List.of(ensuingDecision1, ensuingDecision2))
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

  static Stream<Arguments> testCasesForIncludedAttributes() {
    return Stream.of(
        Arguments.of(
            "'documentNumber' (Dokumentnummer)",
            """
               <akn:FRBRthis value="YYTestDoc0013"/>
               """),
        // CoreData/Formaldaten -->
        Arguments.of(
            "'court' (Gericht)",
            """
                <ris:courtLocation>courtLocation</ris:courtLocation>
                <ris:courtType>courtType</ris:courtType>
               """),
        Arguments.of(
            "'decisionDate' (Entscheidungsdatum)",
            """
                <akn:FRBRdate date="2020-01-01" name="Entscheidungsdatum"/>
               """),
        Arguments.of(
            "'documentType' (Dokumenttyp)",
            """
                <ris:documentType akn:eId="dokumenttyp">documentType test</ris:documentType>
               """),
        Arguments.of(
            "'ecli'",
            """
               <akn:FRBRalias name="ECLI" value="ecli test"/>
               """),
        Arguments.of(
            "'fileNumber' (Aktenzeichen)",
            """
              <ris:fileNumbers>
                 <ris:fileNumber>fileNumber test</ris:fileNumber>
              </ris:fileNumbers>
               """),
        Arguments.of(
            "'appraisalBody/judicialBody' (Spruchkörper)",
            """
                <ris:judicialBody>appraisalBody test</ris:judicialBody>
               """),
        // Fixme: should be included -->
        //        Arguments.of(
        //            "'region' (Region)",
        //            """
        //                <ris:region>region test</ris:region>
        //               """),
        // Normen -->
        // Fixme: Add elements for single norm and norm abbreviation once they are also transformed
        Arguments.of(
            "'normReferences' (Normen)",
            """
              <ris:legalForces>
                 <ris:legalForce>legalForce test</ris:legalForce>
              </ris:legalForces>
               """),
        // PreviousDecisions -->
        Arguments.of(
            "'previousDecisions' (Vorgehende Entscheidungen)",
            """
              <ris:previousDecisions>
                  <ris:previousDecision date="2020-01-01">
                     <ris:documentNumber>previous decision document number 1</ris:documentNumber>
                     <ris:fileNumber>previous decision file number</ris:fileNumber>
                     <ris:courtType>previous decision court type</ris:courtType>
                  </ris:previousDecision>
                  <ris:previousDecision date="2020-01-01">
                     <ris:documentNumber>previous decision document number 2</ris:documentNumber>
                     <ris:fileNumber>previous decision file number</ris:fileNumber>
                     <ris:courtType>previous decision court type</ris:courtType>
                  </ris:previousDecision>
               </ris:previousDecisions>
               """),
        // EnsuingDecisions -->
        Arguments.of(
            "'ensuingDecisions' (Nachgehende Entscheidungen)",
            """
              <ris:ensuingDecisions>
                  <ris:ensuingDecision date="2022-10-01">
                     <ris:documentNumber>ensuing decision document number 1</ris:documentNumber>
                     <ris:fileNumber>ensuing decision file number</ris:fileNumber>
                     <ris:courtType>ensuing decision court type</ris:courtType>
                  </ris:ensuingDecision>
                  <ris:ensuingDecision date="2022-10-01">
                     <ris:documentNumber>previous decision document number 2</ris:documentNumber>
                     <ris:fileNumber>ensuing decision file number</ris:fileNumber>
                     <ris:courtType>ensuing decision court type</ris:courtType>
                  </ris:ensuingDecision>
               </ris:ensuingDecisions>
              """),
        // LongTexts/Langtexte -->
        Arguments.of(
            "'dissentingOpinion' (Abweichende Meinung)",
            """
              <akn:motivation ris:domainTerm="Abweichende Meinung">
                  <akn:p>dissenting test</akn:p>
                  <akn:block name="Mitwirkende Richter">
                      <akn:opinion ris:domainTerm="Art der Mitwirkung" type="dissenting" by="#dr-phil-max-mustermann">referenced opinions test 1</akn:opinion>
                      <akn:opinion ris:domainTerm="Art der Mitwirkung" type="dissenting" by="#richterin-maxima-mustermann">referenced opinions test 2</akn:opinion>
                  </akn:block>
              </akn:motivation>
              """),
        Arguments.of(
            "'caseFacts' (Tatbestand)",
            """
                <akn:background ris:domainTerm="Tatbestand">
                  <akn:p>caseFacts test</akn:p>
                </akn:background>
               """),
        Arguments.of(
            "'decisionGrounds/decisionReasons' (Entscheidungsgründe)",
            """
            <akn:motivation ris:domainTerm="Entscheidungsgründe">
                <akn:p>decisionGrounds test</akn:p>
            </akn:motivation>
            """),
        Arguments.of(
            "'grounds/reasons' (Gründe)",
            """
            <akn:motivation ris:domainTerm="Gründe">
                <akn:p>grounds test</akn:p>
            </akn:motivation>
            """),
        Arguments.of(
            "'otherLongText' (Sonstiger Langtext)",
            """
            <akn:motivation ris:domainTerm="Sonstiger Langtext">
                <akn:p>otherLongText test</akn:p>
            </akn:motivation>
            """),
        Arguments.of(
            "'tenor' (Tenor)",
            """
            <akn:decision ris:domainTerm="Tenor">
                <akn:p>tenor test</akn:p>
            </akn:decision>
            """),
        Arguments.of(
            "'outline' (Gliederung)'",
            """
            <akn:introduction ris:domainTerm="Gliederung">
                <akn:p>outline test</akn:p>
            </akn:introduction>
            """),
        Arguments.of(
            "'guidingPrinciple' (Leitsatz)",
            """
            <akn:introduction ris:domainTerm="Leitsatz">
                <akn:p>guidingPrinciple test</akn:p>
            </akn:introduction>
            """),
        Arguments.of(
            "'headline' (Titelzeile)",
            """
                <akn:header>
                   <akn:p>Aktenzeichen: <akn:docNumber refersTo="#aktenzeichen">fileNumber test</akn:docNumber>
                   </akn:p>
                   <akn:p>Entscheidungsdatum: <akn:docDate date="2020-01-01" refersTo="#entscheidungsdatum">01.01.2020</akn:docDate>
                   </akn:p>
                   <akn:p>Gericht: <akn:courtType refersTo="#gericht">courtLabel test</akn:courtType>
                   </akn:p>
                   <akn:p>Dokumenttyp: <akn:docType refersTo="#dokumenttyp">documentType test</akn:docType>
                   </akn:p>
                </akn:header>
               """));
  }

  static Stream<Arguments> testCasesForExcludedAttributes() {
    return Stream.of(
        Arguments.of(
            "'decisionNames' (Entscheidungsnamen)",
            """
            <ris:decisionNames>
               <ris:decisionNames>decisionNames test</ris:decisionNames>
            </ris:decisionNames>
               """),
        Arguments.of(
            "'keywords' (Schlagworte)",
            """
        <akn:classification source="attributsemantik-noch-undefiniert">
            <akn:keyword dictionary="attributsemantik-noch-undefiniert"
                         showAs="attributsemantik-noch-undefiniert"
                         value="keyword test"/>
         </akn:classification>
            """),
        Arguments.of(
            "'headnote' (Orientierungssatz)",
            """
                    <akn:block name="Orientierungssatz">
                       <akn:embeddedStructure>
                          <akn:p>headNote test</akn:p>
                       </akn:embeddedStructure>
                    </akn:block>
                    """),
        Arguments.of(
            "'inputType' (Eingangsart)",
            """
            inputType test
            """),
        Arguments.of(
            "'legalEffect' (Rechtskraft)",
            """
                <ris:legalEffect>ja</ris:legalEffect>
                """),
        Arguments.of(
            "'otherHeadNote' (Sonstiger Orientierungssatz)",
            """
                    <akn:block name="Sonstiger Orientierungssatz">
                       <akn:embeddedStructure>
                          <akn:p>otherHeadNote test</akn:p>
                       </akn:embeddedStructure>
                    </akn:block>
                    """),
        Arguments.of(
            "'procedures' (Vorgänge)",
            """
            <ris:procedures>
              <ris:procedure>previous procedure test</ris:procedure>
           </ris:procedures>
            """),
        Arguments.of(
            "'fieldsOfLaw' (Sachgebiete)",
            """
            <ris:fieldOfLaws/>
            """),
        Arguments.of(
            "'source' (Quelle)",
            """
            sourceRawValue test
            """),
        Arguments.of(
            "'documentationOffice' (Dokumentationsstelle)",
            """
            <ris:documentationOffice>documentationOffice</ris:documentationOffice>
            """),
        Arguments.of(
            "'creatingDocumentationOffice' (erstellende Dokumentationsstelle)",
            """
            creatingDocumentationOffice
            """),
        Arguments.of(
            "'activeCitations' (Aktivzitierung)",
            """
                 citation test
                """),
        Arguments.of(
            "'deviatingCourts' (Abweichende Gerichte)",
            """
              <ris:deviatingCourts>
                <ris:deviatingCourt>deviating court</ris:deviatingCourt>
              </ris:deviatingCourts>
            """),
        Arguments.of(
            "'deviatingDates' (Abweichende Entscheidungsdatum)",
            """
              <ris:deviatingDates>
                  <ris:deviatingDate>2010-05-12</ris:deviatingDate>
               </ris:deviatingDates>
            """),
        Arguments.of(
            "'deviatingEclis' (Abweichende Eclis)",
            """
              <ris:deviatingEclis>
                  <ris:deviatingEcli>deviating ecli test</ris:deviatingEcli>
               </ris:deviatingEclis>
            """),
        Arguments.of(
            "'deviatingFileNumbers' (Abweichende Aktenzeichen)",
            """
              <ris:deviatingFileNumbers>
                  <ris:deviatingFileNumber>deviating fileNumber</ris:deviatingFileNumber>
               </ris:deviatingFileNumbers>
            """),
        Arguments.of(
            "'deviatingDocumentNumbers' (Abweichende Dokumentnummer)",
            """
              <ris:deviatingDocumentNumbers>
                  <ris:deviatingDocumentNumber>deviating document number test</ris:deviatingDocumentNumber>
               </ris:deviatingDocumentNumbers>
            """));
  }
}
