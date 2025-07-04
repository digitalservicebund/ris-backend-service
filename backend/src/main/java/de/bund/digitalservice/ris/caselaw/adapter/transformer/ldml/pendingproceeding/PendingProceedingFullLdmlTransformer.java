package de.bund.digitalservice.ris.caselaw.adapter.transformer.ldml.pendingproceeding;

import static de.bund.digitalservice.ris.caselaw.adapter.MappingUtils.applyIfNotEmpty;
import static de.bund.digitalservice.ris.caselaw.adapter.MappingUtils.nullSafeGet;

import de.bund.digitalservice.ris.caselaw.adapter.DateUtils;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.AknKeyword;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.Classification;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.Meta;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.Proprietary;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.RisMeta;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationOffice;
import de.bund.digitalservice.ris.caselaw.domain.PendingProceeding;
import de.bund.digitalservice.ris.caselaw.domain.PublicationStatus;
import de.bund.digitalservice.ris.caselaw.domain.Status;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.fieldoflaw.FieldOfLaw;
import jakarta.xml.bind.ValidationException;
import java.util.Collections;
import java.util.List;
import javax.xml.parsers.DocumentBuilderFactory;
import lombok.extern.slf4j.Slf4j;

/** Transformer for converting pending proceedings to LDML format for internal portal use. */
@Slf4j
public class PendingProceedingFullLdmlTransformer extends PendingProceedingCommonLdmlTransformer {

  public PendingProceedingFullLdmlTransformer(DocumentBuilderFactory documentBuilderFactory) {
    super(documentBuilderFactory);
  }

  @Override
  protected Meta buildMeta(PendingProceeding pendingProceeding) throws ValidationException {
    validateCoreData(pendingProceeding);

    Meta.MetaBuilder builder = Meta.builder();

    List<AknKeyword> keywords =
        pendingProceeding.contentRelatedIndexing() == null
            ? Collections.emptyList()
            : pendingProceeding.contentRelatedIndexing().keywords().stream()
                .map(AknKeyword::new)
                .toList();

    if (!keywords.isEmpty()) {
      builder.classification(Classification.builder().keyword(keywords).build());
    }

    return builder
        .identification(buildIdentification(pendingProceeding))
        .proprietary(Proprietary.builder().meta(buildRisMeta(pendingProceeding)).build())
        .build();
  }

  private RisMeta buildRisMeta(PendingProceeding pendingProceeding) {
    var builder = buildCommonRisMeta(pendingProceeding);

    var contentRelatedIndexing = pendingProceeding.contentRelatedIndexing();
    if (contentRelatedIndexing != null && contentRelatedIndexing.fieldsOfLaw() != null) {
      applyIfNotEmpty(
          contentRelatedIndexing.fieldsOfLaw().stream().map(FieldOfLaw::text).toList(),
          builder::fieldOfLaw);
    }

    var coreData = pendingProceeding.coreData();
    if (coreData != null) {
      if (coreData.deviatingDecisionDates() != null) {
        applyIfNotEmpty(
            coreData.deviatingDecisionDates().stream().map(DateUtils::toDateString).toList(),
            builder::deviatingDate);
      }
      applyIfNotEmpty(coreData.deviatingCourts(), builder::deviatingCourt);
      applyIfNotEmpty(coreData.deviatingFileNumbers(), builder::deviatingFileNumber);
      applyIfNotEmpty(coreData.fileNumbers(), builder::fileNumbers);

      builder.documentationOffice(
          nullSafeGet(coreData.documentationOffice(), DocumentationOffice::abbreviation));
    }

    Status lastStatus = pendingProceeding.status();

    return builder
        .publicationStatus(
            nullSafeGet(
                nullSafeGet(lastStatus, Status::publicationStatus), PublicationStatus::toString))
        .error(lastStatus != null && lastStatus.withError())
        .build();
  }
}
