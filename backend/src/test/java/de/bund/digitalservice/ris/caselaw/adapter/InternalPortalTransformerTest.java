package de.bund.digitalservice.ris.caselaw.adapter;

import static org.assertj.core.api.Assertions.assertThat;

import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.CaseLawLdml;
import de.bund.digitalservice.ris.caselaw.adapter.transformer.InternalPortalTransformer;
import de.bund.digitalservice.ris.caselaw.domain.CoreData;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnit;
import de.bund.digitalservice.ris.caselaw.domain.LongTexts;
import de.bund.digitalservice.ris.caselaw.domain.PreviousDecision;
import de.bund.digitalservice.ris.caselaw.domain.ShortTexts;
import de.bund.digitalservice.ris.caselaw.domain.court.Court;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.documenttype.DocumentType;
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

@ExtendWith(MockitoExtension.class)
class InternalPortalTransformerTest {

  private static DocumentationUnit testDocumentUnit;
  static XmlUtilService xmlUtilService = new XmlUtilService(new TransformerFactoryImpl());

  private static InternalPortalTransformer subject;

  @BeforeAll
  static void setUpBeforeClass() {
    DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();

    subject = new InternalPortalTransformer(documentBuilderFactory);

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
    Optional<CaseLawLdml> ldml = subject.transformToLdml(testDocumentUnit);
    Assertions.assertTrue(ldml.isPresent());
    Optional<String> fileContent = xmlUtilService.ldmlToString(ldml.get());
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
    Optional<CaseLawLdml> ldml = subject.transformToLdml(dissentingCaseLaw);
    Assertions.assertTrue(ldml.isPresent());
    Optional<String> fileContent = xmlUtilService.ldmlToString(ldml.get());
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
    Optional<CaseLawLdml> ldml = subject.transformToLdml(headnoteCaseLaw);
    Assertions.assertTrue(ldml.isPresent());
    Optional<String> fileContent = xmlUtilService.ldmlToString(ldml.get());
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
    Optional<CaseLawLdml> ldml = subject.transformToLdml(otherHeadnoteCaseLaw);
    Assertions.assertTrue(ldml.isPresent());
    Optional<String> fileContent = xmlUtilService.ldmlToString(ldml.get());
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
    Optional<CaseLawLdml> ldml = subject.transformToLdml(groundsCaseLaw);
    Assertions.assertTrue(ldml.isPresent());
    Optional<String> fileContent = xmlUtilService.ldmlToString(ldml.get());
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
    Optional<CaseLawLdml> ldml = subject.transformToLdml(otherLongTextCaseLaw);
    Assertions.assertTrue(ldml.isPresent());
    Optional<String> fileContent = xmlUtilService.ldmlToString(ldml.get());
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

    Optional<CaseLawLdml> ldml = subject.transformToLdml(otherLongTextCaseLaw);

    assertThat(ldml).isPresent();
    Optional<String> fileContent = xmlUtilService.ldmlToString(ldml.get());
    assertThat(fileContent).isPresent();
    assertThat(StringUtils.deleteWhitespace(fileContent.get()))
        .contains(StringUtils.deleteWhitespace(expected));
  }
}
