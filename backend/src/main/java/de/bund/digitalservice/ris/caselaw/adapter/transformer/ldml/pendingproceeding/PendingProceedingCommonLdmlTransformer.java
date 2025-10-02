package de.bund.digitalservice.ris.caselaw.adapter.transformer.ldml.pendingproceeding;

import static de.bund.digitalservice.ris.caselaw.adapter.MappingUtils.applyIfNotEmpty;
import static de.bund.digitalservice.ris.caselaw.adapter.MappingUtils.nullSafeGet;

import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.AknEmbeddedStructureInBlock;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.AknMultipleBlock;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.CaseLawLdml;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.DocumentType;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.JaxbHtml;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.Judgment;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.JudgmentBody;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.Meta;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.RisMeta;
import de.bund.digitalservice.ris.caselaw.adapter.exception.LdmlTransformationException;
import de.bund.digitalservice.ris.caselaw.adapter.transformer.ldml.DocumentationUnitLdmlTransformer;
import de.bund.digitalservice.ris.caselaw.adapter.transformer.ldml.HtmlTransformer;
import de.bund.digitalservice.ris.caselaw.domain.LegalForce;
import de.bund.digitalservice.ris.caselaw.domain.PendingProceeding;
import de.bund.digitalservice.ris.caselaw.domain.PendingProceedingShortTexts;
import de.bund.digitalservice.ris.caselaw.domain.court.Court;
import jakarta.xml.bind.ValidationException;
import java.util.Objects;
import javax.xml.parsers.DocumentBuilderFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

/**
 * Abstract base class for transforming pending proceedings into LDML case law format. Provides
 * common transformation logic and helper methods.
 */
@Slf4j
public abstract class PendingProceedingCommonLdmlTransformer
    implements DocumentationUnitLdmlTransformer<PendingProceeding> {

  protected final HtmlTransformer htmlTransformer;

  protected PendingProceedingCommonLdmlTransformer(DocumentBuilderFactory documentBuilderFactory) {
    this.htmlTransformer = new HtmlTransformer(documentBuilderFactory);
  }

  public CaseLawLdml transformToLdml(PendingProceeding pendingProceeding) {
    try {
      return CaseLawLdml.builder().judgment(buildJudgment(pendingProceeding)).build();
    } catch (ValidationException e) {
      if (e.getMessage().contains("Empty judgment body")) {
        throw new LdmlTransformationException("Missing judgment body.", e);
      }
      throw new LdmlTransformationException("LDML validation failed.", e);
    }
  }

  private Judgment buildJudgment(PendingProceeding pendingProceeding) throws ValidationException {
    return Judgment.builder()
        .name(pendingProceeding.coreData().documentType().label())
        .header(buildHeader(pendingProceeding))
        .meta(buildMeta(pendingProceeding))
        .judgmentBody(buildJudgmentBody(pendingProceeding))
        .build();
  }

  protected abstract JaxbHtml buildHeader(PendingProceeding pendingProceeding)
      throws ValidationException;

  protected RisMeta.RisMetaBuilder buildCommonRisMeta(PendingProceeding pendingProceeding) {
    RisMeta.RisMetaBuilder builder = RisMeta.builder();

    if (pendingProceeding.previousDecisions() != null) {
      applyIfNotEmpty(
          buildRelatedDecisions(pendingProceeding.previousDecisions()), builder::previousDecision);
    }

    var contentRelatedIndexing = pendingProceeding.contentRelatedIndexing();
    if (contentRelatedIndexing != null && contentRelatedIndexing.norms() != null) {
      applyIfNotEmpty(
          contentRelatedIndexing.norms().stream()
              .flatMap(normReference -> normReference.singleNorms().stream())
              .filter(Objects::nonNull)
              .map(
                  singleNorm -> {
                    var type = nullSafeGet(singleNorm.legalForce(), LegalForce::type);
                    return type != null ? type.label() : null;
                  })
              .filter(Objects::nonNull)
              .toList(),
          builder::legalForce);
    }

    var coreData = pendingProceeding.coreData();
    if (coreData != null) {
      applyIfNotEmpty(coreData.fileNumbers(), builder::fileNumbers);

      builder
          .documentType(
              DocumentType.builder()
                  .eId("dokumenttyp")
                  .value(coreData.documentType().label())
                  .build())
          .courtLocation(nullSafeGet(coreData.court(), Court::location))
          .courtType(nullSafeGet(coreData.court(), Court::type));
    }

    return builder;
  }

  private JudgmentBody buildJudgmentBody(PendingProceeding pendingProceeding)
      throws ValidationException {

    JudgmentBody.JudgmentBodyBuilder builder = JudgmentBody.builder();

    var shortTexts = pendingProceeding.shortTexts();

    builder
        .motivation(
            JaxbHtml.build(
                htmlTransformer.htmlStringToObjectList(
                    nullSafeGet(shortTexts, PendingProceedingShortTexts::legalIssue))))
        .introduction(buildIntroduction(pendingProceeding))
        .background(null)
        .decision(buildDecision(pendingProceeding));

    var judgmentBody = builder.build();

    if (judgmentBody.getIntroduction() == null
        && judgmentBody.getBackground() == null
        && judgmentBody.getDecision() == null
        && judgmentBody.getMotivation() == null) {
      throw new ValidationException("Empty judgment body");
    }

    return judgmentBody;
  }

  private AknMultipleBlock buildIntroduction(PendingProceeding pendingProceeding) {
    var shortTexts = pendingProceeding.shortTexts();

    var admissionOfAppeal = nullSafeGet(shortTexts, PendingProceedingShortTexts::admissionOfAppeal);
    var appellant = nullSafeGet(shortTexts, PendingProceedingShortTexts::appellant);

    if (StringUtils.isNotEmpty(admissionOfAppeal) && StringUtils.isNotEmpty(appellant)) {
      return new AknMultipleBlock()
          .withBlock(
              AknEmbeddedStructureInBlock.Appellant.NAME,
              AknEmbeddedStructureInBlock.Appellant.build(
                  JaxbHtml.build(htmlTransformer.htmlStringToObjectList(appellant))))
          .withBlock(
              AknEmbeddedStructureInBlock.AdmissionOfAppeal.NAME,
              AknEmbeddedStructureInBlock.AdmissionOfAppeal.build(
                  JaxbHtml.build(htmlTransformer.htmlStringToObjectList(admissionOfAppeal))));
    }
    return null;
  }

  private AknMultipleBlock buildDecision(PendingProceeding pendingProceeding) {
    var shortTexts = pendingProceeding.shortTexts();

    var resolutionNote = nullSafeGet(shortTexts, PendingProceedingShortTexts::resolutionNote);

    if (StringUtils.isNotEmpty(resolutionNote)) {
      return new AknMultipleBlock()
          .withBlock(
              AknEmbeddedStructureInBlock.ResolutionNote.NAME,
              AknEmbeddedStructureInBlock.ResolutionNote.build(
                  JaxbHtml.build(htmlTransformer.htmlStringToObjectList(resolutionNote))));
    }
    return null;
  }

  protected abstract Meta buildMeta(PendingProceeding pendingProceeding) throws ValidationException;
}
