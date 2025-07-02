package de.bund.digitalservice.ris.caselaw.adapter.transformer;

import de.bund.digitalservice.ris.caselaw.adapter.XmlUtilService;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.CaseLawLdml;
import de.bund.digitalservice.ris.caselaw.adapter.transformer.ldml.decision.DecisionReducedLdmlTransformer;
import de.bund.digitalservice.ris.caselaw.domain.ActiveCitation;
import de.bund.digitalservice.ris.caselaw.domain.ContentRelatedIndexing;
import de.bund.digitalservice.ris.caselaw.domain.CoreData;
import de.bund.digitalservice.ris.caselaw.domain.Decision;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationOffice;
import de.bund.digitalservice.ris.caselaw.domain.EnsuingDecision;
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
  void testEntireLdml() {
    // Arrange
    var expected =
        String.format(
            """
                <?xml version="1.0" encoding="utf-8"?>
                <akn:akomaNtoso xmlns:akn="http://docs.oasis-open.org/legaldocml/ns/akn/3.0/WD17"
                                xmlns:ris="http://example.com/0.1/"
                                xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                                xsi:schemaLocation="http://docs.oasis-open.org/legaldocml/ns/akn/3.0/WD17 https://docs.oasis-open.org/legaldocml/akn-core/v1.0/csprd02/part2-specs/schemas/akomantoso30.xsd">
                   <akn:judgment name="attributsemantik-noch-undefiniert">
                      <akn:meta>
                         <akn:identification source="attributsemantik-noch-undefiniert">
                            <akn:FRBRWork>
                               <akn:FRBRthis value="YYTestDoc0013"/>
                               <akn:FRBRuri value="YYTestDoc0013"/>
                               <akn:FRBRalias name="uebergreifende-id" value="%s"/>
                               <akn:FRBRalias name="ecli" value="ecli test"/>
                               <akn:FRBRdate date="2020-01-01" name="entscheidungsdatum"/>
                               <akn:FRBRauthor href="attributsemantik-noch-undefiniert"/>
                               <akn:FRBRcountry value="de"/>
                            </akn:FRBRWork>
                            <akn:FRBRExpression>
                               <akn:FRBRthis value="YYTestDoc0013/dokument"/>
                               <akn:FRBRuri value="YYTestDoc0013/dokument"/>
                               <akn:FRBRdate date="2020-01-01" name="entscheidungsdatum"/>
                               <akn:FRBRauthor href="attributsemantik-noch-undefiniert"/>
                               <akn:FRBRlanguage language="deu"/>
                            </akn:FRBRExpression>
                            <akn:FRBRManifestation>
                               <akn:FRBRthis value="YYTestDoc0013/dokument.xml"/>
                               <akn:FRBRuri value="YYTestDoc0013/dokument.xml"/>
                               <akn:FRBRdate date="2020-01-01" name="entscheidungsdatum"/>
                               <akn:FRBRauthor href="attributsemantik-noch-undefiniert"/>
                            </akn:FRBRManifestation>
                         </akn:identification>
                         <akn:proprietary source="attributsemantik-noch-undefiniert">
                            <ris:meta>
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
                               <ris:fileNumbers>
                                  <ris:fileNumber>fileNumber test</ris:fileNumber>
                               </ris:fileNumbers>
                               <ris:documentType>documentType test</ris:documentType>
                               <ris:courtLocation>courtLocation test</ris:courtLocation>
                               <ris:courtType>courtType test</ris:courtType>
                               <ris:legalForces>
                                  <ris:legalForce>legalForce test</ris:legalForce>
                               </ris:legalForces>
                               <ris:judicialBody>appraisalBody test</ris:judicialBody>
                            </ris:meta>
                         </akn:proprietary>
                      </akn:meta>
                      <akn:header>
                         <akn:p>headline test</akn:p>
                      </akn:header>
                      <akn:judgmentBody>
                         <akn:motivation>
                            <akn:p>guidingPrinciple test</akn:p>
                         </akn:motivation>
                         <akn:introduction>
                            <akn:block name="Gliederung">
                               <akn:embeddedStructure>
                                  <akn:p>outline test</akn:p>
                               </akn:embeddedStructure>
                            </akn:block>
                            <akn:block name="Tenor">
                               <akn:embeddedStructure>
                                  <akn:p>tenor test</akn:p>
                               </akn:embeddedStructure>
                            </akn:block>
                         </akn:introduction>
                         <akn:background>
                            <akn:p>caseFacts test</akn:p>
                         </akn:background>
                         <akn:decision>
                            <akn:block name="Entscheidungsgründe">
                               <akn:embeddedStructure>
                                  <akn:p>decisionGrounds test</akn:p>
                               </akn:embeddedStructure>
                            </akn:block>
                            <akn:block name="Gründe">
                               <akn:embeddedStructure>
                                  <akn:p>grounds test</akn:p>
                               </akn:embeddedStructure>
                            </akn:block>
                            <akn:block name="Sonstiger Langtext">
                               <akn:embeddedStructure>
                                  <akn:p>otherLongText test</akn:p>
                               </akn:embeddedStructure>
                            </akn:block>
                            <akn:block name="Abweichende Meinung">
                               <akn:opinion>
                                  <akn:embeddedStructure>
                                     <akn:p>dissenting test</akn:p>
                                  </akn:embeddedStructure>
                               </akn:opinion>
                            </akn:block>
                         </akn:decision>
                      </akn:judgmentBody>
                   </akn:judgment>
                </akn:akomaNtoso>
                """,
            documentationUnitId);

    // Act
    CaseLawLdml ldml = subject.transformToLdml(testDocumentUnit);

    // Assert
    Assertions.assertNotNull(ldml);
    Optional<String> fileContent = xmlUtilService.ldmlToString(ldml);
    Assertions.assertTrue(fileContent.isPresent());
    Assertions.assertEquals(expected, fileContent.get());
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
                    .documentationOffice(
                        DocumentationOffice.builder()
                            .abbreviation("documentationOffice test")
                            .build())
                    .creatingDocOffice(
                        DocumentationOffice.builder()
                            .abbreviation("creatingDocumentationOffice test")
                            .build())
                    .court(
                        Court.builder()
                            .label("courtLabel test")
                            .type("courtType test")
                            .location("courtLocation test")
                            .region("region test")
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
                        DocumentationOffice.builder()
                            .abbreviation("documentationOffice test")
                            .build())
                    .creatingDocOffice(
                        DocumentationOffice.builder()
                            .abbreviation("documentationOffice test")
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
                                .name("participating judge test")
                                .referencedOpinions("referenced opinions test")
                                .build()))
                    .build())
            .shortTexts(
                ShortTexts.builder()
                    .guidingPrinciple("<p>guidingPrinciple test</p>")
                    .headline("<p>headline test</p>")
                    .decisionName("decisionName test")
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
        // CoreData/Stammdaten -->
        Arguments.of(
            "'court' (Gericht)",
            """
                <ris:courtLocation>courtLocation test</ris:courtLocation>
                <ris:courtType>courtType test</ris:courtType>
               """),
        Arguments.of(
            "'decisionDate' (Entscheidungsdatum)",
            """
                <akn:FRBRdate date="2020-01-01" name="entscheidungsdatum"/>
               """),
        Arguments.of(
            "'documentType' (Dokumenttyp)",
            """
                <ris:documentType>documentType test</ris:documentType>
               """),
        Arguments.of(
            "'ecli'",
            """
               <akn:FRBRalias name="ecli" value="ecli test"/>
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
                   <akn:block name="Abweichende Meinung">
                      <akn:opinion>
                         <akn:embeddedStructure>
                            <akn:p>dissenting test</akn:p>
                         </akn:embeddedStructure>
                      </akn:opinion>
                   </akn:block>
                   """),
        Arguments.of(
            "'caseFacts' (Tatbestand)",
            """
                <akn:background>
                  <akn:p>caseFacts test</akn:p>
                </akn:background>
               """),
        Arguments.of(
            "'decisionGrounds/decisionReasons' (Entscheidungsgründe)",
            """
              <akn:block name="Entscheidungsgründe">
                 <akn:embeddedStructure>
                    <akn:p>decisionGrounds test</akn:p>
                 </akn:embeddedStructure>
              </akn:block>
               """),
        Arguments.of(
            "'grounds/reasons' (Gründe)",
            """
              <akn:block name="Gründe">
                 <akn:embeddedStructure>
                    <akn:p>grounds test</akn:p>
                 </akn:embeddedStructure>
              </akn:block>
               """),
        Arguments.of(
            "'otherLongText' (Sonstiger Langtext)",
            """
               <akn:block name="Sonstiger Langtext">
                 <akn:embeddedStructure>
                    <akn:p>otherLongText test</akn:p>
                 </akn:embeddedStructure>
               </akn:block>
               """),
        Arguments.of(
            "'tenor' (Tenor)",
            """
               <akn:block name="Tenor">
                   <akn:embeddedStructure>
                      <akn:p>tenor test</akn:p>
                   </akn:embeddedStructure>
                </akn:block>
               """),
        Arguments.of(
            "'outline' (Gliederung)'",
            """
                <akn:block name="Gliederung">
                    <akn:embeddedStructure>
                        <akn:p>outline test</akn:p>
                    </akn:embeddedStructure>
                </akn:block>
                """),
        // Fixme: should be included -->
        //        Arguments.of(
        //            "'participatingJudges' (Mitwirkende Richter)",
        //            """
        //               <akn:introduction>
        //                  <akn:block name="Mitwirkende Richter">
        //                     <akn:embeddedStructure>
        //                        <akn:p>participating judge test</akn:p>
        //                     </akn:embeddedStructure>
        //                  </akn:block>
        //               </akn:introduction>
        //               """),
        // ShortTexts/Kurztexte -->
        Arguments.of(
            "'guidingPrinciple' (Leitsatz)",
            """
               <akn:motivation>
                 <akn:p>guidingPrinciple test</akn:p>
               </akn:motivation>
               """),
        Arguments.of(
            "'headline' (Titelzeile)",
            """
                <akn:header>
                   <akn:p>headline test</akn:p>
                </akn:header>
               """));
  }

  static Stream<Arguments> testCasesForExcludedAttributes() {
    return Stream.of(
        Arguments.of(
            "'decisionNames' (Entscheidungsnamen)",
            """
            <ris:decisionNames>
               <ris:decisionName>decisionName test</ris:decisionName>
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
            <ris:documentationOffice>documentationOffice test</ris:documentationOffice>
            """),
        Arguments.of(
            "'creatingDocumentationOffice' (erstellende Dokumentationsstelle)",
            """
            documentationOffice test
            """),
        Arguments.of(
            "'status' (Veröffentlichungsstatus)",
            """
                 <ris:publicationStatus>PUBLISHED</ris:publicationStatus>
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
            """));
  }
}
