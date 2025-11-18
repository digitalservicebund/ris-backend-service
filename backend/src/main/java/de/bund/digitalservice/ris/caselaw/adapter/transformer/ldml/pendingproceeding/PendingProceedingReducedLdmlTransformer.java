package de.bund.digitalservice.ris.caselaw.adapter.transformer.ldml.pendingproceeding;

import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.header.Header;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.header.Paragraph;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.meta.Meta;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.meta.proprietary.Proprietary;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.meta.proprietary.RisMeta;
import de.bund.digitalservice.ris.caselaw.domain.PendingProceeding;
import java.util.ArrayList;
import java.util.List;
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
  protected Header buildHeader(PendingProceeding pendingProceeding) {
    List<Paragraph> paragraphs = new ArrayList<>();
    paragraphs = buildCommonHeader(pendingProceeding, paragraphs);
    return Header.builder().paragraphs(paragraphs).build();
  }

  @Override
  public boolean isFullLDML() {
    return true;
  }
}
