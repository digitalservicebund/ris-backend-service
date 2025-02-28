package de.bund.digitalservice.ris.caselaw.adapter.transformer;

import static de.bund.digitalservice.ris.caselaw.adapter.MappingUtils.applyIfNotEmpty;
import static de.bund.digitalservice.ris.caselaw.adapter.MappingUtils.nullSafeGet;

import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.Meta;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.Proprietary;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.RisMeta;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnit;
import de.bund.digitalservice.ris.caselaw.domain.court.Court;
import jakarta.xml.bind.ValidationException;
import javax.xml.parsers.DocumentBuilderFactory;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PublicPortalTransformer extends CommonPortalTransformer {

  public PublicPortalTransformer(DocumentBuilderFactory documentBuilderFactory) {
    super(documentBuilderFactory);
  }

  @Override
  protected Meta buildMeta(DocumentationUnit documentationUnit) throws ValidationException {
    validateCoreData(documentationUnit);

    Meta.MetaBuilder builder = Meta.builder();

    return builder
        .identification(buildIdentification(documentationUnit))
        .proprietary(Proprietary.builder().meta(buildRisMeta(documentationUnit)).build())
        .build();
  }

  private RisMeta buildRisMeta(DocumentationUnit documentationUnit) {
    var builder = buildCommonRisMeta(documentationUnit);

    var coreData = documentationUnit.coreData();
    if (coreData != null) {
      if (coreData.deviatingDecisionDates() != null) {
        applyIfNotEmpty(coreData.fileNumbers(), builder::fileNumbers);

        builder
            .documentType(coreData.documentType().label())
            .courtLocation(nullSafeGet(coreData.court(), Court::location))
            .courtType(nullSafeGet(coreData.court(), Court::type))
            .judicialBody(nullIfEmpty(coreData.appraisalBody()));
      }
    }
    return builder.build();
  }
}
