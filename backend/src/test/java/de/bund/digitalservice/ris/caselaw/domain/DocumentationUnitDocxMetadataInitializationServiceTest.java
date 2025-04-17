package de.bund.digitalservice.ris.caselaw.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.CourtDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseCourtRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.PostgresCourtRepositoryImpl;
import de.bund.digitalservice.ris.caselaw.domain.court.CourtRepository;
import de.bund.digitalservice.ris.caselaw.domain.docx.Docx2Html;
import de.bund.digitalservice.ris.caselaw.domain.docx.DocxMetadataProperty;
import de.bund.digitalservice.ris.caselaw.domain.exception.DocumentationUnitNotExistsException;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.documenttype.DocumentType;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@Import({
  DocumentationUnitDocxMetadataInitializationService.class,
  PostgresCourtRepositoryImpl.class
})
class DocumentationUnitDocxMetadataInitializationServiceTest {
  private static final UUID TEST_UUID = UUID.fromString("88888888-4444-4444-4444-121212121212");
  @MockitoSpyBean private DocumentationUnitDocxMetadataInitializationService service;

  @Autowired private CourtRepository courtRepository;

  @MockitoBean private DocumentationUnitRepository repository;
  @MockitoBean private DatabaseCourtRepository databaseCourtRepository;
  @MockitoBean private DocumentTypeRepository documentTypeRepository;

  private DocumentationUnit documentationUnit;

  @BeforeEach
  void beforeEach() {
    CoreData coreData = CoreData.builder().fileNumbers(List.of()).build();
    documentationUnit = DocumentationUnit.builder().coreData(coreData).build();
  }

  @Test
  void testInitializeCoreData_withMetadataSet() {
    Map<DocxMetadataProperty, String> properties =
        Map.of(
            DocxMetadataProperty.FILE_NUMBER,
            "VII ZR 10/23",
            DocxMetadataProperty.DECISION_DATE,
            "01.12.2000",
            DocxMetadataProperty.COURT_TYPE,
            "AG",
            DocxMetadataProperty.COURT_LOCATION,
            "Berlin",
            DocxMetadataProperty.COURT,
            "BFH",
            DocxMetadataProperty.APPRAISAL_BODY,
            "2. Senat",
            DocxMetadataProperty.DOCUMENT_TYPE,
            "Urt",
            DocxMetadataProperty.ECLI,
            "ECLI:ABCD",
            DocxMetadataProperty.PROCEDURE,
            "my-procedure-from-metadata",
            DocxMetadataProperty.LEGAL_EFFECT,
            "Nein");
    Docx2Html docx2html = new Docx2Html(null, List.of(), properties);

    when(documentTypeRepository.findUniqueCaselawBySearchStr("Urt"))
        .thenReturn(Optional.of(DocumentType.builder().label("Urt").build()));

    when(databaseCourtRepository.findOneByTypeAndLocation("AG", "Berlin"))
        .thenReturn(Optional.of(CourtDTO.builder().type("AG").location("Berlin").build()));
    User user = User.builder().name("test").build();
    service.initializeCoreData(documentationUnit, docx2html, user);

    ArgumentCaptor<DocumentationUnit> documentationUnitCaptor =
        ArgumentCaptor.forClass(DocumentationUnit.class);
    verify(repository, times(2)).save(documentationUnitCaptor.capture());
    CoreData savedCoreData = documentationUnitCaptor.getValue().coreData();

    assertEquals("ECLI:ABCD", savedCoreData.ecli());
    assertEquals("AG Berlin", savedCoreData.court().label());
    assertEquals(List.of("VII ZR 10/23"), savedCoreData.fileNumbers());
    assertEquals(LegalEffect.NO.getLabel(), savedCoreData.legalEffect());
    assertEquals("2. Senat", savedCoreData.appraisalBody());
    assertEquals("Urt", savedCoreData.documentType().label());
    assertEquals("my-procedure-from-metadata", savedCoreData.procedure().label());
    assertEquals(
        "2000-12-01",
        savedCoreData.decisionDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
  }

  @Test
  void testInitializeCoreData_doNotSetEcliIfMultipleFound() {
    List<String> ecliList = List.of("ECLI:TEST", "ECLI:TEST2");
    Docx2Html docx2html = new Docx2Html(null, ecliList, Collections.emptyMap());
    User user = User.builder().name("test").build();
    service.initializeCoreData(documentationUnit, docx2html, user);

    ArgumentCaptor<DocumentationUnit> documentationUnitCaptor =
        ArgumentCaptor.forClass(DocumentationUnit.class);
    verify(repository, times(2)).save(documentationUnitCaptor.capture());
    CoreData savedCoreData = documentationUnitCaptor.getValue().coreData();

    assertNull(savedCoreData.ecli());
  }

  @Test
  void testInitializeCoreData_prioritizeEcliFromMetadata() {
    List<String> ecliList = Collections.singletonList("ECLI:FOOTER");
    Map<DocxMetadataProperty, String> properties = Map.of(DocxMetadataProperty.ECLI, "ECLI:ABCD");

    Docx2Html docx2html = new Docx2Html(null, ecliList, properties);
    User user = User.builder().name("test").build();
    service.initializeCoreData(documentationUnit, docx2html, user);

    ArgumentCaptor<DocumentationUnit> documentationUnitCaptor =
        ArgumentCaptor.forClass(DocumentationUnit.class);
    verify(repository, times(2)).save(documentationUnitCaptor.capture());
    CoreData savedCoreData = documentationUnitCaptor.getValue().coreData();

    assertEquals("ECLI:ABCD", savedCoreData.ecli());
  }

  @Test
  void testInitializeCoreData_initializeLegalEffectIfExplicitlyNotSpecified()
      throws DocumentationUnitNotExistsException {

    CoreData coreData =
        CoreData.builder()
            .fileNumbers(List.of())
            .legalEffect(LegalEffect.NOT_SPECIFIED.getLabel())
            .build();
    DocumentationUnit docUnit = DocumentationUnit.builder().coreData(coreData).build();
    when(repository.findByUuid(TEST_UUID)).thenReturn(docUnit);

    Map<DocxMetadataProperty, String> properties =
        Map.of(DocxMetadataProperty.LEGAL_EFFECT, "Nein");
    Docx2Html docx2html = new Docx2Html(null, List.of(), properties);
    User user = User.builder().name("test").build();
    service.initializeCoreData(docUnit, docx2html, user);

    ArgumentCaptor<DocumentationUnit> documentationUnitCaptor =
        ArgumentCaptor.forClass(DocumentationUnit.class);
    verify(repository, times(2)).save(documentationUnitCaptor.capture());

    CoreData savedCoreData = documentationUnitCaptor.getValue().coreData();
    assertEquals(LegalEffect.NO.getLabel(), savedCoreData.legalEffect());
  }

  @Test
  void testInitializeCoreData_shouldNotInitializeAmbiguousCourt() {
    Map<DocxMetadataProperty, String> properties = Map.of(DocxMetadataProperty.COURT, "AG B");
    Docx2Html docx2html = new Docx2Html(null, List.of(), properties);

    when(databaseCourtRepository.findByExactSearchString("AG B")).thenReturn(List.of());
    User user = User.builder().name("test").build();
    service.initializeCoreData(documentationUnit, docx2html, user);

    ArgumentCaptor<DocumentationUnit> documentationUnitCaptor =
        ArgumentCaptor.forClass(DocumentationUnit.class);
    verify(repository, times(2)).save(documentationUnitCaptor.capture());
    CoreData savedCoreData = documentationUnitCaptor.getValue().coreData();

    assertNull(savedCoreData.court());
  }

  @Test
  void testInitializeCoreData_shouldInitializeUniqueCourtByType() {
    Map<DocxMetadataProperty, String> properties = Map.of(DocxMetadataProperty.COURT_TYPE, "BFH");
    Docx2Html docx2html = new Docx2Html(null, List.of(), properties);

    when(databaseCourtRepository.findOneByType("BFH"))
        .thenReturn(Optional.of(CourtDTO.builder().type("BFH").build()));
    User user = User.builder().name("test").build();
    service.initializeCoreData(documentationUnit, docx2html, user);

    ArgumentCaptor<DocumentationUnit> documentationUnitCaptor =
        ArgumentCaptor.forClass(DocumentationUnit.class);
    verify(repository, times(2)).save(documentationUnitCaptor.capture());
    CoreData savedCoreData = documentationUnitCaptor.getValue().coreData();

    assertEquals("BFH", savedCoreData.court().label());
  }

  @Test
  void testInitializeCoreData_shouldNotInitializeAmbiguousCourtType() {
    Map<DocxMetadataProperty, String> properties = Map.of(DocxMetadataProperty.COURT_TYPE, "AG");
    Docx2Html docx2html = new Docx2Html(null, List.of(), properties);

    when(courtRepository.findByTypeAndLocation("AG", null)).thenReturn(Optional.empty());
    User user = User.builder().name("test").build();
    service.initializeCoreData(documentationUnit, docx2html, user);

    ArgumentCaptor<DocumentationUnit> documentationUnitCaptor =
        ArgumentCaptor.forClass(DocumentationUnit.class);
    verify(repository, times(2)).save(documentationUnitCaptor.capture());
    CoreData savedCoreData = documentationUnitCaptor.getValue().coreData();

    assertNull(savedCoreData.court());
  }

  @Test
  void testInitializeCoreData_shouldNotInitializeAmbiguousCourtLocation() {
    Map<DocxMetadataProperty, String> properties =
        Map.of(DocxMetadataProperty.COURT_LOCATION, "Bonn");
    Docx2Html docx2html = new Docx2Html(null, List.of(), properties);

    when(databaseCourtRepository.findOneByTypeAndLocation(null, "Bonn"))
        .thenReturn(Optional.empty());
    User user = User.builder().name("test").build();
    service.initializeCoreData(documentationUnit, docx2html, user);

    ArgumentCaptor<DocumentationUnit> documentationUnitCaptor =
        ArgumentCaptor.forClass(DocumentationUnit.class);
    verify(repository, times(2)).save(documentationUnitCaptor.capture());
    CoreData savedCoreData = documentationUnitCaptor.getValue().coreData();

    assertNull(savedCoreData.court());
  }

  @Test
  void testInitializeCoreData_shouldUseCourtAsFallbackIfNoTypeAndLocation() {
    Map<DocxMetadataProperty, String> properties = Map.of(DocxMetadataProperty.COURT, "LG Bern");
    Docx2Html docx2html = new Docx2Html(null, List.of(), properties);

    when(databaseCourtRepository.findByExactSearchString("LG Bern"))
        .thenReturn(List.of(CourtDTO.builder().type("LG").location("Bern").build()));
    User user = User.builder().name("test").build();
    service.initializeCoreData(documentationUnit, docx2html, user);

    ArgumentCaptor<DocumentationUnit> documentationUnitCaptor =
        ArgumentCaptor.forClass(DocumentationUnit.class);
    verify(repository, times(2)).save(documentationUnitCaptor.capture());
    CoreData savedCoreData = documentationUnitCaptor.getValue().coreData();

    assertEquals("LG Bern", savedCoreData.court().label());
  }

  @Test
  void testInitializeCoreData_shouldUseTypeAndLocationIfUnique() {
    Map<DocxMetadataProperty, String> properties =
        Map.of(
            DocxMetadataProperty.COURT_TYPE,
            "LG",
            DocxMetadataProperty.COURT_LOCATION,
            "Bern",
            DocxMetadataProperty.COURT,
            "LG Bernau");
    Docx2Html docx2html = new Docx2Html(null, List.of(), properties);

    when(databaseCourtRepository.findOneByTypeAndLocation("LG", "Bern"))
        .thenReturn(Optional.of(CourtDTO.builder().type("LG").location("Bern").build()));

    when(databaseCourtRepository.findByExactSearchString("LG Bernau"))
        .thenReturn(
            List.of(
                CourtDTO.builder().type("LG").location("Bernau").build(),
                CourtDTO.builder().type("LG").location("Bern").build()));
    User user = User.builder().name("test").build();
    service.initializeCoreData(documentationUnit, docx2html, user);

    ArgumentCaptor<DocumentationUnit> documentationUnitCaptor =
        ArgumentCaptor.forClass(DocumentationUnit.class);
    verify(repository, times(2)).save(documentationUnitCaptor.capture());
    CoreData savedCoreData = documentationUnitCaptor.getValue().coreData();

    assertEquals("LG Bern", savedCoreData.court().label());
  }

  @Test
  void testInitializeCoreData_shouldUseCourtIfTypeAndLocationNotFound() {
    List<String> ecliList = Collections.singletonList("ECLI:TEST");
    Map<DocxMetadataProperty, String> properties =
        Map.of(
            DocxMetadataProperty.COURT_TYPE,
            "LG",
            DocxMetadataProperty.COURT_LOCATION,
            "Bern 1",
            DocxMetadataProperty.COURT,
            "LG Bernau");
    Docx2Html docx2html = new Docx2Html(null, ecliList, properties);

    when(databaseCourtRepository.findOneByTypeAndLocation("LG", "Bern 1"))
        .thenReturn(Optional.empty());

    when(databaseCourtRepository.findByExactSearchString("LG Bernau"))
        .thenReturn(List.of(CourtDTO.builder().type("LG").location("Bernau").build()));
    User user = User.builder().name("test").build();
    service.initializeCoreData(documentationUnit, docx2html, user);

    ArgumentCaptor<DocumentationUnit> documentationUnitCaptor =
        ArgumentCaptor.forClass(DocumentationUnit.class);
    verify(repository, times(2)).save(documentationUnitCaptor.capture());
    CoreData savedCoreData = documentationUnitCaptor.getValue().coreData();

    assertEquals("LG Bernau", savedCoreData.court().label());
  }
}
