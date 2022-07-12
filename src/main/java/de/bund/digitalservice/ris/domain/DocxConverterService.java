package de.bund.digitalservice.ris.domain;

import de.bund.digitalservice.ris.domain.docx.DocUnitBorderNumber;
import de.bund.digitalservice.ris.domain.docx.DocUnitDocx;
import de.bund.digitalservice.ris.domain.docx.DocUnitNumberingList;
import de.bund.digitalservice.ris.domain.docx.DocUnitNumberingListEntry;
import de.bund.digitalservice.ris.domain.docx.DocUnitParagraphElement;
import de.bund.digitalservice.ris.domain.docx.Docx2Html;
import de.bund.digitalservice.ris.utils.DocxConverter;
import de.bund.digitalservice.ris.utils.DocxConverterException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.docx4j.model.listnumbering.ListNumberingDefinition;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.BinaryPartAbstractImage;
import org.docx4j.openpackaging.parts.WordprocessingML.ImageJpegPart;
import org.docx4j.openpackaging.parts.WordprocessingML.ImagePngPart;
import org.docx4j.openpackaging.parts.WordprocessingML.MetafileEmfPart;
import org.docx4j.wml.Style;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.async.AsyncResponseTransformer;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.S3Object;

@Service
public class DocxConverterService {
  private static final Logger LOGGER = LoggerFactory.getLogger(DocxConverterService.class);

  private final S3AsyncClient client;
  private final DocumentBuilderFactory documentBuilderFactory;
  private final DocxConverter converter;

  @Value("${otc.obs.bucket-name}")
  private String bucketName;

  public DocxConverterService(
      S3AsyncClient client,
      DocumentBuilderFactory documentBuilderFactory,
      DocxConverter converter) {
    this.client = client;
    this.documentBuilderFactory = documentBuilderFactory;
    this.converter = converter;
  }

  public String getOriginalText(WordprocessingMLPackage mlPackage) {
    if (mlPackage == null) {
      return "<no word file selected>";
    }

    String originalText;
    originalText = mlPackage.getMainDocumentPart().getXML();

    try {
      DocumentBuilder dBuilder = documentBuilderFactory.newDocumentBuilder();
      Document doc = dBuilder.parse(new InputSource(new StringReader(originalText)));
      XPath xPath = XPathFactory.newInstance().newXPath();
      String expression = "/document//t";
      NodeList nodeList =
          (NodeList) xPath.compile(expression).evaluate(doc, XPathConstants.NODESET);

      StringBuilder sb = new StringBuilder();
      for (var i = 0; i < nodeList.getLength(); i++) {
        sb.append(nodeList.item(i).getTextContent());
      }
      originalText = sb.toString();
    } catch (ParserConfigurationException
        | IOException
        | SAXException
        | XPathExpressionException e) {
      throw new DocxConverterException("Couldn't read all text elements of docx xml!", e);
    }

    return originalText;
  }

  public Mono<ResponseEntity<List<String>>> getDocxFiles() {
    ListObjectsV2Request request = ListObjectsV2Request.builder().bucket(bucketName).build();

    CompletableFuture<ListObjectsV2Response> futureResponse = client.listObjectsV2(request);

    return Mono.fromFuture(futureResponse)
        .map(
            response -> {
              List<String> keys = response.contents().stream().map(S3Object::key).toList();
              return ResponseEntity.ok(keys);
            });
  }

  public Mono<Docx2Html> getHtml(String fileName) {
    GetObjectRequest request = GetObjectRequest.builder().bucket(bucketName).key(fileName).build();

    CompletableFuture<ResponseBytes<GetObjectResponse>> futureResponse =
        client.getObject(request, AsyncResponseTransformer.toBytes());

    return Mono.fromFuture(futureResponse)
        .map(response -> getDocumentParagraphs(response.asInputStream()));
  }

  private Docx2Html getDocumentParagraphs(InputStream inputStream) {
    if (inputStream == null) {
      return Docx2Html.EMPTY;
    }

    List<DocUnitDocx> packedList = new ArrayList<>();
    DocUnitBorderNumber[] lastBorderNumber = {null};
    DocUnitNumberingList[] lastNumberingList = {null};

    WordprocessingMLPackage mlPackage;
    try {
      mlPackage = WordprocessingMLPackage.load(inputStream);
    } catch (Docx4JException e) {
      throw new DocxConverterException("Couldn't load docx file!", e);
    }

    converter.setStyles(readStyles(mlPackage));
    converter.setImages(readImages(mlPackage));
    converter.setNumbering(readNumbering(mlPackage));

    mlPackage.getMainDocumentPart().getContent().stream()
        .peek(el -> LOGGER.info("element: {}", el))
        .map(converter::convert)
        .filter(Objects::nonNull)
        .forEach(
            element -> {
              if (packBorderNumberElements(element, packedList, lastBorderNumber)) {
                return;
              }

              if (packNumberingListEntries(element, packedList, lastNumberingList)) {
                return;
              }

              packedList.add(element);
            });

    var content = packedList.stream().map(DocUnitDocx::toHtmlString).collect(Collectors.joining());

    return new Docx2Html(content);
  }

  private boolean packBorderNumberElements(
      DocUnitDocx element, List<DocUnitDocx> packedList, DocUnitBorderNumber[] lastBorderNumber) {
    if (lastBorderNumber[0] == null && !(element instanceof DocUnitBorderNumber)) {
      return false;
    }

    DocUnitBorderNumber last = lastBorderNumber[0];
    boolean packed = false;
    if (element instanceof DocUnitBorderNumber borderNumber) {
      lastBorderNumber[0] = borderNumber;
      if (last != null) {
        packedList.add(last);
      }
      packed = true;
    } else if (element instanceof DocUnitParagraphElement paragraphElement) {
      lastBorderNumber[0] = null;
      last.addParagraphElement(paragraphElement);
      packedList.add(last);
      packed = true;
    } else {
      lastBorderNumber[0] = null;
    }

    return packed;
  }

  private boolean packNumberingListEntries(
      DocUnitDocx element, List<DocUnitDocx> packedList, DocUnitNumberingList[] lastNumberingList) {
    if (lastNumberingList[0] == null && !(element instanceof DocUnitNumberingListEntry)) {
      return false;
    }

    boolean packed = false;
    DocUnitNumberingList last = lastNumberingList[0];
    if (element instanceof DocUnitNumberingListEntry numberingListEntry) {
      if (last == null
          || !last.getNumId().equals(numberingListEntry.numId())
          || !last.getiLvl().equals(numberingListEntry.iLvl())) {
        if (last != null) {
          packedList.add(last);
        }
        lastNumberingList[0] =
            new DocUnitNumberingList(
                numberingListEntry.numberFormat(),
                numberingListEntry.numId(),
                numberingListEntry.iLvl());
        lastNumberingList[0].addNumberingListEntry(numberingListEntry);
      } else {
        last.addNumberingListEntry(numberingListEntry);
      }
      packed = true;
    } else {
      packedList.add(lastNumberingList[0]);
      lastNumberingList[0] = null;
    }

    return packed;
  }

  private Map<String, ListNumberingDefinition> readNumbering(WordprocessingMLPackage mlPackage) {
    if (mlPackage == null
        || mlPackage.getMainDocumentPart() == null
        || mlPackage.getMainDocumentPart().getNumberingDefinitionsPart() == null) {
      return Collections.emptyMap();
    }

    return mlPackage
        .getMainDocumentPart()
        .getNumberingDefinitionsPart()
        .getInstanceListDefinitions();
  }

  private Map<String, Style> readStyles(WordprocessingMLPackage mlPackage) {
    if (mlPackage == null
        || mlPackage.getMainDocumentPart() == null
        || mlPackage.getMainDocumentPart().getStyleDefinitionsPart() == null) {
      return Collections.emptyMap();
    }

    return mlPackage
        .getMainDocumentPart()
        .getStyleDefinitionsPart()
        .getJaxbElement()
        .getStyle()
        .stream()
        .collect(Collectors.toMap(k -> k.getName().getVal(), Function.identity()));
  }

  private Map<String, BinaryPartAbstractImage> readImages(WordprocessingMLPackage mlPackage) {
    if (mlPackage == null
        || mlPackage.getParts() == null
        || mlPackage.getParts().getParts() == null) {
      return Collections.emptyMap();
    }

    Map<String, BinaryPartAbstractImage> images = new HashMap<>();

    mlPackage
        .getParts()
        .getParts()
        .values()
        .forEach(
            part -> {
              if (part instanceof ImageJpegPart jpegPart) {
                part.getSourceRelationships()
                    .forEach(relationship -> images.put(relationship.getId(), jpegPart));
              } else if (part instanceof MetafileEmfPart emfPart) {
                part.getSourceRelationships()
                    .forEach(relationship -> images.put(relationship.getId(), emfPart));
              } else if (part instanceof ImagePngPart pngPart) {
                part.getSourceRelationships()
                    .forEach(relationship -> images.put(relationship.getId(), pngPart));
              } else if (part instanceof BinaryPartAbstractImage imagePart) {
                throw new DocxConverterException(
                    "unknown image file format: " + imagePart.getClass().getName());
              }
            });

    return images;
  }
}
