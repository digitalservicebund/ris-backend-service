package de.bund.digitalservice.ris.caselaw.adapter.transformer.ldml;

import de.bund.digitalservice.ris.caselaw.adapter.PortalTransformer;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.CaseLawLdml;
import de.bund.digitalservice.ris.caselaw.adapter.transformer.ldml.decision.DecisionInternalLdmlTransformer;
import de.bund.digitalservice.ris.caselaw.adapter.transformer.ldml.pendingproceeding.PendingProceedingInternalLdmlTransformer;
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
public class InternalLdmlTransformer implements PortalTransformer {

  private final DecisionInternalLdmlTransformer decisionInternalLdmlTransformer;
  private final PendingProceedingInternalLdmlTransformer pendingProceedingInternalLdmlTransformer;

  public InternalLdmlTransformer(DocumentBuilderFactory documentBuilderFactory) {
    this.decisionInternalLdmlTransformer =
        new DecisionInternalLdmlTransformer(documentBuilderFactory);
    this.pendingProceedingInternalLdmlTransformer =
        new PendingProceedingInternalLdmlTransformer(documentBuilderFactory);
  }

  @Override
  public CaseLawLdml transformToLdml(DocumentationUnit documentationUnit) {
    if (documentationUnit instanceof Decision decision) {
      return decisionInternalLdmlTransformer.transformToLdml(decision);
    } else if (documentationUnit instanceof PendingProceeding pendingProceeding) {
      return pendingProceedingInternalLdmlTransformer.transformToLdml(pendingProceeding);
    } else {
      throw new IllegalArgumentException(
          "Unsupported documentation unit type: " + documentationUnit.kind());
    }
  }
}
