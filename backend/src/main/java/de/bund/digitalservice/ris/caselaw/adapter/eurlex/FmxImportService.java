package de.bund.digitalservice.ris.caselaw.adapter.eurlex;

import de.bund.digitalservice.ris.caselaw.adapter.XmlUtilService;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.AttachmentDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.AttachmentRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.AttachmentS3DTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.AttachmentS3Repository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDocumentationUnitRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentationUnitDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.EurLexResultDTO;
import de.bund.digitalservice.ris.caselaw.adapter.exception.FmxImporterException;
import de.bund.digitalservice.ris.caselaw.domain.CoreData;
import de.bund.digitalservice.ris.caselaw.domain.Decision;
import de.bund.digitalservice.ris.caselaw.domain.DocumentTypeCategory;
import de.bund.digitalservice.ris.caselaw.domain.DocumentTypeRepository;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnitRepository;
import de.bund.digitalservice.ris.caselaw.domain.FmxRepository;
import de.bund.digitalservice.ris.caselaw.domain.InboxStatus;
import de.bund.digitalservice.ris.caselaw.domain.LongTexts;
import de.bund.digitalservice.ris.caselaw.domain.Source;
import de.bund.digitalservice.ris.caselaw.domain.SourceValue;
import de.bund.digitalservice.ris.caselaw.domain.TransformationService;
import de.bund.digitalservice.ris.caselaw.domain.User;
import de.bund.digitalservice.ris.caselaw.domain.court.Court;
import de.bund.digitalservice.ris.caselaw.domain.court.CourtRepository;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.documenttype.DocumentType;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
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
import org.apache.logging.log4j.util.Strings;
import org.springframework.data.mapping.MappingException;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

@Service
@Slf4j
public class FmxImportService implements TransformationService {

  public static final String FILE_NUMBER_XPATH = "//REF.CASE/NO.CASE";
  public static final String ECLI_XPATH = "//NO.ECLI/@ECLI";
  public static final String CELEX_XPATH = "//NO.CELEX";
  public static final String AUTHOR_XPATH = "//AUTHOR";
  public static final String DATE_XPATH =
      "//CURR.TITLE/PAGE.HEADER/P/HT/DATE/@ISO | //CURR.TITLE/PAGE.HEADER/P/DATE/@ISO";
  public static final String JURISDICTION_XPATH = "//JURISDICTION";
  public static final String CONTENTS_XPATH =
      "//CONTENTS.JUDGMENT | //CONTENTS.ORDER | //CONTENTS.OPINION";
  public static final String SIGNATURES_XPATH = "//SIGNATURE.CASE";
  public static final String PREAMBLE_GEN_XPATH = "//PREAMBLE.GEN";
  public static final String ENACTING_TERMS_CJT_XPATH = "//ENACTING.TERMS.CJT";
  public static final String FINAL_XPATH = "//FINAL";
  public static final String NOTE_XPATH = "//TITLE//NOTE";

  public static final String JUDGMENT_TYPE = "JUDGMENT";
  public static final String ORDER_TYPE = "ORDER";
  public static final String OPINION_TYPE = "OPINION";

  private final DocumentationUnitRepository documentationUnitRepository;
  private final CourtRepository courtRepository;
  private final DocumentTypeRepository documentTypeRepository;
  private final FmxRepository fmxRepository;
  private final AttachmentRepository attachmentRepository;
  private final AttachmentS3Repository attachmentS3Repository;
  private final DatabaseDocumentationUnitRepository databaseDocumentationUnitRepository;
  private final EurLexResultRepository eurLexResultRepository;
  private final EurlexRetrievalService eurlexRetrievalService;
  private final XmlUtilService xmlUtilService;

  private Transformer xsltTransformer;

  private final XPath xPath;

  public FmxImportService(
      DocumentationUnitRepository documentationUnitRepository,
      CourtRepository courtRepository,
      DocumentTypeRepository documentTypeRepository,
      FmxRepository fmxRepository,
      AttachmentRepository attachmentRepository,
      AttachmentS3Repository attachmentS3Repository,
      DatabaseDocumentationUnitRepository databaseDocumentationUnitRepository,
      EurLexResultRepository eurLexResultRepository,
      EurlexRetrievalService eurlexRetrievalService,
      XmlUtilService xmlUtilService) {
    this.documentationUnitRepository = documentationUnitRepository;
    this.courtRepository = courtRepository;
    this.documentTypeRepository = documentTypeRepository;
    this.fmxRepository = fmxRepository;
    this.attachmentRepository = attachmentRepository;
    this.attachmentS3Repository = attachmentS3Repository;
    this.databaseDocumentationUnitRepository = databaseDocumentationUnitRepository;
    this.eurLexResultRepository = eurLexResultRepository;
    this.eurlexRetrievalService = eurlexRetrievalService;
    this.xmlUtilService = xmlUtilService;

    xPath = XPathFactory.newInstance().newXPath();
  }

  public void getDataFromEurlex(String celexNumber, Decision decision, User user) {
    Optional<EurLexResultDTO> eurLexResultDTO =
        eurLexResultRepository.findByCelexNumber(celexNumber);
    if (eurLexResultDTO.isEmpty()) {
      throw new FmxImporterException(
          "Could not find matching Eurlex Result for Celex Number " + celexNumber);
    }
    String sourceUrl = eurLexResultDTO.get().getUri();
    String fmxFileContent = eurlexRetrievalService.requestSingleEurlexDocument(sourceUrl);

    if (Strings.isNotBlank(fmxFileContent)) {
      attachFmxToDocumentationUnit(decision.uuid(), fmxFileContent, sourceUrl);
      extractMetaDataFromFmx(fmxFileContent, decision, user);
    } else {
      throw new FmxImporterException("FMX file has no content.");
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

    AttachmentS3DTO attachmentS3DTO =
        AttachmentS3DTO.builder()
            .documentationUnit(documentationUnitDTO)
            .filename("Originalentscheidung")
            .format("fmx")
            .uploadTimestamp(Instant.now())
            .build();

    attachmentRepository.save(attachmentDTO);
    attachmentS3Repository.save(attachmentS3DTO);
  }

  private void extractMetaDataFromFmx(String fileContent, Decision decision, User user) {
    xsltTransformer = initialiseXsltTransformer();
    try {
      final Document doc = parseFmx(fileContent);
      String rootTag = doc.getDocumentElement().getTagName();
      String fileNumber = xPath.compile(FILE_NUMBER_XPATH).evaluate(doc);
      String ecli = xPath.compile(ECLI_XPATH).evaluate(doc);
      String celex = xPath.compile(CELEX_XPATH).evaluate(doc);
      String author = xPath.compile(AUTHOR_XPATH).evaluate(doc);
      String decisionDate = xPath.compile(DATE_XPATH).evaluate(doc);
      Node jurisdiction =
          (Node) xPath.compile(JURISDICTION_XPATH).evaluate(doc, XPathConstants.NODE);
      Node content = (Node) xPath.compile(CONTENTS_XPATH).evaluate(doc, XPathConstants.NODE);
      Node signatures = (Node) xPath.compile(SIGNATURES_XPATH).evaluate(doc, XPathConstants.NODE);
      Node preambleGen =
          (Node) xPath.compile(PREAMBLE_GEN_XPATH).evaluate(doc, XPathConstants.NODE);
      Node enactingTermsCjt =
          (Node) xPath.compile(ENACTING_TERMS_CJT_XPATH).evaluate(doc, XPathConstants.NODE);
      Node finalNode = (Node) xPath.compile(FINAL_XPATH).evaluate(doc, XPathConstants.NODE);
      Node note = (Node) xPath.compile(NOTE_XPATH).evaluate(doc, XPathConstants.NODE);

      CoreData.CoreDataBuilder coreDataBuilder = decision.coreData().toBuilder();
      LongTexts.LongTextsBuilder longTextsBuilder = decision.longTexts().toBuilder();

      coreDataBuilder.sources(List.of(Source.builder().value(SourceValue.L).build()));
      CoreData coreData =
          transformCoreData(
              coreDataBuilder, rootTag, decisionDate, ecli, fileNumber, celex, author);

      LongTexts longTexts;
      if (JUDGMENT_TYPE.equals(rootTag) || ORDER_TYPE.equals(rootTag)) {
        longTexts = transformLongTexts(longTextsBuilder, content, jurisdiction, signatures, note);
      } else if (OPINION_TYPE.equals(rootTag)) {
        longTexts =
            transformOpinionLongTexts(
                longTextsBuilder, content, preambleGen, enactingTermsCjt, finalNode, note);
      } else {
        longTexts = longTextsBuilder.build();
      }

      Decision updatedDecision =
          decision.toBuilder()
              .inboxStatus(InboxStatus.EU)
              .coreData(coreData)
              .longTexts(longTexts)
              .build();

      documentationUnitRepository.save(
          updatedDecision,
          user,
          "EU-Entscheidung angelegt für "
              + decision.coreData().documentationOffice().abbreviation(),
          false);
    } catch (XPathExpressionException exception) {
      throw new FmxImporterException("Failed to extract data from FMX file.", exception);
    }
  }

  private Transformer initialiseXsltTransformer() {
    try {
      Templates fmxToHtml = xmlUtilService.getTemplates("xml/fmxToHtml.xslt");
      return fmxToHtml.newTransformer();
    } catch (TransformerConfigurationException e) {
      throw new FmxImporterException("Failed to initialise XSLT transformer.", e);
    }
  }

  @SuppressWarnings("java:S2755")
  private DocumentBuilder initialiseDocumentBuilder() {
    final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    try {
      factory.setIgnoringComments(true);
      factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
      return factory.newDocumentBuilder();
    } catch (ParserConfigurationException e) {
      throw new FmxImporterException("Failed to initialise document builder.", e);
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
      throw new FmxImporterException("Failed to parse FMX file content.", e);
    }
  }

  private CoreData transformCoreData(
      CoreData.CoreDataBuilder coreDataBuilder,
      String rootTag,
      String decisionDate,
      String ecli,
      String fileNumber,
      String celex,
      String author) {
    if (Strings.isNotBlank(decisionDate)) {
      coreDataBuilder.decisionDate(
          LocalDate.parse(decisionDate, DateTimeFormatter.ofPattern("yyyyMMdd")));
    }
    coreDataBuilder.ecli(ecli);
    coreDataBuilder.fileNumbers(List.of(fileNumber));
    coreDataBuilder.celexNumber(celex);
    coreDataBuilder.court(transformCourt(author).orElse(null));
    coreDataBuilder.documentType(transformDocumentType(rootTag).orElse(null));

    return coreDataBuilder.build();
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

  private Optional<DocumentType> transformDocumentType(String rootTag) {
    var rootNodeToDocTypeMap = new HashMap<String, String>();
    rootNodeToDocTypeMap.put(JUDGMENT_TYPE, "Urteil");
    rootNodeToDocTypeMap.put(ORDER_TYPE, "Beschluss");
    rootNodeToDocTypeMap.put(OPINION_TYPE, "Gutachten");

    var docTypeLabel = rootNodeToDocTypeMap.get(rootTag);
    Optional<DocumentType> documentType = Optional.empty();
    if (docTypeLabel != null) {
      documentType = documentTypeRepository.findUniqueCaselawBySearchStr(docTypeLabel);
    }
    if (documentType.isEmpty() && docTypeLabel != null) {
      var docTypes =
          documentTypeRepository.findDocumentTypesBySearchStrAndCategory(
              docTypeLabel, DocumentTypeCategory.CASELAW);
      if (!docTypes.isEmpty()) {
        documentType = Optional.of(docTypes.getFirst());
      }
    }
    return documentType;
  }

  private LongTexts transformLongTexts(
      LongTexts.LongTextsBuilder longTextsBuilder,
      Node content,
      Node jurisdiction,
      Node signatures,
      Node note) {

    if (content != null && jurisdiction != null) {
      content.removeChild(jurisdiction);
    }
    if (content != null && signatures != null) {
      content.appendChild(signatures);
    }
    if (content != null && note != null) {
      content.appendChild(note);
    }
    longTextsBuilder.tenor(transformLongTextNode(jurisdiction));
    longTextsBuilder.reasons(transformLongTextNode(content));

    return longTextsBuilder.build();
  }

  private LongTexts transformOpinionLongTexts(
      LongTexts.LongTextsBuilder longTextsBuilder,
      Node content,
      Node preambleGen,
      Node enactingTermsCjt,
      Node finalNode,
      Node note) {
    if (content != null && preambleGen != null && enactingTermsCjt != null) {
      content.removeChild(preambleGen);
      content.removeChild(enactingTermsCjt);
    }
    if (content != null && finalNode != null) {
      content.appendChild(finalNode);
    }
    if (content != null && note != null) {
      content.appendChild(note);
    }
    var tenor = transformLongTextNode(preambleGen) + transformLongTextNode(enactingTermsCjt);
    longTextsBuilder.tenor(tenor);
    longTextsBuilder.reasons(transformLongTextNode(content));

    return longTextsBuilder.build();
  }

  private String transformLongTextNode(Node textNode) {
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
