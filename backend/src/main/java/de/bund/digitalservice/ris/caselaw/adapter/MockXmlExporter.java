package de.bund.digitalservice.ris.caselaw.adapter;

import de.bund.digitalservice.ris.caselaw.domain.Decision;
import de.bund.digitalservice.ris.caselaw.domain.LegalPeriodicalEdition;
import de.bund.digitalservice.ris.caselaw.domain.XmlExporter;
import de.bund.digitalservice.ris.caselaw.domain.XmlTransformationResult;
import java.time.Instant;
import java.util.List;
import org.springframework.context.annotation.Primary;

/**
 * Mock implementation of the {@link XmlExporter} interface. This implementation is used for testing
 * purposes.
 */
@Primary
public class MockXmlExporter implements XmlExporter {

  /**
   * Generates a mock XML export result.
   *
   * @param decision the document unit
   * @return the XML export result
   */
  @Override
  public XmlTransformationResult transformToXml(Decision decision, boolean prettyPrint) {
    return new XmlTransformationResult(
        (decision.shortTexts() != null && decision.shortTexts().headnote() != null)
            ? decision.shortTexts().headnote()
            : "xml",
        decision.coreData().decisionDate() != null,
        List.of("message 1", "message 2"),
        "test.xml",
        Instant.now());
  }

  @Override
  public String generateEncryptedXMLString(Decision decision) {
    return "";
  }

  /**
   * Generates a mock XML export result.
   *
   * @param edition the edition
   * @return the XML export result
   */
  @Override
  public List<XmlTransformationResult> transformToXml(LegalPeriodicalEdition edition) {
    return edition.references().stream()
        .map(
            reference ->
                new XmlTransformationResult(
                    "citation: "
                        + reference.citation()
                        + " docunit: "
                        + reference.documentationUnit().getDocumentNumber(),
                    true,
                    List.of("message 1", "message 2"),
                    reference.documentationUnit().getDocumentNumber() + ".xml",
                    Instant.now()))
        .toList();
  }
}
