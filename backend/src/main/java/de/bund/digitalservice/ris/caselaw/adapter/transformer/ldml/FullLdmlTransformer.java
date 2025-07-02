package de.bund.digitalservice.ris.caselaw.adapter.transformer.ldml;

import de.bund.digitalservice.ris.caselaw.adapter.PortalTransformer;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.CaseLawLdml;
import de.bund.digitalservice.ris.caselaw.adapter.transformer.ldml.decision.DecisionFullLdmlTransformer;
import de.bund.digitalservice.ris.caselaw.adapter.transformer.ldml.pendingproceeding.PendingProceedingFullLdmlTransformer;
import de.bund.digitalservice.ris.caselaw.domain.Decision;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnit;
import de.bund.digitalservice.ris.caselaw.domain.PendingProceeding;
import javax.xml.parsers.DocumentBuilderFactory;
import lombok.extern.slf4j.Slf4j;

/**
 * Transformer for converting documentation units to LDML format for internal portal use. Includes
 * additional metadata like classification and keywords for internal processing.
 */
@Slf4j
public class FullLdmlTransformer implements PortalTransformer {

  private final DecisionFullLdmlTransformer decisionFullLdmlTransformer;
  private final PendingProceedingFullLdmlTransformer pendingProceedingFullLdmlTransformer;

  public FullLdmlTransformer(DocumentBuilderFactory documentBuilderFactory) {
    this.decisionFullLdmlTransformer = new DecisionFullLdmlTransformer(documentBuilderFactory);
    this.pendingProceedingFullLdmlTransformer =
        new PendingProceedingFullLdmlTransformer(documentBuilderFactory);
  }

  @Override
  public CaseLawLdml transformToLdml(DocumentationUnit documentationUnit) {
    if (documentationUnit instanceof Decision decision) {
      return decisionFullLdmlTransformer.transformToLdml(decision);
    } else if (documentationUnit instanceof PendingProceeding pendingProceeding) {
      return pendingProceedingFullLdmlTransformer.transformToLdml(pendingProceeding);
    } else {
      throw new IllegalArgumentException(
          "Unsupported documentation unit type: " + documentationUnit.kind());
    }
  }
}
