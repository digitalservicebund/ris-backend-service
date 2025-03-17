package de.bund.digitalservice.ris.caselaw.adapter;

import static org.assertj.core.api.Assertions.assertThat;

import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.CaseLawLdml;
import de.bund.digitalservice.ris.caselaw.adapter.transformer.InternalPortalTransformer;
import de.bund.digitalservice.ris.caselaw.domain.ActiveCitation;
import de.bund.digitalservice.ris.caselaw.domain.ContentRelatedIndexing;
import de.bund.digitalservice.ris.caselaw.domain.CoreData;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationOffice;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnit;
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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class InternalPortalTransformerTest {

  private static DocumentationUnit testDocumentUnit;
  static XmlUtilService xmlUtilService = new XmlUtilService(new TransformerFactoryImpl());

  private static InternalPortalTransformer subject;
  private static UUID documentationUnitId;

  @BeforeAll
  static void setUpBeforeClass() {
    DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();

    subject = new InternalPortalTransformer(documentBuilderFactory);

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
        DocumentationUnit.builder()
            .uuid(UUID.randomUUID())
            .coreData(
                CoreData.builder()
                    .ecli("testecli")
                    .court(
                        Court.builder().type("testCourtType").location("testCourtLocation").build())
                    .documentType(
                        DocumentType.builder().label("testDocumentTypeAbbreviation").build())
                    .legalEffect("ja")
                    .fileNumbers(List.of("testFileNumber"))
                    .decisionDate(LocalDate.of(2020, 1, 1))
                    .build())
            .documentNumber("testDocumentNumber")
            .longTexts(LongTexts.builder().caseFacts("<p>Example content 1</p>").build())
            .shortTexts(ShortTexts.builder().build())
            .previousDecisions(List.of(related1, related2))
            .build();
  }

  @Test
  @DisplayName("Fallback title test")
  void documentNumberIsFallbackTitleTest() {
    String expected =
        """
      <akn:header>
         <akn:p>testDocumentNumber</akn:p>
      </akn:header>
     """;
    CaseLawLdml ldml = subject.transformToLdml(testDocumentUnit);
    Assertions.assertNotNull(ldml);
    Optional<String> fileContent = xmlUtilService.ldmlToString(ldml);
    Assertions.assertTrue(fileContent.isPresent());
    Assertions.assertTrue(
        StringUtils.deleteWhitespace(fileContent.get())
            .contains(StringUtils.deleteWhitespace(expected)));
  }

  @Test
  @DisplayName("Dissenting Opinion test")
  void dissentingOpinionTest() {
    String expected =
        """
           <akn:block name="Abweichende Meinung">
              <akn:opinion>
                 <akn:embeddedStructure>
                    <akn:p>dissenting test</akn:p>
                 </akn:embeddedStructure>
              </akn:opinion>
           </akn:block>
           """;
    DocumentationUnit dissentingCaseLaw =
        testDocumentUnit.toBuilder()
            .longTexts(
                testDocumentUnit.longTexts().toBuilder()
                    .dissentingOpinion("<p>dissenting test</p>")
                    .build())
            .build();
    CaseLawLdml ldml = subject.transformToLdml(dissentingCaseLaw);
    Assertions.assertNotNull(ldml);
    Optional<String> fileContent = xmlUtilService.ldmlToString(ldml);
    Assertions.assertTrue(fileContent.isPresent());
    Assertions.assertTrue(
        StringUtils.deleteWhitespace(fileContent.get())
            .contains(StringUtils.deleteWhitespace(expected)));
  }

  @Test
  @DisplayName("Headnote test")
  void headnoteTest() {
    String expected =
        """
            <akn:block name="Orientierungssatz">
               <akn:embeddedStructure>
                  <akn:p>headnote test</akn:p>
               </akn:embeddedStructure>
            </akn:block>
           """;
    DocumentationUnit headnoteCaseLaw =
        testDocumentUnit.toBuilder()
            .shortTexts(
                testDocumentUnit.shortTexts().toBuilder().headnote("<p>headnote test</p>").build())
            .build();
    CaseLawLdml ldml = subject.transformToLdml(headnoteCaseLaw);
    Assertions.assertNotNull(ldml);
    Optional<String> fileContent = xmlUtilService.ldmlToString(ldml);
    Assertions.assertTrue(fileContent.isPresent());
    Assertions.assertTrue(
        StringUtils.deleteWhitespace(fileContent.get())
            .contains(StringUtils.deleteWhitespace(expected)));
  }

  @Test
  @DisplayName("OtherHeadnote test")
  void otherHeadnoteTest() {
    String expected =
        """
            <akn:block name="Sonstiger Orientierungssatz">
               <akn:embeddedStructure>
                  <akn:p>other headnote test</akn:p>
               </akn:embeddedStructure>
            </akn:block>
           """;
    DocumentationUnit otherHeadnoteCaseLaw =
        testDocumentUnit.toBuilder()
            .shortTexts(
                testDocumentUnit.shortTexts().toBuilder()
                    .otherHeadnote("<p>other headnote test</p>")
                    .build())
            .build();
    CaseLawLdml ldml = subject.transformToLdml(otherHeadnoteCaseLaw);
    Assertions.assertNotNull(ldml);
    Optional<String> fileContent = xmlUtilService.ldmlToString(ldml);
    Assertions.assertTrue(fileContent.isPresent());
    Assertions.assertTrue(
        StringUtils.deleteWhitespace(fileContent.get())
            .contains(StringUtils.deleteWhitespace(expected)));
  }

  @Test
  @DisplayName("Grounds test")
  void groundTest() {
    String expected =
        """
            <akn:block name="Gründe">
               <akn:embeddedStructure>
                  <akn:p>grounds test</akn:p>
               </akn:embeddedStructure>
            </akn:block>
           """;
    DocumentationUnit groundsCaseLaw =
        testDocumentUnit.toBuilder()
            .longTexts(
                testDocumentUnit.longTexts().toBuilder().reasons("<p>grounds test</p>").build())
            .build();
    CaseLawLdml ldml = subject.transformToLdml(groundsCaseLaw);
    Assertions.assertNotNull(ldml);
    Optional<String> fileContent = xmlUtilService.ldmlToString(ldml);
    Assertions.assertTrue(fileContent.isPresent());
    Assertions.assertTrue(
        StringUtils.deleteWhitespace(fileContent.get())
            .contains(StringUtils.deleteWhitespace(expected)));
  }

  @Test
  @DisplayName("OtherLongText without main decision test")
  void otherLongTextWithoutMainDecisionTest() {
    String expected =
        """
         <akn:decision>
            <akn:block name="Sonstiger Langtext">
               <akn:embeddedStructure>
                  <akn:p>Other long text test</akn:p>
               </akn:embeddedStructure>
            </akn:block>
         </akn:decision>
         """;
    DocumentationUnit otherLongTextCaseLaw =
        testDocumentUnit.toBuilder()
            .longTexts(
                testDocumentUnit.longTexts().toBuilder()
                    .otherLongText("<p>Other long text test</p>")
                    .build())
            .build();
    CaseLawLdml ldml = subject.transformToLdml(otherLongTextCaseLaw);
    Assertions.assertNotNull(ldml);
    Optional<String> fileContent = xmlUtilService.ldmlToString(ldml);
    Assertions.assertTrue(fileContent.isPresent());
    Assertions.assertTrue(
        StringUtils.deleteWhitespace(fileContent.get())
            .contains(StringUtils.deleteWhitespace(expected)));
  }

  @Test
  @DisplayName("Long text with non breaking spaces")
  void testTransformToLdml_longTextWithNBSP_shouldReplaceItWithUnicode() {
    String expected =
        """
         <akn:decision>
            <akn:block name="Gründe">
               <akn:embeddedStructure>
                  <akn:p>text with non\u00a0breaking\u00a0spaces</akn:p>
               </akn:embeddedStructure>
            </akn:block>
         </akn:decision>
         """;
    DocumentationUnit otherLongTextCaseLaw =
        testDocumentUnit.toBuilder()
            .longTexts(
                LongTexts.builder()
                    .reasons("<p>text with non&nbsp;breaking&nbsp;spaces</p>")
                    .build())
            .build();

    CaseLawLdml ldml = subject.transformToLdml(otherLongTextCaseLaw);
    Assertions.assertNotNull(ldml);
    Optional<String> fileContent = xmlUtilService.ldmlToString(ldml);
    assertThat(fileContent).isPresent();
    assertThat(StringUtils.deleteWhitespace(fileContent.get()))
        .contains(StringUtils.deleteWhitespace(expected));
  }

  @Test
  @DisplayName("Non self closing break inside block element")
  void testTransform_borderNumber() {
    String expected =
        """
                                <akn:hcontainer name="randnummer">
                                   <akn:num>1</akn:num>
                                   <akn:content>
                                     <akn:p>Lorem ipsum</akn:p>
                                   </akn:content>
                                </akn:hcontainer>
                                """;
    DocumentationUnit otherLongTextCaseLaw =
        testDocumentUnit.toBuilder()
            .longTexts(
                LongTexts.builder()
                    .reasons(
                        """
                                    <border-number>
                                        <number>1</number>
                                        <content>
                                            <p>Lorem ipsum</p>
                                        </content>
                                    </border-number>
                                    """)
                    .build())
            .build();

    CaseLawLdml ldml = subject.transformToLdml(otherLongTextCaseLaw);

    Assertions.assertNotNull(ldml);
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
                                  <akn:p alternativeTo="textWrapper">Hello</akn:p>
                                  <akn:p> paragraph</akn:p>
                                  <akn:p alternativeTo="textWrapper"> world!</akn:p>
                                </akn:header>
                                """;
    DocumentationUnit otherLongTextCaseLaw =
        testDocumentUnit.toBuilder()
            .shortTexts(ShortTexts.builder().headline("Hello<p> paragraph</p> world!").build())
            .build();

    CaseLawLdml ldml = subject.transformToLdml(otherLongTextCaseLaw);

    Assertions.assertNotNull(ldml);
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
                                <akn:keyworddictionary="attributsemantik-noch-undefiniert"showAs="attributsemantik-noch-undefiniert"value="keyword1"/>
                                """;
    DocumentationUnit otherLongTextCaseLaw =
        testDocumentUnit.toBuilder()
            .contentRelatedIndexing(
                ContentRelatedIndexing.builder().keywords(List.of("keyword 1")).build())
            .build();

    CaseLawLdml ldml = subject.transformToLdml(otherLongTextCaseLaw);

    Assertions.assertNotNull(ldml);
    Optional<String> fileContent = xmlUtilService.ldmlToString(ldml);
    assertThat(fileContent).isPresent();
    assertThat(StringUtils.deleteWhitespace(fileContent.get()))
        .contains(StringUtils.deleteWhitespace(expected));
  }

  static Stream<Arguments> provideTagFormattingTestCases() {
    return Stream.of(
        // Image with closing tag
        Arguments.of(
            "<p><img alt=\"alt text\" height=\"70\" src=\"path/to/image\" /></p>",
            "<akn:p><akn:imgalt=\"alttext\"height=\"70\"src=\"path/to/image\"/></akn:p>"),
        // Image without closing tag
        Arguments.of(
            "<p><img alt=\"alt text\" height=\"70\" src=\"path/to/image\"></p>",
            "<akn:p><akn:img alt=\"alt text\" height=\"70\" src=\"path/to/image\"/></akn:p>"),
        // Self closing break not inside block element
        Arguments.of("<br />", "<akn:p><akn:br/></akn:p>"),
        // Self closing break inside block element
        Arguments.of("<p><br /></p>", "<akn:p><akn:br/></akn:p>"),
        // Non self closing break not inside block element
        Arguments.of("<br>", "<akn:p><akn:br/></akn:p>"),
        // Non self closing break inside block element
        Arguments.of("<p><br></p>", "<akn:p><akn:br/></akn:p>"));
  }

  @ParameterizedTest
  @MethodSource("provideTagFormattingTestCases")
  @DisplayName("Transform self closing HTML tags to LDML")
  void testTransformTags(String inputHtml, String expectedFragment) {
    DocumentationUnit otherLongTextCaseLaw =
        testDocumentUnit.toBuilder()
            .longTexts(LongTexts.builder().reasons(inputHtml).build())
            .build();

    CaseLawLdml ldml = subject.transformToLdml(otherLongTextCaseLaw);

    Assertions.assertNotNull(ldml);
    Optional<String> fileContent = xmlUtilService.ldmlToString(ldml);
    assertThat(fileContent).isPresent();
    assertThat(StringUtils.deleteWhitespace(fileContent.get()))
        .contains(StringUtils.deleteWhitespace(expectedFragment));
  }

  @Test
  void testEntireLdml() {
    var documentationUnit = getEntireDocumentationUnit();
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
         <akn:classification source="attributsemantik-noch-undefiniert">
            <akn:keyword dictionary="attributsemantik-noch-undefiniert"
                         showAs="attributsemantik-noch-undefiniert"
                         value="keyword test"/>
         </akn:classification>
         <akn:proprietary source="attributsemantik-noch-undefiniert">
            <ris:meta>
               <ris:decisionNames>
                  <ris:decisionName>decisionName test</ris:decisionName>
               </ris:decisionNames>
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
               <ris:legalEffect>ja</ris:legalEffect>
               <ris:fieldOfLaws/>
               <ris:judicialBody>appraisalBody test</ris:judicialBody>
               <ris:deviatingCourts>
                  <ris:deviatingCourt>deviating court</ris:deviatingCourt>
               </ris:deviatingCourts>
               <ris:deviatingDates>
                  <ris:deviatingDate>2010-05-12</ris:deviatingDate>
               </ris:deviatingDates>
               <ris:deviatingEclis>
                  <ris:deviatingEcli>deviating ecli test</ris:deviatingEcli>
               </ris:deviatingEclis>
               <ris:deviatingFileNumbers>
                  <ris:deviatingFileNumber>deviating fileNumber</ris:deviatingFileNumber>
               </ris:deviatingFileNumbers>
               <ris:publicationStatus>PUBLISHED</ris:publicationStatus>
               <ris:error>false</ris:error>
               <ris:documentationOffice>documentationOffice test</ris:documentationOffice>
               <ris:procedures>
                  <ris:procedure>previous procedure test</ris:procedure>
               </ris:procedures>
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
            <akn:block name="Orientierungssatz">
               <akn:embeddedStructure>
                  <akn:p>headNote test</akn:p>
               </akn:embeddedStructure>
            </akn:block>
            <akn:block name="Sonstiger Orientierungssatz">
               <akn:embeddedStructure>
                  <akn:p>otherHeadNote test</akn:p>
               </akn:embeddedStructure>
            </akn:block>
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
    CaseLawLdml ldml = subject.transformToLdml(documentationUnit);

    // Assert
    Assertions.assertNotNull(ldml);
    Optional<String> fileContent = xmlUtilService.ldmlToString(ldml);
    Assertions.assertTrue(fileContent.isPresent());
    Assertions.assertEquals(expected, fileContent.get());
  }

  DocumentationUnit getEntireDocumentationUnit() {
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
            .build();
    EnsuingDecision ensuingDecision2 =
        ensuingDecision1.toBuilder().documentNumber("previous decision document number 2").build();

    return DocumentationUnit.builder()
        .uuid(documentationUnitId)
        .status(Status.builder().publicationStatus(PublicationStatus.PUBLISHED).build())
        .note("note test")
        .coreData(
            CoreData.builder()
                .ecli("ecli test")
                .documentationOffice(
                    DocumentationOffice.builder().abbreviation("documentationOffice test").build())
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
                    DocumentationOffice.builder().abbreviation("documentationOffice test").build())
                .creatingDocOffice(
                    DocumentationOffice.builder().abbreviation("documentationOffice test").build())
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
                .fieldsOfLaw(List.of(FieldOfLaw.builder().identifier("fieldOfLaw test").build()))
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
}
