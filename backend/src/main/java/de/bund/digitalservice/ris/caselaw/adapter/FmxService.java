package de.bund.digitalservice.ris.caselaw.adapter;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.AttachmentDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.AttachmentRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDocumentationUnitRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentationUnitDTO;
import de.bund.digitalservice.ris.caselaw.adapter.exception.FmxTransformationException;
import de.bund.digitalservice.ris.caselaw.domain.CoreData;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnit;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnitRepository;
import de.bund.digitalservice.ris.caselaw.domain.FmxRepository;
import de.bund.digitalservice.ris.caselaw.domain.LongTexts;
import de.bund.digitalservice.ris.caselaw.domain.court.Court;
import de.bund.digitalservice.ris.caselaw.domain.court.CourtRepository;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mapping.MappingException;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

@Service
@Slf4j
public class FmxService {

  public static final String FILE_NUMBER_XPATH = "//REF.CASE/NO.CASE";
  public static final String ECLI_XPATH = "//NO.ECLI/@ECLI";
  public static final String CELEX_XPATH = "//NO.CELEX";
  public static final String AUTHOR_XPATH = "//AUTHOR";
  public static final String DATE_XPATH = "//CURR.TITLE/PAGE.HEADER/P/HT/DATE/@ISO";
  public static final String TENOR_XPATH = "//JURISDICTION";
  public static final String REASONS_XPATH = "//CONTENTS.JUDGMENT | //CONTENTS.ORDER";
  public static final String SIGNATURES_XPATH = "//SIGNATURE.CASE";

  private final DocumentationUnitRepository documentationUnitRepository;
  private final CourtRepository courtRepository;
  private final FmxRepository fmxRepository;
  private final AttachmentRepository attachmentRepository;
  private final DatabaseDocumentationUnitRepository databaseDocumentationUnitRepository;

  private Transformer xsltTransformer;
  private final Templates fmxToHtml;

  private final XPath xPath;

  public FmxService(
      DocumentationUnitRepository documentationUnitRepository,
      CourtRepository courtRepository,
      FmxRepository fmxRepository,
      AttachmentRepository attachmentRepository,
      DatabaseDocumentationUnitRepository databaseDocumentationUnitRepository,
      XmlUtilService xmlUtilService) {
    this.documentationUnitRepository = documentationUnitRepository;
    this.courtRepository = courtRepository;
    this.fmxRepository = fmxRepository;
    this.attachmentRepository = attachmentRepository;
    this.databaseDocumentationUnitRepository = databaseDocumentationUnitRepository;

    fmxToHtml = xmlUtilService.getTemplates("caselawhandover/fmxToHtml.xslt");
    xPath = XPathFactory.newInstance().newXPath();
  }

  public void getDataFromEurlex(String celexNumber, DocumentationUnit documentationUnit) {
    String fmxFileContent = null;
    String sourceUrl = "https://publications.europa.eu/resource/cellar/" + celexNumber + "/zip";
    try {
      HttpClient client =
          HttpClient.newBuilder()
              .followRedirects(HttpClient.Redirect.ALWAYS) // follow redirects
              .build();

      HttpRequest request =
          HttpRequest.newBuilder()
              .uri(new URI(sourceUrl))
              .GET()
              .header("Content-Type", "application/zip;mtype=fmx4")
              .header("Accept-Language", "de")
              .build();

      HttpResponse<byte[]> response = client.send(request, HttpResponse.BodyHandlers.ofByteArray());
      ZipInputStream zipInputStream = new ZipInputStream(new ByteArrayInputStream(response.body()));

      ZipEntry entry;
      while ((entry = zipInputStream.getNextEntry()) != null) {
        if (entry.getName().endsWith(".xml")) {
          fmxFileContent = new String(zipInputStream.readAllBytes(), StandardCharsets.UTF_8);
        }
      }
    } catch (IOException | InterruptedException | URISyntaxException ex) {
      throw new FmxTransformationException("Downloading FMX file from Eurlex Database failed.", ex);
    }

    if (fmxFileContent != null && !fmxFileContent.isBlank()) {
      attachFmxToDocumentationUnit(documentationUnit.uuid(), fmxFileContent, sourceUrl);
      extractMetaDataFromFmx(fmxFileContent, documentationUnit);
    }
  }

  private void attachFmxToDocumentationUnit(
      UUID documentationUnitId, String fmxFileContent, String sourceUrl) {
    fmxRepository.attachFmxToDocumentationUnit(documentationUnitId, fmxFileContent, sourceUrl);
    DocumentationUnitDTO documentationUnitDTO =
        databaseDocumentationUnitRepository.findById(documentationUnitId).orElseThrow();

    AttachmentDTO attachmentDTO =
        AttachmentDTO.builder()
            .documentationUnit(documentationUnitDTO)
            .filename("Originalentscheidung")
            .format("fmx")
            .uploadTimestamp(Instant.now())
            .build();

    attachmentRepository.save(attachmentDTO);
  }

  public void extractMetaDataFromFmx(String fileContent, DocumentationUnit documentationUnit) {
    if (fileContent == null || fileContent.isBlank()) {
      throw new FmxTransformationException("FMX file has no content.");
    }
    xsltTransformer = initialiseXsltTransformer();
    try {
      final Document doc = parseFmx(fileContent);
      String fileNumber = xPath.compile(FILE_NUMBER_XPATH).evaluate(doc);
      String ecli = xPath.compile(ECLI_XPATH).evaluate(doc);
      String celex = xPath.compile(CELEX_XPATH).evaluate(doc);
      String author = xPath.compile(AUTHOR_XPATH).evaluate(doc);
      String decisionDate = xPath.compile(DATE_XPATH).evaluate(doc);
      Node tenor = (Node) xPath.compile(TENOR_XPATH).evaluate(doc, XPathConstants.NODE);
      Node reasons = (Node) xPath.compile(REASONS_XPATH).evaluate(doc, XPathConstants.NODE);
      Node signatures = (Node) xPath.compile(SIGNATURES_XPATH).evaluate(doc, XPathConstants.NODE);
      reasons.removeChild(tenor);
      reasons.appendChild(signatures);

      CoreData.CoreDataBuilder coreDataBuilder = documentationUnit.coreData().toBuilder();
      LongTexts.LongTextsBuilder longTextsBuilder = documentationUnit.longTexts().toBuilder();

      coreDataBuilder.decisionDate(
          LocalDate.parse(decisionDate, DateTimeFormatter.ofPattern("yyyyMMdd")));
      coreDataBuilder.ecli(ecli);
      coreDataBuilder.fileNumbers(List.of(fileNumber));
      coreDataBuilder.celexNumber(celex);
      coreDataBuilder.court(transformCourt(author).orElse(null));

      longTextsBuilder.tenor(transformLongText(tenor));
      longTextsBuilder.reasons(transformLongText(reasons));

      CoreData coreData = coreDataBuilder.build();
      LongTexts longTexts = longTextsBuilder.build();

      DocumentationUnit updatedDocumentationUnit =
          documentationUnit.toBuilder().coreData(coreData).longTexts(longTexts).build();

      documentationUnitRepository.save(updatedDocumentationUnit);
    } catch (XPathExpressionException exception) {
      throw new FmxTransformationException("Failed to extract data from FMX file.", exception);
    }
  }

  private Transformer initialiseXsltTransformer() {
    try {
      return fmxToHtml.newTransformer();
    } catch (TransformerConfigurationException e) {
      throw new FmxTransformationException("Failed to initialise XSLT transformer.", e);
    }
  }

  private DocumentBuilder initialiseDocumentBuilder() {
    final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    try {
      factory.setIgnoringComments(true);
      factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
      return factory.newDocumentBuilder();
    } catch (ParserConfigurationException e) {
      throw new FmxTransformationException("Failed to initialise document builder.", e);
    }
  }

  private Document parseFmx(String fmxFileContent) {
    try {
      final DocumentBuilder builder = initialiseDocumentBuilder();
      final Document doc;
      doc = builder.parse(new ByteArrayInputStream(fmxFileContent.getBytes()));
      doc.getDocumentElement().normalize();
      return doc;
    } catch (SAXException | IOException e) {
      throw new FmxTransformationException("Failed to parse FMX file content.", e);
    }
  }

  private Optional<Court> transformCourt(String author) {
    var authorToCourtMap = new HashMap<String, String>();
    authorToCourtMap.put("CJ", "EuGH");
    authorToCourtMap.put("T", "EuG");

    var courtType = authorToCourtMap.get(author);
    Optional<Court> court = Optional.empty();
    if (courtType != null) {
      court = courtRepository.findByTypeAndLocation(courtType, null);
    }
    if (court.isEmpty() && courtType != null) {
      court = courtRepository.findUniqueBySearchString(courtType);
    }
    return court;
  }

  private String transformLongText(Node textNode) {
    try {
      StringWriter xsltOutput = new StringWriter();
      xsltTransformer.transform(new DOMSource(textNode), new StreamResult(xsltOutput));
      return xsltOutput.toString();
    } catch (TransformerException e) {
      log.error("Xslt transformation error.", e);
      throw new MappingException(e.getMessage());
    }
  }
}
