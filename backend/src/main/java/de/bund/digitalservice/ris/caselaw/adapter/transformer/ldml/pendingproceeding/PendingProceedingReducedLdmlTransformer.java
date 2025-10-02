package de.bund.digitalservice.ris.caselaw.adapter.transformer.ldml.pendingproceeding;

import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.JaxbHtml;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.Meta;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.Proprietary;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.RisMeta;
import de.bund.digitalservice.ris.caselaw.domain.PendingProceeding;
import jakarta.xml.bind.ValidationException;
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
  protected Meta buildMeta(PendingProceeding pendingProceeding) throws ValidationException {
    validateCoreData(pendingProceeding);

    Meta.MetaBuilder builder = Meta.builder();

    return builder
        .identification(buildIdentification(pendingProceeding))
        .references(buildReferences(pendingProceeding))
        .proprietary(Proprietary.builder().meta(buildRisMeta(pendingProceeding)).build())
        .build();
  }

  private RisMeta buildRisMeta(PendingProceeding pendingProceeding) {
    return buildCommonRisMeta(pendingProceeding).build();
  }

  @Override
  protected JaxbHtml buildHeader(PendingProceeding pendingProceeding) throws ValidationException {
    return JaxbHtml.build(
        htmlTransformer.htmlStringToObjectList(buildCommonHeader(pendingProceeding)));
  }
}
