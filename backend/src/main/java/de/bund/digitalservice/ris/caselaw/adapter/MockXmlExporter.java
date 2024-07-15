package de.bund.digitalservice.ris.caselaw.adapter;

import de.bund.digitalservice.ris.caselaw.domain.DocumentUnit;
import de.bund.digitalservice.ris.caselaw.domain.XmlExportResult;
import de.bund.digitalservice.ris.caselaw.domain.XmlExporter;
import java.time.Instant;
import java.util.List;

public class MockXmlExporter implements XmlExporter {

  @Override
  public XmlExportResult generateXml(DocumentUnit documentUnit) {
    return new XmlExportResult(
        "xml",
        documentUnit.coreData().decisionDate() != null,
        List.of("message 1", "message 2"),
        "test.xml",
        Instant.now());
  }

  @Override
  public String generateEncryptedXMLString(DocumentUnit documentUnit) {
    return "";
  }
}
