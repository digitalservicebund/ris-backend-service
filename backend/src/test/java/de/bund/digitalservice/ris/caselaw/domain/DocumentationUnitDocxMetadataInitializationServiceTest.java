package de.bund.digitalservice.ris.caselaw.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.bund.digitalservice.ris.caselaw.adapter.DatabaseDocumentUnitStatusService;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDocumentationOfficeRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDocumentationUnitRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseStatusRepository;
import de.bund.digitalservice.ris.caselaw.domain.court.Court;
import de.bund.digitalservice.ris.caselaw.domain.court.CourtRepository;
import de.bund.digitalservice.ris.caselaw.domain.docx.Docx2Html;
import de.bund.digitalservice.ris.caselaw.domain.docx.DocxMetadataProperty;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.documenttype.DocumentType;
import de.bund.digitalservice.ris.caselaw.domain.mapper.PatchMapperService;
import jakarta.validation.Validator;
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
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@Import({DocumentUnitService.class, DatabaseDocumentUnitStatusService.class})
class DocumentationUnitDocxMetadataInitializationServiceTest {
  private static final UUID TEST_UUID = UUID.fromString("88888888-4444-4444-4444-121212121212");
  @SpyBean private DocumentationUnitDocxMetadataInitializationService service;
  @MockBean private DatabaseDocumentationUnitRepository documentationUnitRepository;
  @MockBean private DatabaseStatusRepository statusRepository;
  @MockBean private DocumentUnitRepository repository;
  @MockBean private CourtRepository courtRepository;
  @MockBean private DatabaseDocumentationOfficeRepository documentationOfficeRepository;
  @MockBean private AttachmentService attachmentService;
  @MockBean private PatchMapperService patchMapperService;
  @MockBean private DocumentNumberService documentNumberService;
  @MockBean private DocumentNumberRecyclingService documentNumberRecyclingService;

  @MockBean private MailService mailService;

  @MockBean private DocumentTypeRepository documentTypeRepository;
  @MockBean private Validator validator;

  @BeforeEach
  void beforeEach() {
    CoreData coreData = CoreData.builder().fileNumbers(List.of()).build();
    DocumentUnit documentUnit = DocumentUnit.builder().coreData(coreData).build();
    when(repository.findByUuid(TEST_UUID)).thenReturn(Optional.of(documentUnit));
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

    when(courtRepository.findByTypeAndLocation("AG", "Berlin"))
        .thenReturn(Optional.of(Court.builder().label("AG Berlin").build()));
    when(documentTypeRepository.findUniqueCaselawBySearchStr("Urt"))
        .thenReturn(Optional.of(DocumentType.builder().label("Urt").build()));

    service.initializeCoreData(TEST_UUID, docx2html);

    ArgumentCaptor<DocumentUnit> documentUnitCaptor = ArgumentCaptor.forClass(DocumentUnit.class);
    verify(repository, times(2)).save(documentUnitCaptor.capture());
    CoreData savedCoreData = documentUnitCaptor.getValue().coreData();

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

    service.initializeCoreData(TEST_UUID, docx2html);

    ArgumentCaptor<DocumentUnit> documentUnitCaptor = ArgumentCaptor.forClass(DocumentUnit.class);
    verify(repository, times(2)).save(documentUnitCaptor.capture());
    CoreData savedCoreData = documentUnitCaptor.getValue().coreData();

    assertNull(savedCoreData.ecli());
  }

  @Test
  void testInitializeCoreData_prioritizeEcliFromFooter() {
    List<String> ecliList = Collections.singletonList("ECLI:FOOTER");
    Map<DocxMetadataProperty, String> properties = Map.of(DocxMetadataProperty.ECLI, "ECLI:ABCD");

    Docx2Html docx2html = new Docx2Html(null, ecliList, properties);

    service.initializeCoreData(TEST_UUID, docx2html);

    ArgumentCaptor<DocumentUnit> documentUnitCaptor = ArgumentCaptor.forClass(DocumentUnit.class);
    verify(repository, times(2)).save(documentUnitCaptor.capture());
    CoreData savedCoreData = documentUnitCaptor.getValue().coreData();

    assertEquals("ECLI:FOOTER", savedCoreData.ecli());
  }

  @Test
  void testInitializeCoreData_initializeLegalEffectIfExplicitlyNotSpecified() {
    CoreData coreData =
        CoreData.builder()
            .fileNumbers(List.of())
            .legalEffect(LegalEffect.NOT_SPECIFIED.getLabel())
            .build();
    DocumentUnit documentUnit = DocumentUnit.builder().coreData(coreData).build();
    when(repository.findByUuid(TEST_UUID)).thenReturn(Optional.of(documentUnit));

    Map<DocxMetadataProperty, String> properties =
        Map.of(DocxMetadataProperty.LEGAL_EFFECT, "Nein");
    Docx2Html docx2html = new Docx2Html(null, List.of(), properties);

    service.initializeCoreData(TEST_UUID, docx2html);

    ArgumentCaptor<DocumentUnit> documentUnitCaptor = ArgumentCaptor.forClass(DocumentUnit.class);
    verify(repository, times(2)).save(documentUnitCaptor.capture());

    CoreData savedCoreData = documentUnitCaptor.getValue().coreData();
    assertEquals(LegalEffect.NO.getLabel(), savedCoreData.legalEffect());
  }

  @Test
  void testInitializeCoreData_shouldNotInitializeAmbiguousCourt() {
    Map<DocxMetadataProperty, String> properties = Map.of(DocxMetadataProperty.COURT, "AG B");
    Docx2Html docx2html = new Docx2Html(null, List.of(), properties);

    when(courtRepository.findBySearchStr("AG B"))
        .thenReturn(
            List.of(
                Court.builder().label("AG Berlin").build(),
                Court.builder().label("AG Bernau").build()));

    service.initializeCoreData(TEST_UUID, docx2html);

    ArgumentCaptor<DocumentUnit> documentUnitCaptor = ArgumentCaptor.forClass(DocumentUnit.class);
    verify(repository, times(2)).save(documentUnitCaptor.capture());
    CoreData savedCoreData = documentUnitCaptor.getValue().coreData();

    assertNull(savedCoreData.court());
  }

  @Test
  void testInitializeCoreData_shouldNotInitializeAmbiguousCourtType() {
    Map<DocxMetadataProperty, String> properties = Map.of(DocxMetadataProperty.COURT_TYPE, "AG");
    Docx2Html docx2html = new Docx2Html(null, List.of(), properties);

    when(courtRepository.findBySearchStr("AG"))
        .thenReturn(
            List.of(
                Court.builder().label("AG Berlin").build(),
                Court.builder().label("AG Bernau").build()));

    service.initializeCoreData(TEST_UUID, docx2html);

    ArgumentCaptor<DocumentUnit> documentUnitCaptor = ArgumentCaptor.forClass(DocumentUnit.class);
    verify(repository, times(2)).save(documentUnitCaptor.capture());
    CoreData savedCoreData = documentUnitCaptor.getValue().coreData();

    assertNull(savedCoreData.court());
  }

  @Test
  void testInitializeCoreData_shouldNotInitializeAmbiguousCourtLocation() {
    Map<DocxMetadataProperty, String> properties =
        Map.of(DocxMetadataProperty.COURT_LOCATION, "Bonn");
    Docx2Html docx2html = new Docx2Html(null, List.of(), properties);

    when(courtRepository.findBySearchStr("Bonn"))
        .thenReturn(
            List.of(
                Court.builder().label("AG Bonn").build(),
                Court.builder().label("LG Bonn").build()));

    service.initializeCoreData(TEST_UUID, docx2html);

    ArgumentCaptor<DocumentUnit> documentUnitCaptor = ArgumentCaptor.forClass(DocumentUnit.class);
    verify(repository, times(2)).save(documentUnitCaptor.capture());
    CoreData savedCoreData = documentUnitCaptor.getValue().coreData();

    assertNull(savedCoreData.court());
  }

  @Test
  void testInitializeCoreData_shouldUseCourtAsFallbackIfNoTypeAndLocation() {
    Map<DocxMetadataProperty, String> properties = Map.of(DocxMetadataProperty.COURT, "BFH");
    Docx2Html docx2html = new Docx2Html(null, List.of(), properties);

    when(courtRepository.findUniqueBySearchString("BFH"))
        .thenReturn(Optional.of(Court.builder().label("BFH").build()));

    service.initializeCoreData(TEST_UUID, docx2html);

    ArgumentCaptor<DocumentUnit> documentUnitCaptor = ArgumentCaptor.forClass(DocumentUnit.class);
    verify(repository, times(2)).save(documentUnitCaptor.capture());
    CoreData savedCoreData = documentUnitCaptor.getValue().coreData();

    assertEquals("BFH", savedCoreData.court().label());
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

    when(courtRepository.findByTypeAndLocation("LG", "Bern"))
        .thenReturn(Optional.of(Court.builder().label("LG Bern").build()));

    service.initializeCoreData(TEST_UUID, docx2html);

    ArgumentCaptor<DocumentUnit> documentUnitCaptor = ArgumentCaptor.forClass(DocumentUnit.class);
    verify(repository, times(2)).save(documentUnitCaptor.capture());
    CoreData savedCoreData = documentUnitCaptor.getValue().coreData();

    assertEquals("LG Bern", savedCoreData.court().label());
  }

  @Test
  void testInitializeCoreData_shouldUseCourtIfTypeAndLocationAreAmbiguous() {
    List<String> ecliList = Collections.singletonList("ECLI:TEST");
    Map<DocxMetadataProperty, String> properties =
        Map.of(
            DocxMetadataProperty.COURT_TYPE,
            "LG",
            DocxMetadataProperty.COURT_LOCATION,
            "Bern",
            DocxMetadataProperty.COURT,
            "LG Bernau");
    Docx2Html docx2html = new Docx2Html(null, ecliList, properties);

    when(courtRepository.findByTypeAndLocation("LG", "Bern")).thenReturn(Optional.empty());

    when(courtRepository.findUniqueBySearchString("LG Bernau"))
        .thenReturn(Optional.of(Court.builder().label("LG Bernau").build()));

    service.initializeCoreData(TEST_UUID, docx2html);

    ArgumentCaptor<DocumentUnit> documentUnitCaptor = ArgumentCaptor.forClass(DocumentUnit.class);
    verify(repository, times(2)).save(documentUnitCaptor.capture());
    CoreData savedCoreData = documentUnitCaptor.getValue().coreData();

    assertEquals("LG Bernau", savedCoreData.court().label());
  }
}
