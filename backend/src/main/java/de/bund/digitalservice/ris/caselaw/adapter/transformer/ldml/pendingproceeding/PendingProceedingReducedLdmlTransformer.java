package de.bund.digitalservice.ris.caselaw.adapter.transformer.ldml.pendingproceeding;

import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.meta.Meta;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.meta.proprietary.Proprietary;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.meta.proprietary.RisMeta;
import de.bund.digitalservice.ris.caselaw.domain.PendingProceeding;
import javax.xml.parsers.DocumentBuilderFactory;
import lombok.extern.slf4j.Slf4j;

/** Transformer for converting pending proceedings to the reduced LDML format */
@Slf4j
public class PendingProceedingReducedLdmlTransformer
    extends PendingProceedingCommonLdmlTransformer {

  public PendingProceedingReducedLdmlTransformer(DocumentBuilderFactory documentBuilderFactory) {
    super(documentBuilderFactory);
  }

  @Override
  protected Meta buildMeta(PendingProceeding pendingProceeding) {
    Meta.MetaBuilder builder = Meta.builder();

    return builder
        .identification(buildIdentification(pendingProceeding))
        .references(buildReferences(pendingProceeding))
        .proprietary(Proprietary.builder().meta(buildRisMeta(pendingProceeding)).build())
        .analysis(buildAnalysis(pendingProceeding))
        .build();
  }

  private RisMeta buildRisMeta(PendingProceeding pendingProceeding) {
    return buildCommonRisMeta(pendingProceeding).build();
  }

  @Override
  public boolean isFullLDML() {
    return false;
  }
}
