package de.bund.digitalservice.ris.caselaw.adapter;

import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.CaseLawLdml;
import de.bund.digitalservice.ris.caselaw.adapter.transformer.DocumentationUnitToLdmlTransformer;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnit;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnitRepository;
import jakarta.xml.bind.JAXB;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.List;
import java.util.Optional;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Templates;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mapping.MappingException;
import org.springframework.stereotype.Service;
import org.xml.sax.SAXException;

@Service
@Profile({"default"})
public class CaseLawPostgresToS3Exporter {

  private static final Logger logger = LogManager.getLogger(CaseLawPostgresToS3Exporter.class);
  private static final int EXPORT_BATCH_SIZE = 1000;

  private final DocumentationUnitRepository documentationUnitRepository;
  private final DocumentBuilderFactory documentBuilderFactory;
  private final LdmlBucket ldmlBucket;
  private final Templates htmlToAknHtml;

  private final Schema schema;

  @Autowired
  public CaseLawPostgresToS3Exporter(
      DocumentationUnitRepository documentationUnitRepository,
      XmlUtilService xmlUtilService,
      DocumentBuilderFactory documentBuilderFactory,
      LdmlBucket ldmlBucket) {

    this.documentationUnitRepository = documentationUnitRepository;
    this.documentBuilderFactory = documentBuilderFactory;
    this.ldmlBucket = ldmlBucket;
    this.htmlToAknHtml = xmlUtilService.getTemplates("caselawhandover/htmlToAknHtml.xslt");
    this.schema = xmlUtilService.getSchema("caselawhandover/shared/akomantoso30.xsd");
  }

  //  @Async
  //  @EventListener(value = ApplicationReadyEvent.class)
  public void uploadCaseLaw() {
    logger.info("Export caselaw process has started");
    List<DocumentationUnit> documentationUnitsToTransform =
        documentationUnitRepository.getRandomDocumentationUnits();

    // Case law handover: decide on LDML update strategy (frequency, incremental, etc.)
    if (!documentationUnitsToTransform.isEmpty()) {
      // Only process the first batch for now so dev environment only indexes 2000 entries
      saveOneBatch(documentationUnitsToTransform);
    }

    logger.info("Export caselaw process is done");
  }

  public void saveOneBatch(List<DocumentationUnit> documentationUnits) {
    for (DocumentationUnit documentationUnit : documentationUnits) {
      Optional<CaseLawLdml> ldml =
          DocumentationUnitToLdmlTransformer.transformToLdml(
              documentationUnit, documentBuilderFactory);

      if (ldml.isPresent()) {
        Optional<String> fileContent = ldmlToString(ldml.get());
        fileContent.ifPresent(s -> ldmlBucket.save(ldml.get().getUniqueId() + ".xml", s));
      }
    }
  }

  public Optional<String> ldmlToString(CaseLawLdml ldml) {
    StringWriter jaxbOutput = new StringWriter();
    JAXB.marshal(ldml, jaxbOutput);

    try {
      String ldmlAsXmlString = XmlUtilService.xsltTransform(htmlToAknHtml, jaxbOutput.toString());
      schema.newValidator().validate(new StreamSource(new StringReader(ldmlAsXmlString)));
      return Optional.of(ldmlAsXmlString);
    } catch (SAXException | MappingException | IOException e) {
      logXsdError(ldml.getUniqueId(), jaxbOutput.toString(), e);
      return Optional.empty();
    }
  }

  private void logXsdError(String caseLawId, String beforeXslt, Exception e) {
    String hint = "";
    if (beforeXslt.contains("<akn:judgmentBody/>")) {
      hint = "Ldml contained <judgementBody/>. An empty judgementBody isn't allowed.";
    } else if (beforeXslt.contains("KARE600062214")) {
      hint = "KARE600062214 contains an invalid width (escaping issue)";
    } else if (beforeXslt.contains("JURE200002538")) {
      hint = "JURE200002538 contains an invalid href (invalid whitespace in the middle of the url)";
    }
    logger.error("Error: {} Case Law {} does not match akomantoso30.xsd. {}", hint, caseLawId, e);
  }
}
