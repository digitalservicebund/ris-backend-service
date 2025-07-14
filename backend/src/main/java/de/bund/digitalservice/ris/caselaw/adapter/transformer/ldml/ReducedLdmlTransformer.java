package de.bund.digitalservice.ris.caselaw.adapter.transformer.ldml;

import de.bund.digitalservice.ris.caselaw.adapter.PortalTransformer;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.CaseLawLdml;
import de.bund.digitalservice.ris.caselaw.adapter.transformer.ldml.decision.DecisionReducedLdmlTransformer;
import de.bund.digitalservice.ris.caselaw.adapter.transformer.ldml.pendingproceeding.PendingProceedingReducedLdmlTransformer;
import de.bund.digitalservice.ris.caselaw.domain.Decision;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnit;
import de.bund.digitalservice.ris.caselaw.domain.PendingProceeding;
import javax.xml.parsers.DocumentBuilderFactory;
import lombok.extern.slf4j.Slf4j;

/**
 * Transformer for converting documentation units to LDML format in full LDML format. Excludes
 * metadata like classification and keywords. Currently, the public Prototype Portal is under
 * restrictions and must not use full LDML for legal reasons.
 */
@Slf4j
public class ReducedLdmlTransformer implements PortalTransformer {

  private final DecisionReducedLdmlTransformer decisionPublicLdmlTransformer;
  private final PendingProceedingReducedLdmlTransformer pendingProceedingReducedLdmlTransformer;

  public ReducedLdmlTransformer(DocumentBuilderFactory documentBuilderFactory) {
    this.decisionPublicLdmlTransformer = new DecisionReducedLdmlTransformer(documentBuilderFactory);
    this.pendingProceedingReducedLdmlTransformer =
        new PendingProceedingReducedLdmlTransformer(documentBuilderFactory);
  }

  @Override
  public CaseLawLdml transformToLdml(DocumentationUnit documentationUnit) {
    if (documentationUnit instanceof Decision decision) {
      return decisionPublicLdmlTransformer.transformToLdml(decision);
    } else if (documentationUnit instanceof PendingProceeding pendingProceeding) {
      return pendingProceedingReducedLdmlTransformer.transformToLdml(pendingProceeding);
    } else {
      throw new IllegalArgumentException(
          "Unsupported documentation unit type: " + documentationUnit.kind());
    }
  }
}
