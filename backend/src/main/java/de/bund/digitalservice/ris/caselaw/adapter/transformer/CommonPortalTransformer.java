package de.bund.digitalservice.ris.caselaw.adapter.transformer;

import static de.bund.digitalservice.ris.caselaw.adapter.MappingUtils.applyIfNotEmpty;
import static de.bund.digitalservice.ris.caselaw.adapter.MappingUtils.nullSafeGet;
import static de.bund.digitalservice.ris.caselaw.adapter.MappingUtils.validate;
import static de.bund.digitalservice.ris.caselaw.adapter.MappingUtils.validateNotNull;

import de.bund.digitalservice.ris.caselaw.adapter.DateUtils;
import de.bund.digitalservice.ris.caselaw.adapter.PortalTransformer;
import de.bund.digitalservice.ris.caselaw.adapter.XmlUtilService;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.AknEmbeddedStructureInBlock;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.AknMultipleBlock;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.CaseLawLdml;
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
import de.bund.digitalservice.ris.caselaw.domain.CoreData;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnit;
import de.bund.digitalservice.ris.caselaw.domain.LegalForce;
import de.bund.digitalservice.ris.caselaw.domain.LongTexts;
import de.bund.digitalservice.ris.caselaw.domain.RelatedDocumentationUnit;
import de.bund.digitalservice.ris.caselaw.domain.ShortTexts;
import de.bund.digitalservice.ris.caselaw.domain.court.Court;
import jakarta.xml.bind.ValidationException;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.mapping.MappingException;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Abstract base class for transforming documentation units into LDML case law format. Provides
 * common transformation logic and helper methods.
 */
@Slf4j
public abstract class CommonPortalTransformer implements PortalTransformer {

  private final DocumentBuilderFactory documentBuilderFactory;

  protected CommonPortalTransformer(DocumentBuilderFactory documentBuilderFactory) {
    this.documentBuilderFactory = documentBuilderFactory;
  }

  public CaseLawLdml transformToLdml(DocumentationUnit documentationUnit) {
    try {
      return CaseLawLdml.builder().judgment(buildJudgment(documentationUnit)).build();
    } catch (ValidationException e) {
      if (e.getMessage().contains("Empty judgment body")) {
        throw new LdmlTransformationException("Missing judgment body.", e);
      }
      throw new LdmlTransformationException("LDML validation failed.", e);
    }
  }

  private Judgment buildJudgment(DocumentationUnit documentationUnit) throws ValidationException {
    return Judgment.builder()
        .header(buildHeader(documentationUnit))
        .meta(buildMeta(documentationUnit))
        .judgmentBody(buildJudgmentBody(documentationUnit))
        .build();
  }

  private JaxbHtml buildHeader(DocumentationUnit documentationUnit) throws ValidationException {
    validateCoreData(documentationUnit);
    var coreData = documentationUnit.coreData();
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
            nullSafeGet(documentationUnit.shortTexts(), ShortTexts::headline), fallbackTitle);

    validateNotNull(title, "Title missing");
    return JaxbHtml.build(htmlStringToObjectList(title));
  }

  protected abstract Meta buildMeta(DocumentationUnit documentationUnit) throws ValidationException;

  protected abstract AknMultipleBlock buildIntroduction(DocumentationUnit documentationUnit);

  protected RisMeta.RisMetaBuilder buildCommonRisMeta(DocumentationUnit documentationUnit) {
    RisMeta.RisMetaBuilder builder = RisMeta.builder();

    if (documentationUnit.previousDecisions() != null) {
      applyIfNotEmpty(
          buildRelatedDecisions(documentationUnit.previousDecisions()), builder::previousDecision);
    }
    if (documentationUnit.ensuingDecisions() != null) {
      applyIfNotEmpty(
          buildRelatedDecisions(documentationUnit.ensuingDecisions()), builder::ensuingDecision);
    }

    var contentRelatedIndexing = documentationUnit.contentRelatedIndexing();
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

    return builder;
  }

  private JudgmentBody buildJudgmentBody(DocumentationUnit documentationUnit)
      throws ValidationException {

    JudgmentBody.JudgmentBodyBuilder builder = JudgmentBody.builder();

    var shortTexts = documentationUnit.shortTexts();
    var longTexts = documentationUnit.longTexts();

    builder
        // set guidingPrinciple/Leitsatz
        .motivation(
            JaxbHtml.build(
                htmlStringToObjectList(nullSafeGet(shortTexts, ShortTexts::guidingPrinciple))))
        // set headnote/Orientierungssatz, "other headnote"/"Sonstiger Orientierungssatz",
        // Outline/Gliederung, Tenor/Tenor
        .introduction(buildIntroduction(documentationUnit))
        // set caseFacts/Tatbestand
        .background(
            JaxbHtml.build(htmlStringToObjectList(nullSafeGet(longTexts, LongTexts::caseFacts))))
        // set decisionReasons/Entscheidungsgründe, reasons/Gründe, otherLongText/"Sonstiger,
        // dissentingOpinion/"Abweichende Meinung"
        // Langtext"
        .decision(buildDecision(documentationUnit));

    var judgmentBody = builder.build();

    if (judgmentBody.getIntroduction() == null
        && judgmentBody.getBackground() == null
        && judgmentBody.getDecision() == null
        && judgmentBody.getMotivation() == null) {
      throw new ValidationException("Empty judgment body");
    }

    return judgmentBody;
  }

  private AknMultipleBlock buildDecision(DocumentationUnit documentationUnit) {
    var longTexts = documentationUnit.longTexts();

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
                  JaxbHtml.build(htmlStringToObjectList(decisionReasons))))
          .withBlock(
              AknEmbeddedStructureInBlock.Reasons.NAME,
              AknEmbeddedStructureInBlock.Reasons.build(
                  JaxbHtml.build(htmlStringToObjectList(reasons))))
          .withBlock(
              AknEmbeddedStructureInBlock.OtherLongText.NAME,
              AknEmbeddedStructureInBlock.OtherLongText.build(
                  JaxbHtml.build(htmlStringToObjectList(otherLongText))))
          .withBlock(Opinions.NAME, Opinions.build(htmlStringToObjectList(dissentingOpinion)));
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

  protected Identification buildIdentification(DocumentationUnit documentationUnit)
      throws ValidationException {
    validateNotNull(documentationUnit.documentNumber(), "Unique identifier missing");
    validateNotNull(documentationUnit.uuid(), "Caselaw UUID missing");
    validateNotNull(
        nullSafeGet(documentationUnit.coreData(), CoreData::decisionDate), "DecisionDate missing");

    // Case law handover: When we always have an ecli, use that instead for the uniqueId
    String uniqueId = documentationUnit.documentNumber();
    FrbrDate frbrDate =
        new FrbrDate(
            DateUtils.toDateString(
                nullSafeGet(documentationUnit.coreData(), CoreData::decisionDate)));
    FrbrAuthor frbrAuthor = new FrbrAuthor();

    List<FrbrAlias> aliases = generateAliases(documentationUnit);

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
            .frbrLanguage(new FrbrLanguage())
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

  protected List<FrbrAlias> generateAliases(DocumentationUnit documentationUnit) {
    List<FrbrAlias> aliases = new ArrayList<>();

    aliases.add(new FrbrAlias("uebergreifende-id", documentationUnit.uuid().toString()));

    if (documentationUnit.coreData() != null && documentationUnit.coreData().ecli() != null) {
      aliases.add(new FrbrAlias("ecli", documentationUnit.coreData().ecli()));
    }

    return aliases;
  }

  protected String nullIfEmpty(String input) {
    if (StringUtils.isEmpty(input)) {
      return null;
    }
    return input;
  }

  protected List<Object> htmlStringToObjectList(String html) {
    if (StringUtils.isBlank(html)) {
      return Collections.emptyList();
    }

    html = html.replace("&nbsp;", "&#160;");

    // Pre-process:
    // HTML allows tags that are not closed. However, XML does not. That's why we do
    // this string-manipulation based workaround of closing the img and br tag.
    // Colgroup are style elements for columns in table and are not needed */
    html =
        html.replaceAll("(<img\\b[^>]*?)(?<!/)>", "$1/>")
            .replaceAll("<\\s*br\\s*>(?!\\s*<\\s*/\\s*br\\s*>)", "<br/>")
            .replaceAll("<colgroup[^>]*>.*?</colgroup>", "");

    try {
      String wrapped = "<wrapper>" + html + "</wrapper>";

      DocumentBuilder builder = documentBuilderFactory.newDocumentBuilder();
      Document doc = builder.parse(new InputSource(new StringReader(wrapped)));

      NodeList childNodes = doc.getDocumentElement().getChildNodes();

      return XmlUtilService.toList(childNodes).stream().map(e -> (Object) e).toList();
    } catch (ParserConfigurationException | IOException | SAXException e) {
      log.error("Xml transformation error.", e);
      throw new MappingException(e.getMessage());
    }
  }

  protected void validateCoreData(DocumentationUnit documentationUnit) throws ValidationException {
    if (documentationUnit.coreData() != null) {
      validateNotNull(documentationUnit.coreData().court(), "Court missing");
      if (documentationUnit.coreData().court() != null) {
        validateNotNull(documentationUnit.coreData().court().type(), "CourtType missing");
        validateNotNull(documentationUnit.coreData().court().type(), "CourtLabel missing");
      }
      validateNotNull(documentationUnit.coreData().documentType(), "DocumentType missing");
      validate(!documentationUnit.coreData().fileNumbers().isEmpty(), "FileNumber missing");
      validateNotNull(documentationUnit.coreData().decisionDate(), "DecisionDate missing");
    } else {
      throw new ValidationException("Core data is null");
    }
  }
}
