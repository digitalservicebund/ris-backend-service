package de.bund.digitalservice.ris.caselaw.adapter.transformer.ldml.decision;

import static de.bund.digitalservice.ris.caselaw.adapter.MappingUtils.applyIfNotEmpty;
import static de.bund.digitalservice.ris.caselaw.adapter.MappingUtils.nullSafeGet;

import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.CaseLawLdml;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.JaxbHtml;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.Judgment;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.header.Header;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.judgementbody.Block;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.judgementbody.Introduction;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.judgementbody.JudgmentBody;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.judgementbody.Motivation;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.judgementbody.Opinion;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.meta.Meta;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.meta.identification.FrbrLanguage;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.meta.proprietary.DocumentRef;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.meta.proprietary.ForeignLanguageVersion;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.meta.proprietary.RisMeta;
import de.bund.digitalservice.ris.caselaw.adapter.exception.LdmlTransformationException;
import de.bund.digitalservice.ris.caselaw.adapter.transformer.ldml.DocumentationUnitLdmlTransformer;
import de.bund.digitalservice.ris.caselaw.adapter.transformer.ldml.HtmlTransformer;
import de.bund.digitalservice.ris.caselaw.domain.Decision;
import de.bund.digitalservice.ris.caselaw.domain.LegalForce;
import de.bund.digitalservice.ris.caselaw.domain.LongTexts;
import jakarta.xml.bind.ValidationException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.xml.parsers.DocumentBuilderFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

/**
 * Abstract base class for transforming decisions into LDML case law format. Provides common
 * transformation logic and helper methods.
 */
@Slf4j
public abstract class DecisionCommonLdmlTransformer
    implements DocumentationUnitLdmlTransformer<Decision> {

  protected final HtmlTransformer htmlTransformer;

  protected DecisionCommonLdmlTransformer(DocumentBuilderFactory documentBuilderFactory) {
    this.htmlTransformer = new HtmlTransformer(documentBuilderFactory);
  }

  public CaseLawLdml transformToLdml(Decision decision) {
    try {
      return CaseLawLdml.builder().judgment(buildJudgment(decision)).build();
    } catch (ValidationException e) {
      if (e.getMessage().contains("Empty judgment body")) {
        throw new LdmlTransformationException("Missing judgment body.", e);
      }
      throw new LdmlTransformationException("LDML validation failed.", e);
    }
  }

  private Judgment buildJudgment(Decision decision) throws ValidationException {
    var judgmentBuilder =
        Judgment.builder()
            .header(buildHeader(decision))
            .meta(buildMeta(decision))
            .judgmentBody(buildJudgmentBody(decision));
    if (decision.coreData() != null && decision.coreData().documentType() != null) {
      judgmentBuilder.name(decision.coreData().documentType().label());
    }
    return judgmentBuilder.build();
  }

  protected abstract Header buildHeader(Decision decision) throws ValidationException;

  protected abstract Meta buildMeta(Decision decision) throws ValidationException;

  protected RisMeta.RisMetaBuilder buildCommonRisMeta(Decision decision) {
    RisMeta.RisMetaBuilder builder = RisMeta.builder();

    if (decision.previousDecisions() != null) {
      applyIfNotEmpty(
          buildRelatedDecisions(decision.previousDecisions()), builder::previousDecision);
    }
    if (decision.ensuingDecisions() != null) {
      applyIfNotEmpty(buildRelatedDecisions(decision.ensuingDecisions()), builder::ensuingDecision);
    }

    if (decision.contentRelatedIndexing() != null
        && decision.contentRelatedIndexing().norms() != null) {
      applyIfNotEmpty(
          decision.contentRelatedIndexing().norms().stream()
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
    if (decision.contentRelatedIndexing() != null
        && decision.contentRelatedIndexing().foreignLanguageVersions() != null) {
      applyIfNotEmpty(
          decision.contentRelatedIndexing().foreignLanguageVersions().stream()
              .map(
                  foreignLanguageVersion ->
                      ForeignLanguageVersion.builder()
                          .documentRef(
                              DocumentRef.builder()
                                  .href(foreignLanguageVersion.link())
                                  .showAs(foreignLanguageVersion.languageCode().label())
                                  .build())
                          .frbrLanguage(
                              new FrbrLanguage(
                                  foreignLanguageVersion.languageCode().isoCode3Letters()))
                          .build())
              .toList(),
          builder::foreignLanguageVersions);
    }

    return builder;
  }

  private JudgmentBody buildJudgmentBody(Decision decision) throws ValidationException {

    JudgmentBody.JudgmentBodyBuilder builder = JudgmentBody.builder();

    builder
        .motivations(buildMotivations(decision))
        .introductions(buildIntroductions(decision))
        .background(buildBackground(decision))
        .decision(buildDecision(decision));

    var judgmentBody = builder.build();

    if (judgmentBody.getIntroductions().isEmpty()
        && judgmentBody.getBackground() == null
        && judgmentBody.getDecision() == null
        && judgmentBody.getMotivations().isEmpty()) {
      throw new ValidationException("Empty judgment body");
    }

    return judgmentBody;
  }

  protected List<Introduction> buildIntroductions(Decision decision) {
    List<Introduction> introductions = new ArrayList<>();

    var longTexts = decision.longTexts();
    var shortTexts = decision.shortTexts();

    // Leitsatz
    if (shortTexts != null && shortTexts.guidingPrinciple() != null) {
      var introduction =
          Introduction.builder()
              .content(htmlTransformer.htmlStringToObjectList(shortTexts.guidingPrinciple()))
              .build();
      introduction.setDomainTerm("Leitsatz");
      introductions.add(introduction);
    }

    // Gliederung
    if (longTexts != null && longTexts.outline() != null) {
      var introduction =
          Introduction.builder()
              .content(htmlTransformer.htmlStringToObjectList(longTexts.outline()))
              .build();
      introduction.setDomainTerm("Gliederung");
      introductions.add(introduction);
    }
    return introductions;
  }

  private List<Motivation> buildMotivations(Decision decision) {
    List<Motivation> motivations = new ArrayList<>();

    var longTexts = decision.longTexts();
    if (longTexts != null) {
      // Gründe
      var reasons = longTexts.reasons();
      if (reasons != null) {
        var motivation =
            Motivation.builder()
                .content(htmlTransformer.htmlStringToObjectList(longTexts.reasons()))
                .build();
        motivation.setDomainTerm("Gründe");
        motivations.add(motivation);
      }

      // Entscheidungsgründe
      var decisionReasons = longTexts.decisionReasons();
      if (decisionReasons != null) {
        var motivation =
            Motivation.builder()
                .content(htmlTransformer.htmlStringToObjectList(longTexts.decisionReasons()))
                .build();
        motivation.setDomainTerm("Entscheidungsgründe");
        motivations.add(motivation);
      }

      // Sonstiger Langtext
      var otherLongTexts = longTexts.otherLongText();
      if (otherLongTexts != null) {
        var motivation =
            Motivation.builder()
                .content(htmlTransformer.htmlStringToObjectList(longTexts.otherLongText()))
                .build();
        motivation.setDomainTerm("Sonstiger Langtext");
        motivations.add(motivation);
      }

      // Abweichende Meinung + Mitwirkende Richter
      var dissentingOpinion = longTexts.dissentingOpinion();
      if (dissentingOpinion != null) {
        var motivation = buildDissentingOpinion(decision);
        if (motivation != null) {
          motivation.setDomainTerm("Abweichende Meinung");
          motivations.add(motivation);
        }
      }
    }
    return motivations;
  }

  private Motivation buildDissentingOpinion(Decision decision) {
    var longTexts = decision.longTexts();
    if (longTexts == null) {
      return null;
    }

    String dissentingOpinion = longTexts.dissentingOpinion();
    var participatingJudges = longTexts.participatingJudges();

    List<Opinion> opinions = new ArrayList<>();

    if (participatingJudges != null && !participatingJudges.isEmpty()) {
      for (var judge : participatingJudges) {
        String byAttribute = "#" + toKebabCase(judge.name());
        Opinion opinion;
        if (judge.referencedOpinions() != null) {
          opinion =
              new Opinion(
                  "dissenting", "Art der Mitwirkung", byAttribute, judge.referencedOpinions());
        } else {
          opinion = new Opinion("dissenting", null, byAttribute, null);
        }
        opinions.add(opinion);
      }
    }

    Block block = null;
    if (!opinions.isEmpty()) {
      block = new Block("Mitwirkende Richter", opinions);
    }

    List<Object> content = new ArrayList<>();
    if (dissentingOpinion != null && !dissentingOpinion.isBlank()) {
      content.addAll(htmlTransformer.htmlStringToObjectList(dissentingOpinion));
    }

    if (block != null) {
      content.add(block);
    }

    Motivation motivation = new Motivation();
    motivation.setDomainTerm("Abweichende Meinung");
    motivation.setContent(content);

    return motivation;
  }

  private JaxbHtml buildBackground(Decision decision) {
    var longTexts = decision.longTexts();

    var caseFacts = nullSafeGet(longTexts, LongTexts::caseFacts);

    if (StringUtils.isNotEmpty(caseFacts)) {
      JaxbHtml html = JaxbHtml.build(htmlTransformer.htmlStringToObjectList(caseFacts));
      html.setDomainTerm("Tatbestand");
      return html;
    }
    return null;
  }

  private JaxbHtml buildDecision(Decision decision) {
    var longTexts = decision.longTexts();

    var tenor = nullSafeGet(longTexts, LongTexts::tenor);

    if (StringUtils.isNotEmpty(tenor)) {
      var tenorHtml = JaxbHtml.build(htmlTransformer.htmlStringToObjectList(tenor));
      tenorHtml.setDomainTerm("Tenor");
      return tenorHtml;
    }
    return null;
  }

  protected String nullIfEmpty(String input) {
    if (StringUtils.isEmpty(input)) {
      return null;
    }
    return input;
  }
}
