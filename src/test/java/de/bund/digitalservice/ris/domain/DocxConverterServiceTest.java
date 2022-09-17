package de.bund.digitalservice.ris.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.bund.digitalservice.ris.config.ConverterConfig;
import de.bund.digitalservice.ris.domain.docx.BorderNumber;
import de.bund.digitalservice.ris.domain.docx.DocumentUnitDocx;
import de.bund.digitalservice.ris.domain.docx.Docx2Html;
import de.bund.digitalservice.ris.domain.docx.DocxImagePart;
import de.bund.digitalservice.ris.domain.docx.NumberingList.DocumentUnitNumberingListNumberFormat;
import de.bund.digitalservice.ris.domain.docx.NumberingListEntry;
import de.bund.digitalservice.ris.domain.docx.NumberingListEntryIndex;
import de.bund.digitalservice.ris.domain.docx.ParagraphElement;
import de.bund.digitalservice.ris.domain.docx.RunTextElement;
import de.bund.digitalservice.ris.domain.docx.TableCellElement;
import de.bund.digitalservice.ris.domain.docx.TableElement;
import de.bund.digitalservice.ris.domain.docx.TableRowElement;
import de.bund.digitalservice.ris.utils.DocxConverter;
import de.bund.digitalservice.ris.utils.DocxConverterException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import javax.xml.parsers.DocumentBuilderFactory;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.Part;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.Parts;
import org.docx4j.openpackaging.parts.WordprocessingML.BinaryPartAbstractImage;
import org.docx4j.openpackaging.parts.WordprocessingML.ImageJpegPart;
import org.docx4j.openpackaging.parts.WordprocessingML.ImagePngPart;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.openpackaging.parts.WordprocessingML.MetafileEmfPart;
import org.docx4j.openpackaging.parts.WordprocessingML.StyleDefinitionsPart;
import org.docx4j.relationships.Relationship;
import org.docx4j.wml.JcEnumeration;
import org.docx4j.wml.Style;
import org.docx4j.wml.Styles;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.test.StepVerifier;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.async.AsyncResponseTransformer;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.S3Object;

@ExtendWith(SpringExtension.class)
@Import({DocxConverterService.class, ConverterConfig.class})
class DocxConverterServiceTest {

  @Autowired DocxConverterService service;

  @MockBean S3AsyncClient client;

  @Mock WordprocessingMLPackage mlPackage;

  @Mock ResponseBytes<GetObjectResponse> responseBytes;

  @Autowired DocumentBuilderFactory documentBuilderFactory;

  @MockBean DocxConverter converter;

  @Captor ArgumentCaptor<Map<String, Style>> styleMapCaptor;

  @Captor ArgumentCaptor<Map<String, DocxImagePart>> imageMapCaptor;

  @Test
  void testGetOriginalText() {
    MainDocumentPart mockedMainDocumentPart = mock(MainDocumentPart.class);
    when(mlPackage.getMainDocumentPart()).thenReturn(mockedMainDocumentPart);
    when(mlPackage.getMainDocumentPart().getXML())
        .thenReturn("<document><p><t>text</t></p></document>");

    var result = service.getOriginalText(mlPackage);

    assertEquals("text", result);
  }

  @Test
  void testGetDocxFiles() {
    ListObjectsV2Response response =
        ListObjectsV2Response.builder()
            .contents(S3Object.builder().key("test.docx").build())
            .build();
    when(client.listObjectsV2(any(ListObjectsV2Request.class)))
        .thenReturn(CompletableFuture.completedFuture(response));

    StepVerifier.create(service.getDocxFiles())
        .consumeNextWith(
            responseEntity -> {
              assertNotNull(responseEntity);
              assertEquals(1, Objects.requireNonNull(responseEntity.getBody()).size());
              assertEquals("test.docx", responseEntity.getBody().get(0));
              assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
            })
        .verifyComplete();
  }

  @Test
  void testGetHtml() {
    new TestDocumentGenerator(client, responseBytes, mlPackage, converter)
        .addContent("1", generateText("test"))
        .addContent("2", generateBorderNumber("1"))
        .addContent("3", generateText("border number 1"))
        .addContent("4", generateBorderNumber("2"))
        .addContent("5", generateText("border number 2"))
        .addContent("6", generateTable("table content"))
        .generate();

    try (MockedStatic<WordprocessingMLPackage> mockedMLPackageStatic =
        mockStatic(WordprocessingMLPackage.class)) {
      mockedMLPackageStatic
          .when(() -> WordprocessingMLPackage.load(any(InputStream.class)))
          .thenReturn(mlPackage);

      StepVerifier.create(service.getHtml("test.docx"))
          .consumeNextWith(
              docx2Html -> {
                assertNotNull(docx2Html);
                assertEquals(
                    "<p>test</p>"
                        + "<border-number><number>1</number><content><p>border number 1</p></content></border-number>"
                        + "<border-number><number>2</number><content><p>border number 2</p></content></border-number>"
                        + "<table style=\"border-collapse: collapse;\"><tr><td style=\"min-width: 5px; padding: 5px;\"><p>table content</p></td></tr></table>",
                    docx2Html.content());
              })
          .verifyComplete();
    }
  }

  @Test
  void testGetHtml_withStyleInformation() {
    when(client.getObject(any(GetObjectRequest.class), any(AsyncResponseTransformer.class)))
        .thenReturn(CompletableFuture.completedFuture(responseBytes));
    when(responseBytes.asInputStream()).thenReturn(new ByteArrayInputStream(new byte[] {}));
    MainDocumentPart mainDocumentPart = mock(MainDocumentPart.class);
    StyleDefinitionsPart styleDefinitionsPart = mock(StyleDefinitionsPart.class);
    Styles styles = mock(Styles.class);
    Style style = mock(Style.class);
    when(style.getStyleId()).thenReturn("test-style");
    when(styles.getStyle()).thenReturn(List.of(style));
    when(styleDefinitionsPart.getJaxbElement()).thenReturn(styles);
    when(mainDocumentPart.getStyleDefinitionsPart()).thenReturn(styleDefinitionsPart);
    when(mlPackage.getMainDocumentPart()).thenReturn(mainDocumentPart);
    when(mainDocumentPart.getContent()).thenReturn(Collections.emptyList());

    try (MockedStatic<WordprocessingMLPackage> mockedMLPackageStatic =
        mockStatic(WordprocessingMLPackage.class)) {
      mockedMLPackageStatic
          .when(() -> WordprocessingMLPackage.load(any(InputStream.class)))
          .thenReturn(mlPackage);

      StepVerifier.create(service.getHtml("test.docx"))
          .consumeNextWith(Assertions::assertNotNull)
          .verifyComplete();

      verify(converter).setStyles(styleMapCaptor.capture());
      assertTrue(styleMapCaptor.getValue().containsKey("test-style"));
      assertEquals(style, styleMapCaptor.getValue().get("test-style"));
    }
  }

  @Test
  void testGetHtml_withImages() throws InvalidFormatException, IOException {
    when(client.getObject(any(GetObjectRequest.class), any(AsyncResponseTransformer.class)))
        .thenReturn(CompletableFuture.completedFuture(responseBytes));
    when(responseBytes.asInputStream()).thenReturn(new ByteArrayInputStream(new byte[] {}));
    MainDocumentPart mainDocumentPart = mock(MainDocumentPart.class);
    Parts parts = mock(Parts.class);
    HashMap<PartName, Part> partMap = new HashMap<>();
    PartName partName = new PartName("/emfPart");
    BinaryPartAbstractImage part = new MetafileEmfPart(partName);
    Relationship relationship = new Relationship();
    relationship.setId("emfPart");
    part.getSourceRelationships().add(relationship);
    InputStream emfStream =
        DocxConverterServiceTest.class.getClassLoader().getResourceAsStream("test.emf");
    part.setBinaryData(emfStream.readAllBytes());
    partMap.put(partName, part);
    partName = new PartName("/jpegPart");
    part = new ImageJpegPart(partName);
    relationship = new Relationship();
    relationship.setId("jpegPart");
    part.getSourceRelationships().add(relationship);
    part.setBinaryData(new byte[] {});
    partMap.put(partName, part);
    partName = new PartName("/pngPart");
    part = new ImagePngPart(partName);
    relationship = new Relationship();
    relationship.setId("pngPart");
    part.getSourceRelationships().add(relationship);
    part.setBinaryData(new byte[] {});
    partMap.put(partName, part);
    when(parts.getParts()).thenReturn(partMap);
    when(mlPackage.getParts()).thenReturn(parts);
    when(mlPackage.getMainDocumentPart()).thenReturn(mainDocumentPart);
    when(mainDocumentPart.getContent()).thenReturn(Collections.emptyList());

    try (MockedStatic<WordprocessingMLPackage> mockedMLPackageStatic =
        mockStatic(WordprocessingMLPackage.class)) {
      mockedMLPackageStatic
          .when(() -> WordprocessingMLPackage.load(any(InputStream.class)))
          .thenReturn(mlPackage);

      StepVerifier.create(service.getHtml("test.docx"))
          .consumeNextWith(Assertions::assertNotNull)
          .verifyComplete();

      verify(converter).setImages(imageMapCaptor.capture());
      Map<String, DocxImagePart> imageMapValue = imageMapCaptor.getValue();
      assertEquals(3, imageMapValue.values().size());
      assertTrue(imageMapValue.containsKey("emfPart"));
      assertEquals("image/png", imageMapValue.get("emfPart").contentType());
      assertTrue(imageMapValue.containsKey("jpegPart"));
      assertEquals("image/jpeg", imageMapValue.get("jpegPart").contentType());
      assertTrue(imageMapValue.containsKey("pngPart"));
      assertEquals("image/png", imageMapValue.get("pngPart").contentType());
    }
  }

  @Test
  void testGetHtml_withEmptyBorderNumber() {
    new TestDocumentGenerator(client, responseBytes, mlPackage, converter)
        .addContent("1", generateText("test"))
        .addContent("2", generateBorderNumber("1"))
        .addContent("3", generateBorderNumber("2"))
        .addContent("4", generateText("border number 2"))
        .generate();

    try (MockedStatic<WordprocessingMLPackage> mockedMLPackageStatic =
        mockStatic(WordprocessingMLPackage.class)) {
      mockedMLPackageStatic
          .when(() -> WordprocessingMLPackage.load(any(InputStream.class)))
          .thenReturn(mlPackage);

      StepVerifier.create(service.getHtml("test.docx"))
          .consumeNextWith(
              docx2Html -> {
                assertNotNull(docx2Html);
                assertEquals(
                    "<p>test</p><border-number><number>1</number></border-number>"
                        + "<border-number><number>2</number><content><p>border number 2</p>"
                        + "</content></border-number>",
                    docx2Html.content());
              })
          .verifyComplete();
    }
  }

  @Nested
  @DisplayName("Test render List HTML")
  class TestRenderListHTML {
    private String lvlText;
    private String startVal;
    private String restartNumberingAfterBreak;
    private String color;
    private String fontStyle;
    private String fontSize;
    private boolean lvlPicBullet;
    private boolean isLgl;
    private DocumentUnitNumberingListNumberFormat numberFormat;
    private String iLvl;
    private JcEnumeration lvlJc;
    private String suff;
    private List<NumberingListEntry> entries;

    @BeforeEach
    void setUp() {
      lvlText = "";
      startVal = "1";
      restartNumberingAfterBreak = "";
      color = "";
      fontStyle = "";
      fontSize = "";
      lvlPicBullet = false;
      isLgl = false;
      numberFormat = DocumentUnitNumberingListNumberFormat.BULLET;
      iLvl = "0";
      lvlJc = JcEnumeration.RIGHT;
      suff = "space";
      entries = new ArrayList<>();
    }

    NumberingListEntryIndex createNumberingListEntryIndex() {
      return new NumberingListEntryIndex(
          lvlText,
          startVal,
          restartNumberingAfterBreak,
          color,
          fontStyle,
          fontSize,
          lvlPicBullet,
          isLgl,
          numberFormat,
          iLvl,
          lvlJc,
          suff);
    }

    @Test
    void testGetHtml_withTwoNumberingListEntries() {
      entries.add(
          (NumberingListEntry)
              generateNumberingListEntry("bullet list entry 1", createNumberingListEntryIndex()));
      entries.add(
          (NumberingListEntry)
              generateNumberingListEntry("bullet list entry 2", createNumberingListEntryIndex()));
      numberFormat = DocumentUnitNumberingListNumberFormat.DECIMAL;
      entries.add(
          (NumberingListEntry)
              generateNumberingListEntry("decimal list entry 1", createNumberingListEntryIndex()));
      entries.add(
          (NumberingListEntry)
              generateNumberingListEntry("decimal list entry 2", createNumberingListEntryIndex()));

      TestDocumentGenerator generator =
          new TestDocumentGenerator(client, responseBytes, mlPackage, converter);
      int index = 0;
      for (DocumentUnitDocx entry : entries) {
        generator.addContent(String.valueOf(++index), entry);
      }
      generator.generate();

      try (MockedStatic<WordprocessingMLPackage> mockedMLPackageStatic =
          mockStatic(WordprocessingMLPackage.class)) {
        mockedMLPackageStatic
            .when(() -> WordprocessingMLPackage.load(any(InputStream.class)))
            .thenReturn(mlPackage);

        StepVerifier.create(service.getHtml("test.docx"))
            .consumeNextWith(
                docx2Html -> {
                  assertNotNull(docx2Html);
                  assertEquals(
                      "<ul style=\"list-style-type:disc;\">"
                          + "<li style=\"display:table-row\">"
                          + "<p style=\"display:table-cell;text-align:right;\"><span style=\"\">&#9679;</span><span> </span></p>"
                          + "<p>bullet list entry 1</p>"
                          + "</li>"
                          + "<li style=\"display:table-row\">"
                          + "<p style=\"display:table-cell;text-align:right;\"><span style=\"\">&#9679;</span><span> </span></p>"
                          + "<p>bullet list entry 2</p>"
                          + "</li>"
                          + "</ul>"
                          + "<ol style=\"list-style-type:decimal;\">"
                          + "<li style=\"display:table-row\">"
                          + "<p style=\"display:table-cell;text-align:right;\"><span style=\"\"></span><span> </span></p>"
                          + "<p>decimal list entry 1</p>"
                          + "</li>"
                          + "<li style=\"display:table-row\">"
                          + "<p style=\"display:table-cell;text-align:right;\"><span style=\"\"></span><span> </span></p>"
                          + "<p>decimal list entry 2</p>"
                          + "</li>"
                          + "</ol>",
                      docx2Html.content());
                })
            .verifyComplete();
      }
    }

    @Test
    void testGetHtml_withTwoNumberingListEntriesAndMiddleText() {
      numberFormat = DocumentUnitNumberingListNumberFormat.UPPER_LETTER;
      entries.add(
          (NumberingListEntry)
              generateNumberingListEntry("list entry 1", createNumberingListEntryIndex()));
      TestDocumentGenerator generator =
          new TestDocumentGenerator(client, responseBytes, mlPackage, converter);
      int index = 0;
      for (DocumentUnitDocx entry : entries) {
        generator.addContent(String.valueOf(++index), entry);
      }
      generator.addContent(String.valueOf(++index), generateText("Middle Text"));
      entries = new ArrayList<>();
      numberFormat = DocumentUnitNumberingListNumberFormat.LOWER_LETTER;
      entries.add(
          (NumberingListEntry)
              generateNumberingListEntry("list entry 1", createNumberingListEntryIndex()));
      for (DocumentUnitDocx entry : entries) {
        generator.addContent(String.valueOf(++index), entry);
      }
      generator.addContent(String.valueOf(++index), generateText("Middle Text"));
      entries = new ArrayList<>();
      numberFormat = DocumentUnitNumberingListNumberFormat.UPPER_ROMAN;
      entries.add(
          (NumberingListEntry)
              generateNumberingListEntry("list entry 1", createNumberingListEntryIndex()));
      for (DocumentUnitDocx entry : entries) {
        generator.addContent(String.valueOf(++index), entry);
      }
      generator.addContent(String.valueOf(++index), generateText("Middle Text"));
      entries = new ArrayList<>();
      numberFormat = DocumentUnitNumberingListNumberFormat.LOWER_ROMAN;
      entries.add(
          (NumberingListEntry)
              generateNumberingListEntry("list entry 1", createNumberingListEntryIndex()));
      for (DocumentUnitDocx entry : entries) {
        generator.addContent(String.valueOf(++index), entry);
      }
      generator.addContent(String.valueOf(++index), generateText("Middle Text"));
      entries = new ArrayList<>();
      numberFormat = DocumentUnitNumberingListNumberFormat.DECIMAL;
      entries.add(
          (NumberingListEntry)
              generateNumberingListEntry("list entry 1", createNumberingListEntryIndex()));
      for (DocumentUnitDocx entry : entries) {
        generator.addContent(String.valueOf(++index), entry);
      }
      generator.generate();

      try (MockedStatic<WordprocessingMLPackage> mockedMLPackageStatic =
          mockStatic(WordprocessingMLPackage.class)) {
        mockedMLPackageStatic
            .when(() -> WordprocessingMLPackage.load(any(InputStream.class)))
            .thenReturn(mlPackage);

        StepVerifier.create(service.getHtml("test.docx"))
            .consumeNextWith(
                docx2Html -> {
                  assertNotNull(docx2Html);
                  assertEquals(
                      "<ol style=\"list-style-type:lower-latin;\">"
                          + "<li style=\"display:table-row\">"
                          + "<p style=\"display:table-cell;text-align:right;\"><span style=\"\"></span><span> </span></p><p>list entry 1</p>"
                          + "</li></ol>"
                          + "<p>Middle Text</p>"
                          + "<ol style=\"list-style-type:upper-latin;\">"
                          + "<li style=\"display:table-row\">"
                          + "<p style=\"display:table-cell;text-align:right;\"><span style=\"\"></span><span> </span></p><p>list entry 1</p>"
                          + "</li></ol>"
                          + "<p>Middle Text</p>"
                          + "<ol style=\"list-style-type:upper-roman;\">"
                          + "<li style=\"display:table-row\">"
                          + "<p style=\"display:table-cell;text-align:right;\"><span style=\"\"></span><span> </span></p><p>list entry 1</p>"
                          + "</li></ol>"
                          + "<p>Middle Text</p>"
                          + "<ol style=\"list-style-type:lower-roman\">"
                          + "<li style=\"display:table-row\">"
                          + "<p style=\"display:table-cell;text-align:right;\"><span style=\"\"></span><span> </span></p><p>list entry 1</p>"
                          + "</li></ol>"
                          + "<p>Middle Text</p>"
                          + "<ol style=\"list-style-type:decimal;\">"
                          + "<li style=\"display:table-row\">"
                          + "<p style=\"display:table-cell;text-align:right;\"><span style=\"\"></span><span> </span></p><p>list entry 1</p>"
                          + "</li></ol>",
                      docx2Html.content());
                })
            .verifyComplete();
      }
    }

    @Test
    void testGetHtml_listWithStyle() {
      fontStyle = "Symbol";
      fontSize = "28";
      color = "000000";
      entries.add(
          (NumberingListEntry)
              generateNumberingListEntry("bullet list entry 1", createNumberingListEntryIndex()));
      entries.add(
          (NumberingListEntry)
              generateNumberingListEntry("bullet list entry 2", createNumberingListEntryIndex()));
      entries.add(
          (NumberingListEntry)
              generateNumberingListEntry("bullet list entry 3", createNumberingListEntryIndex()));
      TestDocumentGenerator generator =
          new TestDocumentGenerator(client, responseBytes, mlPackage, converter);
      int index = 0;
      for (DocumentUnitDocx entry : entries) {
        generator.addContent(String.valueOf(++index), entry);
      }
      generator.generate();

      try (MockedStatic<WordprocessingMLPackage> mockedMLPackageStatic =
          mockStatic(WordprocessingMLPackage.class)) {
        mockedMLPackageStatic
            .when(() -> WordprocessingMLPackage.load(any(InputStream.class)))
            .thenReturn(mlPackage);

        StepVerifier.create(service.getHtml("test.docx"))
            .consumeNextWith(
                docx2Html -> {
                  assertNotNull(docx2Html);
                  assertEquals(
                      "<ul style=\"list-style-type:none;display:table;\">"
                          + "<li style=\"display:table-row\">"
                          + "<p style=\"display:table-cell;text-align:right;\"><span style=\"font-family:Symbol;font-size:14pt;color:#000000;\">&#9679;</span><span> </span></p>"
                          + "<p>bullet list entry 1</p>"
                          + "</li>"
                          + "<li style=\"display:table-row\">"
                          + "<p style=\"display:table-cell;text-align:right;\"><span style=\"font-family:Symbol;font-size:14pt;color:#000000;\">&#9679;</span><span> </span></p>"
                          + "<p>bullet list entry 2</p>"
                          + "</li>"
                          + "<li style=\"display:table-row\">"
                          + "<p style=\"display:table-cell;text-align:right;\"><span style=\"font-family:Symbol;font-size:14pt;color:#000000;\">&#9679;</span><span> </span></p>"
                          + "<p>bullet list entry 3</p>"
                          + "</li>"
                          + "</ul>",
                      docx2Html.content());
                })
            .verifyComplete();
      }
    }

    @Test
    void testGetHtml_picBulletList() {
      lvlPicBullet = true;
      entries.add(
          (NumberingListEntry)
              generateNumberingListEntry("bullet list entry 1", createNumberingListEntryIndex()));
      TestDocumentGenerator generator =
          new TestDocumentGenerator(client, responseBytes, mlPackage, converter);
      int index = 0;
      for (DocumentUnitDocx entry : entries) {
        generator.addContent(String.valueOf(++index), entry);
      }
      generator.generate();

      try (MockedStatic<WordprocessingMLPackage> mockedMLPackageStatic =
          mockStatic(WordprocessingMLPackage.class)) {
        mockedMLPackageStatic
            .when(() -> WordprocessingMLPackage.load(any(InputStream.class)))
            .thenReturn(mlPackage);

        StepVerifier.create(service.getHtml("test.docx"))
            .consumeNextWith(
                docx2Html -> {
                  assertNotNull(docx2Html);
                  assertEquals(
                      "<ul style=\"list-style-type:disc;\">"
                          + "<li style=\"display:table-row\">"
                          + "<p style=\"display:table-cell;text-align:right;\"><span style=\"\">&#9679;</span><span> </span></p>"
                          + "<p>bullet list entry 1</p>"
                          + "</li></ul>",
                      docx2Html.content());
                })
            .verifyComplete();
      }
    }

    @Test
    void testGetHtml_LglList() {
      isLgl = true;
      entries.add(
          (NumberingListEntry)
              generateNumberingListEntry("bullet list entry 1", createNumberingListEntryIndex()));
      TestDocumentGenerator generator =
          new TestDocumentGenerator(client, responseBytes, mlPackage, converter);
      int index = 0;
      for (DocumentUnitDocx entry : entries) {
        generator.addContent(String.valueOf(++index), entry);
      }
      generator.generate();

      try (MockedStatic<WordprocessingMLPackage> mockedMLPackageStatic =
          mockStatic(WordprocessingMLPackage.class)) {
        mockedMLPackageStatic
            .when(() -> WordprocessingMLPackage.load(any(InputStream.class)))
            .thenReturn(mlPackage);

        StepVerifier.create(service.getHtml("test.docx"))
            .consumeNextWith(
                docx2Html -> {
                  assertNotNull(docx2Html);
                  assertEquals(
                      "<ul style=\"list-style-type:disc;\"><li style=\"list-style-type:decimal\"><p>bullet list entry 1</p></li></ul>",
                      docx2Html.content());
                })
            .verifyComplete();
      }
    }

    @Test
    void testGetHtml_IndexLeftAlign() {
      lvlJc = JcEnumeration.LEFT;
      entries.add(
          (NumberingListEntry)
              generateNumberingListEntry("bullet list entry 1", createNumberingListEntryIndex()));
      TestDocumentGenerator generator =
          new TestDocumentGenerator(client, responseBytes, mlPackage, converter);
      int index = 0;
      for (DocumentUnitDocx entry : entries) {
        generator.addContent(String.valueOf(++index), entry);
      }
      generator.generate();

      try (MockedStatic<WordprocessingMLPackage> mockedMLPackageStatic =
          mockStatic(WordprocessingMLPackage.class)) {
        mockedMLPackageStatic
            .when(() -> WordprocessingMLPackage.load(any(InputStream.class)))
            .thenReturn(mlPackage);

        StepVerifier.create(service.getHtml("test.docx"))
            .consumeNextWith(
                docx2Html -> {
                  assertNotNull(docx2Html);
                  assertEquals(
                      "<ul style=\"list-style-type:disc;\">"
                          + "<li style=\"display:table-row\">"
                          + "<p style=\"display:table-cell;text-align:left;\"><span style=\"\">&#9679;</span><span> </span></p>"
                          + "<p>bullet list entry 1</p>"
                          + "</li></ul>",
                      docx2Html.content());
                })
            .verifyComplete();
      }
    }

    @Test
    void testGetHtml_IndexCenterAlign() {
      lvlJc = JcEnumeration.CENTER;
      entries.add(
          (NumberingListEntry)
              generateNumberingListEntry("bullet list entry 1", createNumberingListEntryIndex()));
      TestDocumentGenerator generator =
          new TestDocumentGenerator(client, responseBytes, mlPackage, converter);
      int index = 0;
      for (DocumentUnitDocx entry : entries) {
        generator.addContent(String.valueOf(++index), entry);
      }
      generator.generate();

      try (MockedStatic<WordprocessingMLPackage> mockedMLPackageStatic =
          mockStatic(WordprocessingMLPackage.class)) {
        mockedMLPackageStatic
            .when(() -> WordprocessingMLPackage.load(any(InputStream.class)))
            .thenReturn(mlPackage);

        StepVerifier.create(service.getHtml("test.docx"))
            .consumeNextWith(
                docx2Html -> {
                  assertNotNull(docx2Html);
                  assertEquals(
                      "<ul style=\"list-style-type:disc;\">"
                          + "<li style=\"display:table-row\">"
                          + "<p style=\"display:table-cell;text-align:center;\"><span style=\"\">&#9679;</span><span> </span></p>"
                          + "<p>bullet list entry 1</p>"
                          + "</li></ul>",
                      docx2Html.content());
                })
            .verifyComplete();
      }
    }

    @Test
    void testGetHtml_IndexDistanceNothing() {
      suff = "nothing";
      entries.add(
          (NumberingListEntry)
              generateNumberingListEntry("bullet list entry 1", createNumberingListEntryIndex()));
      TestDocumentGenerator generator =
          new TestDocumentGenerator(client, responseBytes, mlPackage, converter);
      int index = 0;
      for (DocumentUnitDocx entry : entries) {
        generator.addContent(String.valueOf(++index), entry);
      }
      generator.generate();

      try (MockedStatic<WordprocessingMLPackage> mockedMLPackageStatic =
          mockStatic(WordprocessingMLPackage.class)) {
        mockedMLPackageStatic
            .when(() -> WordprocessingMLPackage.load(any(InputStream.class)))
            .thenReturn(mlPackage);

        StepVerifier.create(service.getHtml("test.docx"))
            .consumeNextWith(
                docx2Html -> {
                  assertNotNull(docx2Html);
                  assertEquals(
                      "<ul style=\"list-style-type:disc;\">"
                          + "<li style=\"display:table-row\">"
                          + "<p style=\"display:table-cell;text-align:right;\"><span style=\"\">&#9679;</span><span></span></p>"
                          + "<p>bullet list entry 1</p>"
                          + "</li></ul>",
                      docx2Html.content());
                })
            .verifyComplete();
      }
    }

    @Test
    void testGetHtml_emptyStartValue() {
      startVal = "";
      lvlText = "%1.";
      numberFormat = DocumentUnitNumberingListNumberFormat.DECIMAL;
      entries.add(
          (NumberingListEntry)
              generateNumberingListEntry("bullet list entry 1", createNumberingListEntryIndex()));
      TestDocumentGenerator generator =
          new TestDocumentGenerator(client, responseBytes, mlPackage, converter);
      int index = 0;
      for (DocumentUnitDocx entry : entries) {
        generator.addContent(String.valueOf(++index), entry);
      }
      generator.generate();

      try (MockedStatic<WordprocessingMLPackage> mockedMLPackageStatic =
          mockStatic(WordprocessingMLPackage.class)) {
        mockedMLPackageStatic
            .when(() -> WordprocessingMLPackage.load(any(InputStream.class)))
            .thenReturn(mlPackage);

        StepVerifier.create(service.getHtml("test.docx"))
            .consumeNextWith(
                docx2Html -> {
                  assertNotNull(docx2Html);
                  assertEquals(
                      "<ol style=\"list-style-type:none;display:table;\">"
                          + "<li style=\"display:table-row\">"
                          + "<p style=\"display:table-cell;text-align:right;\"><span style=\"\">1.</span><span> </span></p>"
                          + "<p>bullet list entry 1</p>"
                          + "</li></ol>",
                      docx2Html.content());
                })
            .verifyComplete();
      }
    }

    @Test
    void testGetHtml_IndexDistanceTab() {
      suff = "tab";
      entries.add(
          (NumberingListEntry)
              generateNumberingListEntry("bullet list entry 1", createNumberingListEntryIndex()));
      TestDocumentGenerator generator =
          new TestDocumentGenerator(client, responseBytes, mlPackage, converter);
      int index = 0;
      for (DocumentUnitDocx entry : entries) {
        generator.addContent(String.valueOf(++index), entry);
      }
      generator.generate();

      try (MockedStatic<WordprocessingMLPackage> mockedMLPackageStatic =
          mockStatic(WordprocessingMLPackage.class)) {
        mockedMLPackageStatic
            .when(() -> WordprocessingMLPackage.load(any(InputStream.class)))
            .thenReturn(mlPackage);

        StepVerifier.create(service.getHtml("test.docx"))
            .consumeNextWith(
                docx2Html -> {
                  assertNotNull(docx2Html);
                  assertEquals(
                      "<ul style=\"list-style-type:disc;\">"
                          + "<li style=\"display:table-row\">"
                          + "<p style=\"display:table-cell;text-align:right;\">"
                          + "<span style=\"\">&#9679;</span><span>&emsp;</span></p>"
                          + "<p>bullet list entry 1</p>"
                          + "</li></ul>",
                      docx2Html.content());
                })
            .verifyComplete();
      }
    }

    @Test
    void testGetHtml_restartIndexAfterBreak() {
      numberFormat = DocumentUnitNumberingListNumberFormat.DECIMAL;
      iLvl = "0";
      lvlText = "%1.";
      entries.add(
          (NumberingListEntry)
              generateNumberingListEntry("list entry 1", createNumberingListEntryIndex()));
      iLvl = "1";
      lvlText = "%2.";
      entries.add(
          (NumberingListEntry)
              generateNumberingListEntry("list entry 1", createNumberingListEntryIndex()));
      iLvl = "0";
      lvlText = "%1.";
      entries.add(
          (NumberingListEntry)
              generateNumberingListEntry("list entry 1", createNumberingListEntryIndex()));
      iLvl = "1";
      lvlText = "%2.";
      restartNumberingAfterBreak = "5";
      entries.add(
          (NumberingListEntry)
              generateNumberingListEntry("list entry 1", createNumberingListEntryIndex()));
      TestDocumentGenerator generator =
          new TestDocumentGenerator(client, responseBytes, mlPackage, converter);
      int index = 0;
      for (DocumentUnitDocx entry : entries) {
        generator.addContent(String.valueOf(++index), entry);
      }
      generator.generate();

      try (MockedStatic<WordprocessingMLPackage> mockedMLPackageStatic =
          mockStatic(WordprocessingMLPackage.class)) {
        mockedMLPackageStatic
            .when(() -> WordprocessingMLPackage.load(any(InputStream.class)))
            .thenReturn(mlPackage);

        StepVerifier.create(service.getHtml("test.docx"))
            .consumeNextWith(
                docx2Html -> {
                  assertNotNull(docx2Html);
                  assertEquals(
                      "<ol style=\"list-style-type:none;display:table;\">"
                          + "<li style=\"display:table-row\">"
                          + "<p style=\"display:table-cell;text-align:right;\"><span style=\"\">1.</span><span> </span></p>"
                          + "<p>list entry 1</p>"
                          + "</li>"
                          + "<ol style=\"list-style-type:none;display:table;\">"
                          + "<li style=\"display:table-row\">"
                          + "<p style=\"display:table-cell;text-align:right;\"><span style=\"\">1.</span><span> </span></p>"
                          + "<p>list entry 1</p>"
                          + "</li>"
                          + "</ol>"
                          + "<li style=\"display:table-row\">"
                          + "<p style=\"display:table-cell;text-align:right;\"><span style=\"\">2.</span><span> </span></p>"
                          + "<p>list entry 1</p>"
                          + "</li>"
                          + "<ol style=\"list-style-type:none;display:table;\">"
                          + "<li style=\"display:table-row\">"
                          + "<p style=\"display:table-cell;text-align:right;\"><span style=\"\">6.</span><span> </span></p>"
                          + "<p>list entry 1</p>"
                          + "</li>"
                          + "</ol>"
                          + "</ol>",
                      docx2Html.content());
                })
            .verifyComplete();
      }
    }

    @Test
    void testGetHtml_withThreeLvlOfNumberingListEntriesBullet() {
      entries.add(
          (NumberingListEntry)
              generateNumberingListEntry("bullet list entry 1", createNumberingListEntryIndex()));
      entries.add(
          (NumberingListEntry)
              generateNumberingListEntry("bullet list entry 2", createNumberingListEntryIndex()));
      iLvl = "1";
      fontStyle = "Courier New";
      entries.add(
          (NumberingListEntry)
              generateNumberingListEntry("bullet list entry 2.1", createNumberingListEntryIndex()));
      entries.add(
          (NumberingListEntry)
              generateNumberingListEntry("bullet list entry 2.2", createNumberingListEntryIndex()));
      iLvl = "2";
      fontStyle = "Wingdings";
      entries.add(
          (NumberingListEntry)
              generateNumberingListEntry(
                  "bullet list entry 2.2.1", createNumberingListEntryIndex()));
      entries.add(
          (NumberingListEntry)
              generateNumberingListEntry(
                  "bullet list entry 2.2.2", createNumberingListEntryIndex()));
      iLvl = "1";
      fontStyle = "Courier New";
      entries.add(
          (NumberingListEntry)
              generateNumberingListEntry("bullet list entry 2.3", createNumberingListEntryIndex()));
      iLvl = "0";
      fontStyle = "Symbol";
      entries.add(
          (NumberingListEntry)
              generateNumberingListEntry("bullet list entry 3", createNumberingListEntryIndex()));

      TestDocumentGenerator generator =
          new TestDocumentGenerator(client, responseBytes, mlPackage, converter);
      int index = 0;
      for (DocumentUnitDocx entry : entries) {
        generator.addContent(String.valueOf(++index), entry);
      }
      generator.generate();

      try (MockedStatic<WordprocessingMLPackage> mockedMLPackageStatic =
          mockStatic(WordprocessingMLPackage.class)) {
        mockedMLPackageStatic
            .when(() -> WordprocessingMLPackage.load(any(InputStream.class)))
            .thenReturn(mlPackage);

        StepVerifier.create(service.getHtml("test.docx"))
            .consumeNextWith(
                docx2Html -> {
                  assertNotNull(docx2Html);
                  assertEquals(
                      "<ul style=\"list-style-type:disc;\">"
                          + "<li style=\"display:table-row\">"
                          + "<p style=\"display:table-cell;text-align:right;\"><span style=\"\">&#9679;</span><span> </span></p>"
                          + "<p>bullet list entry 1</p>"
                          + "</li>"
                          + "<li style=\"display:table-row\">"
                          + "<p style=\"display:table-cell;text-align:right;\"><span style=\"\">&#9679;</span><span> </span></p>"
                          + "<p>bullet list entry 2</p></li><ul style=\"list-style-type:none;display:table;\">"
                          + "<li style=\"display:table-row\">"
                          + "<p style=\"display:table-cell;text-align:right;\"><span style=\"font-family:Courier New;\">&#9675;</span><span> </span></p>"
                          + "<p>bullet list entry 2.1</p>"
                          + "</li>"
                          + "<li style=\"display:table-row\">"
                          + "<p style=\"display:table-cell;text-align:right;\"><span style=\"font-family:Courier New;\">&#9675;</span><span> </span></p>"
                          + "<p>bullet list entry 2.2</p>"
                          + "</li>"
                          + "<ul style=\"list-style-type:none;display:table;\">"
                          + "<li style=\"display:table-row\">"
                          + "<p style=\"display:table-cell;text-align:right;\"><span style=\"font-family:Wingdings;\">&#9642;</span><span> </span></p>"
                          + "<p>bullet list entry 2.2.1</p>"
                          + "</li>"
                          + "<li style=\"display:table-row\">"
                          + "<p style=\"display:table-cell;text-align:right;\"><span style=\"font-family:Wingdings;\">&#9642;</span><span> </span></p>"
                          + "<p>bullet list entry 2.2.2</p>"
                          + "</li>"
                          + "</ul>"
                          + "<li style=\"display:table-row\">"
                          + "<p style=\"display:table-cell;text-align:right;\"><span style=\"font-family:Courier New;\">&#9675;</span><span> </span></p>"
                          + "<p>bullet list entry 2.3</p>"
                          + "</li>"
                          + "</ul>"
                          + "<li style=\"display:table-row\">"
                          + "<p style=\"display:table-cell;text-align:right;\">"
                          + "<span style=\"font-family:Symbol;\">&#9679;</span><span> </span></p>"
                          + "<p>bullet list entry 3</p>"
                          + "</li>"
                          + "</ul>",
                      docx2Html.content());
                })
            .verifyComplete();
      }
    }

    @Test
    void testGetHtml_withThreeLvlOfNumberingListEntriesLowerRoman() {
      numberFormat = DocumentUnitNumberingListNumberFormat.LOWER_ROMAN;
      lvlText = "%1.";
      entries.add(
          (NumberingListEntry)
              generateNumberingListEntry(
                  "lower roman list entry 1", createNumberingListEntryIndex()));
      entries.add(
          (NumberingListEntry)
              generateNumberingListEntry(
                  "lower roman list entry 2", createNumberingListEntryIndex()));
      iLvl = "1";
      lvlText = "%2.";
      entries.add(
          (NumberingListEntry)
              generateNumberingListEntry(
                  "lower roman list entry 2.1", createNumberingListEntryIndex()));
      entries.add(
          (NumberingListEntry)
              generateNumberingListEntry(
                  "lower roman list entry 2.2", createNumberingListEntryIndex()));
      iLvl = "2";
      lvlText = "%3.";
      entries.add(
          (NumberingListEntry)
              generateNumberingListEntry(
                  "lower roman list entry 2.2.1", createNumberingListEntryIndex()));
      entries.add(
          (NumberingListEntry)
              generateNumberingListEntry(
                  "lower roman list entry 2.2.2", createNumberingListEntryIndex()));
      iLvl = "1";
      lvlText = "%2.";
      entries.add(
          (NumberingListEntry)
              generateNumberingListEntry(
                  "lower roman list entry 2.3", createNumberingListEntryIndex()));
      iLvl = "0";
      lvlText = "%1.";
      entries.add(
          (NumberingListEntry)
              generateNumberingListEntry(
                  "lower roman list entry 3", createNumberingListEntryIndex()));

      TestDocumentGenerator generator =
          new TestDocumentGenerator(client, responseBytes, mlPackage, converter);
      int index = 0;
      for (DocumentUnitDocx entry : entries) {
        generator.addContent(String.valueOf(++index), entry);
      }
      generator.generate();

      try (MockedStatic<WordprocessingMLPackage> mockedMLPackageStatic =
          mockStatic(WordprocessingMLPackage.class)) {
        mockedMLPackageStatic
            .when(() -> WordprocessingMLPackage.load(any(InputStream.class)))
            .thenReturn(mlPackage);

        StepVerifier.create(service.getHtml("test.docx"))
            .consumeNextWith(
                docx2Html -> {
                  assertNotNull(docx2Html);
                  assertEquals(
                      "<ol style=\"list-style-type:none;display:table;\">"
                          + "<li style=\"display:table-row\">"
                          + "<p style=\"display:table-cell;text-align:right;\"><span style=\"\">i.</span><span> </span></p>"
                          + "<p>lower roman list entry 1</p>"
                          + "</li>"
                          + "<li style=\"display:table-row\">"
                          + "<p style=\"display:table-cell;text-align:right;\">"
                          + "<span style=\"\">ii.</span><span> </span></p>"
                          + "<p>lower roman list entry 2</p>"
                          + "</li>"
                          + "<ol style=\"list-style-type:none;display:table;\">"
                          + "<li style=\"display:table-row\">"
                          + "<p style=\"display:table-cell;text-align:right;\"><span style=\"\">i.</span><span> </span></p>"
                          + "<p>lower roman list entry 2.1</p>"
                          + "</li>"
                          + "<li style=\"display:table-row\">"
                          + "<p style=\"display:table-cell;text-align:right;\"><span style=\"\">ii.</span><span> </span></p>"
                          + "<p>lower roman list entry 2.2</p>"
                          + "</li>"
                          + "<ol style=\"list-style-type:none;display:table;\">"
                          + "<li style=\"display:table-row\">"
                          + "<p style=\"display:table-cell;text-align:right;\"><span style=\"\">i.</span><span> </span></p>"
                          + "<p>lower roman list entry 2.2.1</p>"
                          + "</li>"
                          + "<li style=\"display:table-row\">"
                          + "<p style=\"display:table-cell;text-align:right;\"><span style=\"\">ii.</span><span> </span></p>"
                          + "<p>lower roman list entry 2.2.2</p>"
                          + "</li>"
                          + "</ol>"
                          + "<li style=\"display:table-row\">"
                          + "<p style=\"display:table-cell;text-align:right;\"><span style=\"\">iii.</span><span> </span></p>"
                          + "<p>lower roman list entry 2.3</p>"
                          + "</li>"
                          + "</ol>"
                          + "<li style=\"display:table-row\">"
                          + "<p style=\"display:table-cell;text-align:right;\"><span style=\"\">iii.</span><span> </span></p>"
                          + "<p>lower roman list entry 3</p>"
                          + "</li>"
                          + "</ol>",
                      docx2Html.content());
                })
            .verifyComplete();
      }
    }

    @Test
    void testGetHtml_withThreeLvlOfNumberingListEntriesUpperRoman() {
      numberFormat = DocumentUnitNumberingListNumberFormat.UPPER_ROMAN;
      lvlText = "%1.";
      entries.add(
          (NumberingListEntry)
              generateNumberingListEntry(
                  "upper roman list entry 1", createNumberingListEntryIndex()));
      entries.add(
          (NumberingListEntry)
              generateNumberingListEntry(
                  "upper roman list entry 2", createNumberingListEntryIndex()));
      iLvl = "1";
      lvlText = "%2.";
      entries.add(
          (NumberingListEntry)
              generateNumberingListEntry(
                  "upper roman list entry 2.1", createNumberingListEntryIndex()));
      entries.add(
          (NumberingListEntry)
              generateNumberingListEntry(
                  "upper roman list entry 2.2", createNumberingListEntryIndex()));
      iLvl = "2";
      lvlText = "%3.";
      entries.add(
          (NumberingListEntry)
              generateNumberingListEntry(
                  "upper roman list entry 2.2.1", createNumberingListEntryIndex()));
      entries.add(
          (NumberingListEntry)
              generateNumberingListEntry(
                  "upper roman list entry 2.2.2", createNumberingListEntryIndex()));
      iLvl = "1";
      lvlText = "%2.";
      entries.add(
          (NumberingListEntry)
              generateNumberingListEntry(
                  "upper roman list entry 2.3", createNumberingListEntryIndex()));
      iLvl = "0";
      lvlText = "%1.";
      entries.add(
          (NumberingListEntry)
              generateNumberingListEntry(
                  "upper roman list entry 3", createNumberingListEntryIndex()));

      TestDocumentGenerator generator =
          new TestDocumentGenerator(client, responseBytes, mlPackage, converter);
      int index = 0;
      for (DocumentUnitDocx entry : entries) {
        generator.addContent(String.valueOf(++index), entry);
      }
      generator.generate();

      try (MockedStatic<WordprocessingMLPackage> mockedMLPackageStatic =
          mockStatic(WordprocessingMLPackage.class)) {
        mockedMLPackageStatic
            .when(() -> WordprocessingMLPackage.load(any(InputStream.class)))
            .thenReturn(mlPackage);

        StepVerifier.create(service.getHtml("test.docx"))
            .consumeNextWith(
                docx2Html -> {
                  assertNotNull(docx2Html);
                  assertEquals(
                      "<ol style=\"list-style-type:none;display:table;\">"
                          + "<li style=\"display:table-row\">"
                          + "<p style=\"display:table-cell;text-align:right;\"><span style=\"\">I.</span><span> </span></p>"
                          + "<p>upper roman list entry 1</p>"
                          + "</li>"
                          + "<li style=\"display:table-row\">"
                          + "<p style=\"display:table-cell;text-align:right;\"><span style=\"\">II.</span><span> </span></p>"
                          + "<p>upper roman list entry 2</p>"
                          + "</li>"
                          + "<ol style=\"list-style-type:none;display:table;\">"
                          + "<li style=\"display:table-row\">"
                          + "<p style=\"display:table-cell;text-align:right;\"><span style=\"\">I.</span><span> </span></p>"
                          + "<p>upper roman list entry 2.1</p>"
                          + "</li>"
                          + "<li style=\"display:table-row\">"
                          + "<p style=\"display:table-cell;text-align:right;\"><span style=\"\">II.</span><span> </span></p>"
                          + "<p>upper roman list entry 2.2</p>"
                          + "</li>"
                          + "<ol style=\"list-style-type:none;display:table;\">"
                          + "<li style=\"display:table-row\">"
                          + "<p style=\"display:table-cell;text-align:right;\"><span style=\"\">I.</span><span> </span></p>"
                          + "<p>upper roman list entry 2.2.1</p>"
                          + "</li>"
                          + "<li style=\"display:table-row\">"
                          + "<p style=\"display:table-cell;text-align:right;\"><span style=\"\">II.</span><span> </span></p>"
                          + "<p>upper roman list entry 2.2.2</p>"
                          + "</li>"
                          + "</ol>"
                          + "<li style=\"display:table-row\">"
                          + "<p style=\"display:table-cell;text-align:right;\"><span style=\"\">III.</span><span> </span></p>"
                          + "<p>upper roman list entry 2.3</p>"
                          + "</li>"
                          + "</ol>"
                          + "<li style=\"display:table-row\">"
                          + "<p style=\"display:table-cell;text-align:right;\"><span style=\"\">III.</span><span> </span></p>"
                          + "<p>upper roman list entry 3</p>"
                          + "</li>"
                          + "</ol>",
                      docx2Html.content());
                })
            .verifyComplete();
      }
    }

    @Test
    void testGetHtml_withThreeLvlOfNumberingListEntriesLowerLetter() {
      numberFormat = DocumentUnitNumberingListNumberFormat.LOWER_LETTER;
      lvlText = "%1)";
      entries.add(
          (NumberingListEntry)
              generateNumberingListEntry(
                  "lower letter list entry 1", createNumberingListEntryIndex()));
      entries.add(
          (NumberingListEntry)
              generateNumberingListEntry(
                  "lower letter list entry 2", createNumberingListEntryIndex()));
      iLvl = "1";
      lvlText = "%2.";
      entries.add(
          (NumberingListEntry)
              generateNumberingListEntry(
                  "lower letter list entry 2.1", createNumberingListEntryIndex()));
      entries.add(
          (NumberingListEntry)
              generateNumberingListEntry(
                  "lower letter list entry 2.2", createNumberingListEntryIndex()));
      iLvl = "2";
      lvlText = "(%3)";
      entries.add(
          (NumberingListEntry)
              generateNumberingListEntry(
                  "lower letter list entry 2.2.1", createNumberingListEntryIndex()));
      entries.add(
          (NumberingListEntry)
              generateNumberingListEntry(
                  "lower letter list entry 2.2.2", createNumberingListEntryIndex()));
      iLvl = "1";
      lvlText = "%2.";
      entries.add(
          (NumberingListEntry)
              generateNumberingListEntry(
                  "lower letter list entry 2.3", createNumberingListEntryIndex()));
      iLvl = "0";
      lvlText = "%1)";
      entries.add(
          (NumberingListEntry)
              generateNumberingListEntry(
                  "lower letter list entry 3", createNumberingListEntryIndex()));

      TestDocumentGenerator generator =
          new TestDocumentGenerator(client, responseBytes, mlPackage, converter);
      int index = 0;
      for (DocumentUnitDocx entry : entries) {
        generator.addContent(String.valueOf(++index), entry);
      }
      generator.generate();

      try (MockedStatic<WordprocessingMLPackage> mockedMLPackageStatic =
          mockStatic(WordprocessingMLPackage.class)) {
        mockedMLPackageStatic
            .when(() -> WordprocessingMLPackage.load(any(InputStream.class)))
            .thenReturn(mlPackage);

        StepVerifier.create(service.getHtml("test.docx"))
            .consumeNextWith(
                docx2Html -> {
                  assertNotNull(docx2Html);
                  assertEquals(
                      "<ol style=\"list-style-type:none;display:table;\">"
                          + "<li style=\"display:table-row\">"
                          + "<p style=\"display:table-cell;text-align:right;\"><span style=\"\">a)</span><span> </span></p>"
                          + "<p>lower letter list entry 1</p>"
                          + "</li>"
                          + "<li style=\"display:table-row\">"
                          + "<p style=\"display:table-cell;text-align:right;\"><span style=\"\">b)</span><span> </span></p>"
                          + "<p>lower letter list entry 2</p>"
                          + "</li>"
                          + "<ol style=\"list-style-type:none;display:table;\">"
                          + "<li style=\"display:table-row\">"
                          + "<p style=\"display:table-cell;text-align:right;\"><span style=\"\">a.</span><span> </span></p>"
                          + "<p>lower letter list entry 2.1</p>"
                          + "</li>"
                          + "<li style=\"display:table-row\">"
                          + "<p style=\"display:table-cell;text-align:right;\"><span style=\"\">b.</span><span> </span></p>"
                          + "<p>lower letter list entry 2.2</p>"
                          + "</li>"
                          + "<ol style=\"list-style-type:none;display:table;\">"
                          + "<li style=\"display:table-row\">"
                          + "<p style=\"display:table-cell;text-align:right;\"><span style=\"\">(a)</span><span> </span></p>"
                          + "<p>lower letter list entry 2.2.1</p>"
                          + "</li>"
                          + "<li style=\"display:table-row\">"
                          + "<p style=\"display:table-cell;text-align:right;\"><span style=\"\">(b)</span><span> </span></p>"
                          + "<p>lower letter list entry 2.2.2</p>"
                          + "</li>"
                          + "</ol>"
                          + "<li style=\"display:table-row\">"
                          + "<p style=\"display:table-cell;text-align:right;\"><span style=\"\">c.</span><span> </span></p>"
                          + "<p>lower letter list entry 2.3</p></li></ol><li style=\"display:table-row\">"
                          + "<p style=\"display:table-cell;text-align:right;\"><span style=\"\">c)</span><span> </span></p>"
                          + "<p>lower letter list entry 3</p>"
                          + "</li>"
                          + "</ol>",
                      docx2Html.content());
                })
            .verifyComplete();
      }
    }

    @Test
    void testGetHtml_withThreeLvlOfNumberingListEntriesUpperLetter() {
      numberFormat = DocumentUnitNumberingListNumberFormat.UPPER_LETTER;
      lvlText = "%1)";
      entries.add(
          (NumberingListEntry)
              generateNumberingListEntry(
                  "upper letter list entry 1", createNumberingListEntryIndex()));
      entries.add(
          (NumberingListEntry)
              generateNumberingListEntry(
                  "upper letter list entry 2", createNumberingListEntryIndex()));
      iLvl = "1";
      lvlText = "%2.";
      entries.add(
          (NumberingListEntry)
              generateNumberingListEntry(
                  "upper letter list entry 2.1", createNumberingListEntryIndex()));
      entries.add(
          (NumberingListEntry)
              generateNumberingListEntry(
                  "upper letter list entry 2.2", createNumberingListEntryIndex()));
      iLvl = "2";
      lvlText = "(%3)";
      entries.add(
          (NumberingListEntry)
              generateNumberingListEntry(
                  "upper letter list entry 2.2.1", createNumberingListEntryIndex()));
      entries.add(
          (NumberingListEntry)
              generateNumberingListEntry(
                  "upper letter list entry 2.2.2", createNumberingListEntryIndex()));
      iLvl = "1";
      lvlText = "%2.";
      entries.add(
          (NumberingListEntry)
              generateNumberingListEntry(
                  "upper letter list entry 2.3", createNumberingListEntryIndex()));
      iLvl = "0";
      lvlText = "%1)";
      entries.add(
          (NumberingListEntry)
              generateNumberingListEntry(
                  "upper letter list entry 3", createNumberingListEntryIndex()));

      TestDocumentGenerator generator =
          new TestDocumentGenerator(client, responseBytes, mlPackage, converter);
      int index = 0;
      for (DocumentUnitDocx entry : entries) {
        generator.addContent(String.valueOf(++index), entry);
      }
      generator.generate();

      try (MockedStatic<WordprocessingMLPackage> mockedMLPackageStatic =
          mockStatic(WordprocessingMLPackage.class)) {
        mockedMLPackageStatic
            .when(() -> WordprocessingMLPackage.load(any(InputStream.class)))
            .thenReturn(mlPackage);

        StepVerifier.create(service.getHtml("test.docx"))
            .consumeNextWith(
                docx2Html -> {
                  assertNotNull(docx2Html);
                  assertEquals(
                      "<ol style=\"list-style-type:none;display:table;\">"
                          + "<li style=\"display:table-row\">"
                          + "<p style=\"display:table-cell;text-align:right;\"><span style=\"\">A)</span><span> </span></p>"
                          + "<p>upper letter list entry 1</p>"
                          + "</li>"
                          + "<li style=\"display:table-row\">"
                          + "<p style=\"display:table-cell;text-align:right;\"><span style=\"\">B)</span><span> </span></p>"
                          + "<p>upper letter list entry 2</p>"
                          + "</li>"
                          + "<ol style=\"list-style-type:none;display:table;\">"
                          + "<li style=\"display:table-row\">"
                          + "<p style=\"display:table-cell;text-align:right;\"><span style=\"\">A.</span><span> </span></p>"
                          + "<p>upper letter list entry 2.1</p>"
                          + "</li>"
                          + "<li style=\"display:table-row\">"
                          + "<p style=\"display:table-cell;text-align:right;\"><span style=\"\">B.</span><span> </span></p>"
                          + "<p>upper letter list entry 2.2</p>"
                          + "</li>"
                          + "<ol style=\"list-style-type:none;display:table;\">"
                          + "<li style=\"display:table-row\">"
                          + "<p style=\"display:table-cell;text-align:right;\"><span style=\"\">(A)</span><span> </span></p>"
                          + "<p>upper letter list entry 2.2.1</p>"
                          + "</li>"
                          + "<li style=\"display:table-row\">"
                          + "<p style=\"display:table-cell;text-align:right;\"><span style=\"\">(B)</span><span> </span></p>"
                          + "<p>upper letter list entry 2.2.2</p>"
                          + "</li>"
                          + "</ol>"
                          + "<li style=\"display:table-row\">"
                          + "<p style=\"display:table-cell;text-align:right;\"><span style=\"\">C.</span><span> </span></p>"
                          + "<p>upper letter list entry 2.3</p>"
                          + "</li>"
                          + "</ol>"
                          + "<li style=\"display:table-row\">"
                          + "<p style=\"display:table-cell;text-align:right;\"><span style=\"\">C)</span><span> </span></p>"
                          + "<p>upper letter list entry 3</p>"
                          + "</li>"
                          + "</ol>",
                      docx2Html.content());
                })
            .verifyComplete();
      }
    }

    @Test
    void testGetHtml_withThreeLvlOfNumberingListEntriesDecimal() {
      numberFormat = DocumentUnitNumberingListNumberFormat.DECIMAL;
      lvlText = "%1.";
      entries.add(
          (NumberingListEntry)
              generateNumberingListEntry("decimal list entry 1", createNumberingListEntryIndex()));
      entries.add(
          (NumberingListEntry)
              generateNumberingListEntry("decimal list entry 2", createNumberingListEntryIndex()));
      iLvl = "1";
      lvlText = "%1.%2.";
      entries.add(
          (NumberingListEntry)
              generateNumberingListEntry(
                  "decimal list entry 2.1", createNumberingListEntryIndex()));
      entries.add(
          (NumberingListEntry)
              generateNumberingListEntry(
                  "decimal list entry 2.2", createNumberingListEntryIndex()));
      iLvl = "2";
      lvlText = "%1.%2.%3.";
      entries.add(
          (NumberingListEntry)
              generateNumberingListEntry(
                  "decimal list entry 2.2.1", createNumberingListEntryIndex()));
      entries.add(
          (NumberingListEntry)
              generateNumberingListEntry(
                  "decimal list entry 2.2.2", createNumberingListEntryIndex()));
      iLvl = "1";
      lvlText = "%1.%2.";
      entries.add(
          (NumberingListEntry)
              generateNumberingListEntry(
                  "decimal list entry 2.3", createNumberingListEntryIndex()));
      iLvl = "0";
      lvlText = "%1.";
      entries.add(
          (NumberingListEntry)
              generateNumberingListEntry("decimal list entry 3", createNumberingListEntryIndex()));

      TestDocumentGenerator generator =
          new TestDocumentGenerator(client, responseBytes, mlPackage, converter);
      int index = 0;
      for (DocumentUnitDocx entry : entries) {
        generator.addContent(String.valueOf(++index), entry);
      }
      generator.generate();

      try (MockedStatic<WordprocessingMLPackage> mockedMLPackageStatic =
          mockStatic(WordprocessingMLPackage.class)) {
        mockedMLPackageStatic
            .when(() -> WordprocessingMLPackage.load(any(InputStream.class)))
            .thenReturn(mlPackage);

        StepVerifier.create(service.getHtml("test.docx"))
            .consumeNextWith(
                docx2Html -> {
                  assertNotNull(docx2Html);
                  assertEquals(
                      "<ol style=\"list-style-type:none;display:table;\">"
                          + "<li style=\"display:table-row\">"
                          + "<p style=\"display:table-cell;text-align:right;\"><span style=\"\">1.</span><span> </span></p>"
                          + "<p>decimal list entry 1</p>"
                          + "</li>"
                          + "<li style=\"display:table-row\">"
                          + "<p style=\"display:table-cell;text-align:right;\"><span style=\"\">2.</span><span> </span></p>"
                          + "<p>decimal list entry 2</p>"
                          + "</li>"
                          + "<ol style=\"list-style-type:none;display:table;\">"
                          + "<li style=\"display:table-row\">"
                          + "<p style=\"display:table-cell;text-align:right;\"><span style=\"\">2.1.</span><span> </span></p>"
                          + "<p>decimal list entry 2.1</p>"
                          + "</li>"
                          + "<li style=\"display:table-row\">"
                          + "<p style=\"display:table-cell;text-align:right;\"><span style=\"\">2.2.</span><span> </span></p>"
                          + "<p>decimal list entry 2.2</p>"
                          + "</li>"
                          + "<ol style=\"list-style-type:none;display:table;\">"
                          + "<li style=\"display:table-row\">"
                          + "<p style=\"display:table-cell;text-align:right;\"><span style=\"\">2.2.1.</span><span> </span></p>"
                          + "<p>decimal list entry 2.2.1</p>"
                          + "</li>"
                          + "<li style=\"display:table-row\">"
                          + "<p style=\"display:table-cell;text-align:right;\"><span style=\"\">2.2.2.</span><span> </span></p>"
                          + "<p>decimal list entry 2.2.2</p>"
                          + "</li>"
                          + "</ol>"
                          + "<li style=\"display:table-row\">"
                          + "<p style=\"display:table-cell;text-align:right;\"><span style=\"\">2.3.</span><span> </span></p>"
                          + "<p>decimal list entry 2.3</p>"
                          + "</li>"
                          + "</ol>"
                          + "<li style=\"display:table-row\">"
                          + "<p style=\"display:table-cell;text-align:right;\"><span style=\"\">3.</span><span> </span></p>"
                          + "<p>decimal list entry 3</p>"
                          + "</li>"
                          + "</ol>",
                      docx2Html.content());
                })
            .verifyComplete();
      }
    }

    @Test
    void testGetHtml_withThreeLvlOfNumberingListEntriesMixed() {
      numberFormat = DocumentUnitNumberingListNumberFormat.DECIMAL;
      lvlText = "%1.";
      entries.add(
          (NumberingListEntry)
              generateNumberingListEntry("list entry 1", createNumberingListEntryIndex()));
      entries.add(
          (NumberingListEntry)
              generateNumberingListEntry("list entry 2", createNumberingListEntryIndex()));
      iLvl = "1";
      numberFormat = DocumentUnitNumberingListNumberFormat.BULLET;
      fontStyle = "Symbol";
      fontSize = "18";
      entries.add(
          (NumberingListEntry)
              generateNumberingListEntry("list entry 2.1", createNumberingListEntryIndex()));
      entries.add(
          (NumberingListEntry)
              generateNumberingListEntry("list entry 2.2", createNumberingListEntryIndex()));
      iLvl = "2";
      numberFormat = DocumentUnitNumberingListNumberFormat.DECIMAL;
      lvlText = "%1.%2.%3.";
      entries.add(
          (NumberingListEntry)
              generateNumberingListEntry("list entry 2.2.1", createNumberingListEntryIndex()));
      entries.add(
          (NumberingListEntry)
              generateNumberingListEntry("list entry 2.2.2", createNumberingListEntryIndex()));
      iLvl = "1";
      lvlText = "%1.%2.";
      entries.add(
          (NumberingListEntry)
              generateNumberingListEntry("list entry 2.3", createNumberingListEntryIndex()));
      iLvl = "2";
      numberFormat = DocumentUnitNumberingListNumberFormat.BULLET;
      fontStyle = "Symbol";
      fontSize = "18";
      entries.add(
          (NumberingListEntry)
              generateNumberingListEntry("list entry 2.3.1", createNumberingListEntryIndex()));
      numberFormat = DocumentUnitNumberingListNumberFormat.DECIMAL;
      iLvl = "0";
      lvlText = "%1.";
      entries.add(
          (NumberingListEntry)
              generateNumberingListEntry("list entry 3", createNumberingListEntryIndex()));

      TestDocumentGenerator generator =
          new TestDocumentGenerator(client, responseBytes, mlPackage, converter);
      int index = 0;
      for (DocumentUnitDocx entry : entries) {
        generator.addContent(String.valueOf(++index), entry);
      }
      generator.generate();

      try (MockedStatic<WordprocessingMLPackage> mockedMLPackageStatic =
          mockStatic(WordprocessingMLPackage.class)) {
        mockedMLPackageStatic
            .when(() -> WordprocessingMLPackage.load(any(InputStream.class)))
            .thenReturn(mlPackage);

        StepVerifier.create(service.getHtml("test.docx"))
            .consumeNextWith(
                docx2Html -> {
                  assertNotNull(docx2Html);
                  assertEquals(
                      "<ol style=\"list-style-type:none;display:table;\">"
                          + "<li style=\"display:table-row\">"
                          + "<p style=\"display:table-cell;text-align:right;\"><span style=\"\">1.</span><span> </span></p>"
                          + "<p>list entry 1</p>"
                          + "</li>"
                          + "<li style=\"display:table-row\">"
                          + "<p style=\"display:table-cell;text-align:right;\"><span style=\"\">2.</span><span> </span></p>"
                          + "<p>list entry 2</p>"
                          + "</li>"
                          + "<ul style=\"list-style-type:none;display:table;\">"
                          + "<li style=\"display:table-row\">"
                          + "<p style=\"display:table-cell;text-align:right;\"><span style=\"font-family:Symbol;font-size:9pt;\">&#9679;</span><span> </span></p>"
                          + "<p>list entry 2.1</p>"
                          + "</li>"
                          + "<li style=\"display:table-row\">"
                          + "<p style=\"display:table-cell;text-align:right;\"><span style=\"font-family:Symbol;font-size:9pt;\">&#9679;</span><span> </span></p>"
                          + "<p>list entry 2.2</p>"
                          + "</li>"
                          + "<ol style=\"list-style-type:none;display:table;\">"
                          + "<li style=\"display:table-row\">"
                          + "<p style=\"display:table-cell;text-align:right;\"><span style=\"font-family:Symbol;font-size:9pt;\">2..1.</span><span> </span></p>"
                          + "<p>list entry 2.2.1</p>"
                          + "</li>"
                          + "<li style=\"display:table-row\">"
                          + "<p style=\"display:table-cell;text-align:right;\"><span style=\"font-family:Symbol;font-size:9pt;\">2..2.</span><span> </span></p>"
                          + "<p>list entry 2.2.2</p>"
                          + "</li>"
                          + "</ol>"
                          + "<li style=\"display:table-row\">"
                          + "<p style=\"display:table-cell;text-align:right;\"><span style=\"font-family:Symbol;font-size:9pt;\">2.1.</span><span> </span></p>"
                          + "<p>list entry 2.3</p>"
                          + "</li>"
                          + "<ul style=\"list-style-type:none;display:table;\">"
                          + "<li style=\"display:table-row\">"
                          + "<p style=\"display:table-cell;text-align:right;\"><span style=\"font-family:Symbol;font-size:9pt;\">&#9679;</span><span> </span></p>"
                          + "<p>list entry 2.3.1</p>"
                          + "</li>"
                          + "</ul>"
                          + "</ul>"
                          + "<li style=\"display:table-row\">"
                          + "<p style=\"display:table-cell;text-align:right;\"><span style=\"font-family:Symbol;font-size:9pt;\">3.</span><span> </span></p>"
                          + "<p>list entry 3</p>"
                          + "</li></ol>",
                      docx2Html.content());
                })
            .verifyComplete();
      }
    }
  }

  @Test
  void testGetHtml_withInputStreamIsNull() {
    when(client.getObject(any(GetObjectRequest.class), any(AsyncResponseTransformer.class)))
        .thenReturn(CompletableFuture.completedFuture(responseBytes));
    when(responseBytes.asInputStream()).thenReturn(null);

    StepVerifier.create(service.getHtml("test.docx"))
        .consumeNextWith(docx2Html -> assertEquals(Docx2Html.EMPTY, docx2Html))
        .verifyComplete();
  }

  @Test
  void testGetHtml_withLoadDocxThrowsException() {
    when(client.getObject(any(GetObjectRequest.class), any(AsyncResponseTransformer.class)))
        .thenReturn(CompletableFuture.completedFuture(responseBytes));
    when(responseBytes.asInputStream()).thenReturn(new ByteArrayInputStream(new byte[] {}));

    try (MockedStatic<WordprocessingMLPackage> mockedMLPackageStatic =
        mockStatic(WordprocessingMLPackage.class)) {
      mockedMLPackageStatic
          .when(() -> WordprocessingMLPackage.load(any(InputStream.class)))
          .thenThrow(Docx4JException.class);

      StepVerifier.create(service.getHtml("test.docx"))
          .expectErrorMatches(
              throwable ->
                  throwable instanceof DocxConverterException
                      && throwable.getMessage().equals("Couldn't load docx file!"))
          .verify();
    }
  }

  private ParagraphElement generateText(String text) {
    var textElement = new ParagraphElement();
    var runTextElement = new RunTextElement();
    runTextElement.setText(text);
    textElement.addRunElement(runTextElement);
    return textElement;
  }

  private BorderNumber generateBorderNumber(String text) {
    BorderNumber borderNumber = new BorderNumber();
    borderNumber.addNumberText(text);
    return borderNumber;
  }

  private TableElement generateTable(String text) {
    List<DocumentUnitDocx> paragraphElements = List.of(generateText(text));
    List<TableCellElement> cells = List.of(new TableCellElement(paragraphElements));
    List<TableRowElement> rows = List.of(new TableRowElement(cells));

    return new TableElement(rows);
  }

  private DocumentUnitDocx generateNumberingListEntry(
      String text, NumberingListEntryIndex numberingListEntryIndex) {
    var paragraphElement = generateText(text);

    return new NumberingListEntry(paragraphElement, numberingListEntryIndex);
  }

  private static class TestDocumentGenerator {

    private final List<Object> ids = new ArrayList<>();
    private final S3AsyncClient client;
    private final ResponseBytes<GetObjectResponse> responseBytes;
    private final WordprocessingMLPackage mlPackage;
    private final DocxConverter converter;

    public TestDocumentGenerator(
        S3AsyncClient client,
        ResponseBytes<GetObjectResponse> responseBytes,
        WordprocessingMLPackage mlPackage,
        DocxConverter converter) {

      this.client = client;
      this.responseBytes = responseBytes;
      this.mlPackage = mlPackage;
      this.converter = converter;
    }

    private TestDocumentGenerator addContent(String id, DocumentUnitDocx documentUnitDocx) {
      ids.add(id);
      when(converter.convert(id)).thenReturn(documentUnitDocx);

      return this;
    }

    private void generate() {
      when(client.getObject(any(GetObjectRequest.class), any(AsyncResponseTransformer.class)))
          .thenReturn(CompletableFuture.completedFuture(responseBytes));
      when(responseBytes.asInputStream()).thenReturn(new ByteArrayInputStream(new byte[] {}));
      MainDocumentPart mainDocumentPart = mock(MainDocumentPart.class);
      when(mlPackage.getMainDocumentPart()).thenReturn(mainDocumentPart);
      when(mainDocumentPart.getContent()).thenReturn(ids);
    }
  }
}
