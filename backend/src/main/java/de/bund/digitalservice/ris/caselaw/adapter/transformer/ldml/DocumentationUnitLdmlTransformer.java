package de.bund.digitalservice.ris.caselaw.adapter.transformer.ldml;

import static de.bund.digitalservice.ris.caselaw.adapter.MappingUtils.nullSafeGet;
import static de.bund.digitalservice.ris.caselaw.adapter.MappingUtils.validate;
import static de.bund.digitalservice.ris.caselaw.adapter.MappingUtils.validateNotNull;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import de.bund.digitalservice.ris.caselaw.adapter.DateUtils;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.CaseLawLdml;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.TlcElement;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.header.CourtType;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.header.DocDate;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.header.DocNumber;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.header.DocType;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.header.EmbeddedStructure;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.header.Paragraph;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.header.ShortTitle;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.meta.analysis.CaselawReference;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.meta.analysis.Fundstelle;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.meta.analysis.ImplicitReference;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.meta.analysis.Norm;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.meta.analysis.Periodikum;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.meta.analysis.Rechtszug;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.meta.identification.FrbrAlias;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.meta.identification.FrbrAuthor;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.meta.identification.FrbrCountry;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.meta.identification.FrbrDate;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.meta.identification.FrbrElement;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.meta.identification.FrbrLanguage;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.meta.identification.Identification;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.meta.proprietary.AktenzeichenListe;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.meta.proprietary.Dokumenttyp;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.meta.proprietary.Gericht;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.meta.references.References;
import de.bund.digitalservice.ris.caselaw.domain.ContentRelatedIndexing;
import de.bund.digitalservice.ris.caselaw.domain.CoreData;
import de.bund.digitalservice.ris.caselaw.domain.Decision;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationOffice;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnit;
import de.bund.digitalservice.ris.caselaw.domain.NormReference;
import de.bund.digitalservice.ris.caselaw.domain.PendingProceeding;
import de.bund.digitalservice.ris.caselaw.domain.PreviousDecision;
import de.bund.digitalservice.ris.caselaw.domain.Reference;
import de.bund.digitalservice.ris.caselaw.domain.RelatedDocumentationUnit;
import de.bund.digitalservice.ris.caselaw.domain.SingleNorm;
import de.bund.digitalservice.ris.caselaw.domain.court.Court;
import jakarta.xml.bind.ValidationException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import org.springframework.util.CollectionUtils;

public interface DocumentationUnitLdmlTransformer<T extends DocumentationUnit> {
  Set<String> GERMAN_STATES =
      Set.of(
          "BW", "BY", "BE", "BB", "HB", "HH", "HE", "MV", "NI", "NW", "RP", "SL", "SN", "ST", "SH",
          "TH");

  CaseLawLdml transformToLdml(T documentationUnit);

  default Identification buildIdentification(DocumentationUnit documentationUnit) {
    String uniqueId = documentationUnit.documentNumber();
    FrbrDate frbrDecisionDate =
        new FrbrDate(
            DateUtils.toDateString(
                nullSafeGet(documentationUnit.coreData(), CoreData::decisionDate)),
            getDateName(documentationUnit));
    FrbrDate frbrDecisionDateWithEid =
        new FrbrDate(
            DateUtils.toDateString(
                nullSafeGet(documentationUnit.coreData(), CoreData::decisionDate)),
            getDateName(documentationUnit));
    frbrDecisionDateWithEid.setEid(toKebabCase(getDateName(documentationUnit)));
    FrbrDate frbrPublicationDate =
        new FrbrDate(DateUtils.toDateString(LocalDate.now()), "XML Transformation");
    FrbrAuthor workExpressionAuthor = new FrbrAuthor("#gericht");
    FrbrAuthor manifestationAuthor =
        new FrbrAuthor(
            "#"
                + getDocOfficeEid(
                    nullSafeGet(documentationUnit.coreData(), CoreData::documentationOffice)));

    List<FrbrAlias> aliases = generateAliases(documentationUnit);

    var elementBuilder =
        FrbrElement.builder()
            .frbrAlias(aliases)
            .frbrDate(frbrDecisionDateWithEid)
            .frbrAuthor(workExpressionAuthor);

    if (documentationUnit.coreData() != null
        && documentationUnit.coreData().court() != null
        && documentationUnit.coreData().court().regions() != null
        && !documentationUnit.coreData().court().regions().isEmpty()) {
      elementBuilder.frbrCountry(
          // The schema says ISO 3166-1 Alpha-2 code, but we only have Alpha-3 available (for now)
          // (We have only two regions for a handful of German courts)
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
      DocumentationUnit documentationUnit, List<Paragraph> paragraphs) {
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
      var court = documentationUnit.coreData().court();
      TlcElement tlcCourt = new TlcElement("gericht", "", court.label());
      tlcOrganizations.add(tlcCourt);

      if ((court.location() != null)) {
        TlcElement tlcCourtLocation = new TlcElement("gerichtsort", "", court.location());
        tlcLocations.add(tlcCourtLocation);
      }
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

  @Nonnull
  default List<ImplicitReference> buildCommonImplicitReferences(
      DocumentationUnit documentationUnit) {
    return Stream.concat(
            buildNormen(documentationUnit).stream(),
            buildVorgehendeEntscheidungen(documentationUnit).stream())
        .toList();
  }

  @Nonnull
  default List<ImplicitReference> buildFundstellen(DocumentationUnit documentationUnit) {
    List<ImplicitReference> fundstellen = new ArrayList<>();

    List<Reference> caselawRefs =
        Optional.ofNullable(documentationUnit.caselawReferences()).orElse(List.of());
    List<Reference> literatureRefs =
        Optional.ofNullable(documentationUnit.literatureReferences()).orElse(List.of());

    for (Reference ref : caselawRefs) {
      if (ref == null) continue;
      fundstellen.add(buildFundstelleRechtsprechung(ref));
    }

    for (Reference ref : literatureRefs) {
      if (ref == null) continue;
      fundstellen.add(buildFundstelleLiteraturUnselbststaendig(ref));
    }

    return fundstellen;
  }

  @Nonnull
  private ImplicitReference buildFundstelleLiteraturUnselbststaendig(Reference ref) {
    var builder = Fundstelle.builder().domainTerm("Fundstelle unselbstständige Literatur");

    Periodikum periodikum = buildPeriodikum(ref);
    builder.periodikum(periodikum);

    if (isNotBlank(ref.citation())) {
      var zitatstelle = Fundstelle.Zitatstelle.builder().value(ref.citation()).build();
      builder.zitatstelle(zitatstelle);
    }

    var fundstelle = builder.build();
    return ImplicitReference.builder()
        .domainTerm(Fundstelle.DOMAIN_TERM)
        .fundstelleLiteraturUnselbststaendig(fundstelle)
        .build();
  }

  private ImplicitReference buildFundstelleRechtsprechung(Reference ref) {
    Fundstelle.FundstelleBuilder builder = Fundstelle.builder();

    Periodikum periodikum = buildPeriodikum(ref);
    builder.periodikum(periodikum);

    if (isNotBlank(ref.citation())) {
      var zitatstelle = Fundstelle.Zitatstelle.builder().value(ref.citation()).build();
      builder.zitatstelle(zitatstelle);
    }

    Fundstelle fundstelle = builder.build();
    return ImplicitReference.builder()
        .domainTerm(Fundstelle.DOMAIN_TERM)
        .fundstelle(fundstelle)
        .build();
  }

  @Nonnull
  default Periodikum buildPeriodikum(@Nonnull Reference ref) {
    Periodikum.PeriodikumBuilder builder = Periodikum.builder();

    String abbreviation = null;
    String title = null;
    String subtitle = null;
    Boolean isPrimary = ref.primaryReference();

    if (ref.legalPeriodical() != null) {
      abbreviation = ref.legalPeriodical().abbreviation();
      title = ref.legalPeriodical().title();
      subtitle = ref.legalPeriodical().subtitle();
      if (isPrimary != null) {
        builder.typ(
            Periodikum.Typ.builder().value(isPrimary ? "amtlich" : "nicht-amtlich").build());
      }
    } else if (isNotBlank(ref.legalPeriodicalRawValue())) {
      abbreviation = ref.legalPeriodicalRawValue();
      if (isPrimary != null) {
        builder.typ(
            Periodikum.Typ.builder().value(isPrimary ? "amtlich" : "nicht-amtlich").build());
      }
    }

    if (isNotBlank(abbreviation)) {
      builder.periodikumAbkuerzung(
          Periodikum.PeriodikumAbkuerzung.builder().value(abbreviation).build());
    }
    if (isNotBlank(title)) {
      builder.titel(Periodikum.Titel.builder().value(title).build());
    }
    if (isNotBlank(subtitle)) {
      builder.untertitel(Periodikum.Untertitel.builder().value(subtitle).build());
    }

    return builder.build();
  }

  @Nonnull
  private List<ImplicitReference> buildVorgehendeEntscheidungen(
      DocumentationUnit documentationUnit) {
    List<ImplicitReference> vorhergehendeEntscheidungen = new ArrayList<>();
    var previousDecisions = documentationUnit.previousDecisions();
    if (previousDecisions != null) {
      for (PreviousDecision previousDecision : previousDecisions) {
        if (previousDecision == null) continue;

        var vorgehendBuilder = Rechtszug.Vorgehend.builder();
        buildCaselawReference(previousDecision, vorgehendBuilder);
        vorhergehendeEntscheidungen.add(
            ImplicitReference.builder()
                .domainTerm("Rechtszug")
                .vorgehend(vorgehendBuilder.build())
                .build());
      }
    }
    return vorhergehendeEntscheidungen;
  }

  @Nonnull
  private List<ImplicitReference> buildNormen(DocumentationUnit documentationUnit) {
    List<ImplicitReference> normen = new ArrayList<>();

    var norms =
        Optional.ofNullable(documentationUnit.contentRelatedIndexing())
            .map(ContentRelatedIndexing::norms)
            .orElse(null);
    if (!CollectionUtils.isEmpty(norms)) {
      for (NormReference normRef : norms) {
        if (normRef == null) continue;

        Norm norm = buildNorm(normRef).build();

        normen.add(ImplicitReference.builder().domainTerm("Norm").norm(norm).build());
      }
    }
    return normen;
  }

  @SuppressWarnings("java:S1854")
  default void buildCaselawReference(
      RelatedDocumentationUnit relatedDocUnit,
      CaselawReference.CaselawReferenceBuilder<
              ?, ? extends CaselawReference.CaselawReferenceBuilder<?, ?>>
          builder) {
    if (relatedDocUnit.getDocumentType() != null
        && isNotBlank(relatedDocUnit.getDocumentType().label())) {
      builder.dokumenttyp(
          Dokumenttyp.builder().eId(null).value(relatedDocUnit.getDocumentType().label()).build());
    }
    if (relatedDocUnit.getDecisionDate() != null) {
      builder.datum(
          Rechtszug.Datum.builder()
              .value(DateUtils.toDateString(relatedDocUnit.getDecisionDate()))
              .build());
    }
    if (isNotBlank(relatedDocUnit.getDocumentNumber())) {
      builder.dokumentnummer(
          Rechtszug.Dokumentnummer.builder().value(relatedDocUnit.getDocumentNumber()).build());
    }
    if (isNotBlank(relatedDocUnit.getFileNumber())) {
      builder.aktenzeichen(
          AktenzeichenListe.Aktenzeichen.builder().value(relatedDocUnit.getFileNumber()).build());
    }

    Court court = relatedDocUnit.getCourt();
    if (court != null) {
      builder.gericht(buildGericht(court));
    }
  }

  default Gericht buildGericht(Court court) {
    Gericht.GerichtBuilder gerichtBuilder = Gericht.builder();
    if (isNotBlank(court.type())) {
      gerichtBuilder.typ(Gericht.GerichtTyp.builder().value(court.type()).build());
    }
    if (isNotBlank(court.location())) {
      gerichtBuilder.ort(Gericht.GerichtOrt.builder().value(court.location()).build());
    }
    if (isFullLDML() && isNotBlank(court.jurisdictionType())) {
      gerichtBuilder.gerichtsbarkeit(
          Gericht.Gerichtsbarkeit.builder().value(court.jurisdictionType()).build());
    }
    return gerichtBuilder.build();
  }

  default Norm.NormBuilder buildNorm(NormReference normRef) {
    Norm.NormBuilder normBuilder = Norm.builder();
    String abbr = null;
    if (normRef.normAbbreviation() != null
        && isNotBlank(normRef.normAbbreviation().abbreviation())) {
      abbr = normRef.normAbbreviation().abbreviation();
    } else if (isNotBlank(normRef.normAbbreviationRawValue())) {
      abbr = normRef.normAbbreviationRawValue();
    }
    if (isNotBlank(abbr)) {
      normBuilder.abkuerzung(Norm.Abkuerzung.builder().value(abbr).build());
    }

    if (normRef.normAbbreviation() != null
        && isNotBlank(normRef.normAbbreviation().officialLongTitle())) {
      normBuilder.normTitel(
          Norm.NormTitel.builder().value(normRef.normAbbreviation().officialLongTitle()).build());
    }

    List<Norm.Einzelnorm> einzelNormen = new ArrayList<>();
    if (!CollectionUtils.isEmpty(normRef.singleNorms())) {
      for (SingleNorm singleNorm : normRef.singleNorms()) {
        if (singleNorm == null) continue;
        Norm.Einzelnorm einzelnorm = buildEinzelnorm(singleNorm);
        einzelNormen.add(einzelnorm);
      }
    }
    if (!einzelNormen.isEmpty()) {
      normBuilder.einzelnormen(einzelNormen);
    }
    return normBuilder;
  }

  default Norm.Einzelnorm buildEinzelnorm(SingleNorm singleNorm) {
    Norm.Einzelnorm.EinzelnormBuilder einzelnormBuilder = Norm.Einzelnorm.builder();

    if (isNotBlank(singleNorm.singleNorm())) {
      einzelnormBuilder.bezeichnung(
          Norm.Bezeichnung.builder().value(singleNorm.singleNorm()).build());
    }
    if (isFullLDML() && singleNorm.legalForce() != null) {
      Norm.Gesetzeskraft.GesetzeskraftBuilder gkBuilder = Norm.Gesetzeskraft.builder();
      if (singleNorm.legalForce().type() != null
          && isNotBlank(singleNorm.legalForce().type().label())) {
        gkBuilder.typ(
            Norm.GesetzeskraftTyp.builder().value(singleNorm.legalForce().type().label()).build());
      }
      if (singleNorm.legalForce().region() != null
          && isNotBlank(singleNorm.legalForce().region().longText())) {
        gkBuilder.geltungsbereich(
            Norm.Geltungsbereich.builder()
                .value(singleNorm.legalForce().region().longText())
                .build());
      }
      einzelnormBuilder.gesetzeskraft(gkBuilder.build());
    }
    if (singleNorm.dateOfVersion() != null) {
      einzelnormBuilder.datum(
          Norm.Fassungsdatum.builder()
              .value(DateUtils.toDateString(singleNorm.dateOfVersion()))
              .build());
    }
    if (isNotBlank(singleNorm.dateOfRelevance())) {
      einzelnormBuilder.jahr(Norm.Jahr.builder().value(singleNorm.dateOfRelevance()).build());
    }
    return einzelnormBuilder.build();
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
        && documentationUnit.coreData().celexNumber() != null
        && this.isFullLDML()) {
      aliases.add(new FrbrAlias("CELEX-Nummer", documentationUnit.coreData().celexNumber()));
    }

    if (documentationUnit.coreData() != null
        && documentationUnit.coreData().fileNumbers() != null) {
      List<String> fileNumbers = documentationUnit.coreData().fileNumbers();
      boolean first = true;
      for (String fileNumber : fileNumbers) {
        if (fileNumber != null) {
          var alias = new FrbrAlias("Aktenzeichen", fileNumber);
          if (first) {
            alias.setEid("aktenzeichen");
            first = false;
          }
          aliases.add(alias);
        }
      }
    }
    return aliases;
  }

  boolean isFullLDML();
}
