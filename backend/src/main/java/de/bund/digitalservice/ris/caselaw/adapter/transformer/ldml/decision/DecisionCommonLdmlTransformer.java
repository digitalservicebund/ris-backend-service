package de.bund.digitalservice.ris.caselaw.adapter.transformer.ldml.decision;

import static de.bund.digitalservice.ris.caselaw.adapter.MappingUtils.applyIfNotEmpty;
import static de.bund.digitalservice.ris.caselaw.adapter.MappingUtils.nullSafeGet;
import static de.bund.digitalservice.ris.caselaw.adapter.MappingUtils.validate;
import static de.bund.digitalservice.ris.caselaw.adapter.MappingUtils.validateNotNull;

import de.bund.digitalservice.ris.caselaw.adapter.DateUtils;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.AknEmbeddedStructureInBlock;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.AknMultipleBlock;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.CaseLawLdml;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.DocumentRef;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.ForeignLanguageVersion;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.FrbrAlias;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.FrbrAuthor;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.FrbrCountry;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.FrbrDate;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.FrbrElement;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.FrbrLanguage;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.Identification;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.JaxbHtml;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.Judgment;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.JudgmentBody;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.Meta;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.Opinions;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.RelatedDecision;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.RisMeta;
import de.bund.digitalservice.ris.caselaw.adapter.exception.LdmlTransformationException;
import de.bund.digitalservice.ris.caselaw.adapter.transformer.ldml.DocumentationUnitLdmlTransformer;
import de.bund.digitalservice.ris.caselaw.adapter.transformer.ldml.HtmlTransformer;
import de.bund.digitalservice.ris.caselaw.domain.CoreData;
import de.bund.digitalservice.ris.caselaw.domain.Decision;
import de.bund.digitalservice.ris.caselaw.domain.LegalForce;
import de.bund.digitalservice.ris.caselaw.domain.LongTexts;
import de.bund.digitalservice.ris.caselaw.domain.RelatedDocumentationUnit;
import de.bund.digitalservice.ris.caselaw.domain.ShortTexts;
import de.bund.digitalservice.ris.caselaw.domain.court.Court;
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
    return Judgment.builder()
        .header(buildHeader(decision))
        .meta(buildMeta(decision))
        .judgmentBody(buildJudgmentBody(decision))
        .build();
  }

  protected abstract JaxbHtml buildHeader(Decision decision) throws ValidationException;

  protected String buildCommonHeader(Decision decision) throws ValidationException {
    validateCoreData(decision);
    var coreData = decision.coreData();

    StringBuilder builder = new StringBuilder();

    // Aktenzeichen
    if (coreData.fileNumbers() != null && !coreData.fileNumbers().isEmpty()) {
      builder
          .append("<p>Aktenzeichen: <akn:docNumber refersTo=\"#aktenzeichen\">")
          .append(coreData.fileNumbers().getFirst())
          .append("</akn:docNumber></p>");
    }

    // Entscheidungsdatum
    if (coreData.decisionDate() != null) {
      builder
          .append("<p>Entscheidungsdatum: <akn:docDate refersTo=\"#entscheidungsdatum\" date=\"")
          .append(DateUtils.toDateString(coreData.decisionDate()))
          .append("\">")
          .append(DateUtils.toFormattedDateString(coreData.decisionDate()))
          .append("</akn:docDate></p>");
    }

    // Gericht
    if (coreData.court() != null) {
      builder
          .append("<p>Gericht: <akn:courtType refersTo=\"#gericht\">")
          .append(coreData.court().label())
          .append("</akn:courtType></p>");
    }

    // Dokumenttyp
    if (coreData.documentType().label() != null) {
      builder
          .append("<p>")
          .append("Dokumenttyp: ")
          .append("<akn:docType refersTo=\"#dokumenttyp\">")
          .append(coreData.documentType().label())
          .append("</akn:docType>")
          .append("</p>");
    }

    return builder.toString();
  }

  protected abstract Meta buildMeta(Decision decision) throws ValidationException;

  protected abstract AknMultipleBlock buildIntroduction(Decision decision);

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

    var shortTexts = decision.shortTexts();
    var longTexts = decision.longTexts();

    builder
        // set guidingPrinciple/Leitsatz
        .motivation(
            JaxbHtml.build(
                htmlTransformer.htmlStringToObjectList(
                    nullSafeGet(shortTexts, ShortTexts::guidingPrinciple))))
        // set headnote/Orientierungssatz, "other headnote"/"Sonstiger Orientierungssatz",
        // Outline/Gliederung, Tenor/Tenor
        .introduction(buildIntroduction(decision))
        // set caseFacts/Tatbestand
        .background(buildBackground(decision))
        // set decisionReasons/Entscheidungsgründe, reasons/Gründe, otherLongText/"Sonstiger,
        // dissentingOpinion/"Abweichende Meinung"
        // Langtext"
        .decision(buildDecision(decision));

    var judgmentBody = builder.build();

    if (judgmentBody.getIntroduction() == null
        && judgmentBody.getBackground() == null
        && judgmentBody.getDecision() == null
        && judgmentBody.getMotivation() == null) {
      throw new ValidationException("Empty judgment body");
    }

    return judgmentBody;
  }

  private JaxbHtml buildBackground(Decision decision) {
    var longTexts = decision.longTexts();
    var caseFacts = nullSafeGet(longTexts, LongTexts::caseFacts);

    if (StringUtils.isNotEmpty(caseFacts)) {
      JaxbHtml backgroundHtml = JaxbHtml.build(htmlTransformer.htmlStringToObjectList(caseFacts));
      backgroundHtml.setDomainTerm("Tatbestand");
      return backgroundHtml;
    }
    return null;
  }

  private AknMultipleBlock buildDecision(Decision decision) {
    var longTexts = decision.longTexts();

    var decisionReasons = nullSafeGet(longTexts, LongTexts::decisionReasons);
    var reasons = nullSafeGet(longTexts, LongTexts::reasons);
    var otherLongText = nullSafeGet(longTexts, LongTexts::otherLongText);
    var dissentingOpinion = nullSafeGet(longTexts, LongTexts::dissentingOpinion);

    if (StringUtils.isNotEmpty(decisionReasons)
        || StringUtils.isNotEmpty(reasons)
        || StringUtils.isNotEmpty(otherLongText)
        || StringUtils.isNotEmpty(dissentingOpinion)) {
      return new AknMultipleBlock()
          .withBlock(
              AknEmbeddedStructureInBlock.DecisionReasons.NAME,
              AknEmbeddedStructureInBlock.DecisionReasons.build(
                  JaxbHtml.build(htmlTransformer.htmlStringToObjectList(decisionReasons))))
          .withBlock(
              AknEmbeddedStructureInBlock.Reasons.NAME,
              AknEmbeddedStructureInBlock.Reasons.build(
                  JaxbHtml.build(htmlTransformer.htmlStringToObjectList(reasons))))
          .withBlock(
              AknEmbeddedStructureInBlock.OtherLongText.NAME,
              AknEmbeddedStructureInBlock.OtherLongText.build(
                  JaxbHtml.build(htmlTransformer.htmlStringToObjectList(otherLongText))))
          .withBlock(
              Opinions.NAME,
              Opinions.build(htmlTransformer.htmlStringToObjectList(dissentingOpinion)));
    }
    return null;
  }

  protected List<RelatedDecision> buildRelatedDecisions(
      List<? extends RelatedDocumentationUnit> relatedDecisions) {
    List<RelatedDecision> previousDecision = new ArrayList<>();
    for (RelatedDocumentationUnit current : relatedDecisions) {
      RelatedDecision decision =
          RelatedDecision.builder()
              .date(DateUtils.toDateString(current.getDecisionDate()))
              .documentNumber(current.getDocumentNumber())
              .fileNumber(current.getFileNumber())
              .courtType(nullSafeGet(current.getCourt(), Court::type))
              .build();
      previousDecision.add(decision);
    }
    return previousDecision;
  }

  protected Identification buildIdentification(Decision decision) throws ValidationException {
    validateNotNull(decision.documentNumber(), "Unique identifier missing");
    validateNotNull(decision.uuid(), "Caselaw UUID missing");
    validateNotNull(
        nullSafeGet(decision.coreData(), CoreData::decisionDate), "DecisionDate missing");

    // Case law handover: When we always have an ecli, use that instead for the uniqueId
    String uniqueId = decision.documentNumber();
    FrbrDate frbrDate =
        new FrbrDate(
            DateUtils.toDateString(nullSafeGet(decision.coreData(), CoreData::decisionDate)),
            "entscheidungsdatum");
    FrbrAuthor frbrAuthor = new FrbrAuthor();

    List<FrbrAlias> aliases = generateAliases(decision);

    FrbrElement work =
        FrbrElement.builder()
            .frbrAlias(aliases)
            .frbrDate(frbrDate)
            .frbrAuthor(frbrAuthor)
            .frbrCountry(new FrbrCountry())
            .build()
            .withFrbrThisAndUri(uniqueId);

    FrbrElement expression =
        FrbrElement.builder()
            .frbrDate(frbrDate)
            .frbrAuthor(frbrAuthor)
            .frbrLanguage(new FrbrLanguage("deu"))
            .build()
            .withFrbrThisAndUri(uniqueId + "/dokument");

    FrbrElement manifestation =
        FrbrElement.builder()
            .frbrDate(frbrDate)
            .frbrAuthor(frbrAuthor)
            .build()
            .withFrbrThisAndUri(uniqueId + "/dokument.xml");

    return Identification.builder()
        .frbrWork(work)
        .frbrExpression(expression)
        .frbrManifestation(manifestation)
        .build();
  }

  protected List<FrbrAlias> generateAliases(Decision decision) {
    List<FrbrAlias> aliases = new ArrayList<>();

    aliases.add(new FrbrAlias("uebergreifende-id", decision.uuid().toString()));

    if (decision.coreData() != null && decision.coreData().ecli() != null) {
      aliases.add(new FrbrAlias("ecli", decision.coreData().ecli()));
    }

    if (decision.coreData() != null && decision.coreData().celexNumber() != null) {
      aliases.add(new FrbrAlias("celex", decision.coreData().celexNumber()));
    }

    return aliases;
  }

  protected String nullIfEmpty(String input) {
    if (StringUtils.isEmpty(input)) {
      return null;
    }
    return input;
  }

  protected void validateCoreData(Decision decision) throws ValidationException {
    if (decision.coreData() != null) {
      validateNotNull(decision.coreData().court(), "Court missing");
      if (decision.coreData().court() != null) {
        validateNotNull(decision.coreData().court().type(), "CourtType missing");
        validateNotNull(decision.coreData().court().type(), "CourtLabel missing");
      }
      validateNotNull(decision.coreData().documentType(), "DocumentType missing");
      validate(!decision.coreData().fileNumbers().isEmpty(), "FileNumber missing");
      validateNotNull(decision.coreData().decisionDate(), "DecisionDate missing");
    } else {
      throw new ValidationException("Core data is null");
    }
  }
}
