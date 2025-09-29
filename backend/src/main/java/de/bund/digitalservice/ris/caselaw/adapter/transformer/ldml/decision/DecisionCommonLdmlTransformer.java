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
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.References;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.RelatedDecision;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.RisMeta;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.TlcElement;
import de.bund.digitalservice.ris.caselaw.adapter.exception.LdmlTransformationException;
import de.bund.digitalservice.ris.caselaw.adapter.transformer.ldml.DocumentationUnitLdmlTransformer;
import de.bund.digitalservice.ris.caselaw.adapter.transformer.ldml.HtmlTransformer;
import de.bund.digitalservice.ris.caselaw.domain.CoreData;
import de.bund.digitalservice.ris.caselaw.domain.Decision;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationOffice;
import de.bund.digitalservice.ris.caselaw.domain.LegalForce;
import de.bund.digitalservice.ris.caselaw.domain.LongTexts;
import de.bund.digitalservice.ris.caselaw.domain.RelatedDocumentationUnit;
import de.bund.digitalservice.ris.caselaw.domain.ShortTexts;
import de.bund.digitalservice.ris.caselaw.domain.court.Court;
import jakarta.xml.bind.ValidationException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.xml.parsers.DocumentBuilderFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
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

  private JaxbHtml buildHeader(Decision decision) throws ValidationException {
    validateCoreData(decision);
    var coreData = decision.coreData();
    String fallbackTitle =
        "<p>"
            + coreData.court().label()
            + ", "
            + DateUtils.toFormattedDateString(coreData.decisionDate())
            + ", "
            + coreData.fileNumbers().getFirst()
            + "</p>";
    String title =
        ObjectUtils.defaultIfNull(
            nullSafeGet(decision.shortTexts(), ShortTexts::headline), fallbackTitle);

    validateNotNull(title, "Title missing");
    return JaxbHtml.build(htmlTransformer.htmlStringToObjectList(title));
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
        .background(
            JaxbHtml.build(
                htmlTransformer.htmlStringToObjectList(
                    nullSafeGet(longTexts, LongTexts::caseFacts))))
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

    String uniqueId = decision.documentNumber();
    FrbrDate frbrDecisionDate =
        new FrbrDate(
            DateUtils.toDateString(nullSafeGet(decision.coreData(), CoreData::decisionDate)),
            "Entscheidungsdatum");
    FrbrDate frbrPublicationDate =
        new FrbrDate(DateUtils.toDateString(LocalDate.now()), "XML Transformation");
    FrbrAuthor workExpressionAuthor =
        new FrbrAuthor("#" + getCourtEid(nullSafeGet(decision.coreData(), CoreData::court)));
    FrbrAuthor manifestationAuthor =
        new FrbrAuthor(
            "#" + getDocOfficeEid(nullSafeGet(decision.coreData(), CoreData::documentationOffice)));

    List<FrbrAlias> aliases = generateAliases(decision);

    FrbrElement work =
        FrbrElement.builder()
            .frbrAlias(aliases)
            .frbrDate(frbrDecisionDate)
            .frbrAuthor(workExpressionAuthor)
            .frbrCountry(new FrbrCountry())
            .build()
            .withFrbrThisAndUri(uniqueId);

    FrbrElement expression =
        FrbrElement.builder()
            .frbrDate(frbrDecisionDate)
            .frbrAuthor(workExpressionAuthor)
            .frbrLanguage(new FrbrLanguage("deu"))
            .build()
            .withFrbrThisAndUri(uniqueId + "/dokument");

    FrbrElement manifestation =
        FrbrElement.builder()
            .frbrDate(frbrPublicationDate)
            .frbrAuthor(manifestationAuthor)
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

    aliases.add(new FrbrAlias("Übergreifende ID", decision.uuid().toString()));
    aliases.add(new FrbrAlias("Dokumentnummer", decision.documentNumber()));

    if (decision.coreData() != null && decision.coreData().ecli() != null) {
      aliases.add(new FrbrAlias("ECLI", decision.coreData().ecli()));
    }

    if (decision.coreData() != null && decision.coreData().celexNumber() != null) {
      aliases.add(new FrbrAlias("CELEX Nummer", decision.coreData().celexNumber()));
    }

    if (decision.coreData() != null && decision.coreData().fileNumbers() != null) {
      decision
          .coreData()
          .fileNumbers()
          .forEach(fileNumber -> aliases.add(new FrbrAlias("Aktenzeichen", fileNumber)));
    }

    return aliases;
  }

  protected References buildReferences(Decision decision) {
    References.ReferencesBuilder referencesBuilder = References.builder();
    List<TlcElement> tlcOrganizations = new ArrayList<>();

    TlcElement ds =
        new TlcElement("ds", "https://www.digitalservice.bund.de", "DigitalService des Bundes");
    tlcOrganizations.add(ds);

    if (decision.coreData() != null && decision.coreData().documentationOffice() != null) {
      DocumentationOffice docOffice = decision.coreData().documentationOffice();
      String docOfficeEId = getDocOfficeEid(docOffice);
      TlcElement tlcDocOffice = new TlcElement(docOfficeEId, "", docOffice.abbreviation());
      tlcOrganizations.add(tlcDocOffice);
    }

    if (decision.coreData() != null && decision.coreData().court() != null) {
      String courtEId = getCourtEid(decision.coreData().court());
      TlcElement tlcCourt = new TlcElement(courtEId, "", decision.coreData().court().label());
      tlcOrganizations.add(tlcCourt);
    }

    return referencesBuilder.tlcOrganization(tlcOrganizations).build();
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

  private String getCourtEid(Court court) {
    return court.type().toLowerCase() + "-" + court.location().toLowerCase();
  }

  private String getDocOfficeEid(DocumentationOffice documentationOffice) {
    return documentationOffice.abbreviation().toLowerCase() + "-dok";
  }
}
