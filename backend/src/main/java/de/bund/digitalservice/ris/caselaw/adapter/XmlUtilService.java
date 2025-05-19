package de.bund.digitalservice.ris.caselaw.adapter;

import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.CaseLawLdml;
import de.bund.digitalservice.ris.caselaw.adapter.exception.LdmlTransformationException;
import jakarta.xml.bind.JAXB;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.xml.XMLConstants;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.mapping.MappingException;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

@Service
@Slf4j
public class XmlUtilService {
  private final TransformerFactory transformerFactory;
  private final Templates htmlToAknHtml;
  private final Schema schema;

  public XmlUtilService(@Qualifier("saxon") TransformerFactory transformerFactory) {
    this.transformerFactory = transformerFactory;
    this.htmlToAknHtml = getTemplates("xml/htmlToAknHtml.xslt");
    this.schema = getSchema("xml/akomantoso30.xsd");
  }

  public Templates getTemplates(String filePath) {
    try {
      ClassPathResource xsltResource = new ClassPathResource(filePath);
      String fileContent = IOUtils.toString(xsltResource.getInputStream(), StandardCharsets.UTF_8);
      return transformerFactory.newTemplates(new StreamSource(new StringReader(fileContent)));
    } catch (TransformerConfigurationException | IOException e) {
      log.error("XSLT initialization error.", e);
      throw new LdmlTransformationException("XSLT initialization error.", e);
    }
  }

  public Schema getSchema(String filePath) {
    try {
      return SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI)
          .newSchema(ResourceUtils.getFile("classpath:" + filePath));
    } catch (SAXException | FileNotFoundException e) {
      log.error("Failure during CaseLawPostgresToS3Exporter initialization", e);
      throw new LdmlTransformationException(
          "Failure during CaseLawPostgresToS3Exporter initialization", e);
    }
  }

  public static List<Node> toList(NodeList nodeList) {
    // Needed because NodeList doesn't implement Iterable
    List<Node> nodes = new ArrayList<>();

    for (int i = 0; i < nodeList.getLength(); i++) {
      nodes.add(nodeList.item(i));
    }

    return nodes;
  }

  public static String xsltTransform(Templates templates, String content) {
    try {
      StringWriter xsltOutput = new StringWriter();
      Transformer transformer = templates.newTransformer();
      transformer.transform(
          new StreamSource(new StringReader(content.strip())), new StreamResult(xsltOutput));
      return xsltOutput.toString();
    } catch (TransformerException e) {
      log.error("Xslt transformation error.", e);
      throw new MappingException(e.getMessage());
    }
  }

  public Optional<String> ldmlToString(CaseLawLdml ldml) {
    StringWriter jaxbOutput = new StringWriter();
    JAXB.marshal(ldml, jaxbOutput);

    try {
      String ldmlAsXmlString = XmlUtilService.xsltTransform(htmlToAknHtml, jaxbOutput.toString());
      if (ldmlAsXmlString.contains("akn:unknownUseCaseDiscovered")) {
        int hintStart = Math.max(0, ldmlAsXmlString.indexOf("akn:unknownUseCaseDiscovered") - 10);
        int hintEnd = Math.min(ldmlAsXmlString.length(), hintStart + 60);
        String hint =
            "\"..." + ldmlAsXmlString.substring(hintStart, hintEnd).replace("\n", "") + "...\"";
        log.error(
            "Invalid ldml produced for {}. A new unsupported attribute or elements was discovered."
                + " It is either an error or needs to be added to the allow list. hint : {}",
            ldml.getUniqueId(),
            hint);
        return Optional.empty();
      }

      schema.newValidator().validate(new StreamSource(new StringReader(ldmlAsXmlString)));
      return Optional.of(ldmlAsXmlString);
    } catch (SAXException | MappingException | IOException e) {
      logXsdError(ldml.getUniqueId(), jaxbOutput.toString(), e);
      return Optional.empty();
    }
  }

  @SuppressWarnings("java:S5852")
  private void logXsdError(String caseLawId, String beforeXslt, Exception e) {
    String hint = "";
    if (beforeXslt.contains("<akn:judgmentBody/>")) {
      hint = "Ldml contained <judgementBody/>. An empty judgementBody isn't allowed.";
    } else if (beforeXslt.contains("KARE600062214")) {
      hint = "KARE600062214 contains an invalid width (escaping issue)";
    } else if (beforeXslt.contains("JURE200002538")) {
      hint = "JURE200002538 contains an invalid href (invalid whitespace in the middle of the url)";
    } else if (beforeXslt.matches("(?s).*?<akn:header>.*?<div.*?>.*?</akn:header>.*")) {
      hint = "Ldml contained <div> inside title.";
    } else if (beforeXslt.matches("(?s).*?<akn:header>.*?<br.*?>.*?</akn:header>.*")) {
      hint = "Ldml contained <br> inside title.";
    }
    log.error("Error: {} Case Law {} does not match akomantoso30.xsd. {}", hint, caseLawId, e);
  }
}
