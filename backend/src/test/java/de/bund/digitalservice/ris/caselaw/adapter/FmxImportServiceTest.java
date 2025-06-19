package de.bund.digitalservice.ris.caselaw.adapter;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.AttachmentDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.AttachmentRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDocumentationUnitRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentationUnitDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.EurLexResultDTO;
import de.bund.digitalservice.ris.caselaw.adapter.eurlex.EurLexResultRepository;
import de.bund.digitalservice.ris.caselaw.adapter.eurlex.FmxImportService;
import de.bund.digitalservice.ris.caselaw.adapter.eurlex.HttpEurlexRetrievalService;
import de.bund.digitalservice.ris.caselaw.adapter.exception.FmxImporterException;
import de.bund.digitalservice.ris.caselaw.domain.CoreData;
import de.bund.digitalservice.ris.caselaw.domain.DocumentTypeCategory;
import de.bund.digitalservice.ris.caselaw.domain.DocumentTypeRepository;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationOffice;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnit;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnitRepository;
import de.bund.digitalservice.ris.caselaw.domain.FmxRepository;
import de.bund.digitalservice.ris.caselaw.domain.InboxStatus;
import de.bund.digitalservice.ris.caselaw.domain.LongTexts;
import de.bund.digitalservice.ris.caselaw.domain.ShortTexts;
import de.bund.digitalservice.ris.caselaw.domain.SourceValue;
import de.bund.digitalservice.ris.caselaw.domain.User;
import de.bund.digitalservice.ris.caselaw.domain.court.Court;
import de.bund.digitalservice.ris.caselaw.domain.court.CourtRepository;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.documenttype.DocumentType;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import javax.xml.transform.Templates;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamSource;
import net.sf.saxon.jaxp.SaxonTransformerFactory;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@Import({
  FmxImportService.class,
  DocumentationUnitRepository.class,
  CourtRepository.class,
  DocumentTypeRepository.class,
  FmxRepository.class,
  AttachmentRepository.class,
  DatabaseDocumentationUnitRepository.class,
  HttpEurlexRetrievalService.class,
  XmlUtilService.class
})
class FmxImportServiceTest {

  @Autowired private FmxImportService service;

  @MockitoBean DocumentationUnitRepository documentationUnitRepository;
  @MockitoBean CourtRepository courtRepository;
  @MockitoBean DocumentTypeRepository documentTypeRepository;
  @MockitoBean FmxRepository fmxRepository;
  @MockitoBean AttachmentRepository attachmentRepository;
  @MockitoBean DatabaseDocumentationUnitRepository databaseDocumentationUnitRepository;
  @MockitoBean EurLexResultRepository eurLexResultRepository;
  @MockitoBean HttpEurlexRetrievalService retrievalService;
  @MockitoBean XmlUtilService xmlUtilService;

  @Mock User user;

  private final TransformerFactory transformerFactory = new SaxonTransformerFactory();

  @BeforeEach
  void setup() throws TransformerConfigurationException, IOException {
    ClassPathResource xsltResource = new ClassPathResource("xml/fmxToHtml.xslt");
    String fileContent = IOUtils.toString(xsltResource.getInputStream(), StandardCharsets.UTF_8);
    Templates templates =
        transformerFactory.newTemplates(new StreamSource(new StringReader(fileContent)));
    when(xmlUtilService.getTemplates("xml/fmxToHtml.xslt")).thenReturn(templates);
  }

  @Test
  void shouldAttachFmxToDocumentationUnit() {
    String celexNumber = "CELEX1234";
    UUID id = UUID.randomUUID();
    DocumentationUnit documentationUnit =
        DocumentationUnit.builder()
            .uuid(id)
            .coreData(
                CoreData.builder()
                    .documentationOffice(DocumentationOffice.builder().abbreviation("DS").build())
                    .build())
            .longTexts(LongTexts.builder().build())
            .shortTexts(ShortTexts.builder().build())
            .build();

    EurLexResultDTO eurLexResultDTO = mock(EurLexResultDTO.class);
    when(eurLexResultDTO.getUri())
        .thenReturn("https://publications.europa.eu/resource/celex/" + celexNumber);
    when(eurLexResultRepository.findByCelexNumber(any())).thenReturn(Optional.of(eurLexResultDTO));

    doReturn(judgment)
        .when(retrievalService)
        .requestSingleEurlexDocument(
            "https://publications.europa.eu/resource/celex/" + celexNumber);
    ArgumentCaptor<AttachmentDTO> attachmentCaptor = ArgumentCaptor.forClass(AttachmentDTO.class);
    DocumentationUnitDTO documentationUnitDTO = mock(DocumentationUnitDTO.class);
    doReturn(Optional.of(documentationUnitDTO))
        .when(databaseDocumentationUnitRepository)
        .findById(id);

    service.getDataFromEurlex(celexNumber, documentationUnit, user);

    verify(attachmentRepository).save(attachmentCaptor.capture());
    AttachmentDTO attachmentDTO = attachmentCaptor.getValue();
    assertThat(attachmentDTO.getFilename()).isEqualTo("Originalentscheidung");
    assertThat(attachmentDTO.getFormat()).isEqualTo("fmx");
  }

  @Test
  void judgment_shouldExtractMetadata() {
    String celexNumber = "62022CJ0303";
    String uri = "https://publications.europa.eu/resource/celex/" + celexNumber;
    UUID id = UUID.randomUUID();
    DocumentationUnit documentationUnit =
        DocumentationUnit.builder()
            .uuid(id)
            .coreData(
                CoreData.builder()
                    .documentationOffice(DocumentationOffice.builder().abbreviation("DS").build())
                    .build())
            .longTexts(LongTexts.builder().build())
            .shortTexts(ShortTexts.builder().build())
            .build();

    EurLexResultDTO eurLexResultDTO = mock(EurLexResultDTO.class);
    when(eurLexResultDTO.getUri()).thenReturn(uri);
    when(eurLexResultRepository.findByCelexNumber(any())).thenReturn(Optional.of(eurLexResultDTO));

    doReturn(judgment).when(retrievalService).requestSingleEurlexDocument(uri);
    ArgumentCaptor<DocumentationUnit> docUnitCaptor =
        ArgumentCaptor.forClass(DocumentationUnit.class);
    DocumentationUnitDTO documentationUnitDTO = mock(DocumentationUnitDTO.class);
    doReturn(Optional.of(documentationUnitDTO))
        .when(databaseDocumentationUnitRepository)
        .findById(id);
    when(courtRepository.findByTypeAndLocation("EuGH", null))
        .thenReturn(Optional.ofNullable(Court.builder().label("EuGH").build()));
    when(documentTypeRepository.findUniqueCaselawBySearchStr("Urteil"))
        .thenReturn(Optional.of(DocumentType.builder().label("Urteil").build()));

    service.getDataFromEurlex(celexNumber, documentationUnit, user);

    verify(documentationUnitRepository)
        .save(docUnitCaptor.capture(), eq(user), eq("EU-Entscheidung angelegt für DS"));
    DocumentationUnit savedDocUnit = docUnitCaptor.getValue();
    assertThat(savedDocUnit.coreData().court().label()).isEqualTo("EuGH");
    assertThat(savedDocUnit.coreData().fileNumbers().getFirst()).isEqualTo("C-303/22");
    assertThat(savedDocUnit.coreData().decisionDate()).isEqualTo(LocalDate.of(2017, 2, 14));
    assertThat(savedDocUnit.coreData().ecli()).isEqualTo("ECLI:EU:C:2024:60");
    assertThat(savedDocUnit.coreData().celexNumber()).isEqualTo(celexNumber);
    assertThat(savedDocUnit.coreData().documentType().label()).isEqualTo("Urteil");
    assertThat(savedDocUnit.coreData().source().value()).isEqualTo(SourceValue.L);
    assertThat(savedDocUnit.shortTexts().headnote()).isEqualTo("CELEX Nummer: " + celexNumber);
    assertThat(savedDocUnit.inboxStatus()).isEqualTo(InboxStatus.EU);
  }

  @Test
  void order_shouldExtractMetadata() {
    String celexNumber = "62018TO0235(04)";
    String uri = "https://publications.europa.eu/resource/celex/" + celexNumber;
    UUID id = UUID.randomUUID();
    DocumentationUnit documentationUnit =
        DocumentationUnit.builder()
            .uuid(id)
            .coreData(
                CoreData.builder()
                    .documentationOffice(DocumentationOffice.builder().abbreviation("DS").build())
                    .build())
            .longTexts(LongTexts.builder().build())
            .shortTexts(ShortTexts.builder().build())
            .build();

    EurLexResultDTO eurLexResultDTO = mock(EurLexResultDTO.class);
    when(eurLexResultDTO.getUri()).thenReturn(uri);
    when(eurLexResultRepository.findByCelexNumber(any())).thenReturn(Optional.of(eurLexResultDTO));

    doReturn(order).when(retrievalService).requestSingleEurlexDocument(uri);
    ArgumentCaptor<DocumentationUnit> docUnitCaptor =
        ArgumentCaptor.forClass(DocumentationUnit.class);
    DocumentationUnitDTO documentationUnitDTO = mock(DocumentationUnitDTO.class);
    doReturn(Optional.of(documentationUnitDTO))
        .when(databaseDocumentationUnitRepository)
        .findById(id);
    when(courtRepository.findByTypeAndLocation("EuG", null))
        .thenReturn(Optional.ofNullable(Court.builder().label("EuG").build()));
    when(documentTypeRepository.findDocumentTypesBySearchStrAndCategory(
            "Beschluss", DocumentTypeCategory.CASELAW))
        .thenReturn(List.of(DocumentType.builder().label("Beschluss").build()));

    service.getDataFromEurlex(celexNumber, documentationUnit, user);

    verify(documentationUnitRepository)
        .save(docUnitCaptor.capture(), eq(user), eq("EU-Entscheidung angelegt für DS"));
    DocumentationUnit savedDocUnit = docUnitCaptor.getValue();
    assertThat(savedDocUnit.coreData().court().label()).isEqualTo("EuG");
    assertThat(savedDocUnit.coreData().fileNumbers().getFirst()).isEqualTo("T-235/18");
    assertThat(savedDocUnit.coreData().decisionDate()).isEqualTo(LocalDate.of(2024, 2, 29));
    assertThat(savedDocUnit.coreData().ecli()).isEqualTo("ECLI:EU:T:2024:142");
    assertThat(savedDocUnit.coreData().celexNumber()).isEqualTo(celexNumber);
    assertThat(savedDocUnit.coreData().documentType().label()).isEqualTo("Beschluss");
    assertThat(savedDocUnit.coreData().source().value()).isEqualTo(SourceValue.L);
    assertThat(savedDocUnit.shortTexts().headnote()).isEqualTo("CELEX Nummer: " + celexNumber);
    assertThat(savedDocUnit.inboxStatus()).isEqualTo(InboxStatus.EU);
  }

  @Test
  void opinion_shouldExtractMetadata() {
    String celexNumber = "62013CV0001";
    String uri = "https://publications.europa.eu/resource/celex/" + celexNumber;
    UUID id = UUID.randomUUID();
    DocumentationUnit documentationUnit =
        DocumentationUnit.builder()
            .uuid(id)
            .coreData(
                CoreData.builder()
                    .documentationOffice(DocumentationOffice.builder().abbreviation("DS").build())
                    .build())
            .longTexts(LongTexts.builder().build())
            .shortTexts(ShortTexts.builder().build())
            .build();

    EurLexResultDTO eurLexResultDTO = mock(EurLexResultDTO.class);
    when(eurLexResultDTO.getUri()).thenReturn(uri);
    when(eurLexResultRepository.findByCelexNumber(any())).thenReturn(Optional.of(eurLexResultDTO));

    doReturn(opinion).when(retrievalService).requestSingleEurlexDocument(uri);
    ArgumentCaptor<DocumentationUnit> docUnitCaptor =
        ArgumentCaptor.forClass(DocumentationUnit.class);
    DocumentationUnitDTO documentationUnitDTO = mock(DocumentationUnitDTO.class);
    doReturn(Optional.of(documentationUnitDTO))
        .when(databaseDocumentationUnitRepository)
        .findById(id);
    when(courtRepository.findByTypeAndLocation("EuGH", null))
        .thenReturn(Optional.ofNullable(Court.builder().label("EuGH").build()));
    when(documentTypeRepository.findDocumentTypesBySearchStrAndCategory(
            "Gutachten", DocumentTypeCategory.CASELAW))
        .thenReturn(List.of(DocumentType.builder().label("Gutachten").build()));

    service.getDataFromEurlex(celexNumber, documentationUnit, user);

    verify(documentationUnitRepository)
        .save(docUnitCaptor.capture(), eq(user), eq("EU-Entscheidung angelegt für DS"));
    DocumentationUnit savedDocUnit = docUnitCaptor.getValue();
    assertThat(savedDocUnit.coreData().court().label()).isEqualTo("EuGH");
    assertThat(savedDocUnit.coreData().fileNumbers().getFirst()).isEqualTo("Avis 1/13");
    assertThat(savedDocUnit.coreData().decisionDate()).isEqualTo(LocalDate.of(2014, 10, 14));
    assertThat(savedDocUnit.coreData().ecli()).isEqualTo("ECLI:EU:C:2014:2303");
    assertThat(savedDocUnit.coreData().celexNumber()).isEqualTo(celexNumber);
    assertThat(savedDocUnit.coreData().documentType().label()).isEqualTo("Gutachten");
    assertThat(savedDocUnit.coreData().source().value()).isEqualTo(SourceValue.L);
    assertThat(savedDocUnit.shortTexts().headnote()).isEqualTo("CELEX Nummer: " + celexNumber);
    assertThat(savedDocUnit.inboxStatus()).isEqualTo(InboxStatus.EU);
  }

  @Test
  void judgment_shouldExtractLongTexts() {
    String celexNumber = "CELEX1234";
    UUID id = UUID.randomUUID();
    DocumentationUnit documentationUnit =
        DocumentationUnit.builder()
            .uuid(id)
            .coreData(
                CoreData.builder()
                    .documentationOffice(DocumentationOffice.builder().abbreviation("DS").build())
                    .build())
            .longTexts(LongTexts.builder().build())
            .shortTexts(ShortTexts.builder().build())
            .build();

    EurLexResultDTO eurLexResultDTO = mock(EurLexResultDTO.class);
    when(eurLexResultDTO.getUri())
        .thenReturn("https://publications.europa.eu/resource/celex/" + celexNumber);
    when(eurLexResultRepository.findByCelexNumber(any())).thenReturn(Optional.of(eurLexResultDTO));

    doReturn(judgment)
        .when(retrievalService)
        .requestSingleEurlexDocument(
            "https://publications.europa.eu/resource/celex/" + celexNumber);
    ArgumentCaptor<DocumentationUnit> docUnitCaptor =
        ArgumentCaptor.forClass(DocumentationUnit.class);
    DocumentationUnitDTO documentationUnitDTO = mock(DocumentationUnitDTO.class);
    when(databaseDocumentationUnitRepository.findById(id))
        .thenReturn(Optional.of(documentationUnitDTO));

    service.getDataFromEurlex(celexNumber, documentationUnit, user);

    verify(documentationUnitRepository)
        .save(docUnitCaptor.capture(), eq(user), eq("EU-Entscheidung angelegt für DS"));
    DocumentationUnit savedDocUnit = docUnitCaptor.getValue();
    assertThat(savedDocUnit.longTexts().reasons()).contains("Urteil");
    assertThat(savedDocUnit.longTexts().reasons()).contains("Unterschriften");
    assertThat(savedDocUnit.longTexts().reasons()).contains("<p>Verfahrenssprache: Englisch.</p>");
    assertThat(savedDocUnit.longTexts().reasons())
        .doesNotContain("Beschluss des Gerichts (Zweite erweiterte Kammer)");
    assertThat(savedDocUnit.longTexts().reasons())
        .doesNotContain(
            "Aus diesen Gründen hat der Gerichtshof (Vierte Kammer) für Recht erkannt:");
    assertThat(savedDocUnit.longTexts().tenor())
        .contains("Aus diesen Gründen hat der Gerichtshof (Vierte Kammer) für Recht erkannt:");
  }

  @Test
  void order_shouldExtractLongTexts() {
    String celexNumber = "CELEX1234";
    UUID id = UUID.randomUUID();
    DocumentationUnit documentationUnit =
        DocumentationUnit.builder()
            .uuid(id)
            .coreData(
                CoreData.builder()
                    .documentationOffice(DocumentationOffice.builder().abbreviation("DS").build())
                    .build())
            .longTexts(LongTexts.builder().build())
            .shortTexts(ShortTexts.builder().build())
            .build();

    EurLexResultDTO eurLexResultDTO = mock(EurLexResultDTO.class);
    when(eurLexResultDTO.getUri())
        .thenReturn("https://publications.europa.eu/resource/celex/" + celexNumber);
    when(eurLexResultRepository.findByCelexNumber(any())).thenReturn(Optional.of(eurLexResultDTO));

    doReturn(order)
        .when(retrievalService)
        .requestSingleEurlexDocument(
            "https://publications.europa.eu/resource/celex/" + celexNumber);
    ArgumentCaptor<DocumentationUnit> docUnitCaptor =
        ArgumentCaptor.forClass(DocumentationUnit.class);
    DocumentationUnitDTO documentationUnitDTO = mock(DocumentationUnitDTO.class);
    when(databaseDocumentationUnitRepository.findById(id))
        .thenReturn(Optional.of(documentationUnitDTO));

    service.getDataFromEurlex(celexNumber, documentationUnit, user);

    verify(documentationUnitRepository)
        .save(docUnitCaptor.capture(), eq(user), eq("EU-Entscheidung angelegt für DS"));
    DocumentationUnit savedDocUnit = docUnitCaptor.getValue();
    assertThat(savedDocUnit.longTexts().reasons()).contains("Beschluss");
    assertThat(savedDocUnit.longTexts().reasons()).contains("Luxemburg, den");
    assertThat(savedDocUnit.longTexts().reasons()).contains("Der Kanzler");
    assertThat(savedDocUnit.longTexts().reasons()).contains("V.&nbsp;Di Bucci");
    assertThat(savedDocUnit.longTexts().reasons())
        .doesNotContain(
            "Aus diesen Gründen hat DAS GERICHT (Zweite erweiterte Kammer) beschlossen:");
    assertThat(savedDocUnit.longTexts().reasons()).contains("<p>Verfahrenssprache: Deutsch.</p>");
    assertThat(savedDocUnit.longTexts().reasons()).doesNotContain("25.&nbsp;Januar 2024");
    assertThat(savedDocUnit.longTexts().tenor())
        .contains("Aus diesen Gründen hat DAS GERICHT (Zweite erweiterte Kammer) beschlossen:");
  }

  @Test
  void opinion_shouldExtractLongTexts() {
    String celexNumber = "CELEX1234";
    UUID id = UUID.randomUUID();
    DocumentationUnit documentationUnit =
        DocumentationUnit.builder()
            .uuid(id)
            .coreData(
                CoreData.builder()
                    .documentationOffice(DocumentationOffice.builder().abbreviation("DS").build())
                    .build())
            .longTexts(LongTexts.builder().build())
            .shortTexts(ShortTexts.builder().build())
            .build();

    EurLexResultDTO eurLexResultDTO = mock(EurLexResultDTO.class);
    when(eurLexResultDTO.getUri())
        .thenReturn("https://publications.europa.eu/resource/celex/" + celexNumber);
    when(eurLexResultRepository.findByCelexNumber(any())).thenReturn(Optional.of(eurLexResultDTO));

    doReturn(opinion)
        .when(retrievalService)
        .requestSingleEurlexDocument(
            "https://publications.europa.eu/resource/celex/" + celexNumber);
    ArgumentCaptor<DocumentationUnit> docUnitCaptor =
        ArgumentCaptor.forClass(DocumentationUnit.class);
    DocumentationUnitDTO documentationUnitDTO = mock(DocumentationUnitDTO.class);
    when(databaseDocumentationUnitRepository.findById(id))
        .thenReturn(Optional.of(documentationUnitDTO));

    service.getDataFromEurlex(celexNumber, documentationUnit, user);

    verify(documentationUnitRepository)
        .save(docUnitCaptor.capture(), eq(user), eq("EU-Entscheidung angelegt für DS"));
    DocumentationUnit savedDocUnit = docUnitCaptor.getValue();
    assertThat(savedDocUnit.longTexts().reasons()).contains("Gutachten");
    assertThat(savedDocUnit.longTexts().reasons()).contains("Unterschriften");
    assertThat(savedDocUnit.longTexts().reasons())
        .doesNotContain(
            "Folglich äußert sich der Gerichtshof (Große Kammer) gutachtlich wie folgt:");
    assertThat(savedDocUnit.longTexts().reasons())
        .doesNotContain("Das Einverständnis zum Beitritt eines Drittstaats...");
    assertThat(savedDocUnit.longTexts().tenor())
        .contains("Folglich äußert sich der Gerichtshof (Große Kammer) gutachtlich wie folgt:");
    assertThat(savedDocUnit.longTexts().tenor())
        .contains("Das Einverständnis zum Beitritt eines Drittstaats...");
  }

  @Test
  void emptyXml_shouldThrowTransformationException() {
    String celexNumber = "CELEX1234";
    UUID id = UUID.randomUUID();
    DocumentationUnit documentationUnit =
        DocumentationUnit.builder()
            .uuid(id)
            .coreData(
                CoreData.builder()
                    .documentationOffice(DocumentationOffice.builder().abbreviation("DS").build())
                    .build())
            .longTexts(LongTexts.builder().build())
            .shortTexts(ShortTexts.builder().build())
            .build();

    EurLexResultDTO eurLexResultDTO = mock(EurLexResultDTO.class);
    when(eurLexResultDTO.getUri())
        .thenReturn("https://publications.europa.eu/resource/celex/" + celexNumber);
    when(eurLexResultRepository.findByCelexNumber(any())).thenReturn(Optional.of(eurLexResultDTO));

    doReturn("")
        .when(retrievalService)
        .requestSingleEurlexDocument(
            "https://publications.europa.eu/resource/celex/" + celexNumber);

    assertThatExceptionOfType(FmxImporterException.class)
        .isThrownBy(() -> service.getDataFromEurlex(celexNumber, documentationUnit, user))
        .withMessageContaining("FMX file has no content.");
  }

  @Test
  void malformedXml_shouldThrowTransformationException() {
    String celexNumber = "CELEX1234";
    UUID id = UUID.randomUUID();
    DocumentationUnit documentationUnit =
        DocumentationUnit.builder()
            .uuid(id)
            .coreData(
                CoreData.builder()
                    .documentationOffice(DocumentationOffice.builder().abbreviation("DS").build())
                    .build())
            .longTexts(LongTexts.builder().build())
            .shortTexts(ShortTexts.builder().build())
            .build();

    EurLexResultDTO eurLexResultDTO = mock(EurLexResultDTO.class);
    when(eurLexResultDTO.getUri())
        .thenReturn("https://publications.europa.eu/resource/celex/" + celexNumber);
    when(eurLexResultRepository.findByCelexNumber(any())).thenReturn(Optional.of(eurLexResultDTO));

    doReturn("lorem ipsum")
        .when(retrievalService)
        .requestSingleEurlexDocument(
            "https://publications.europa.eu/resource/celex/" + celexNumber);
    DocumentationUnitDTO documentationUnitDTO = mock(DocumentationUnitDTO.class);
    when(databaseDocumentationUnitRepository.findById(id))
        .thenReturn(Optional.of(documentationUnitDTO));

    assertThatExceptionOfType(FmxImporterException.class)
        .isThrownBy(() -> service.getDataFromEurlex(celexNumber, documentationUnit, user))
        .withMessageContaining("Failed to parse FMX file content.");
  }

  @Test
  void missingEurlexResult_shouldThrowTransformationException() {
    String celexNumber = "CELEX1234";
    UUID id = UUID.randomUUID();
    DocumentationUnit documentationUnit =
        DocumentationUnit.builder()
            .uuid(id)
            .coreData(
                CoreData.builder()
                    .documentationOffice(DocumentationOffice.builder().abbreviation("DS").build())
                    .build())
            .longTexts(LongTexts.builder().build())
            .shortTexts(ShortTexts.builder().build())
            .build();

    EurLexResultDTO eurLexResultDTO = mock(EurLexResultDTO.class);
    when(eurLexResultDTO.getUri())
        .thenReturn("https://publications.europa.eu/resource/celex/" + celexNumber);
    when(eurLexResultRepository.findByCelexNumber(any())).thenReturn(Optional.empty());

    assertThatExceptionOfType(FmxImporterException.class)
        .isThrownBy(() -> service.getDataFromEurlex(celexNumber, documentationUnit, user))
        .withMessageContaining(
            "Could not find matching Eurlex Result for Celex Number " + celexNumber);
  }

  private final String judgment =
      """
      <?xml version="1.0" encoding="UTF-8"?>
      <JUDGMENT xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                xsi:noNamespaceSchemaLocation="http://formex.publications.europa.eu/schema/formex-05.58-20161101.xd">
          <BIB.JUDGMENT>
              <REF.CASE FILE="n.a.">
                  <NO.CASE>C-303/22</NO.CASE>
              </REF.CASE>
              <NO.CELEX>62022CJ0303</NO.CELEX>
              <NO.ECLI ECLI="ECLI:EU:C:2024:60">EU:C:2024:60</NO.ECLI>
              <AUTHOR>CJ</AUTHOR>
          </BIB.JUDGMENT>
          <CURR.TITLE>
                  <PAGE.HEADER>
                      <P>Urteil vom <DATE ISO="20170214">14. 2. 2017</DATE></P>
                  </PAGE.HEADER>
              </CURR.TITLE>
          <TITLE>
              <TI>
                  <P>Beschluss des Gerichts (Zweite erweiterte Kammer)</P>
                  <NOTE TYPE="FOOTNOTE" NUMBERING="STAR" NOTE.ID="E0001" NUMBERING.CONTINUED="YES">
                      <P>Verfahrenssprache: Englisch.</P>
                  </NOTE>
              </TI>
          </TITLE>
          <CONTENTS.JUDGMENT><TXT>Urteil</TXT>
              <JURISDICTION>
                  <INTRO>Aus diesen Gründen hat der Gerichtshof (Vierte Kammer) für Recht erkannt:</INTRO>
              </JURISDICTION>
          </CONTENTS.JUDGMENT>
          <SIGNATURE.CASE>
              <SIGNATORY>
                  <P>Unterschriften</P>
              </SIGNATORY>
          </SIGNATURE.CASE>
      </JUDGMENT>
      """;

  private final String order =
      """
      <?xml version="1.0" encoding="UTF-8"?>
      <ORDER xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                xsi:noNamespaceSchemaLocation="http://formex.publications.europa.eu/schema/formex-05.58-20161101.xd">
          <BIB.ORDER>
              <REF.CASE FILE="n.a.">
                  <NO.CASE>T-235/18</NO.CASE>
              </REF.CASE>
              <NO.CELEX>62018TO0235(04)</NO.CELEX>
              <NO.ECLI ECLI="ECLI:EU:T:2024:142">EU:T:2024:142</NO.ECLI>
              <AUTHOR>T</AUTHOR>
          </BIB.ORDER>
          <CURR.TITLE>
                  <PAGE.HEADER>
                      <P>Beschluss vom <DATE ISO="20240229">29. 2. 2024</DATE></P>
                  </PAGE.HEADER>
              </CURR.TITLE>
          <TITLE>
              <TI>
                  <P>
                      <DATE ISO="20240125">25. Januar 2024</DATE>
                      <NOTE TYPE="FOOTNOTE" NUMBERING="STAR" NOTE.ID="E0001">
                          <P>Verfahrenssprache: Deutsch.</P>
                      </NOTE>
                  </P>
              </TI>
          </TITLE>
          <CONTENTS.ORDER><TXT>Beschluss</TXT>
              <JURISDICTION>
                  <INTRO>Aus diesen Gründen hat DAS GERICHT (Zweite erweiterte Kammer) beschlossen:</INTRO>
              </JURISDICTION>
          </CONTENTS.ORDER>
          <SIGNATURE.CASE>
              <P>Luxemburg, den <DATE ISO="20240229">29. Februar 2024</DATE></P>
              <SIGNATORY>
                  <P>Der Kanzler</P>
                  <P>V. Di Bucci</P>
              </SIGNATORY>
          </SIGNATURE.CASE>
      </ORDER>
      """;

  private final String opinion =
      """
      <?xml version="1.0" encoding="UTF-8"?>
      <OPINION xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                xsi:noNamespaceSchemaLocation="http://formex.publications.europa.eu/schema/formex-05.58-20161101.xd">
          <BIB.OPINION>
              <REF.CASE FILE="n.a.">
                  <NO.CASE>Avis 1/13</NO.CASE>
              </REF.CASE>
              <NO.CELEX>62013CV0001</NO.CELEX>
              <NO.ECLI ECLI="ECLI:EU:C:2014:2303">EU:C:2014:2303</NO.ECLI>
              <AUTHOR>CJ</AUTHOR>
          </BIB.OPINION>
          <CURR.TITLE>
                  <PAGE.HEADER>
                      <P>GUTACHTEN 1/13 VOM vom <DATE ISO="20141014">14. 10. 2014</DATE></P>
                  </PAGE.HEADER>
              </CURR.TITLE>
          <CONTENTS.OPINION><TXT>Gutachten</TXT>
              <PREAMBLE.GEN>
                  <P>Folglich äußert sich der Gerichtshof (Große Kammer) gutachtlich wie folgt:</P>
              </PREAMBLE.GEN>
              <ENACTING.TERMS.CJT>
                  <P>Das Einverständnis zum Beitritt eines Drittstaats...</P>
              </ENACTING.TERMS.CJT>
               <FINAL><P>Unterschriften</P></FINAL>
          </CONTENTS.OPINION>
      </OPINION>
      """;
}
