package de.bund.digitalservice.ris.caselaw.adapter;

import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.CaseLawLdml;
import de.bund.digitalservice.ris.caselaw.domain.Decision;

public interface PortalTransformer {

  CaseLawLdml transformToLdml(Decision decision);
}
