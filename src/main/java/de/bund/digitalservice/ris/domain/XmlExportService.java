package de.bund.digitalservice.ris.domain;

import de.bund.digitalservice.ris.domain.export.JurisXmlExporter;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class XmlExportService {

  public Mono<ResponseEntity<String>> exportJurisXml() {
    DocUnitCreationInfo info = new DocUnitCreationInfo();
    info.setDocumentationCenterAbbreviation("AB");
    info.setDocumentType("CD");
    DocUnit docUnit = DocUnit.createNew(info, 123);

    JurisXmlExporter jurisXmlExporter = new JurisXmlExporter();
    String xml = jurisXmlExporter.generateXml(docUnit);
    String validationReport = jurisXmlExporter.validateXml(xml);

    // TODO

    return Mono.just(ResponseEntity.ok(xml));
  }
}
