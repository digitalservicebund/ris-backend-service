package de.bund.digitalservice.ris.caselaw.adapter.transformer.ldml.decision;

import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.header.Header;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.header.Paragraph;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.meta.Meta;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.meta.proprietary.Proprietary;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.meta.proprietary.RisMeta;
import de.bund.digitalservice.ris.caselaw.domain.Decision;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.DocumentBuilderFactory;
import lombok.extern.slf4j.Slf4j;

/**
 * Transformer for converting decisions to the reduced LDML format. Currently, the public Prototype
 * Portal is under restrictions and must not use full LDML for legal reasons.
 */
@Slf4j
public class DecisionReducedLdmlTransformer extends DecisionCommonLdmlTransformer {

  public DecisionReducedLdmlTransformer(DocumentBuilderFactory documentBuilderFactory) {
    super(documentBuilderFactory);
  }

  @Override
  protected Meta buildMeta(Decision decision) {
    Meta.MetaBuilder builder = Meta.builder();

    return builder
        .identification(buildIdentification(decision, false))
        .references(buildReferences(decision))
        .proprietary(Proprietary.builder().meta(buildRisMeta(decision)).build())
        .build();
  }

  private RisMeta buildRisMeta(Decision decision) {
    var builder = buildCommonRisMeta(decision);
    return builder.build();
  }

  @Override
  protected Header buildHeader(Decision decision) {
    List<Paragraph> paragraphs = new ArrayList<>();

    paragraphs = buildCommonHeader(decision, paragraphs);

    return Header.builder().paragraphs(paragraphs).build();
  }
}
