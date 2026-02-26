package de.bund.digitalservice.ris.caselaw.adapter.transformer.ldml.decision;

import de.bund.digitalservice.ris.caselaw.adapter.DateUtils;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.header.Header;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.header.Paragraph;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.meta.Meta;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.meta.analysis.DokumentarischeKurztexte;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.meta.proprietary.Proprietary;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.meta.proprietary.RisMeta;
import de.bund.digitalservice.ris.caselaw.domain.Decision;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
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
        .identification(buildIdentification(decision))
        .references(buildReferences(decision))
        .proprietary(Proprietary.builder().meta(buildRisMeta(decision)).build())
        .analysis(buildAnalysis(decision))
        .build();
  }

  private RisMeta buildRisMeta(Decision decision) {
    var builder = buildCommonRisMeta(decision);
    return builder.build();
  }

  @Nullable
  protected DokumentarischeKurztexte buildKurztexte(Decision decision) {
    var builder = getCommonKurztexteBuilder(decision);
    DokumentarischeKurztexte kurztexte = builder.build();
    return kurztexte.isEmpty() ? null : kurztexte;
  }

  @Override
  protected Header buildHeader(Decision decision) {
    List<Paragraph> paragraphs = new ArrayList<>();
    var hasFileNumber =
        decision.coreData().fileNumbers() != null && !decision.coreData().fileNumbers().isEmpty();
    var hasDecisionDate = decision.coreData().decisionDate() != null;
    var hasCourt = decision.coreData().court() != null;

    paragraphs = buildCommonHeader(decision, paragraphs);

    if (hasFileNumber && hasDecisionDate && hasCourt) {
      buildHeadline(
          paragraphs,
          decision.coreData().court().label()
              + ", "
              + DateUtils.toFormattedDateString(decision.coreData().decisionDate())
              + ", "
              + decision.coreData().fileNumbers().getFirst(),
          htmlTransformer,
          false);
    }

    return Header.builder().paragraphs(paragraphs).build();
  }

  @Override
  public boolean isFullLDML() {
    return false;
  }
}
