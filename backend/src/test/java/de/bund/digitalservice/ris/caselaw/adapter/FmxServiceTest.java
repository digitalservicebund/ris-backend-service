package de.bund.digitalservice.ris.caselaw.adapter;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.AttachmentDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.AttachmentRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDocumentationUnitRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentationUnitDTO;
import de.bund.digitalservice.ris.caselaw.adapter.exception.FmxTransformationException;
import de.bund.digitalservice.ris.caselaw.domain.CoreData;
import de.bund.digitalservice.ris.caselaw.domain.DocumentTypeRepository;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnit;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnitRepository;
import de.bund.digitalservice.ris.caselaw.domain.FmxRepository;
import de.bund.digitalservice.ris.caselaw.domain.LongTexts;
import de.bund.digitalservice.ris.caselaw.domain.court.Court;
import de.bund.digitalservice.ris.caselaw.domain.court.CourtRepository;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.documenttype.DocumentType;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@Import({
  FmxService.class,
  DocumentationUnitRepository.class,
  CourtRepository.class,
  DocumentTypeRepository.class,
  FmxRepository.class,
  AttachmentRepository.class,
  DatabaseDocumentationUnitRepository.class,
  EurlexRetrievalService.class,
  XmlUtilService.class
})
class FmxServiceTest {

  @Autowired private FmxService service;

  @MockitoBean DocumentationUnitRepository documentationUnitRepository;
  @MockitoBean CourtRepository courtRepository;
  @MockitoBean DocumentTypeRepository documentTypeRepository;
  @MockitoBean FmxRepository fmxRepository;
  @MockitoBean AttachmentRepository attachmentRepository;
  @MockitoBean DatabaseDocumentationUnitRepository databaseDocumentationUnitRepository;
  @MockitoBean EurlexRetrievalService retrievalService;
  @MockitoBean XmlUtilService xmlUtilService;

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
            .coreData(CoreData.builder().build())
            .longTexts(LongTexts.builder().build())
            .build();

    doReturn(xml)
        .when(retrievalService)
        .getDocumentFromEurlex("https://publications.europa.eu/resource/celex/" + celexNumber);
    ArgumentCaptor<AttachmentDTO> attachmentCaptor = ArgumentCaptor.forClass(AttachmentDTO.class);
    DocumentationUnitDTO documentationUnitDTO = mock(DocumentationUnitDTO.class);
    doReturn(Optional.of(documentationUnitDTO))
        .when(databaseDocumentationUnitRepository)
        .findById(id);

    service.getDataFromEurlex(celexNumber, documentationUnit);

    verify(attachmentRepository).save(attachmentCaptor.capture());
    AttachmentDTO attachmentDTO = attachmentCaptor.getValue();
    assertThat(attachmentDTO.getFilename()).isEqualTo("Originalentscheidung");
    assertThat(attachmentDTO.getFormat()).isEqualTo("fmx");
  }

  @Test
  void shouldExtractMetadata() {
    String celexNumber = "CELEX1234";
    UUID id = UUID.randomUUID();
    DocumentationUnit documentationUnit =
        DocumentationUnit.builder()
            .uuid(id)
            .coreData(CoreData.builder().build())
            .longTexts(LongTexts.builder().build())
            .build();

    doReturn(xml)
        .when(retrievalService)
        .getDocumentFromEurlex("https://publications.europa.eu/resource/celex/" + celexNumber);
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

    service.getDataFromEurlex(celexNumber, documentationUnit);

    verify(documentationUnitRepository).save(docUnitCaptor.capture());
    DocumentationUnit savedDocUnit = docUnitCaptor.getValue();
    assertThat(savedDocUnit.coreData().court().label()).isEqualTo("EuGH");
    assertThat(savedDocUnit.coreData().fileNumbers().getFirst()).isEqualTo("C-303/22");
    assertThat(savedDocUnit.coreData().decisionDate()).isEqualTo(LocalDate.of(2017, 2, 14));
    assertThat(savedDocUnit.coreData().ecli()).isEqualTo("ECLI:EU:C:2024:60");
    assertThat(savedDocUnit.coreData().celexNumber()).isEqualTo("62022CJ0303");
    assertThat(savedDocUnit.coreData().documentType().label()).isEqualTo("Urteil");
  }

  @Test
  void shouldExtractLongTexts() {
    String celexNumber = "CELEX1234";
    UUID id = UUID.randomUUID();
    DocumentationUnit documentationUnit =
        DocumentationUnit.builder()
            .uuid(id)
            .coreData(CoreData.builder().build())
            .longTexts(LongTexts.builder().build())
            .build();
    doReturn(xml)
        .when(retrievalService)
        .getDocumentFromEurlex("https://publications.europa.eu/resource/celex/" + celexNumber);
    ArgumentCaptor<DocumentationUnit> docUnitCaptor =
        ArgumentCaptor.forClass(DocumentationUnit.class);
    DocumentationUnitDTO documentationUnitDTO = mock(DocumentationUnitDTO.class);
    doReturn(Optional.of(documentationUnitDTO))
        .when(databaseDocumentationUnitRepository)
        .findById(id);

    service.getDataFromEurlex(celexNumber, documentationUnit);

    verify(documentationUnitRepository).save(docUnitCaptor.capture());
    DocumentationUnit savedDocUnit = docUnitCaptor.getValue();
    assertThat(savedDocUnit.longTexts().reasons()).contains("Urteil");
    assertThat(savedDocUnit.longTexts().reasons()).contains("Unterschriften");
    assertThat(savedDocUnit.longTexts().reasons())
        .doesNotContain(
            "Aus diesen Gründen hat der Gerichtshof (Vierte Kammer) für Recht erkannt:");
    assertThat(savedDocUnit.longTexts().tenor())
        .contains("Aus diesen Gründen hat der Gerichtshof (Vierte Kammer) für Recht erkannt:");
  }

  @Test
  void emptyXml_shouldThrowTransformationException() {
    String celexNumber = "CELEX1234";
    UUID id = UUID.randomUUID();
    DocumentationUnit documentationUnit =
        DocumentationUnit.builder()
            .uuid(id)
            .coreData(CoreData.builder().build())
            .longTexts(LongTexts.builder().build())
            .build();
    doReturn("")
        .when(retrievalService)
        .getDocumentFromEurlex("https://publications.europa.eu/resource/celex/" + celexNumber);

    assertThatExceptionOfType(FmxTransformationException.class)
        .isThrownBy(() -> service.getDataFromEurlex(celexNumber, documentationUnit))
        .withMessageContaining("FMX file has no content.");
  }

  private String xml =
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
}
