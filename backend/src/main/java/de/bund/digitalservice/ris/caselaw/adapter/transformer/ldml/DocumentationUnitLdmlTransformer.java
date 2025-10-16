package de.bund.digitalservice.ris.caselaw.adapter.transformer.ldml;

import static de.bund.digitalservice.ris.caselaw.adapter.MappingUtils.nullSafeGet;
import static de.bund.digitalservice.ris.caselaw.adapter.MappingUtils.validate;
import static de.bund.digitalservice.ris.caselaw.adapter.MappingUtils.validateNotNull;

import de.bund.digitalservice.ris.caselaw.adapter.DateUtils;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.CaseLawLdml;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.RelatedDecision;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.TlcElement;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.header.CourtType;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.header.DocDate;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.header.DocNumber;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.header.DocType;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.header.EmbeddedStructure;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.header.Paragraph;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.header.ShortTitle;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.meta.identification.FrbrAlias;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.meta.identification.FrbrAuthor;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.meta.identification.FrbrCountry;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.meta.identification.FrbrDate;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.meta.identification.FrbrElement;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.meta.identification.FrbrLanguage;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.meta.identification.Identification;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.meta.references.References;
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
        && documentationUnit.coreData().court().regions() != null
        && !documentationUnit.coreData().court().regions().isEmpty()) {
      elementBuilder.frbrCountry(
          // The schmema says ISO 3166-1 Alpha-2 code, but we only have Alpha-3 available (for now)
          // (We have only two regions for a handful of german courts)
          new FrbrCountry(getCountry(documentationUnit.coreData().court().regions().getFirst())));
    }

    FrbrElement work = elementBuilder.build().withFrbrThisAndUri(uniqueId);

    FrbrElement expression =
        FrbrElement.builder()
            .frbrDate(frbrDecisionDate)
            .frbrAuthor(workExpressionAuthor)
            .frbrLanguage(new FrbrLanguage("deu"))
            .build()
            .withFrbrThisAndUri(uniqueId);

    FrbrElement manifestation =
        FrbrElement.builder()
            .frbrDate(frbrPublicationDate)
            .frbrAuthor(manifestationAuthor)
            .build()
            .withFrbrThisAndUri(uniqueId + ".xml");

    return Identification.builder()
        .frbrWork(work)
        .frbrExpression(expression)
        .frbrManifestation(manifestation)
        .build();
  }

  default List<Paragraph> buildCommonHeader(
      DocumentationUnit documentationUnit, List<Paragraph> paragraphs) throws ValidationException {
    validateCoreData(documentationUnit);
    var coreData = documentationUnit.coreData();

    // Aktenzeichen
    if (coreData.fileNumbers() != null && !coreData.fileNumbers().isEmpty()) {
      Paragraph fileNumberParagraph = Paragraph.builder().content(new ArrayList<>()).build();
      fileNumberParagraph.getContent().add("Aktenzeichen: ");
      fileNumberParagraph
          .getContent()
          .add(
              DocNumber.builder()
                  .refersTo("#aktenzeichen")
                  .content(coreData.fileNumbers().getFirst())
                  .build());
      paragraphs.add(fileNumberParagraph);
    }

    // Entscheidungsdatum
    if (coreData.decisionDate() != null) {
      Paragraph decisionDateParagraph = Paragraph.builder().content(new ArrayList<>()).build();
      decisionDateParagraph.getContent().add(getDateName(documentationUnit) + ": ");
      decisionDateParagraph
          .getContent()
          .add(
              DocDate.builder()
                  .date(DateUtils.toDateString(coreData.decisionDate()))
                  .refersTo("#" + toKebabCase(getDateName(documentationUnit)))
                  .content(DateUtils.toFormattedDateString(coreData.decisionDate()))
                  .build());
      paragraphs.add(decisionDateParagraph);
    }

    // Gericht
    if (coreData.court() != null) {
      Paragraph courtParagraph = Paragraph.builder().content(new ArrayList<>()).build();
      courtParagraph.getContent().add("Gericht: ");
      courtParagraph
          .getContent()
          .add(CourtType.builder().refersTo("#gericht").content(coreData.court().label()).build());
      paragraphs.add(courtParagraph);
    }

    // Dokumenttyp
    if (coreData.documentType().label() != null) {
      Paragraph documentTypeParagraph = Paragraph.builder().content(new ArrayList<>()).build();
      documentTypeParagraph.getContent().add("Dokumenttyp: ");
      var docType =
          DocType.builder()
              .refersTo("#dokumenttyp")
              .content(coreData.documentType().label())
              .build();
      documentTypeParagraph.getContent().add(docType);
      paragraphs.add(documentTypeParagraph);
    }

    return paragraphs;
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

  default void buildHeadline(
      List<Paragraph> paragraphs, String headline, HtmlTransformer htmlTransformer) {
    if (headline != null) {
      Paragraph headlineParagraph = Paragraph.builder().content(new ArrayList<>()).build();
      headlineParagraph.getContent().add("Titelzeile: ");
      headlineParagraph
          .getContent()
          .add(
              ShortTitle.builder()
                  .refersTo("#titelzeile")
                  .content(
                      EmbeddedStructure.builder()
                          .content(htmlTransformer.htmlStringToObjectList(headline))
                          .build())
                  .build());
      paragraphs.add(headlineParagraph);
    }
  }

  private String getCourtEid(Court court) {
    return toKebabCase(court.type() + " " + court.location());
  }

  private String getDocOfficeEid(DocumentationOffice documentationOffice) {
    if (documentationOffice == null) {
      return "dokumentationsstelle";
    }
    return toKebabCase(documentationOffice.abbreviation()) + "-dokumentationsstelle";
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

  default String toKebabCase(String input) {
    if (input == null) return null;
    String lower = input.toLowerCase();
    String replaced = lower.replace("ä", "ae").replace("ö", "oe").replace("ü", "ue");

    // Remove all non-letters
    String cleaned = replaced.replaceAll("[^\\p{L}\\s]+", "");

    // Replace whitespace with hyphen and trim
    cleaned = cleaned.trim().replaceAll("\\s+", "-");

    return cleaned;
  }

  private String getCountry(String region) {
    if (GERMAN_STATES.contains(region.toUpperCase())) {
      return "deu";
    }
    return region.toLowerCase();
  }

  private String getDateName(DocumentationUnit documentationUnit) {
    if (documentationUnit instanceof Decision decision) {
      if (decision.coreData().hasDeliveryDate()) {
        return "Datum der Zustellung an Verkündungs statt";
      } else {
        return "Entscheidungsdatum";
      }
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
}
