package de.bund.digitalservice.ris.caselaw.adapter;

import de.bund.digitalservice.ris.caselaw.domain.DocumentUnit;
import de.bund.digitalservice.ris.caselaw.domain.XmlExporter;
import de.bund.digitalservice.ris.caselaw.domain.XmlTransformationResult;
import java.time.Instant;
import java.util.List;

/**
 * Mock implementation of the {@link XmlExporter} interface. This implementation is used for testing
 * purposes.
 */
public class MockXmlExporter implements XmlExporter {

  /**
   * Generates a mock XML export result.
   *
   * @param documentUnit the document unit
   * @return the XML export result
   */
  @Override
  public XmlTransformationResult transformToXml(DocumentUnit documentUnit) {
    return new XmlTransformationResult(
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
