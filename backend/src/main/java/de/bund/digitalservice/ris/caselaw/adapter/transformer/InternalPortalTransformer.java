package de.bund.digitalservice.ris.caselaw.adapter.transformer;

import static de.bund.digitalservice.ris.caselaw.adapter.MappingUtils.applyIfNotEmpty;
import static de.bund.digitalservice.ris.caselaw.adapter.MappingUtils.nullSafeGet;

import de.bund.digitalservice.ris.caselaw.adapter.DateUtils;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.AknKeyword;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.Classification;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.Meta;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.Proprietary;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.RisMeta;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationOffice;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnit;
import de.bund.digitalservice.ris.caselaw.domain.Procedure;
import de.bund.digitalservice.ris.caselaw.domain.PublicationStatus;
import de.bund.digitalservice.ris.caselaw.domain.ShortTexts;
import de.bund.digitalservice.ris.caselaw.domain.Status;
import de.bund.digitalservice.ris.caselaw.domain.court.Court;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.fieldoflaw.FieldOfLaw;
import jakarta.xml.bind.ValidationException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;
import javax.xml.parsers.DocumentBuilderFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

@Slf4j
public class InternalPortalTransformer extends CommonPortalTransformer {

  public InternalPortalTransformer(DocumentBuilderFactory documentBuilderFactory) {
    super(documentBuilderFactory);
  }

  @Override
  protected Meta buildMeta(DocumentationUnit documentationUnit) throws ValidationException {
    validateCoreData(documentationUnit);

    Meta.MetaBuilder builder = Meta.builder();

    List<AknKeyword> keywords =
        documentationUnit.contentRelatedIndexing() == null
            ? Collections.emptyList()
            : documentationUnit.contentRelatedIndexing().keywords().stream()
                .map(AknKeyword::new)
                .toList();

    if (!keywords.isEmpty()) {
      builder.classification(Classification.builder().keyword(keywords).build());
    }

    return builder
        .identification(buildIdentification(documentationUnit))
        .proprietary(Proprietary.builder().meta(buildRisMeta(documentationUnit)).build())
        .build();
  }

  private RisMeta buildRisMeta(DocumentationUnit documentationUnit) {
    var builder = buildCommonRisMeta(documentationUnit);

    var contentRelatedIndexing = documentationUnit.contentRelatedIndexing();
    if (contentRelatedIndexing != null) {
      applyIfNotEmpty(
          contentRelatedIndexing.fieldsOfLaw().stream().map(FieldOfLaw::text).toList(),
          builder::fieldOfLaw);
    }

    var coreData = documentationUnit.coreData();
    if (coreData != null) {
      if (coreData.deviatingDecisionDates() != null) {
        applyIfNotEmpty(
            coreData.deviatingDecisionDates().stream().map(DateUtils::toDateString).toList(),
            builder::deviatingDate);
      }
      applyIfNotEmpty(coreData.deviatingCourts(), builder::deviatingCourt);
      applyIfNotEmpty(coreData.deviatingEclis(), builder::deviatingEcli);
      applyIfNotEmpty(coreData.deviatingFileNumbers(), builder::deviatingFileNumber);
      applyIfNotEmpty(coreData.fileNumbers(), builder::fileNumbers);
      if (coreData.procedure() != null) {
        applyIfNotEmpty(
            Stream.of(coreData.procedure())
                .map(Procedure::label)
                .flatMap(it -> documentationUnit.coreData().previousProcedures().stream())
                .toList(),
            builder::procedure);
      }

      builder
          .documentType(coreData.documentType().label())
          .courtLocation(nullSafeGet(coreData.court(), Court::location))
          .courtType(nullSafeGet(coreData.court(), Court::type))
          .legalEffect(coreData.legalEffect())
          .judicialBody(nullIfEmpty(coreData.appraisalBody()))
          .documentationOffice(
              nullSafeGet(coreData.documentationOffice(), DocumentationOffice::abbreviation));
    }

    var decisionName = nullSafeGet(documentationUnit.shortTexts(), ShortTexts::decisionName);
    if (StringUtils.isNotEmpty(decisionName)) {
      builder.decisionName(List.of(decisionName));
    }

    Status lastStatus = documentationUnit.status();

    return builder
        .publicationStatus(
            nullSafeGet(
                nullSafeGet(lastStatus, Status::publicationStatus), PublicationStatus::toString))
        .error(lastStatus != null && lastStatus.withError())
        .build();
  }
}
