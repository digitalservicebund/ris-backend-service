package de.bund.digitalservice.ris.domain;

import de.bund.digitalservice.ris.domain.docx.DocUnitDocx;
import de.bund.digitalservice.ris.domain.docx.DocUnitRandnummer;
import de.bund.digitalservice.ris.domain.docx.DocUnitTextElement;
import de.bund.digitalservice.ris.domain.docx.Docx2Html;
import de.bund.digitalservice.ris.utils.DocxParagraphConverter;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import lombok.extern.slf4j.Slf4j;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
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
@Slf4j
public class DocxConverterService {
  private final S3AsyncClient client;
  private final DocumentBuilderFactory documentBuilderFactory;

  @Value("${otc.obs.bucket-name}")
  private String bucketName;

  public DocxConverterService(S3AsyncClient client, DocumentBuilderFactory documentBuilderFactory) {
    this.client = client;
    this.documentBuilderFactory = documentBuilderFactory;
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
    DocUnitRandnummer[] lastRandnummer = {null};

    WordprocessingMLPackage mlPackage;
    try {
      mlPackage = WordprocessingMLPackage.load(inputStream);
    } catch (Docx4JException e) {
      throw new DocxConverterException("Couldn't load docx file!", e);
    }

    mlPackage.getMainDocumentPart().getContent().stream()
        .map(DocxParagraphConverter::convert)
        .filter(Objects::nonNull)
        .forEach(
            element -> {
              if (lastRandnummer[0] == null && !(element instanceof DocUnitRandnummer)) {
                packedList.add(element);
              } else if (element instanceof DocUnitRandnummer randnummer) {
                if (lastRandnummer[0] != null) {
                  packedList.add(lastRandnummer[0]);
                }
                lastRandnummer[0] = randnummer;
              } else if (element instanceof DocUnitTextElement textElement) {
                lastRandnummer[0].setTextContent(textElement.getText());
                packedList.add(lastRandnummer[0]);
                lastRandnummer[0] = null;
              }
            });

    Docx2Html docx2Html = new Docx2Html();
    docx2Html.setContent(
        packedList.stream().map(DocUnitDocx::toHtmlString).collect(Collectors.joining()));
    return docx2Html;
  }
}
