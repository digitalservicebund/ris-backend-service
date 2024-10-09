package de.bund.digitalservice.ris.caselaw.adapter.transformer;

import static de.bund.digitalservice.ris.caselaw.adapter.MappingUtils.applyIfNotEmpty;
import static de.bund.digitalservice.ris.caselaw.adapter.MappingUtils.nullSafeGet;
import static de.bund.digitalservice.ris.caselaw.adapter.MappingUtils.validate;
import static de.bund.digitalservice.ris.caselaw.adapter.MappingUtils.validateNotNull;

import de.bund.digitalservice.ris.caselaw.adapter.DateUtils;
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
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DecisionNameDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DeviatingCourtDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DeviatingEcliDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DeviatingFileNumberDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentationOfficeDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentationUnitDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.FileNumberDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.LegalEffectDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.RelatedDocumentationDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.StatusDTO;
import de.bund.digitalservice.ris.caselaw.domain.PublicationStatus;
import jakarta.xml.bind.ValidationException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CaseLawDbEntityToLdmlMapper {

  private static final Logger logger = LogManager.getLogger(CaseLawDbEntityToLdmlMapper.class);

  private CaseLawDbEntityToLdmlMapper() {}

  public static Optional<CaseLawLdml> getLDML(DocumentationUnitDTO caseLaw) {
    try {
      return Optional.of(CaseLawLdml.builder().judgment(buildJudgment(caseLaw)).build());
    } catch (ValidationException e) {
      logger.error("Case law validation failed: {}", e.getMessage());
      return Optional.empty();
    }
  }

  private static Judgment buildJudgment(DocumentationUnitDTO caseLaw) throws ValidationException {
    return Judgment.builder()
        .meta(buildMeta(caseLaw))
        .judgmentBody(buildJudgmentBody(caseLaw))
        .header(buildHeader(caseLaw))
        .build();
  }

  private static Header buildHeader(DocumentationUnitDTO caseLaw) throws ValidationException {
    // Case law handover : define what the title should be if headline is null
    String title =
        ObjectUtils.defaultIfNull(
            caseLaw.getHeadline(), "<p>" + caseLaw.getDocumentNumber() + "</p>");
    validateNotNull(title, "Title missing");
    return new Header()
        .withBlock("title", Title.build(title))
        .withBlock("opinions", Opinions.build(caseLaw.getDissentingOpinion()));
  }

  private static JudgmentBody buildJudgmentBody(DocumentationUnitDTO caseLaw) {

    JudgmentBody.JudgmentBodyBuilder builder = JudgmentBody.builder();

    builder
        .introduction(JaxbHtml.build(caseLaw.getGuidingPrinciple()))
        .background(JaxbHtml.build(caseLaw.getCaseFacts()))
        .decision(Decision.build(caseLaw.getTenor(), caseLaw.getOtherLongText()))
        .arguments(JaxbHtml.build(caseLaw.getDecisionGrounds()));

    if (StringUtils.isNotEmpty(caseLaw.getHeadnote())
        || StringUtils.isNotEmpty(caseLaw.getOtherHeadnote())
        || StringUtils.isNotEmpty(caseLaw.getGrounds())) {

      builder
          .motivation(
              new Motivation()
                  .withBlock(
                      AknEmbeddedStructureInBlock.HeadNote.NAME,
                      AknEmbeddedStructureInBlock.HeadNote.build(
                          JaxbHtml.build(caseLaw.getHeadnote())))
                  .withBlock(
                      AknEmbeddedStructureInBlock.OtherHeadNote.NAME,
                      AknEmbeddedStructureInBlock.OtherHeadNote.build(
                          JaxbHtml.build(caseLaw.getOtherHeadnote())))
                  .withBlock(
                      AknEmbeddedStructureInBlock.Grounds.NAME,
                      AknEmbeddedStructureInBlock.Grounds.build(
                          JaxbHtml.build(caseLaw.getGrounds()))))
          .build();
    }

    return builder.build();
  }

  private static Meta buildMeta(DocumentationUnitDTO caseLaw) throws ValidationException {
    validateNotNull(caseLaw.getCourt(), "Court missing");
    validateNotNull(caseLaw.getCourt().getType(), "CourtType missing");
    validateNotNull(caseLaw.getCourt().getLocation(), "CourtLocation missing");
    validateNotNull(caseLaw.getDocumentType(), "DocumentType missing");
    validateNotNull(caseLaw.getLegalEffect(), "LegalEffect missing");
    validate(!caseLaw.getFileNumbers().isEmpty(), "FileNumber missing");

    Meta.MetaBuilder builder = Meta.builder();

    List<AknKeyword> keywords =
        caseLaw.getDocumentationUnitKeywordDTOs().stream()
            .map(documentationUnitKeywordDTO -> documentationUnitKeywordDTO.getKeyword().getValue())
            .map(AknKeyword::new)
            .toList();
    if (!keywords.isEmpty()) {
      builder.classification(Classification.builder().keyword(keywords).build());
    }

    return builder
        .identification(buildIdentification(caseLaw))
        .proprietary(Proprietary.builder().meta(buildRisMeta(caseLaw)).build())
        .build();
  }

  private static RisMeta buildRisMeta(DocumentationUnitDTO caseLaw) {
    List<RelatedDecision> previousDecision = new ArrayList<>();
    List<RelatedDecision> ensuingDecision = new ArrayList<>();
    for (RelatedDocumentationDTO current : caseLaw.getPreviousDecisions()) {
      RelatedDecision decision =
          RelatedDecision.builder()
              .date(DateUtils.toDateString(current.getDate()))
              .documentNumber(current.getDocumentNumber())
              .fileNumber(current.getFileNumber())
              .courtType(current.getCourt().getType())
              .build();
      previousDecision.add(decision);
    }
    for (RelatedDocumentationDTO current : caseLaw.getEnsuingDecisions()) {
      RelatedDecision decision =
          RelatedDecision.builder()
              .date(DateUtils.toDateString(current.getDate()))
              .documentNumber(current.getDocumentNumber())
              .fileNumber(current.getFileNumber())
              .courtType(current.getCourt().getType())
              .build();
      ensuingDecision.add(decision);
    }
    StatusDTO lastStatus = getLastStatus(caseLaw);

    RisMeta.RisMetaBuilder builder = RisMeta.builder();

    List<String> legalForceLabel = new ArrayList<>();
    caseLaw
        .getNormReferences()
        .forEach(
            normReferenceDTO -> {
              if (normReferenceDTO.getLegalForce() == null) {
                return;
              }

              legalForceLabel.add(normReferenceDTO.getLegalForce().getLegalForceType().getLabel());
            });
    applyIfNotEmpty(legalForceLabel, builder::legalForce);
    applyIfNotEmpty(
        caseLaw.getDocumentationUnitFieldsOfLaw().stream()
            .map(
                documentationUnitFieldOfLawDTO ->
                    documentationUnitFieldOfLawDTO.getFieldOfLaw().getText())
            .toList(),
        builder::fieldOfLaw);
    applyIfNotEmpty(
        caseLaw.getDeviatingDates().stream()
            .map(deviationDate -> DateUtils.toDateString(deviationDate.getValue()))
            .toList(),
        builder::deviatingDate);
    applyIfNotEmpty(
        caseLaw.getDeviatingCourts().stream().map(DeviatingCourtDTO::getValue).toList(),
        builder::deviatingCourt);
    //    applyIfNotEmpty(
    //        caseLaw.getDeviatingDocumentNumbers().stream()
    //            .map(DeviatingDocumentNumber::getValue)
    //            .toList(),
    //        builder::deviatingDocumentNumber);
    applyIfNotEmpty(
        caseLaw.getDeviatingEclis().stream().map(DeviatingEcliDTO::getValue).toList(),
        builder::deviatingEcli);
    applyIfNotEmpty(
        caseLaw.getDeviatingFileNumbers().stream().map(DeviatingFileNumberDTO::getValue).toList(),
        builder::deviatingFileNumber);
    applyIfNotEmpty(
        caseLaw.getDecisionNames().stream().map(DecisionNameDTO::getValue).toList(),
        builder::decisionName);
    applyIfNotEmpty(
        caseLaw.getFileNumbers().stream().map(FileNumberDTO::getValue).toList(),
        builder::fileNumbers);
    applyIfNotEmpty(previousDecision, builder::previousDecision);
    applyIfNotEmpty(ensuingDecision, builder::ensuingDecision);
    applyIfNotEmpty(
        caseLaw.getProcedures().stream()
            .map(
                documentationUnitProcedureDTO ->
                    documentationUnitProcedureDTO.getProcedure().getLabel())
            .toList(),
        builder::procedure);

    return builder
        .documentType(caseLaw.getDocumentType().getLabel())
        .courtLocation(caseLaw.getCourt().getLocation())
        .courtType(caseLaw.getCourt().getType())
        .legalEffect(nullSafeGet(caseLaw.getLegalEffect(), LegalEffectDTO::toString))
        .judicialBody(nullIfEmpty(caseLaw.getJudicialBody()))
        .publicationStatus(
            nullSafeGet(
                nullSafeGet(lastStatus, StatusDTO::getPublicationStatus),
                PublicationStatus::toString))
        .error(lastStatus != null && lastStatus.isWithError())
        .documentationOffice(
            nullSafeGet(caseLaw.getDocumentationOffice(), DocumentationOfficeDTO::getAbbreviation))
        .build();
  }

  private static StatusDTO getLastStatus(DocumentationUnitDTO caselaw) {
    if (!caselaw.getStatus().isEmpty()) {
      return Collections.max(caselaw.getStatus(), Comparator.comparing(StatusDTO::getCreatedAt));
    }
    return null;
  }

  private static Identification buildIdentification(DocumentationUnitDTO caseLaw)
      throws ValidationException {
    validateNotNull(caseLaw.getDocumentNumber(), "Unique identifier missing");
    validateNotNull(caseLaw.getId(), "Caselaw UUID missing");
    validateNotNull(caseLaw.getDecisionDate(), "DecisionDate missing");

    // Case law handover: When we always have an ecli, use that instead for the uniqueId
    String uniqueId = caseLaw.getDocumentNumber();
    FrbrDate frbrDate = new FrbrDate(DateUtils.toDateString(caseLaw.getDecisionDate()));
    FrbrAuthor frbrAuthor = new FrbrAuthor();

    FrbrElement work =
        FrbrElement.builder()
            .frbrAlias(new FrbrAlias(caseLaw.getId().toString()))
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
}
