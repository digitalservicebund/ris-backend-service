package de.bund.digitalservice.ris.caselaw.adapter.transformer;

import static de.bund.digitalservice.ris.caselaw.adapter.MappingUtils.applyIfNotEmpty;
import static de.bund.digitalservice.ris.caselaw.adapter.MappingUtils.nullSafeGet;
import static de.bund.digitalservice.ris.caselaw.adapter.MappingUtils.validate;
import static de.bund.digitalservice.ris.caselaw.adapter.MappingUtils.validateNotNull;

import de.bund.digitalservice.ris.caselaw.adapter.DateUtils;
import de.bund.digitalservice.ris.caselaw.adapter.XmlUtilService;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.AknEmbeddedStructureInBlock;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.AknKeyword;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.CaseLawLdml;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.Classification;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.Decision;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.FrbrAlias;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.FrbrAuthor;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.FrbrCountry;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.FrbrDate;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.FrbrElement;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.FrbrLanguage;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.Header;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.Identification;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.JaxbHtml;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.Judgment;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.JudgmentBody;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.Meta;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.Motivation;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.Opinions;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.Proprietary;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.RelatedDecision;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.RisMeta;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.Title;
import de.bund.digitalservice.ris.caselaw.domain.CoreData;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationOffice;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnit;
import de.bund.digitalservice.ris.caselaw.domain.EnsuingDecision;
import de.bund.digitalservice.ris.caselaw.domain.LongTexts;
import de.bund.digitalservice.ris.caselaw.domain.PreviousDecision;
import de.bund.digitalservice.ris.caselaw.domain.Procedure;
import de.bund.digitalservice.ris.caselaw.domain.PublicationStatus;
import de.bund.digitalservice.ris.caselaw.domain.ShortTexts;
import de.bund.digitalservice.ris.caselaw.domain.Status;
import de.bund.digitalservice.ris.caselaw.domain.court.Court;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.fieldoflaw.FieldOfLaw;
import jakarta.xml.bind.ValidationException;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.data.mapping.MappingException;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

@Slf4j
public class DocumentationUnitToLdmlTransformer {

  private static final Logger logger =
      LogManager.getLogger(DocumentationUnitToLdmlTransformer.class);
  private static DocumentBuilderFactory documentBuilderFactory;

  private DocumentationUnitToLdmlTransformer() {}

  public static Optional<CaseLawLdml> transformToLdml(
      DocumentationUnit documentationUnit, DocumentBuilderFactory documentBuilderFactory) {
    DocumentationUnitToLdmlTransformer.documentBuilderFactory = documentBuilderFactory;

    try {
      return Optional.of(CaseLawLdml.builder().judgment(buildJudgment(documentationUnit)).build());
    } catch (ValidationException e) {
      logger.error("Case law validation failed: {}", e.getMessage());
      return Optional.empty();
    }
  }

  private static Judgment buildJudgment(DocumentationUnit documentationUnit)
      throws ValidationException {
    return Judgment.builder()
        .meta(buildMeta(documentationUnit))
        .judgmentBody(buildJudgmentBody(documentationUnit))
        .header(buildHeader(documentationUnit))
        .build();
  }

  private static Header buildHeader(DocumentationUnit documentationUnit)
      throws ValidationException {
    // Case law handover : define what the title should be if headline is null
    String title =
        ObjectUtils.defaultIfNull(
            nullSafeGet(documentationUnit.shortTexts(), ShortTexts::headline),
            "<p>" + documentationUnit.documentNumber() + "</p>");

    validateNotNull(title, "Title missing");
    String opinion = nullSafeGet(documentationUnit.longTexts(), LongTexts::dissentingOpinion);
    return new Header()
        .withBlock("title", Title.build(htmlStringToObjectList(title)))
        .withBlock("opinions", Opinions.build(htmlStringToObjectList(opinion)));
  }

  private static JudgmentBody buildJudgmentBody(DocumentationUnit documentationUnit) {

    JudgmentBody.JudgmentBodyBuilder builder = JudgmentBody.builder();

    var shortTexts = documentationUnit.shortTexts();
    var longTexts = documentationUnit.longTexts();

    builder
        .introduction(
            JaxbHtml.build(
                htmlStringToObjectList(nullSafeGet(shortTexts, ShortTexts::guidingPrinciple))))
        .background(
            JaxbHtml.build(htmlStringToObjectList(nullSafeGet(longTexts, LongTexts::caseFacts))))
        .decision(
            Decision.build(
                htmlStringToObjectList(nullSafeGet(longTexts, LongTexts::tenor)),
                htmlStringToObjectList(nullSafeGet(longTexts, LongTexts::otherLongText))))
        .arguments(
            JaxbHtml.build(
                htmlStringToObjectList(nullSafeGet(longTexts, LongTexts::decisionReasons))));

    var headnote = nullSafeGet(shortTexts, ShortTexts::headnote);
    var otherHeadnote = nullSafeGet(shortTexts, ShortTexts::otherHeadnote);
    var reasons = nullSafeGet(longTexts, LongTexts::reasons);
    if (StringUtils.isNotEmpty(headnote)
        || StringUtils.isNotEmpty(otherHeadnote)
        || StringUtils.isNotEmpty(reasons)) {

      builder
          .motivation(
              new Motivation()
                  .withBlock(
                      AknEmbeddedStructureInBlock.HeadNote.NAME,
                      AknEmbeddedStructureInBlock.HeadNote.build(
                          JaxbHtml.build(htmlStringToObjectList(headnote))))
                  .withBlock(
                      AknEmbeddedStructureInBlock.OtherHeadNote.NAME,
                      AknEmbeddedStructureInBlock.OtherHeadNote.build(
                          JaxbHtml.build(htmlStringToObjectList(otherHeadnote))))
                  .withBlock(
                      AknEmbeddedStructureInBlock.Grounds.NAME,
                      AknEmbeddedStructureInBlock.Grounds.build(
                          JaxbHtml.build(htmlStringToObjectList(reasons)))))
          .build();
    }

    return builder.build();
  }

  private static Meta buildMeta(DocumentationUnit documentationUnit) throws ValidationException {
    if (documentationUnit.coreData() != null) {
      validateNotNull(documentationUnit.coreData().court(), "Court missing");
      if (documentationUnit.coreData().court() != null) {
        validateNotNull(documentationUnit.coreData().court().type(), "CourtType missing");
        validateNotNull(documentationUnit.coreData().court().location(), "CourtLocation missing");
      }
      validateNotNull(documentationUnit.coreData().documentType(), "DocumentType missing");
      validateNotNull(documentationUnit.coreData().legalEffect(), "LegalEffect missing");
      validate(!documentationUnit.coreData().fileNumbers().isEmpty(), "FileNumber missing");
    } else {
      throw new RuntimeException("Core data is null");
    }

    Meta.MetaBuilder builder = Meta.builder();

    List<AknKeyword> keywords =
        documentationUnit.contentRelatedIndexing() == null
            ? Collections.emptyList()
            : documentationUnit.contentRelatedIndexing().keywords().stream()
                .map(AknKeyword::new)
                .toList();

    if (!keywords.isEmpty()) {
      builder.classification(Classification.builder().keyword(keywords).build());
    }

    return builder
        .identification(buildIdentification(documentationUnit))
        .proprietary(Proprietary.builder().meta(buildRisMeta(documentationUnit)).build())
        .build();
  }

  private static RisMeta buildRisMeta(DocumentationUnit documentationUnit) {
    RisMeta.RisMetaBuilder builder = RisMeta.builder();

    List<RelatedDecision> previousDecision = new ArrayList<>();
    List<RelatedDecision> ensuingDecision = new ArrayList<>();
    if (documentationUnit.previousDecisions() != null) {
      for (PreviousDecision current : documentationUnit.previousDecisions()) {
        RelatedDecision decision =
            RelatedDecision.builder()
                .date(DateUtils.toDateString(current.getDecisionDate()))
                .documentNumber(current.getDocumentNumber())
                .fileNumber(current.getFileNumber())
                .courtType(nullSafeGet(current.getCourt(), Court::type))
                .build();
        previousDecision.add(decision);
      }
    }
    if (documentationUnit.ensuingDecisions() != null) {
      for (EnsuingDecision current : documentationUnit.ensuingDecisions()) {
        RelatedDecision decision =
            RelatedDecision.builder()
                .date(DateUtils.toDateString(current.getDecisionDate()))
                .documentNumber(current.getDocumentNumber())
                .fileNumber(current.getFileNumber())
                .courtType(nullSafeGet(current.getCourt(), Court::type))
                .build();
        ensuingDecision.add(decision);
      }
    }
    applyIfNotEmpty(previousDecision, builder::previousDecision);
    applyIfNotEmpty(ensuingDecision, builder::ensuingDecision);

    var contentRelatedIndexing = documentationUnit.contentRelatedIndexing();
    if (contentRelatedIndexing != null) {
      applyIfNotEmpty(
          contentRelatedIndexing.norms().stream()
              .flatMap(normReference -> normReference.singleNorms().stream())
              .map(singleNorm -> singleNorm.legalForce().type().label())
              .filter(Objects::nonNull)
              .toList(),
          builder::legalForce);
      applyIfNotEmpty(
          contentRelatedIndexing.fieldsOfLaw().stream().map(FieldOfLaw::text).toList(),
          builder::fieldOfLaw);
    }

    var coreData = documentationUnit.coreData();
    if (coreData != null) {
      if (coreData.deviatingDecisionDates() != null) {
        applyIfNotEmpty(
            coreData.deviatingDecisionDates().stream().map(DateUtils::toDateString).toList(),
            builder::deviatingDate);
      }
      applyIfNotEmpty(coreData.deviatingCourts(), builder::deviatingCourt);
      //    applyIfNotEmpty(
      //        caseLaw.getDeviatingDocumentNumbers().stream()
      //            .map(DeviatingDocumentNumber::getValue)
      //            .toList(),
      //        builder::deviatingDocumentNumber);
      applyIfNotEmpty(coreData.deviatingEclis(), builder::deviatingEcli);
      applyIfNotEmpty(coreData.deviatingFileNumbers(), builder::deviatingFileNumber);
      applyIfNotEmpty(coreData.fileNumbers(), builder::fileNumbers);
      if (coreData.procedure() != null) {
        applyIfNotEmpty(
            Stream.of(coreData.procedure())
                .map(Procedure::label)
                .flatMap(it -> documentationUnit.coreData().previousProcedures().stream())
                .toList(),
            builder::procedure);
      }

      builder
          .documentType(coreData.documentType().label())
          .courtLocation(nullSafeGet(coreData.court(), Court::location))
          .courtType(nullSafeGet(coreData.court(), Court::type))
          .legalEffect(coreData.legalEffect())
          .judicialBody(nullIfEmpty(coreData.appraisalBody()))
          .documentationOffice(
              nullSafeGet(coreData.documentationOffice(), DocumentationOffice::abbreviation));
    }

    var decisionName = nullSafeGet(documentationUnit.shortTexts(), ShortTexts::decisionName);
    if (StringUtils.isNotEmpty(decisionName)) {
      builder.decisionName(List.of(decisionName));
    }

    Status lastStatus = documentationUnit.status();

    return builder
        .publicationStatus(
            nullSafeGet(
                nullSafeGet(lastStatus, Status::publicationStatus), PublicationStatus::toString))
        .error(lastStatus != null && lastStatus.withError())
        .build();
  }

  private static Identification buildIdentification(DocumentationUnit documentationUnit)
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

    FrbrElement work =
        FrbrElement.builder()
            .frbrAlias(new FrbrAlias(documentationUnit.uuid().toString()))
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

  private static String nullIfEmpty(String input) {
    if (StringUtils.isEmpty(input)) {
      return null;
    }
    return input;
  }

  private static List<Object> htmlStringToObjectList(String html) {
    if (StringUtils.isBlank(html)) {
      return Collections.emptyList();
    }

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
}
