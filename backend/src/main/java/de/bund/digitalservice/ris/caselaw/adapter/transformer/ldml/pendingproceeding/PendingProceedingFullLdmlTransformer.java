package de.bund.digitalservice.ris.caselaw.adapter.transformer.ldml.pendingproceeding;

import static de.bund.digitalservice.ris.caselaw.adapter.MappingUtils.applyIfNotEmpty;
import static de.bund.digitalservice.ris.caselaw.adapter.MappingUtils.nullSafeGet;

import de.bund.digitalservice.ris.caselaw.adapter.DateUtils;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.header.Header;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.header.Paragraph;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.meta.Classification;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.meta.Keyword;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.meta.Meta;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.meta.proprietary.Proprietary;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.meta.proprietary.RisMeta;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationOffice;
import de.bund.digitalservice.ris.caselaw.domain.PendingProceeding;
import de.bund.digitalservice.ris.caselaw.domain.PublicationStatus;
import de.bund.digitalservice.ris.caselaw.domain.Status;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.fieldoflaw.FieldOfLaw;
import jakarta.xml.bind.ValidationException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.xml.parsers.DocumentBuilderFactory;
import lombok.extern.slf4j.Slf4j;

/** Transformer for converting pending proceedings to full LDML format. */
@Slf4j
public class PendingProceedingFullLdmlTransformer extends PendingProceedingCommonLdmlTransformer {

  public PendingProceedingFullLdmlTransformer(DocumentBuilderFactory documentBuilderFactory) {
    super(documentBuilderFactory);
  }

  @Override
  protected Meta buildMeta(PendingProceeding pendingProceeding) throws ValidationException {
    validateCoreData(pendingProceeding);

    Meta.MetaBuilder builder = Meta.builder();

    List<Keyword> keywords =
        pendingProceeding.contentRelatedIndexing() == null
            ? Collections.emptyList()
            : pendingProceeding.contentRelatedIndexing().keywords().stream()
                .map(Keyword::new)
                .toList();

    if (!keywords.isEmpty()) {
      builder.classification(Classification.builder().keyword(keywords).build());
    }

    return builder
        .identification(buildIdentification(pendingProceeding))
        .references(buildReferences(pendingProceeding))
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
      applyIfNotEmpty(coreData.deviatingDocumentNumbers(), builder::deviatingDocumentNumber);
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

  @Override
  protected Header buildHeader(PendingProceeding pendingProceeding) throws ValidationException {
    List<Paragraph> paragraphs = new ArrayList<>();

    paragraphs = buildCommonHeader(pendingProceeding, paragraphs);
    var shortTexts = pendingProceeding.shortTexts();

    if (shortTexts != null) {
      buildHeadline(paragraphs, shortTexts.headline(), htmlTransformer);
    }

    return Header.builder().paragraphs(paragraphs).build();
  }
}
