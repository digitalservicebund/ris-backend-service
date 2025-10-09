package de.bund.digitalservice.ris.caselaw.adapter.transformer.ldml.decision;

import static de.bund.digitalservice.ris.caselaw.adapter.MappingUtils.applyIfNotEmpty;
import static de.bund.digitalservice.ris.caselaw.adapter.MappingUtils.nullSafeGet;

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
import jakarta.xml.bind.ValidationException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import javax.xml.parsers.DocumentBuilderFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

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
  protected Meta buildMeta(Decision decision) throws ValidationException {
    validateCoreData(decision);

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

  private RisMeta buildRisMeta(Decision decision) {
    var builder = buildCommonRisMeta(decision);

    var contentRelatedIndexing = decision.contentRelatedIndexing();
    if (contentRelatedIndexing != null && contentRelatedIndexing.fieldsOfLaw() != null) {
      applyIfNotEmpty(
          contentRelatedIndexing.fieldsOfLaw().stream().map(FieldOfLaw::text).toList(),
          builder::fieldOfLaw);
    }

    if (contentRelatedIndexing != null && contentRelatedIndexing.definitions() != null) {
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

    var decisionName = nullSafeGet(decision.shortTexts(), ShortTexts::decisionName);
    if (StringUtils.isNotEmpty(decisionName)) {
      builder.decisionName(List.of(decisionName));
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
  protected Header buildHeader(Decision decision) throws ValidationException {
    List<Paragraph> paragraphs = new ArrayList<>();

    paragraphs = buildCommonHeader(decision, paragraphs);
    var shortTexts = decision.shortTexts();

    if (shortTexts != null) {
      // Entscheidungsname
      if (decision.shortTexts().decisionName() != null) {
        Paragraph decisionNameParagraph = Paragraph.builder().content(new ArrayList<>()).build();
        decisionNameParagraph.getContent().add("Entscheidungsname: ");
        decisionNameParagraph
            .getContent()
            .add(
                DocTitle.builder()
                    .refersTo("#entscheidungsname")
                    .content(shortTexts.decisionName())
                    .build());
        paragraphs.add(decisionNameParagraph);
      }

      // Titelzeile
      buildHeadline(paragraphs, shortTexts.headline(), htmlTransformer);
    }

    return Header.builder().paragraphs(paragraphs).build();
  }
}
