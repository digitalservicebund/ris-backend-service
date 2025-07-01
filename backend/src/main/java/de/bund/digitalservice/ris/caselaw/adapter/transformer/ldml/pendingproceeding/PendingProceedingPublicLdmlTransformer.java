package de.bund.digitalservice.ris.caselaw.adapter.transformer.ldml.pendingproceeding;

import javax.xml.parsers.DocumentBuilderFactory;
import lombok.extern.slf4j.Slf4j;

/**
 * Transformer for converting pending proceedings to LDML format for public portal use. Implements
 * specific meta-data mapping for public access.
 */
@Slf4j
public class PendingProceedingPublicLdmlTransformer extends PendingProceedingCommonLdmlTransformer {

  public PendingProceedingPublicLdmlTransformer(DocumentBuilderFactory documentBuilderFactory) {
    super(documentBuilderFactory);
  }
}
