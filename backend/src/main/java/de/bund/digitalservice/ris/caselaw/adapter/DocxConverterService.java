package de.bund.digitalservice.ris.caselaw.adapter;

import de.bund.digitalservice.ris.caselaw.adapter.converter.docx.DocumentUnitDocxListUtils;
import de.bund.digitalservice.ris.caselaw.adapter.converter.docx.DocxConverter;
import de.bund.digitalservice.ris.caselaw.adapter.converter.docx.DocxConverterException;
import de.bund.digitalservice.ris.caselaw.domain.ConverterService;
import de.bund.digitalservice.ris.caselaw.domain.docx.DocumentUnitDocx;
import de.bund.digitalservice.ris.caselaw.domain.docx.Docx2Html;
import de.bund.digitalservice.ris.caselaw.domain.docx.DocxImagePart;
import java.awt.Dimension;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
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
import lombok.extern.slf4j.Slf4j;
import org.docx4j.model.listnumbering.ListNumberingDefinition;
import org.docx4j.openpackaging.contenttype.ContentTypes;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.BinaryPartAbstractImage;
import org.docx4j.openpackaging.parts.WordprocessingML.ImageJpegPart;
import org.docx4j.openpackaging.parts.WordprocessingML.ImagePngPart;
import org.docx4j.openpackaging.parts.WordprocessingML.MetafileEmfPart;
import org.docx4j.wml.Style;
import org.freehep.graphics2d.VectorGraphics;
import org.freehep.graphicsio.ImageConstants;
import org.freehep.graphicsio.ImageGraphics2D;
import org.freehep.graphicsio.emf.EMFInputStream;
import org.freehep.graphicsio.emf.EMFPanel;
import org.freehep.graphicsio.emf.EMFRenderer;
import org.springframework.beans.factory.annotation.Value;
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
@Slf4j
public class DocxConverterService implements ConverterService {
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

  public Mono<List<String>> getDocxFiles() {
    ListObjectsV2Request request = ListObjectsV2Request.builder().bucket(bucketName).build();

    CompletableFuture<ListObjectsV2Response> futureResponse = client.listObjectsV2(request);

    return Mono.fromFuture(futureResponse)
        .map(response -> response.contents().stream().map(S3Object::key).toList());
  }

  public Mono<Docx2Html> getConvertedObject(String fileName) {
    GetObjectRequest request = GetObjectRequest.builder().bucket(bucketName).key(fileName).build();

    CompletableFuture<ResponseBytes<GetObjectResponse>> futureResponse =
        client.getObject(request, AsyncResponseTransformer.toBytes());

    return Mono.fromFuture(futureResponse)
        .map(response -> parseAsDocumentUnitDocxList(response.asInputStream()))
        .map(
            documentUnitDocxList -> {
              List<DocumentUnitDocx> packedList =
                  DocumentUnitDocxListUtils.packList(documentUnitDocxList);
              String content = null;
              if (!packedList.isEmpty()) {
                content =
                    packedList.stream()
                        .map(DocumentUnitDocx::toHtmlString)
                        .collect(Collectors.joining());
              }
              return new Docx2Html(content);
            })
        .doOnError(ex -> log.error("Couldn't convert docx", ex));
  }

  public List<DocumentUnitDocx> parseAsDocumentUnitDocxList(InputStream inputStream) {
    if (inputStream == null) {
      return Collections.emptyList();
    }

    WordprocessingMLPackage mlPackage;
    try {
      mlPackage = WordprocessingMLPackage.load(inputStream);
    } catch (Docx4JException e) {
      throw new DocxConverterException("Couldn't load docx file!", e);
    }

    converter.setStyles(readStyles(mlPackage));
    converter.setImages(readImages(mlPackage));
    converter.setNumbering(readNumbering(mlPackage));

    List<DocumentUnitDocx> documentUnitDocxList =
        mlPackage.getMainDocumentPart().getContent().stream()
            .map(converter::convert)
            .filter(Objects::nonNull)
            .toList();

    DocumentUnitDocxListUtils.postprocessBorderNumbers(documentUnitDocxList);

    return documentUnitDocxList;
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
        .collect(Collectors.toMap(k -> k.getStyleId(), Function.identity()));
  }

  private Map<String, DocxImagePart> readImages(WordprocessingMLPackage mlPackage) {
    if (mlPackage == null
        || mlPackage.getParts() == null
        || mlPackage.getParts().getParts() == null) {
      return Collections.emptyMap();
    }

    Map<String, DocxImagePart> images = new HashMap<>();

    mlPackage
        .getParts()
        .getParts()
        .values()
        .forEach(
            part -> {
              if (part instanceof ImageJpegPart jpegPart) {
                part.getSourceRelationships()
                    .forEach(
                        relationship ->
                            images.put(
                                relationship.getId(),
                                new DocxImagePart(jpegPart.getContentType(), jpegPart.getBytes())));
              } else if (part instanceof MetafileEmfPart emfPart) {
                convertEMF(mlPackage, images, emfPart);
              } else if (part instanceof ImagePngPart pngPart) {
                part.getSourceRelationships()
                    .forEach(
                        relationship ->
                            images.put(
                                relationship.getId(),
                                new DocxImagePart(pngPart.getContentType(), pngPart.getBytes())));
              } else if (part instanceof BinaryPartAbstractImage imagePart) {
                throw new DocxConverterException(
                    "unknown image file format: " + imagePart.getClass().getName());
              }
            });

    return images;
  }

  private void convertEMF(
      WordprocessingMLPackage mlPackage,
      Map<String, DocxImagePart> images,
      MetafileEmfPart emfPart) {
    try {
      EMFInputStream emf = new EMFInputStream(new ByteArrayInputStream(emfPart.getBytes()));
      EMFRenderer renderer = new EMFRenderer(emf);

      EMFPanel emfPanel = new EMFPanel();
      emfPanel.setRenderer(renderer);

      ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
      VectorGraphics g =
          new ImageGraphics2D(
              byteArrayOutputStream,
              new Dimension(emfPanel.getWidth(), emfPanel.getHeight()),
              ImageConstants.PNG);
      g.startExport();
      emfPanel.print(g);
      g.endExport();
      byteArrayOutputStream.close();

      emfPart
          .getSourceRelationships()
          .forEach(
              relationship ->
                  images.put(
                      relationship.getId(),
                      new DocxImagePart(
                          ContentTypes.IMAGE_PNG, byteArrayOutputStream.toByteArray())));
    } catch (Exception ex) {
      log.error("Couldn't convert emf to png", ex);
    }
  }
}
