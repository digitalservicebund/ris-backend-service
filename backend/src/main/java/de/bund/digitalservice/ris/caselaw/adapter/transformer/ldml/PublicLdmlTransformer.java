package de.bund.digitalservice.ris.caselaw.adapter.transformer.ldml;

import de.bund.digitalservice.ris.caselaw.adapter.PortalTransformer;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.CaseLawLdml;
import de.bund.digitalservice.ris.caselaw.domain.Decision;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnit;
import javax.xml.parsers.DocumentBuilderFactory;
import lombok.extern.slf4j.Slf4j;

/**
 * Transformer for converting documentation units to LDML format for public portal use. Implements
 * specific meta-data mapping for public access.
 */
@Slf4j
public class PublicLdmlTransformer implements PortalTransformer {

  private final DecisionPublicLdmlTransformer decisionPublicLdmlTransformer;

  public PublicLdmlTransformer(DocumentBuilderFactory documentBuilderFactory) {
    this.decisionPublicLdmlTransformer = new DecisionPublicLdmlTransformer(documentBuilderFactory);
  }

  @Override
  public CaseLawLdml transformToLdml(DocumentationUnit documentationUnit) {
    if (documentationUnit instanceof Decision decision) {
      return decisionPublicLdmlTransformer.transformToLdml(decision);
    } else {
      throw new IllegalArgumentException(
          "Unsupported documentation unit type: " + documentationUnit.kind());
    }
  }
}
