package de.bund.digitalservice.ris.caselaw.adapter.transformer.ldml.decision;

import static de.bund.digitalservice.ris.caselaw.adapter.MappingUtils.applyIfNotEmpty;
import static de.bund.digitalservice.ris.caselaw.adapter.MappingUtils.nullSafeGet;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import de.bund.digitalservice.ris.caselaw.adapter.DateUtils;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.header.DocTitle;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.header.Header;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.header.Paragraph;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.meta.Classification;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.meta.Keyword;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.meta.Meta;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.meta.proprietary.Definition;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.meta.proprietary.DocumentType;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.meta.proprietary.Proprietary;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.meta.proprietary.RisMeta;
import de.bund.digitalservice.ris.caselaw.domain.ContentRelatedIndexing;
import de.bund.digitalservice.ris.caselaw.domain.Decision;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationOffice;
import de.bund.digitalservice.ris.caselaw.domain.Procedure;
import de.bund.digitalservice.ris.caselaw.domain.PublicationStatus;
import de.bund.digitalservice.ris.caselaw.domain.ShortTexts;
import de.bund.digitalservice.ris.caselaw.domain.Status;
import de.bund.digitalservice.ris.caselaw.domain.court.Court;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.fieldoflaw.FieldOfLaw;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import javax.xml.parsers.DocumentBuilderFactory;
import lombok.extern.slf4j.Slf4j;

/**
 * Transformer for converting decisions to the full LDML format. Includes additional metadata like
 * classification and keywords for internal processing. Currently, the public Prototype Portal is
 * under restrictions and must not use full LDML for legal reasons.
 */
@Slf4j
public class DecisionFullLdmlTransformer extends DecisionCommonLdmlTransformer {

  public DecisionFullLdmlTransformer(DocumentBuilderFactory documentBuilderFactory) {
    super(documentBuilderFactory);
  }

  @Override
  protected Meta buildMeta(Decision decision) {
    Meta.MetaBuilder builder = Meta.builder();

    List<Keyword> keywords =
        decision.contentRelatedIndexing() == null
            ? Collections.emptyList()
            : decision.contentRelatedIndexing().keywords().stream().map(Keyword::new).toList();

    if (!keywords.isEmpty()) {
      builder.classification(Classification.builder().keyword(keywords).build());
    }

    return builder
        .identification(buildIdentification(decision))
        .references(buildReferences(decision))
        .proprietary(Proprietary.builder().meta(buildRisMeta(decision)).build())
        .build();
  }

  @SuppressWarnings("java:S3776")
  private RisMeta buildRisMeta(Decision decision) {
    var builder = buildCommonRisMeta(decision);

    var contentRelatedIndexing = decision.contentRelatedIndexing();
    if (contentRelatedIndexing != null) {
      if (contentRelatedIndexing.fieldsOfLaw() != null) {
        applyIfNotEmpty(
            contentRelatedIndexing.fieldsOfLaw().stream().map(FieldOfLaw::text).toList(),
            builder::fieldOfLaw);
      }

      if (contentRelatedIndexing.definitions() != null) {
        applyIfNotEmpty(
            contentRelatedIndexing.definitions().stream()
                .map(
                    definition ->
                        Definition.builder()
                            .definedTerm(definition.definedTerm())
                            .definingBorderNumber(definition.definingBorderNumber())
                            .build())
                .toList(),
            builder::definitions);
      }
    }

    Optional.ofNullable(contentRelatedIndexing)
        .map(ContentRelatedIndexing::evsf)
        .ifPresent(builder::evfs);

    var coreData = decision.coreData();
    if (coreData != null) {
      if (coreData.deviatingDecisionDates() != null) {
        applyIfNotEmpty(
            coreData.deviatingDecisionDates().stream().map(DateUtils::toDateString).toList(),
            builder::deviatingDate);
      }
      if (coreData.oralHearingDates() != null) {
        applyIfNotEmpty(
            coreData.oralHearingDates().stream().map(DateUtils::toDateString).toList(),
            builder::oralHearingDate);
      }
      applyIfNotEmpty(coreData.deviatingCourts(), builder::deviatingCourt);
      applyIfNotEmpty(coreData.deviatingEclis(), builder::deviatingEcli);
      applyIfNotEmpty(coreData.deviatingFileNumbers(), builder::deviatingFileNumber);
      applyIfNotEmpty(coreData.deviatingDocumentNumbers(), builder::deviatingDocumentNumber);
      applyIfNotEmpty(coreData.fileNumbers(), builder::fileNumbers);
      if (coreData.procedure() != null) {
        applyIfNotEmpty(
            Stream.of(coreData.procedure())
                .map(Procedure::label)
                .flatMap(it -> decision.coreData().previousProcedures().stream())
                .toList(),
            builder::procedure);
      }

      if (coreData.legalEffect() != null) {
        builder.legalEffect(coreData.legalEffect());
      }

      if (coreData.inputTypes() != null) {
        applyIfNotEmpty(coreData.inputTypes(), builder::inputTypes);
      }

      builder
          .documentType(
              DocumentType.builder()
                  .eId("dokumenttyp")
                  .value(coreData.documentType().label())
                  .build())
          .courtLocation(nullSafeGet(coreData.court(), Court::location))
          .courtType(nullSafeGet(coreData.court(), Court::type))
          .judicialBody(nullIfEmpty(coreData.appraisalBody()))
          .documentationOffice(
              nullSafeGet(coreData.documentationOffice(), DocumentationOffice::abbreviation));
    }

    var decisionNames = nullSafeGet(decision.shortTexts(), ShortTexts::decisionNames);
    if (decisionNames != null) {
      builder.decisionName(decisionNames);
    }

    Status lastStatus = decision.status();

    return builder
        .publicationStatus(
            nullSafeGet(
                nullSafeGet(lastStatus, Status::publicationStatus), PublicationStatus::toString))
        .error(lastStatus != null && lastStatus.withError())
        .build();
  }

  @Override
  protected Header buildHeader(Decision decision) {
    List<Paragraph> paragraphs = new ArrayList<>();

    paragraphs = buildCommonHeader(decision, paragraphs);
    var shortTexts = decision.shortTexts();
    var decisionNames = nullSafeGet(shortTexts, ShortTexts::decisionNames);
    var headline = nullSafeGet(shortTexts, ShortTexts::headline);

    // Entscheidungsname
    if (decisionNames != null && !decisionNames.isEmpty()) {
      Paragraph decisionNameParagraph = Paragraph.builder().content(new ArrayList<>()).build();
      decisionNameParagraph.getContent().add("Entscheidungsnamen: ");
      shortTexts
          .decisionNames()
          .forEach(
              decisionName ->
                  decisionNameParagraph
                      .getContent()
                      .add(
                          DocTitle.builder()
                              .refersTo("#entscheidungsname")
                              .content(decisionName)
                              .build()));
      paragraphs.add(decisionNameParagraph);
    }

    // Titelzeile
    if (isNotBlank(headline)) {
      buildHeadline(paragraphs, headline, htmlTransformer);
    }

    return Header.builder().paragraphs(paragraphs).build();
  }
}
