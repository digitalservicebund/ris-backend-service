package de.bund.digitalservice.ris.caselaw.adapter;

import de.bund.digitalservice.ris.caselaw.adapter.converter.docx.DocumentUnitDocxListUtils;
import de.bund.digitalservice.ris.caselaw.adapter.converter.docx.DocxConverter;
import de.bund.digitalservice.ris.caselaw.adapter.converter.docx.DocxConverterException;
import de.bund.digitalservice.ris.caselaw.adapter.converter.docx.FooterConverter;
import de.bund.digitalservice.ris.caselaw.domain.ConverterService;
import de.bund.digitalservice.ris.caselaw.domain.docx.DocumentUnitDocx;
import de.bund.digitalservice.ris.caselaw.domain.docx.Docx2Html;
import de.bund.digitalservice.ris.caselaw.domain.docx.DocxImagePart;
import de.bund.digitalservice.ris.caselaw.domain.docx.ECLIElement;
import de.bund.digitalservice.ris.caselaw.domain.docx.FooterElement;
import de.bund.digitalservice.ris.caselaw.domain.docx.ParagraphElement;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
import org.docx4j.model.structure.HeaderFooterPolicy;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.BinaryPartAbstractImage;
import org.docx4j.openpackaging.parts.WordprocessingML.ImageJpegPart;
import org.docx4j.openpackaging.parts.WordprocessingML.ImagePngPart;
import org.docx4j.openpackaging.parts.WordprocessingML.MetafileEmfPart;
import org.docx4j.wml.Style;
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

  /**
   * Convert docx file to a object with the html content of the word file and some metadata
   * extracted of the docx file.
   *
   * @param fileName name of the file in the bucket
   * @return the generated object with html content and metadata, if the file name is null a empty
   *     mono is returned
   */
  public Mono<Docx2Html> getConvertedObject(String fileName) {
    if (fileName == null) {
      return Mono.empty();
    }

    GetObjectRequest request = GetObjectRequest.builder().bucket(bucketName).key(fileName).build();

    CompletableFuture<ResponseBytes<GetObjectResponse>> futureResponse =
        client.getObject(request, AsyncResponseTransformer.toBytes());

    return Mono.fromFuture(futureResponse)
        .map(response -> parseAsDocumentUnitDocxList(response.asInputStream()))
        .map(
            documentUnitDocxList -> {
              List<DocumentUnitDocx> packedList =
                  DocumentUnitDocxListUtils.packList(documentUnitDocxList);

              List<String> ecliList =
                  packedList.stream()
                      .filter(ECLIElement.class::isInstance)
                      .map(ECLIElement.class::cast)
                      .map(ECLIElement::getText)
                      .toList();

              String content = null;
              if (!packedList.isEmpty()) {
                content =
                    packedList.stream()
                        .map(DocumentUnitDocx::toHtmlString)
                        .collect(Collectors.joining());
              }

              return new Docx2Html(content, ecliList);
            })
        .doOnError(ex -> log.error("Couldn't convert docx", ex));
  }

  /**
   * Convert the content file (docx) into a list of DocumentUnitDocx elements. Read the styles,
   * images, footers and numbering definitions from the docx file.
   *
   * @param inputStream input stream of the content file
   * @return list of DocumentUnitDocx elements
   */
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
    converter.setFooters(readFooters(mlPackage, converter));
    converter.setListNumberingDefinitions(readListNumberingDefinitions(mlPackage));

    List<DocumentUnitDocx> documentUnitDocxList =
        mlPackage.getMainDocumentPart().getContent().stream()
            .map(converter::convert)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());

    Set<FooterElement> footerElements = parseFooterAndIdentifyECLI();
    documentUnitDocxList.addAll(
        0, footerElements.stream().filter(ECLIElement.class::isInstance).toList());
    documentUnitDocxList.addAll(
        footerElements.stream()
            .filter(footerElement -> !(footerElement instanceof ECLIElement))
            .toList());

    DocumentUnitDocxListUtils.postProcessBorderNumbers(documentUnitDocxList);

    return documentUnitDocxList;
  }

  private Set<FooterElement> parseFooterAndIdentifyECLI() {
    Set<FooterElement> footerElements = new HashSet<>();

    // Check if footers are null
    if (converter.getFooters() == null) {
      return footerElements;
    }
    converter
        .getFooters()
        .forEach(
            footer -> {
              if (footer == null || footer.getText() == null) {
                return;
              }

              if (isECLI(footer.getText())) {
                footerElements.add(new ECLIElement(footer));
              } else {
                footerElements.add(new FooterElement(footer));
              }
            });

    return footerElements;
  }

  /**
   * Check if the ECLI has the right format
   *
   * @see <a
   *     href="https://e-justice.europa.eu/175/EN/european_case_law_identifier_ecli?init=true">European
   *     Case Law Identifier</a>
   * @param ecli ECLI to check
   * @return true if the input value is equals to the format specification, else false
   */
  private boolean isECLI(String ecli) {
    if (!ecli.startsWith("ECLI")) {
      return false;
    }

    String[] parts = ecli.split(":");

    if (parts.length != 5) {
      return false;
    }

    if (!parts[0].equals("ECLI")) {
      return false;
    }

    if (parts[2].length() > 7) {
      return false;
    }

    if (parts[3].length() != 4) {
      return false;
    }

    try {
      Integer.parseInt(parts[3]);
    } catch (NumberFormatException ex) {
      return false;
    }

    Pattern pattern = Pattern.compile("[\\w.]{1,25}");
    Matcher matcher = pattern.matcher(parts[4]);

    return matcher.matches();
  }

  private List<ParagraphElement> readFooters(
      WordprocessingMLPackage mlPackage, DocxConverter converter) {
    if (mlPackage == null
        || mlPackage.getDocumentModel() == null
        || mlPackage.getDocumentModel().getSections() == null) {
      return Collections.emptyList();
    }

    List<ParagraphElement> footers = new ArrayList<>();

    mlPackage
        .getDocumentModel()
        .getSections()
        .forEach(
            section -> {
              HeaderFooterPolicy headerFooterPolicy = section.getHeaderFooterPolicy();

              if (headerFooterPolicy.getDefaultFooter() != null) {
                footers.add(
                    FooterConverter.convert(
                        headerFooterPolicy.getDefaultFooter().getContent(), converter));
              }

              if (headerFooterPolicy.getFirstFooter() != null) {
                footers.add(
                    FooterConverter.convert(
                        headerFooterPolicy.getFirstFooter().getContent(), converter));
              }

              if (headerFooterPolicy.getEvenFooter() != null) {
                footers.add(
                    FooterConverter.convert(
                        headerFooterPolicy.getEvenFooter().getContent(), converter));
              }
            });

    return footers;
  }

  private Map<String, ListNumberingDefinition> readListNumberingDefinitions(
      WordprocessingMLPackage mlPackage) {
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
        .collect(Collectors.toMap(Style::getStyleId, Function.identity()));
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
                part.getSourceRelationships()
                    .forEach(
                        relationship ->
                            images.put(
                                relationship.getId(),
                                new DocxImagePart(emfPart.getContentType(), emfPart.getBytes())));
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
}
