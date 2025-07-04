package de.bund.digitalservice.ris.caselaw.adapter.transformer.ldml.pendingproceeding;

import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.Meta;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.Proprietary;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.RisMeta;
import de.bund.digitalservice.ris.caselaw.domain.PendingProceeding;
import jakarta.xml.bind.ValidationException;
import javax.xml.parsers.DocumentBuilderFactory;
import lombok.extern.slf4j.Slf4j;

/**
 * Transformer for converting pending proceedings to LDML format for public portal use. Implements
 * reduced meta-data mapping for public access.
 */
@Slf4j
public class PendingProceedingReducedLdmlTransformer
    extends PendingProceedingCommonLdmlTransformer {

  public PendingProceedingReducedLdmlTransformer(DocumentBuilderFactory documentBuilderFactory) {
    super(documentBuilderFactory);
  }

  @Override
  protected Meta buildMeta(PendingProceeding pendingProceeding) throws ValidationException {
    validateCoreData(pendingProceeding);

    Meta.MetaBuilder builder = Meta.builder();

    return builder
        .identification(buildIdentification(pendingProceeding))
        .proprietary(Proprietary.builder().meta(buildRisMeta(pendingProceeding)).build())
        .build();
  }

  private RisMeta buildRisMeta(PendingProceeding pendingProceeding) {
    return buildCommonRisMeta(pendingProceeding).build();
  }
}
