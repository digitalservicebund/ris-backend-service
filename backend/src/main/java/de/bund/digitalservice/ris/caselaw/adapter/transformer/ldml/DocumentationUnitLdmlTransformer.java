package de.bund.digitalservice.ris.caselaw.adapter.transformer.ldml;

import static de.bund.digitalservice.ris.caselaw.adapter.MappingUtils.nullSafeGet;
import static de.bund.digitalservice.ris.caselaw.adapter.MappingUtils.validate;
import static de.bund.digitalservice.ris.caselaw.adapter.MappingUtils.validateNotNull;

import de.bund.digitalservice.ris.caselaw.adapter.DateUtils;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.CaseLawLdml;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.FrbrAlias;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.FrbrAuthor;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.FrbrCountry;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.FrbrDate;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.FrbrElement;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.FrbrLanguage;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.Identification;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.References;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.RelatedDecision;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.TlcElement;
import de.bund.digitalservice.ris.caselaw.domain.CoreData;
import de.bund.digitalservice.ris.caselaw.domain.Decision;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationOffice;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnit;
import de.bund.digitalservice.ris.caselaw.domain.PendingProceeding;
import de.bund.digitalservice.ris.caselaw.domain.RelatedDocumentationUnit;
import de.bund.digitalservice.ris.caselaw.domain.court.Court;
import jakarta.xml.bind.ValidationException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public interface DocumentationUnitLdmlTransformer<T extends DocumentationUnit> {
  Set<String> GERMAN_STATES =
      Set.of(
          "BW", "BY", "BE", "BB", "HB", "HH", "HE", "MV", "NI", "NW", "RP", "SL", "SN", "ST", "SH",
          "TH");

  CaseLawLdml transformToLdml(T documentationUnit);

  default Identification buildIdentification(DocumentationUnit documentationUnit)
      throws ValidationException {
    validateNotNull(documentationUnit.documentNumber(), "Unique identifier missing");
    validateNotNull(documentationUnit.uuid(), "Caselaw UUID missing");
    validateNotNull(
        nullSafeGet(documentationUnit.coreData(), CoreData::decisionDate), "DecisionDate missing");

    String uniqueId = documentationUnit.documentNumber();
    FrbrDate frbrDecisionDate =
        new FrbrDate(
            DateUtils.toDateString(
                nullSafeGet(documentationUnit.coreData(), CoreData::decisionDate)),
            getDateName(documentationUnit));
    FrbrDate frbrPublicationDate =
        new FrbrDate(DateUtils.toDateString(LocalDate.now()), "XML Transformation");
    FrbrAuthor workExpressionAuthor =
        new FrbrAuthor(
            "#" + getCourtEid(nullSafeGet(documentationUnit.coreData(), CoreData::court)));
    FrbrAuthor manifestationAuthor =
        new FrbrAuthor(
            "#"
                + getDocOfficeEid(
                    nullSafeGet(documentationUnit.coreData(), CoreData::documentationOffice)));

    List<FrbrAlias> aliases = generateAliases(documentationUnit);

    var elementBuilder =
        FrbrElement.builder()
            .frbrAlias(aliases)
            .frbrDate(frbrDecisionDate)
            .frbrAuthor(workExpressionAuthor);

    if (documentationUnit.coreData() != null
        && documentationUnit.coreData().court() != null
        && documentationUnit.coreData().court().region() != null) {
      elementBuilder.frbrCountry(
          // The schmema says ISO 3166-1 Alpha-2 code, but we only have Alpha-3 available (for now)
          new FrbrCountry(getCountry(documentationUnit.coreData().court().region())));
    }

    FrbrElement work = elementBuilder.build().withFrbrThisAndUri(uniqueId);

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

  private String getCountry(String region) {
    if (GERMAN_STATES.contains(region.toUpperCase())) {
      return "deu";
    }
    return region.toLowerCase();
  }

  private String getDateName(DocumentationUnit documentationUnit) {
    if (documentationUnit instanceof Decision) {
      return "Entscheidungsdatum";
    }
    if (documentationUnit instanceof PendingProceeding) {
      return "Mitteilungsdatum";
    }
    return "";
  }

  private List<FrbrAlias> generateAliases(DocumentationUnit documentationUnit) {
    List<FrbrAlias> aliases = new ArrayList<>();

    aliases.add(new FrbrAlias("Übergreifende ID", documentationUnit.uuid().toString()));
    aliases.add(new FrbrAlias("Dokumentnummer", documentationUnit.documentNumber()));

    if (documentationUnit.coreData() != null && documentationUnit.coreData().ecli() != null) {
      aliases.add(new FrbrAlias("ECLI", documentationUnit.coreData().ecli()));
    }

    if (documentationUnit.coreData() != null
        && documentationUnit.coreData().celexNumber() != null) {
      aliases.add(new FrbrAlias("CELEX-Nummer", documentationUnit.coreData().celexNumber()));
    }

    if (documentationUnit.coreData() != null
        && documentationUnit.coreData().fileNumbers() != null) {
      documentationUnit
          .coreData()
          .fileNumbers()
          .forEach(fileNumber -> aliases.add(new FrbrAlias("Aktenzeichen", fileNumber)));
    }
    return aliases;
  }

  default String buildCommonHeader(DocumentationUnit documentationUnit) throws ValidationException {
    validateCoreData(documentationUnit);
    var coreData = documentationUnit.coreData();

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

  default void validateCoreData(DocumentationUnit documentationUnit) throws ValidationException {
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

  default List<RelatedDecision> buildRelatedDecisions(
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

  default References buildReferences(DocumentationUnit documentationUnit) {
    References.ReferencesBuilder referencesBuilder = References.builder();
    List<TlcElement> tlcOrganizations = new ArrayList<>();
    List<TlcElement> tlcPersons = new ArrayList<>();
    List<TlcElement> tlcLocations = new ArrayList<>();

    TlcElement ds =
        new TlcElement(CaseLawLdml.RIS, "", CaseLawLdml.RECHTSINFORMATIONSSYSTEM_DES_BUNDES);
    tlcOrganizations.add(ds);

    if (documentationUnit.coreData() != null
        && documentationUnit.coreData().documentationOffice() != null) {
      DocumentationOffice docOffice = documentationUnit.coreData().documentationOffice();
      String docOfficeEId = getDocOfficeEid(docOffice);
      TlcElement tlcDocOffice = new TlcElement(docOfficeEId, "", docOffice.abbreviation());
      tlcOrganizations.add(tlcDocOffice);
    }

    if (documentationUnit.coreData() != null && documentationUnit.coreData().court() != null) {
      String courtEId = getCourtEid(documentationUnit.coreData().court());
      TlcElement tlcCourt =
          new TlcElement(courtEId, "", documentationUnit.coreData().court().label());
      tlcOrganizations.add(tlcCourt);
      TlcElement tlcCourtLocation =
          new TlcElement("gerichtsort", "", documentationUnit.coreData().court().location());
      tlcLocations.add(tlcCourtLocation);
    }

    if (documentationUnit.coreData() != null
        && documentationUnit.coreData().appraisalBody() != null) {
      TlcElement appraisalBody =
          new TlcElement("spruchkoerper", "", documentationUnit.coreData().appraisalBody());
      tlcOrganizations.add(appraisalBody);
    }

    transformParticipatingJudges(documentationUnit, tlcPersons);

    return referencesBuilder
        .tlcOrganizations(tlcOrganizations)
        .tlcPersons(tlcPersons)
        .tlcLocations(tlcLocations)
        .build();
  }

  private void transformParticipatingJudges(
      DocumentationUnit documentationUnit, List<TlcElement> tlcPersons) {
    if (documentationUnit instanceof Decision decision
        && decision.longTexts() != null
        && !decision.longTexts().participatingJudges().isEmpty()) {
      decision
          .longTexts()
          .participatingJudges()
          .forEach(
              participatingJudge -> {
                TlcElement judge =
                    new TlcElement(
                        toKebabCase(participatingJudge.name()), "", participatingJudge.name());
                tlcPersons.add(judge);
              });
    }
  }

  default String getCourtEid(Court court) {
    return toKebabCase(court.type() + " " + court.location());
  }

  default String getDocOfficeEid(DocumentationOffice documentationOffice) {
    if (documentationOffice == null) {
      return "dokumentationsstelle";
    }
    return toKebabCase(documentationOffice.abbreviation()) + "-dokumentationsstelle";
  }

  private String toKebabCase(String input) {
    if (input == null) return null;

    // Remove all non-letters
    String cleaned = input.replaceAll("[^\\p{L}\\s]+", "");

    cleaned = cleaned.toLowerCase();

    // Replace whitespace with hyphen
    cleaned = cleaned.trim().replaceAll("\\s+", "-");

    return cleaned;
  }
}
