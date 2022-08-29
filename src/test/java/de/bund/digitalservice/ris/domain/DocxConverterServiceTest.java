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
import de.bund.digitalservice.ris.domain.docx.DocUnitDocx;
import de.bund.digitalservice.ris.domain.docx.Docx2Html;
import de.bund.digitalservice.ris.domain.docx.DocxImagePart;
import de.bund.digitalservice.ris.domain.docx.NumberingList.DocUnitNumberingListNumberFormat;
import de.bund.digitalservice.ris.domain.docx.NumberingListEntry;
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
import org.docx4j.wml.Style;
import org.docx4j.wml.Styles;
import org.junit.jupiter.api.Assertions;
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

  @Test
  void testGetHtml_withTwoNumberingListEntries() {
    new TestDocumentGenerator(client, responseBytes, mlPackage, converter)
        .addContent("1", generateText("start test"))
        .addContent(
            "2",
            generateNumberingListEntry(
                "bullet list entry 1", DocUnitNumberingListNumberFormat.BULLET, "0", "0"))
        .addContent(
            "3",
            generateNumberingListEntry(
                "bullet list entry 2", DocUnitNumberingListNumberFormat.BULLET, "0", "0"))
        .addContent(
            "4",
            generateNumberingListEntry(
                "decimal list entry 1", DocUnitNumberingListNumberFormat.DECIMAL, "1", "0"))
        .addContent(
            "5",
            generateNumberingListEntry(
                "decimal list entry 2", DocUnitNumberingListNumberFormat.DECIMAL, "1", "0"))
        .addContent("6", generateText("end text"))
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
                    "<p>start test</p>"
                        + "<ul><li><p>bullet list entry 1</p></li><li><p>bullet list entry 2</p></li></ul>"
                        + "<ol class=\"decimal\"><li><p>decimal list entry 1</p></li><li><p>decimal list entry 2</p></li></ol>"
                        + "<p>end text</p>",
                    docx2Html.content());
              })
          .verifyComplete();
    }
  }

  @Test
  void testGetHtml_withThreeLvlOfNumberingListEntriesBullet() {
    new TestDocumentGenerator(client, responseBytes, mlPackage, converter)
        .addContent("1", generateText("start test"))
        .addContent(
            "2",
            generateNumberingListEntry(
                "bullet list entry 1", DocUnitNumberingListNumberFormat.BULLET, "0", "0"))
        .addContent(
            "3",
            generateNumberingListEntry(
                "bullet list entry 2", DocUnitNumberingListNumberFormat.BULLET, "0", "0"))
        .addContent(
            "4",
            generateNumberingListEntry(
                "bullet list entry 2.1", DocUnitNumberingListNumberFormat.BULLET, "1", "1"))
        .addContent(
            "5",
            generateNumberingListEntry(
                "bullet list entry 2.2", DocUnitNumberingListNumberFormat.BULLET, "1", "1"))
        .addContent(
            "6",
            generateNumberingListEntry(
                "bullet list entry 2.2.1", DocUnitNumberingListNumberFormat.BULLET, "1", "2"))
        .addContent(
            "7",
            generateNumberingListEntry(
                "bullet list entry 2.2.2", DocUnitNumberingListNumberFormat.BULLET, "1", "2"))
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
                    "<p>start test</p>"
                        + "<ul>"
                        + "<li><p>bullet list entry 1</p></li>"
                        + "<li><p>bullet list entry 2</p></li>"
                        + "<ul>"
                        + "<li><p>bullet list entry 2.1</p></li>"
                        + "<li><p>bullet list entry 2.2</p></li>"
                        + "<ul>"
                        + "<li><p>bullet list entry 2.2.1</p></li>"
                        + "<li><p>bullet list entry 2.2.2</p></li>"
                        + "</ul>"
                        + "</ul>"
                        + "</ul>",
                    docx2Html.content());
              })
          .verifyComplete();
    }
  }

  @Test
  void testGetHtml_withThreeLvlOfNumberingListEntriesLowerRoman() {
    new TestDocumentGenerator(client, responseBytes, mlPackage, converter)
        .addContent("1", generateText("start test"))
        .addContent(
            "2",
            generateNumberingListEntry(
                "lower roman list entry 1", DocUnitNumberingListNumberFormat.LOWER_ROMAN, "0", "0"))
        .addContent(
            "3",
            generateNumberingListEntry(
                "lower roman list entry 2", DocUnitNumberingListNumberFormat.LOWER_ROMAN, "0", "0"))
        .addContent(
            "4",
            generateNumberingListEntry(
                "lower roman list entry 2.1",
                DocUnitNumberingListNumberFormat.LOWER_ROMAN,
                "1",
                "1"))
        .addContent(
            "5",
            generateNumberingListEntry(
                "lower roman list entry 2.2",
                DocUnitNumberingListNumberFormat.LOWER_ROMAN,
                "1",
                "1"))
        .addContent(
            "6",
            generateNumberingListEntry(
                "lower roman list entry 2.2.1",
                DocUnitNumberingListNumberFormat.LOWER_ROMAN,
                "1",
                "2"))
        .addContent(
            "7",
            generateNumberingListEntry(
                "lower roman list entry 2.2.2",
                DocUnitNumberingListNumberFormat.LOWER_ROMAN,
                "1",
                "2"))
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
                    "<p>start test</p>"
                        + "<ol class=\"lower-roman\">"
                        + "<li><p>lower roman list entry 1</p></li>"
                        + "<li><p>lower roman list entry 2</p></li>"
                        + "<ol class=\"lower-roman\">"
                        + "<li><p>lower roman list entry 2.1</p></li>"
                        + "<li><p>lower roman list entry 2.2</p></li>"
                        + "<ol class=\"lower-roman\">"
                        + "<li><p>lower roman list entry 2.2.1</p></li>"
                        + "<li><p>lower roman list entry 2.2.2</p></li>"
                        + "</ol>"
                        + "</ol>"
                        + "</ol>",
                    docx2Html.content());
              })
          .verifyComplete();
    }
  }

  @Test
  void testGetHtml_withThreeLvlOfNumberingListEntriesUpperRoman() {
    new TestDocumentGenerator(client, responseBytes, mlPackage, converter)
        .addContent("1", generateText("start test"))
        .addContent(
            "2",
            generateNumberingListEntry(
                "upper roman list entry 1", DocUnitNumberingListNumberFormat.UPPER_ROMAN, "0", "0"))
        .addContent(
            "3",
            generateNumberingListEntry(
                "upper roman list entry 2", DocUnitNumberingListNumberFormat.UPPER_ROMAN, "0", "0"))
        .addContent(
            "4",
            generateNumberingListEntry(
                "upper roman list entry 2.1",
                DocUnitNumberingListNumberFormat.UPPER_ROMAN,
                "1",
                "1"))
        .addContent(
            "5",
            generateNumberingListEntry(
                "upper roman list entry 2.2",
                DocUnitNumberingListNumberFormat.UPPER_ROMAN,
                "1",
                "1"))
        .addContent(
            "6",
            generateNumberingListEntry(
                "upper roman list entry 2.2.1",
                DocUnitNumberingListNumberFormat.UPPER_ROMAN,
                "1",
                "2"))
        .addContent(
            "7",
            generateNumberingListEntry(
                "upper roman list entry 2.2.2",
                DocUnitNumberingListNumberFormat.UPPER_ROMAN,
                "1",
                "2"))
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
                    "<p>start test</p>"
                        + "<ol class=\"upper-roman\">"
                        + "<li><p>upper roman list entry 1</p></li>"
                        + "<li><p>upper roman list entry 2</p></li>"
                        + "<ol class=\"upper-roman\">"
                        + "<li><p>upper roman list entry 2.1</p></li>"
                        + "<li><p>upper roman list entry 2.2</p></li>"
                        + "<ol class=\"upper-roman\">"
                        + "<li><p>upper roman list entry 2.2.1</p></li>"
                        + "<li><p>upper roman list entry 2.2.2</p></li>"
                        + "</ol>"
                        + "</ol>"
                        + "</ol>",
                    docx2Html.content());
              })
          .verifyComplete();
    }
  }

  @Test
  void testGetHtml_withThreeLvlOfNumberingListEntriesLowerLetter() {
    new TestDocumentGenerator(client, responseBytes, mlPackage, converter)
        .addContent("1", generateText("start test"))
        .addContent(
            "2",
            generateNumberingListEntry(
                "lower letter list entry 1",
                DocUnitNumberingListNumberFormat.LOWER_LETTER,
                "0",
                "0"))
        .addContent(
            "3",
            generateNumberingListEntry(
                "lower letter list entry 2",
                DocUnitNumberingListNumberFormat.LOWER_LETTER,
                "0",
                "0"))
        .addContent(
            "4",
            generateNumberingListEntry(
                "lower letter list entry 2.1",
                DocUnitNumberingListNumberFormat.LOWER_LETTER,
                "1",
                "1"))
        .addContent(
            "5",
            generateNumberingListEntry(
                "lower letter list entry 2.2",
                DocUnitNumberingListNumberFormat.LOWER_LETTER,
                "1",
                "1"))
        .addContent(
            "6",
            generateNumberingListEntry(
                "lower letter list entry 2.2.1",
                DocUnitNumberingListNumberFormat.LOWER_LETTER,
                "1",
                "2"))
        .addContent(
            "7",
            generateNumberingListEntry(
                "lower letter list entry 2.2.2",
                DocUnitNumberingListNumberFormat.LOWER_LETTER,
                "1",
                "2"))
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
                    "<p>start test</p>"
                        + "<ol class=\"lower-letter\">"
                        + "<li><p>lower letter list entry 1</p></li>"
                        + "<li><p>lower letter list entry 2</p></li>"
                        + "<ol class=\"lower-letter\">"
                        + "<li><p>lower letter list entry 2.1</p></li>"
                        + "<li><p>lower letter list entry 2.2</p></li>"
                        + "<ol class=\"lower-letter\">"
                        + "<li><p>lower letter list entry 2.2.1</p></li>"
                        + "<li><p>lower letter list entry 2.2.2</p></li>"
                        + "</ol>"
                        + "</ol>"
                        + "</ol>",
                    docx2Html.content());
              })
          .verifyComplete();
    }
  }

  @Test
  void testGetHtml_withThreeLvlOfNumberingListEntriesUpperLetter() {
    new TestDocumentGenerator(client, responseBytes, mlPackage, converter)
        .addContent("1", generateText("start test"))
        .addContent(
            "2",
            generateNumberingListEntry(
                "upper letter list entry 1",
                DocUnitNumberingListNumberFormat.UPPER_LETTER,
                "0",
                "0"))
        .addContent(
            "3",
            generateNumberingListEntry(
                "upper letter list entry 2",
                DocUnitNumberingListNumberFormat.UPPER_LETTER,
                "0",
                "0"))
        .addContent(
            "4",
            generateNumberingListEntry(
                "upper letter list entry 2.1",
                DocUnitNumberingListNumberFormat.UPPER_LETTER,
                "1",
                "1"))
        .addContent(
            "5",
            generateNumberingListEntry(
                "upper letter list entry 2.2",
                DocUnitNumberingListNumberFormat.UPPER_LETTER,
                "1",
                "1"))
        .addContent(
            "6",
            generateNumberingListEntry(
                "upper letter list entry 2.2.1",
                DocUnitNumberingListNumberFormat.UPPER_LETTER,
                "1",
                "2"))
        .addContent(
            "7",
            generateNumberingListEntry(
                "upper letter list entry 2.2.2",
                DocUnitNumberingListNumberFormat.UPPER_LETTER,
                "1",
                "2"))
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
                    "<p>start test</p>"
                        + "<ol class=\"upper-letter\">"
                        + "<li><p>upper letter list entry 1</p></li>"
                        + "<li><p>upper letter list entry 2</p></li>"
                        + "<ol class=\"upper-letter\">"
                        + "<li><p>upper letter list entry 2.1</p></li>"
                        + "<li><p>upper letter list entry 2.2</p></li>"
                        + "<ol class=\"upper-letter\">"
                        + "<li><p>upper letter list entry 2.2.1</p></li>"
                        + "<li><p>upper letter list entry 2.2.2</p></li>"
                        + "</ol>"
                        + "</ol>"
                        + "</ol>",
                    docx2Html.content());
              })
          .verifyComplete();
    }
  }

  @Test
  void testGetHtml_withThreeLvlOfNumberingListEntriesDecimal() {
    new TestDocumentGenerator(client, responseBytes, mlPackage, converter)
        .addContent("1", generateText("start test"))
        .addContent(
            "2",
            generateNumberingListEntry(
                "decimal list entry 1", DocUnitNumberingListNumberFormat.DECIMAL, "0", "0"))
        .addContent(
            "3",
            generateNumberingListEntry(
                "decimal list entry 2", DocUnitNumberingListNumberFormat.DECIMAL, "0", "0"))
        .addContent(
            "4",
            generateNumberingListEntry(
                "decimal list entry 2.1", DocUnitNumberingListNumberFormat.DECIMAL, "1", "1"))
        .addContent(
            "5",
            generateNumberingListEntry(
                "decimal list entry 2.2", DocUnitNumberingListNumberFormat.DECIMAL, "1", "1"))
        .addContent(
            "6",
            generateNumberingListEntry(
                "decimal list entry 2.2.1", DocUnitNumberingListNumberFormat.DECIMAL, "1", "2"))
        .addContent(
            "7",
            generateNumberingListEntry(
                "decimal list entry 2.2.2", DocUnitNumberingListNumberFormat.DECIMAL, "1", "2"))
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
                    "<p>start test</p>"
                        + "<ol class=\"decimal\">"
                        + "<li><p>decimal list entry 1</p></li>"
                        + "<li><p>decimal list entry 2</p></li>"
                        + "<ol class=\"decimal\">"
                        + "<li><p>decimal list entry 2.1</p></li>"
                        + "<li><p>decimal list entry 2.2</p></li>"
                        + "<ol class=\"decimal\">"
                        + "<li><p>decimal list entry 2.2.1</p></li>"
                        + "<li><p>decimal list entry 2.2.2</p></li>"
                        + "</ol>"
                        + "</ol>"
                        + "</ol>",
                    docx2Html.content());
              })
          .verifyComplete();
    }
  }

  @Test
  void testGetHtml_withThreeLvlOfNumberingListEntriesMixed() {
    new TestDocumentGenerator(client, responseBytes, mlPackage, converter)
        .addContent("1", generateText("start test"))
        .addContent(
            "2",
            generateNumberingListEntry(
                "decimal list entry 1", DocUnitNumberingListNumberFormat.DECIMAL, "0", "0"))
        .addContent(
            "3",
            generateNumberingListEntry(
                "decimal list entry 2", DocUnitNumberingListNumberFormat.DECIMAL, "0", "0"))
        .addContent(
            "4",
            generateNumberingListEntry(
                "bullet list entry 2.1", DocUnitNumberingListNumberFormat.BULLET, "1", "1"))
        .addContent(
            "5",
            generateNumberingListEntry(
                "bullet list entry 2.2", DocUnitNumberingListNumberFormat.BULLET, "1", "1"))
        .addContent(
            "6",
            generateNumberingListEntry(
                "decimal list entry 2.2.1", DocUnitNumberingListNumberFormat.DECIMAL, "1", "2"))
        .addContent(
            "7",
            generateNumberingListEntry(
                "decimal list entry 2.2.2", DocUnitNumberingListNumberFormat.DECIMAL, "1", "2"))
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
                    "<p>start test</p>"
                        + "<ol class=\"decimal\">"
                        + "<li><p>decimal list entry 1</p></li>"
                        + "<li><p>decimal list entry 2</p></li>"
                        + "<ul>"
                        + "<li><p>bullet list entry 2.1</p></li>"
                        + "<li><p>bullet list entry 2.2</p></li>"
                        + "<ol class=\"decimal\">"
                        + "<li><p>decimal list entry 2.2.1</p></li>"
                        + "<li><p>decimal list entry 2.2.2</p></li>"
                        + "</ol>"
                        + "</ul>"
                        + "</ol>",
                    docx2Html.content());
              })
          .verifyComplete();
    }
  }

  @Test
  void testGetHtml_withTwoNumberingListEntriesAndMiddleText() {
    new TestDocumentGenerator(client, responseBytes, mlPackage, converter)
        .addContent("1", generateText("start test"))
        .addContent(
            "2",
            generateNumberingListEntry(
                "bullet list entry 1", DocUnitNumberingListNumberFormat.BULLET, "0", "0"))
        .addContent(
            "3",
            generateNumberingListEntry(
                "bullet list entry 2", DocUnitNumberingListNumberFormat.BULLET, "0", "0"))
        .addContent("4", generateText("middle text"))
        .addContent(
            "5",
            generateNumberingListEntry(
                "decimal list entry 1", DocUnitNumberingListNumberFormat.DECIMAL, "1", "0"))
        .addContent(
            "6",
            generateNumberingListEntry(
                "decimal list entry 2", DocUnitNumberingListNumberFormat.DECIMAL, "1", "0"))
        .addContent("7", generateText("end text"))
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
                    "<p>start test</p>"
                        + "<ul><li><p>bullet list entry 1</p></li><li><p>bullet list entry 2</p></li></ul>"
                        + "<p>middle text</p>"
                        + "<ol class=\"decimal\"><li><p>decimal list entry 1</p></li><li><p>decimal list entry 2</p></li></ol>"
                        + "<p>end text</p>",
                    docx2Html.content());
              })
          .verifyComplete();
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
    List<DocUnitDocx> paragraphElements = List.of(generateText(text));
    List<TableCellElement> cells = List.of(new TableCellElement(paragraphElements));
    List<TableRowElement> rows = List.of(new TableRowElement(cells));

    return new TableElement(rows);
  }

  private DocUnitDocx generateNumberingListEntry(
      String text, DocUnitNumberingListNumberFormat numberFormat, String numId, String iLvl) {
    var paragraphElement = generateText(text);

    return new NumberingListEntry(paragraphElement, numberFormat, numId, iLvl);
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

    private TestDocumentGenerator addContent(String id, DocUnitDocx docUnitDocx) {
      ids.add(id);
      when(converter.convert(id)).thenReturn(docUnitDocx);

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
