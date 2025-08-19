package de.bund.digitalservice.ris.caselaw.adapter.transformer.ldml;

import de.bund.digitalservice.ris.caselaw.adapter.PortalTransformer;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.CaseLawLdml;
import de.bund.digitalservice.ris.caselaw.adapter.exception.LdmlTransformationException;
import de.bund.digitalservice.ris.caselaw.adapter.transformer.ldml.decision.DecisionFullLdmlTransformer;
import de.bund.digitalservice.ris.caselaw.adapter.transformer.ldml.pendingproceeding.PendingProceedingFullLdmlTransformer;
import de.bund.digitalservice.ris.caselaw.domain.Decision;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnit;
import de.bund.digitalservice.ris.caselaw.domain.PendingProceeding;
import javax.xml.parsers.DocumentBuilderFactory;
import lombok.extern.slf4j.Slf4j;

/**
 * Transformer for converting documentation units to LDML format in full LDML format. Includes
 * additional metadata like classification and keywords. Currently, the public Prototype Portal is *
 * under restrictions and must not use full LDML for legal reasons.
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
  public CaseLawLdml transformToLdml(DocumentationUnit documentationUnit)
      throws LdmlTransformationException {
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
