package de.bund.digitalservice.ris.caselaw.adapter.transformer.ldml.pendingproceeding;

import static de.bund.digitalservice.ris.caselaw.adapter.MappingUtils.applyIfNotEmpty;
import static de.bund.digitalservice.ris.caselaw.adapter.MappingUtils.nullSafeGet;

import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.CaseLawLdml;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.JaxbHtml;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.Judgment;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.header.Header;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.judgementbody.Introduction;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.judgementbody.JudgmentBody;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.judgementbody.Motivation;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.meta.Meta;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.meta.proprietary.DocumentType;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.meta.proprietary.RisMeta;
import de.bund.digitalservice.ris.caselaw.adapter.exception.LdmlTransformationException;
import de.bund.digitalservice.ris.caselaw.adapter.transformer.ldml.DocumentationUnitLdmlTransformer;
import de.bund.digitalservice.ris.caselaw.adapter.transformer.ldml.HtmlTransformer;
import de.bund.digitalservice.ris.caselaw.domain.LegalForce;
import de.bund.digitalservice.ris.caselaw.domain.PendingProceeding;
import de.bund.digitalservice.ris.caselaw.domain.PendingProceedingShortTexts;
import de.bund.digitalservice.ris.caselaw.domain.court.Court;
import jakarta.xml.bind.ValidationException;
import java.util.ArrayList;
import java.util.List;
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

  protected abstract Header buildHeader(PendingProceeding pendingProceeding)
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

    builder
        .motivations(buildMotivations(pendingProceeding))
        .introductions(buildIntroductions(pendingProceeding))
        .background(null)
        .decision(buildDecision(pendingProceeding));

    var judgmentBody = builder.build();

    if (judgmentBody.getIntroductions().isEmpty()
        && judgmentBody.getBackground() == null
        && judgmentBody.getDecision() == null
        && judgmentBody.getMotivations() == null) {
      throw new ValidationException("Empty judgment body");
    }

    return judgmentBody;
  }

  protected List<Introduction> buildIntroductions(PendingProceeding pendingProceeding) {
    List<Introduction> introductions = new ArrayList<>();

    var shortTexts = pendingProceeding.shortTexts();

    // Rechtsmittelführer
    if (shortTexts != null && shortTexts.appellant() != null) {
      var introduction =
          Introduction.builder()
              .content(htmlTransformer.htmlStringToObjectList(shortTexts.appellant()))
              .build();
      introduction.setDomainTerm("Rechtsmittelführer");
      introductions.add(introduction);
    }

    // Rechtsmittelzulassung
    if (shortTexts != null && shortTexts.admissionOfAppeal() != null) {
      var introduction =
          Introduction.builder()
              .content(htmlTransformer.htmlStringToObjectList(shortTexts.admissionOfAppeal()))
              .build();
      introduction.setDomainTerm("Rechtsmittelzulassung");
      introductions.add(introduction);
    }
    return introductions;
  }

  private List<Motivation> buildMotivations(PendingProceeding pendingProceeding) {
    List<Motivation> motivations = new ArrayList<>();
    var shortTexts = pendingProceeding.shortTexts();

    // Rechtsfrage
    if (shortTexts != null) {
      var legalIssue = shortTexts.legalIssue();
      if (legalIssue != null) {
        var motivation =
            Motivation.builder()
                .content(htmlTransformer.htmlStringToObjectList(shortTexts.legalIssue()))
                .build();
        motivation.setDomainTerm("Rechtsfrage");
        motivations.add(motivation);
      }
    }
    return motivations;
  }

  private JaxbHtml buildDecision(PendingProceeding pendingProceeding) {
    var shortTexts = pendingProceeding.shortTexts();

    var resolutionNote = nullSafeGet(shortTexts, PendingProceedingShortTexts::resolutionNote);

    if (StringUtils.isNotEmpty(resolutionNote)) {
      var resolutionNoteHtml =
          JaxbHtml.build(htmlTransformer.htmlStringToObjectList(resolutionNote));
      resolutionNoteHtml.setDomainTerm("Erledigungsvermerk");
      return resolutionNoteHtml;
    }
    return null;
  }

  protected abstract Meta buildMeta(PendingProceeding pendingProceeding) throws ValidationException;
}
