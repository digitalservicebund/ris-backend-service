package de.bund.digitalservice.ris.caselaw.adapter.transformer.ldml.pendingproceeding;

import static de.bund.digitalservice.ris.caselaw.adapter.MappingUtils.applyIfNotEmpty;
import static de.bund.digitalservice.ris.caselaw.adapter.MappingUtils.nullSafeGet;
import static de.bund.digitalservice.ris.caselaw.adapter.MappingUtils.validate;
import static de.bund.digitalservice.ris.caselaw.adapter.MappingUtils.validateNotNull;

import de.bund.digitalservice.ris.caselaw.adapter.DateUtils;
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
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.RelatedDecision;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.RisMeta;
import de.bund.digitalservice.ris.caselaw.adapter.exception.LdmlTransformationException;
import de.bund.digitalservice.ris.caselaw.adapter.transformer.ldml.DocumentationUnitLdmlTransformer;
import de.bund.digitalservice.ris.caselaw.domain.CoreData;
import de.bund.digitalservice.ris.caselaw.domain.LegalForce;
import de.bund.digitalservice.ris.caselaw.domain.PendingProceeding;
import de.bund.digitalservice.ris.caselaw.domain.PendingProceedingShortTexts;
import de.bund.digitalservice.ris.caselaw.domain.RelatedDocumentationUnit;
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
 * Abstract base class for transforming pending proceedings into LDML case law format. Provides
 * common transformation logic and helper methods.
 */
@Slf4j
public abstract class PendingProceedingCommonLdmlTransformer
    implements DocumentationUnitLdmlTransformer<PendingProceeding> {

  private final DocumentBuilderFactory documentBuilderFactory;

  protected PendingProceedingCommonLdmlTransformer(DocumentBuilderFactory documentBuilderFactory) {
    this.documentBuilderFactory = documentBuilderFactory;
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
        .header(buildHeader(pendingProceeding))
        .meta(buildMeta(pendingProceeding))
        .judgmentBody(buildJudgmentBody(pendingProceeding))
        .build();
  }

  private JaxbHtml buildHeader(PendingProceeding pendingProceeding) throws ValidationException {
    validateCoreData(pendingProceeding);
    var coreData = pendingProceeding.coreData();
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
            nullSafeGet(pendingProceeding.shortTexts(), PendingProceedingShortTexts::headline),
            fallbackTitle);

    validateNotNull(title, "Title missing");
    return JaxbHtml.build(htmlStringToObjectList(title));
  }

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
          .documentType(coreData.documentType().label())
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
                htmlStringToObjectList(
                    nullSafeGet(shortTexts, PendingProceedingShortTexts::legalIssue))))
        .introduction(null)
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

  private AknMultipleBlock buildDecision(PendingProceeding pendingProceeding) {
    var shortTexts = pendingProceeding.shortTexts();

    var resolutionNote = nullSafeGet(shortTexts, PendingProceedingShortTexts::resolutionNote);

    if (StringUtils.isNotEmpty(resolutionNote)) {
      return new AknMultipleBlock()
          .withBlock(
              AknEmbeddedStructureInBlock.ResolutionNote.NAME,
              AknEmbeddedStructureInBlock.ResolutionNote.build(
                  JaxbHtml.build(htmlStringToObjectList(resolutionNote))));
    }
    return null;
  }

  protected List<RelatedDecision> buildRelatedDecisions(
      List<? extends RelatedDocumentationUnit> relatedDecisions) {
    List<RelatedDecision> previousDecision = new ArrayList<>();
    for (RelatedDocumentationUnit current : relatedDecisions) {
      RelatedDecision pendingProceeding =
          RelatedDecision.builder()
              .date(DateUtils.toDateString(current.getDecisionDate()))
              .documentNumber(current.getDocumentNumber())
              .fileNumber(current.getFileNumber())
              .courtType(nullSafeGet(current.getCourt(), Court::type))
              .build();
      previousDecision.add(pendingProceeding);
    }
    return previousDecision;
  }

  protected Identification buildIdentification(PendingProceeding pendingProceeding)
      throws ValidationException {
    validateNotNull(pendingProceeding.documentNumber(), "Unique identifier missing");
    validateNotNull(pendingProceeding.uuid(), "Caselaw UUID missing");

    String uniqueId = pendingProceeding.documentNumber();
    FrbrDate frbrDate =
        new FrbrDate(
            DateUtils.toDateString(
                nullSafeGet(pendingProceeding.coreData(), CoreData::decisionDate)),
            "Mitteilungsdatum");
    FrbrAuthor frbrAuthor = new FrbrAuthor();

    List<FrbrAlias> aliases = generateAliases(pendingProceeding);

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

  protected List<FrbrAlias> generateAliases(PendingProceeding pendingProceeding) {
    return List.of(new FrbrAlias("uebergreifende-id", pendingProceeding.uuid().toString()));
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

  protected void validateCoreData(PendingProceeding pendingProceeding) throws ValidationException {
    if (pendingProceeding.coreData() != null) {
      validateNotNull(pendingProceeding.coreData().court(), "Court missing");
      if (pendingProceeding.coreData().court() != null) {
        validateNotNull(pendingProceeding.coreData().court().type(), "CourtType missing");
        validateNotNull(pendingProceeding.coreData().court().type(), "CourtLabel missing");
      }
      validateNotNull(pendingProceeding.coreData().documentType(), "DocumentType missing");
      validate(!pendingProceeding.coreData().fileNumbers().isEmpty(), "FileNumber missing");
    } else {
      throw new ValidationException("Core data is null");
    }
  }

  protected abstract Meta buildMeta(PendingProceeding pendingProceeding) throws ValidationException;
}
