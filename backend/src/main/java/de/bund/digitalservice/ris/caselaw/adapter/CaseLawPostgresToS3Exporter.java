package de.bund.digitalservice.ris.caselaw.adapter;

import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.CaseLawLdml;
import de.bund.digitalservice.ris.caselaw.adapter.transformer.CaseLawDbEntityToLdmlMapper;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnit;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnitRepository;
import jakarta.xml.bind.JAXB;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import javax.xml.XMLConstants;
import javax.xml.transform.Templates;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import org.apache.commons.collections4.ListUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mapping.MappingException;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Component;
import org.springframework.util.ResourceUtils;
import org.xml.sax.SAXException;

@EnableAsync
@Component
@Profile({"default"})
public class CaseLawPostgresToS3Exporter {

  private static final Logger logger = LogManager.getLogger(CaseLawPostgresToS3Exporter.class);
  private static final int EXPORT_BATCH_SIZE = 1000;

  private final DocumentationUnitRepository documentationUnitRepository;
  private final LdmlBucket ldmlBucket;
  private final Templates htmlToAknHtml;

  private Schema schema;

  @Autowired
  public CaseLawPostgresToS3Exporter(
      DocumentationUnitRepository documentationUnitRepository, LdmlBucket ldmlBucket) {
    this.documentationUnitRepository = documentationUnitRepository;
    this.ldmlBucket = ldmlBucket;
    this.htmlToAknHtml = XmlUtils.getTemplates("caselawhandover/htmlToAknHtml.xslt");
    try {
      this.schema =
          SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI)
              .newSchema(
                  ResourceUtils.getFile("classpath:caselawhandover/shared/akomantoso30.xsd"));
    } catch (SAXException | FileNotFoundException e) {
      logger.error("Failure during CaseLawPostgresToS3Exporter initialization", e);
    }
  }

  //  @Async
  //  @EventListener(value = ApplicationReadyEvent.class)
  public void uploadCaseLaw() {
    logger.info("Export caselaw process has started");
    List<UUID> idsToImport = documentationUnitRepository.getUnprocessedIds();
    List<List<UUID>> idBatches = ListUtils.partition(idsToImport, EXPORT_BATCH_SIZE);

    // Case law handover: decide on LDML update strategy (frequency, incremental, etc.)
    if (!idBatches.isEmpty()) {
      // Only process the first batch for now so dev environment only indexes 2000 entries
      saveOneBatch(idBatches.get(0));
    }
    logger.info("Export caselaw process is done");
  }

  public void saveOneBatch(List<UUID> ids) {
    for (DocumentationUnit documentationUnit : documentationUnitRepository.findByIdIn(ids)) {
      Optional<CaseLawLdml> ldml = CaseLawDbEntityToLdmlMapper.transformToLdml(documentationUnit);
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
      String ldmlAsXmlString = XmlUtils.xsltTransform(htmlToAknHtml, jaxbOutput.toString());
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
