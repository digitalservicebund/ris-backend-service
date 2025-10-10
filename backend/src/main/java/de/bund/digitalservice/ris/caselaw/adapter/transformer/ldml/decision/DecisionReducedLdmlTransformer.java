package de.bund.digitalservice.ris.caselaw.adapter.transformer.ldml.decision;

import static de.bund.digitalservice.ris.caselaw.adapter.MappingUtils.applyIfNotEmpty;
import static de.bund.digitalservice.ris.caselaw.adapter.MappingUtils.nullSafeGet;

import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.header.Header;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.header.Paragraph;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.meta.Meta;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.meta.proprietary.DocumentType;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.meta.proprietary.Proprietary;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.meta.proprietary.RisMeta;
import de.bund.digitalservice.ris.caselaw.domain.Decision;
import de.bund.digitalservice.ris.caselaw.domain.court.Court;
import jakarta.xml.bind.ValidationException;
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
  protected Meta buildMeta(Decision decision) throws ValidationException {
    validateCoreData(decision);

    Meta.MetaBuilder builder = Meta.builder();

    return builder
        .identification(buildIdentification(decision))
        .references(buildReferences(decision))
        .proprietary(Proprietary.builder().meta(buildRisMeta(decision)).build())
        .build();
  }

  private RisMeta buildRisMeta(Decision decision) {
    var builder = buildCommonRisMeta(decision);

    var coreData = decision.coreData();
    if (coreData != null) {
      applyIfNotEmpty(coreData.fileNumbers(), builder::fileNumbers);

      builder
          .documentType(
              DocumentType.builder()
                  .eId("dokumenttyp")
                  .value(coreData.documentType().label())
                  .build())
          .courtLocation(nullSafeGet(coreData.court(), Court::location))
          .courtType(nullSafeGet(coreData.court(), Court::type))
          .judicialBody(nullIfEmpty(coreData.appraisalBody()));
    }
    return builder.build();
  }

  @Override
  protected Header buildHeader(Decision decision) throws ValidationException {
    List<Paragraph> paragraphs = new ArrayList<>();

    paragraphs = buildCommonHeader(decision, paragraphs);

    return Header.builder().paragraphs(paragraphs).build();
  }
}
