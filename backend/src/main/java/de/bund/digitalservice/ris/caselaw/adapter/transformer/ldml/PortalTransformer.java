package de.bund.digitalservice.ris.caselaw.adapter.transformer.ldml;

import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.CaseLawLdml;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnit;

public interface PortalTransformer {

  CaseLawLdml transformToLdml(DocumentationUnit documentationUnit);
}
