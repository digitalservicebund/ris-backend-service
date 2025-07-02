package de.bund.digitalservice.ris.caselaw.adapter.transformer.ldml.decision;

import static de.bund.digitalservice.ris.caselaw.adapter.MappingUtils.applyIfNotEmpty;
import static de.bund.digitalservice.ris.caselaw.adapter.MappingUtils.nullSafeGet;

import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.AknEmbeddedStructureInBlock;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.AknMultipleBlock;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.JaxbHtml;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.Meta;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.Proprietary;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.RisMeta;
import de.bund.digitalservice.ris.caselaw.domain.Decision;
import de.bund.digitalservice.ris.caselaw.domain.LongTexts;
import de.bund.digitalservice.ris.caselaw.domain.court.Court;
import jakarta.xml.bind.ValidationException;
import javax.xml.parsers.DocumentBuilderFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

/**
 * Transformer for converting decisions to LDML format for public portal use. Implements specific
 * meta-data mapping for public access.
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
        .proprietary(Proprietary.builder().meta(buildRisMeta(decision)).build())
        .build();
  }

  private RisMeta buildRisMeta(Decision decision) {
    var builder = buildCommonRisMeta(decision);

    var coreData = decision.coreData();
    if (coreData != null) {
      applyIfNotEmpty(coreData.fileNumbers(), builder::fileNumbers);

      builder
          .documentType(coreData.documentType().label())
          .courtLocation(nullSafeGet(coreData.court(), Court::location))
          .courtType(nullSafeGet(coreData.court(), Court::type))
          .judicialBody(nullIfEmpty(coreData.appraisalBody()));
    }
    return builder.build();
  }

  @Override
  protected AknMultipleBlock buildIntroduction(Decision decision) {
    var longTexts = decision.longTexts();

    var outline = nullSafeGet(longTexts, LongTexts::outline);
    var tenor = nullSafeGet(longTexts, LongTexts::tenor);

    if (StringUtils.isNotEmpty(outline) || StringUtils.isNotEmpty(tenor)) {
      return new AknMultipleBlock()
          .withBlock(
              AknEmbeddedStructureInBlock.Outline.NAME,
              AknEmbeddedStructureInBlock.Outline.build(
                  JaxbHtml.build(htmlStringToObjectList(outline))))
          .withBlock(
              AknEmbeddedStructureInBlock.Tenor.NAME,
              AknEmbeddedStructureInBlock.Tenor.build(
                  JaxbHtml.build(htmlStringToObjectList(tenor))));
    }
    return null;
  }
}
