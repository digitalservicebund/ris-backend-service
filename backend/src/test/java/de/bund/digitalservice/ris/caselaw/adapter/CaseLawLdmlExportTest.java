package de.bund.digitalservice.ris.caselaw.adapter;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.CaseLawLdml;
import de.bund.digitalservice.ris.caselaw.adapter.transformer.DocumentationUnitToLdmlTransformer;
import de.bund.digitalservice.ris.caselaw.domain.CoreData;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnit;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnitRepository;
import de.bund.digitalservice.ris.caselaw.domain.LongTexts;
import de.bund.digitalservice.ris.caselaw.domain.PreviousDecision;
import de.bund.digitalservice.ris.caselaw.domain.ShortTexts;
import de.bund.digitalservice.ris.caselaw.domain.court.Court;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.documenttype.DocumentType;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CaseLawLdmlExportTest {

  static DocumentationUnitRepository documentationUnitRepository;
  static LdmlBucket caseLawBucket;
  static CaseLawPostgresToS3Exporter exporter;
  static DocumentationUnit testDocumentUnit;
  static UUID testUUID;

  @BeforeAll
  static void setUpBeforeClass() {
    documentationUnitRepository = mock(DocumentationUnitRepository.class);
    caseLawBucket = mock(LdmlBucket.class);
    exporter = new CaseLawPostgresToS3Exporter(documentationUnitRepository, caseLawBucket);

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

    testUUID = UUID.randomUUID();
    testDocumentUnit =
        DocumentationUnit.builder()
            .uuid(testUUID)
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

  @BeforeEach
  void mockReset() {
    Mockito.reset(caseLawBucket);
  }

  @Test
  @DisplayName("Should call caselaw bucket save once")
  void exportOneCaseLaw() {
    when(documentationUnitRepository.getRandomDocumentationUnits())
        .thenReturn(List.of(testDocumentUnit));

    exporter.uploadCaseLaw();
    verify(caseLawBucket, times(1)).save(anyString(), anyString());
  }

  @Test
  @DisplayName("Invalid Case Law Ldml should fail validation 1")
  void xsdValidationFailure1() {
    DocumentationUnit invalidTestDocumentUnit =
        testDocumentUnit.toBuilder()
            .longTexts(
                testDocumentUnit.longTexts().toBuilder()
                    .caseFacts("<p>Example <p>nested</p> content 1</p>")
                    .build())
            .build();
    when(documentationUnitRepository.getRandomDocumentationUnits())
        .thenReturn(List.of(invalidTestDocumentUnit));

    exporter.uploadCaseLaw();
    verify(caseLawBucket, times(0)).save(anyString(), anyString());
  }

  @Test
  @DisplayName("Invalid Case Law Ldml should fail validation 2")
  void xsdValidationFailure2() {
    DocumentationUnit invalidTestDocumentUnit =
        testDocumentUnit.toBuilder().longTexts(null).build();
    when(documentationUnitRepository.getRandomDocumentationUnits())
        .thenReturn(List.of(invalidTestDocumentUnit));

    exporter.uploadCaseLaw();
    verify(caseLawBucket, times(0)).save(anyString(), anyString());
  }

  @Test
  @DisplayName("Fallback title test")
  void documentNumberIsFallbackTitleTest() {
    String expected =
        """
           <akn:block name="title">
              <akn:docTitle>
                 <akn:subFlow name="titleWrapper">
                    <akn:p>testDocumentNumber</akn:p>
                 </akn:subFlow>
              </akn:docTitle>
           </akn:block>
           """;
    Optional<CaseLawLdml> ldml =
        DocumentationUnitToLdmlTransformer.transformToLdml(testDocumentUnit);
    Assertions.assertTrue(ldml.isPresent());
    Optional<String> fileContent = exporter.ldmlToString(ldml.get());
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
           <akn:block name="opinions">
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
    Optional<CaseLawLdml> ldml =
        DocumentationUnitToLdmlTransformer.transformToLdml(dissentingCaseLaw);
    Assertions.assertTrue(ldml.isPresent());
    Optional<String> fileContent = exporter.ldmlToString(ldml.get());
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
    Optional<CaseLawLdml> ldml =
        DocumentationUnitToLdmlTransformer.transformToLdml(headnoteCaseLaw);
    Assertions.assertTrue(ldml.isPresent());
    Optional<String> fileContent = exporter.ldmlToString(ldml.get());
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
    Optional<CaseLawLdml> ldml =
        DocumentationUnitToLdmlTransformer.transformToLdml(otherHeadnoteCaseLaw);
    Assertions.assertTrue(ldml.isPresent());
    Optional<String> fileContent = exporter.ldmlToString(ldml.get());
    Assertions.assertTrue(fileContent.isPresent());
    var bla = StringUtils.deleteWhitespace(fileContent.get());
    var blub = StringUtils.deleteWhitespace(expected);
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
    Optional<CaseLawLdml> ldml = DocumentationUnitToLdmlTransformer.transformToLdml(groundsCaseLaw);
    Assertions.assertTrue(ldml.isPresent());
    Optional<String> fileContent = exporter.ldmlToString(ldml.get());
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
    Optional<CaseLawLdml> ldml =
        DocumentationUnitToLdmlTransformer.transformToLdml(otherLongTextCaseLaw);
    Assertions.assertTrue(ldml.isPresent());
    Optional<String> fileContent = exporter.ldmlToString(ldml.get());
    Assertions.assertTrue(fileContent.isPresent());
    Assertions.assertTrue(
        StringUtils.deleteWhitespace(fileContent.get())
            .contains(StringUtils.deleteWhitespace(expected)));
  }
}
