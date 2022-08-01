package de.bund.digitalservice.ris.domain;

import com.fasterxml.jackson.core.JsonProcessingException;
import de.bund.digitalservice.ris.domain.export.JurisXmlExporter;
import java.io.IOException;
import java.util.List;
import javax.xml.parsers.ParserConfigurationException;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.xml.sax.SAXException;
import reactor.core.publisher.Mono;

@Service
public class XmlExportService {

  private final JurisXmlExporter jurisXmlExporter;

  public XmlExportService(JurisXmlExporter jurisXmlExporter) {
    this.jurisXmlExporter = jurisXmlExporter;
  }

  public Mono<ResponseEntity<String>> exportJurisXml() {
    DocUnitCreationInfo info = new DocUnitCreationInfo();
    info.setDocumentationCenterAbbreviation("AB");
    info.setDocumentType("CD");
    DocUnit docUnit = DocUnit.createNew(info, 123);

    String xml;
    try {
      xml = jurisXmlExporter.generateXml(docUnit);
    } catch (JsonProcessingException e) {
      return Mono.just(
          ResponseEntity.internalServerError().body("Failed to generate XML: " + e.getMessage()));
    }

    try {
      List<String> validationResults = jurisXmlExporter.validateXml(xml);
      System.out.println("validationResults: " + validationResults);
    } catch (ParserConfigurationException | SAXException | IOException e) {
      return Mono.just(
          ResponseEntity.internalServerError().body("Failed to validate XML: " + e.getMessage()));
    }

    return Mono.just(ResponseEntity.ok(xml));
  }
}
