package de.bund.digitalservice.ris.caselaw.adapter.transformer.ldml.pendingproceeding;

import javax.xml.parsers.DocumentBuilderFactory;
import lombok.extern.slf4j.Slf4j;

/** Transformer for converting pending proceedings to LDML format for internal portal use. */
@Slf4j
public class PendingProceedingFullLdmlTransformer extends PendingProceedingCommonLdmlTransformer {

  public PendingProceedingFullLdmlTransformer(DocumentBuilderFactory documentBuilderFactory) {
    super(documentBuilderFactory);
  }
}
