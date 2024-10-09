package de.bund.digitalservice.ris.caselaw.adapter;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.CaseLawLdml;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.CourtDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentTypeDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentationUnitDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.FileNumberDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.LegalEffectDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.PreviousDecisionDTO;
import de.bund.digitalservice.ris.caselaw.adapter.transformer.CaseLawDbEntityToLdmlMapper;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnitRepository;
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
  static CaseLawBucket caseLawBucket;
  static CaseLawPostgresToS3Exporter exporter;
  static DocumentationUnitDTO testDocumentUnit;
  static UUID testUUID;

  @BeforeAll
  static void setUpBeforeClass() {
    documentationUnitRepository = mock(DocumentationUnitRepository.class);
    caseLawBucket = mock(CaseLawBucket.class);
    exporter = new CaseLawPostgresToS3Exporter(documentationUnitRepository, caseLawBucket);

    PreviousDecisionDTO related1 =
        PreviousDecisionDTO.builder()
            .date(LocalDate.of(2020, 1, 1))
            .court(CourtDTO.builder().type("Test court type").build())
            .documentType(DocumentTypeDTO.builder().abbreviation("Test decision type").build())
            .fileNumber("Test file number")
            .documentNumber("Test document number 1")
            .build();
    PreviousDecisionDTO related2 =
        related1.toBuilder().documentNumber("Test document number 2").build();

    testUUID = UUID.randomUUID();
    testDocumentUnit =
        DocumentationUnitDTO.builder()
            .id(testUUID)
            .ecli("testecli")
            .court(CourtDTO.builder().type("testCourtType").location("testCourtLocation").build())
            .documentType(
                DocumentTypeDTO.builder().abbreviation("testDocumentTypeAbbreviation").build())
            .legalEffect(LegalEffectDTO.JA)
            .fileNumbers(List.of(FileNumberDTO.builder().value("testFileNumber").build()))
            .documentNumber("testDocumentNumber")
            .decisionDate(LocalDate.of(2020, 1, 1))
            .caseFacts("<p>Example content 1</p>")
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
    when(documentationUnitRepository.getUnprocessedIds()).thenReturn(List.of(UUID.randomUUID()));
    when(documentationUnitRepository.findByIdIn(anyList())).thenReturn(List.of(testDocumentUnit));

    exporter.uploadCaseLaw();
    verify(caseLawBucket, times(1)).save(anyString(), anyString());
  }

  @Test
  @DisplayName("Invalid Case Law Ldml should fail validation 1")
  void xsdValidationFailure1() {
    DocumentationUnitDTO invalidTestDocumentUnit =
        testDocumentUnit.toBuilder().caseFacts("<p>Example <p>nested</p> content 1</p>").build();
    when(documentationUnitRepository.getUnprocessedIds()).thenReturn(List.of(UUID.randomUUID()));
    when(documentationUnitRepository.findByIdIn(anyList()))
        .thenReturn(List.of(invalidTestDocumentUnit));

    exporter.uploadCaseLaw();
    verify(caseLawBucket, times(0)).save(anyString(), anyString());
  }

  @Test
  @DisplayName("Invalid Case Law Ldml should fail validation 2")
  void xsdValidationFailure2() {
    DocumentationUnitDTO invalidTestDocumentUnit =
        testDocumentUnit.toBuilder().caseFacts(null).build();
    when(documentationUnitRepository.getUnprocessedIds()).thenReturn(List.of(UUID.randomUUID()));
    when(documentationUnitRepository.findByIdIn(anyList()))
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
    Optional<CaseLawLdml> ldml = CaseLawDbEntityToLdmlMapper.getLDML(testDocumentUnit);
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
    DocumentationUnitDTO dissentingCaseLaw =
        testDocumentUnit.toBuilder().dissentingOpinion("<p>dissenting test</p>").build();
    Optional<CaseLawLdml> ldml = CaseLawDbEntityToLdmlMapper.getLDML(dissentingCaseLaw);
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
    DocumentationUnitDTO headnoteCaseLaw =
        testDocumentUnit.toBuilder().headnote("<p>headnote test</p>").build();
    Optional<CaseLawLdml> ldml = CaseLawDbEntityToLdmlMapper.getLDML(headnoteCaseLaw);
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
    DocumentationUnitDTO otherHeadnoteCaseLaw =
        testDocumentUnit.toBuilder().otherHeadnote("<p>other headnote test</p>").build();
    Optional<CaseLawLdml> ldml = CaseLawDbEntityToLdmlMapper.getLDML(otherHeadnoteCaseLaw);
    Assertions.assertTrue(ldml.isPresent());
    Optional<String> fileContent = exporter.ldmlToString(ldml.get());
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
    DocumentationUnitDTO groundsCaseLaw =
        testDocumentUnit.toBuilder().grounds("<p>grounds test</p>").build();
    Optional<CaseLawLdml> ldml = CaseLawDbEntityToLdmlMapper.getLDML(groundsCaseLaw);
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
    DocumentationUnitDTO otherLongTextCaseLaw =
        testDocumentUnit.toBuilder().otherLongText("<p>Other long text test</p>").build();
    Optional<CaseLawLdml> ldml = CaseLawDbEntityToLdmlMapper.getLDML(otherLongTextCaseLaw);
    Assertions.assertTrue(ldml.isPresent());
    Optional<String> fileContent = exporter.ldmlToString(ldml.get());
    Assertions.assertTrue(fileContent.isPresent());
    Assertions.assertTrue(
        StringUtils.deleteWhitespace(fileContent.get())
            .contains(StringUtils.deleteWhitespace(expected)));
  }
}
