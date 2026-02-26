package de.bund.digitalservice.ris.caselaw.adapter.transformer.ldml.decision;

import static de.bund.digitalservice.ris.caselaw.adapter.MappingUtils.nullSafeGet;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import de.bund.digitalservice.ris.caselaw.adapter.DateUtils;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.JaxbHtml;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.header.DocTitle;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.header.Header;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.header.Paragraph;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.meta.Classification;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.meta.Keyword;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.meta.Meta;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.meta.analysis.AnhaengigesVerfahren;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.meta.analysis.DokumentarischeKurztexte;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.meta.analysis.Entscheidungsnamen;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.meta.analysis.ImplicitReference;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.meta.identification.FrbrLanguage;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.meta.proprietary.AbweichendeDaten;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.meta.proprietary.AbweichendeDokumentnummern;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.meta.proprietary.AbweichendeEclis;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.meta.proprietary.AktenzeichenListe;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.meta.proprietary.Berichtigungen;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.meta.proprietary.Berufsbilder;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.meta.proprietary.DatenDerMuendlichenVerhandlung;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.meta.proprietary.Definitionen;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.meta.proprietary.DocumentRef;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.meta.proprietary.Eingangsarten;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.meta.proprietary.Einkunftsarten;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.meta.proprietary.Evsf;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.meta.proprietary.FehlerhafteGerichte;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.meta.proprietary.FremdsprachigeFassungen;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.meta.proprietary.Gebuehren;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.meta.proprietary.Gegenstandswerte;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.meta.proprietary.Gesetzgebungsauftrag;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.meta.proprietary.HerkunftDerUebersetzungen;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.meta.proprietary.Herkunftslaender;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.meta.proprietary.Kuendigungsarten;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.meta.proprietary.Kuendigungsgruende;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.meta.proprietary.Notiz;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.meta.proprietary.Proprietary;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.meta.proprietary.Quellen;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.meta.proprietary.Rechtskraft;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.meta.proprietary.Rechtsmittel;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.meta.proprietary.Rechtsmittelzulassung;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.meta.proprietary.RisMeta;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.meta.proprietary.Sachgebiete;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.meta.proprietary.Streitjahre;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.meta.proprietary.Tarifvertraege;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.meta.proprietary.Vorgaenge;
import de.bund.digitalservice.ris.caselaw.domain.ContentRelatedIndexing;
import de.bund.digitalservice.ris.caselaw.domain.CoreData;
import de.bund.digitalservice.ris.caselaw.domain.Correction;
import de.bund.digitalservice.ris.caselaw.domain.Decision;
import de.bund.digitalservice.ris.caselaw.domain.IncomeType;
import de.bund.digitalservice.ris.caselaw.domain.NormReference;
import de.bund.digitalservice.ris.caselaw.domain.RelatedPendingProceeding;
import de.bund.digitalservice.ris.caselaw.domain.ShortTexts;
import de.bund.digitalservice.ris.caselaw.domain.StringUtils;
import de.bund.digitalservice.ris.caselaw.domain.appeal.Appeal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.xml.parsers.DocumentBuilderFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;

/**
 * Transformer for converting decisions to the full LDML format. Includes additional metadata like
 * classification and keywords for internal processing. Currently, the public Prototype Portal is
 * under restrictions and must not use full LDML for legal reasons.
 */
@Slf4j
public class DecisionFullLdmlTransformer extends DecisionCommonLdmlTransformer {

  public DecisionFullLdmlTransformer(DocumentBuilderFactory documentBuilderFactory) {
    super(documentBuilderFactory);
  }

  @Override
  protected Meta buildMeta(Decision decision) {
    Meta.MetaBuilder builder = Meta.builder();

    List<Keyword> keywords =
        decision.contentRelatedIndexing() == null
            ? Collections.emptyList()
            : decision.contentRelatedIndexing().keywords().stream().map(Keyword::new).toList();

    if (!keywords.isEmpty()) {
      builder.classification(Classification.builder().keyword(keywords).build());
    }

    return builder
        .identification(buildIdentification(decision))
        .references(buildReferences(decision))
        .analysis(buildAnalysis(decision))
        .proprietary(Proprietary.builder().meta(buildRisMeta(decision)).build())
        .build();
  }

  @Override
  @Nonnull
  protected List<ImplicitReference> buildImplicitReferences(Decision decision) {
    return Stream.concat(
            Stream.concat(
                Stream.concat(
                    super.buildImplicitReferences(decision).stream(),
                    buildFundstellen(decision).stream()),
                buildAnhaengigeVerfahren(decision).stream()),
            buildNichtanwendungsgesetze(decision).stream())
        .toList();
  }

  @SuppressWarnings({"java:S3776"})
  private RisMeta buildRisMeta(Decision decision) {
    var builder = buildCommonRisMeta(decision);

    var contentRelatedIndexing = decision.contentRelatedIndexing();
    if (contentRelatedIndexing != null) {
      // Sachgebiete
      if (!CollectionUtils.isEmpty(contentRelatedIndexing.fieldsOfLaw())) {
        var sachgebiete = buildSachgebiete(contentRelatedIndexing);
        builder.sachgebiete(sachgebiete);
      }

      // Definitionen
      if (!CollectionUtils.isEmpty(contentRelatedIndexing.definitions())) {
        var definitionen = buildDefinitionen(contentRelatedIndexing);
        builder.definitionen(definitionen);
      }

      // EVSF
      Optional.ofNullable(contentRelatedIndexing.evsf())
          .ifPresent(value -> builder.evsf(Evsf.builder().value(value).build()));

      // Rechtsmittelzulassung
      var rechtsmittelzulassung = buildRechtsmittelzulassung(contentRelatedIndexing);
      builder.rechtsmittelzulassung(rechtsmittelzulassung);

      // Rechtsmittel
      if (contentRelatedIndexing.appeal() != null) {
        var rechtsmittel = buildRechtsmittel(contentRelatedIndexing.appeal());
        builder.rechtsmittel(rechtsmittel);
      }

      // Tarifvertrag
      if (!CollectionUtils.isEmpty(contentRelatedIndexing.collectiveAgreements())) {
        var tarifvertraege = buildTarifvertraege(contentRelatedIndexing);
        builder.tarifvertraege(tarifvertraege);
      }

      // Berufsbild
      if (!CollectionUtils.isEmpty(contentRelatedIndexing.jobProfiles())) {
        var berufsbilder = buildBerufsbilder(contentRelatedIndexing);
        builder.berufsbilder(berufsbilder);
      }

      // Kündigungsart
      if (!CollectionUtils.isEmpty(contentRelatedIndexing.dismissalTypes())) {
        var kuendigungsarten = buildKuendigungsarten(contentRelatedIndexing);
        builder.kuendigungsarten(kuendigungsarten);
      }

      // Kündigungsgrund
      if (!CollectionUtils.isEmpty(contentRelatedIndexing.dismissalGrounds())) {
        var kuendigungsgruende = buildKuendigungsgruende(contentRelatedIndexing);
        builder.kuendigungsgruende(kuendigungsgruende);
      }

      // Fremdsprachige Fassungen
      if (!CollectionUtils.isEmpty(contentRelatedIndexing.foreignLanguageVersions())) {
        var fremdsprachigeFassungen = buildFremdsprachigeFassung(contentRelatedIndexing);
        builder.fremdsprachigeFassungen(fremdsprachigeFassungen);
      }

      // Herkunft der Übersetzungen
      if (!CollectionUtils.isEmpty(contentRelatedIndexing.originOfTranslations())) {
        var herkunftDerUebersetzungen = buildHerkunftDerUebersetzungen(contentRelatedIndexing);
        builder.herkunftDerUebersetzungen(herkunftDerUebersetzungen);
      }

      // Gegenstandswerte
      if (!CollectionUtils.isEmpty(contentRelatedIndexing.objectValues())) {
        var gegenstandswerte = buildGegenstandswerte(contentRelatedIndexing);
        builder.gegenstandswerte(gegenstandswerte);
      }

      // Gebühren
      if (!CollectionUtils.isEmpty(contentRelatedIndexing.abuseFees())) {
        var gebuehren = buildGebuehren(contentRelatedIndexing);
        builder.gebuehren(gebuehren);
      }

      // Herkunftsland
      if (!CollectionUtils.isEmpty(contentRelatedIndexing.countriesOfOrigin())) {
        builder.herkunftslaender(buildHerkunftslaender(contentRelatedIndexing));
      }

      // Einkunftsarten
      if (!CollectionUtils.isEmpty(contentRelatedIndexing.incomeTypes())) {
        var einkunftsarten = buildEinkunftsarten(contentRelatedIndexing);
        builder.einkunftsarten(einkunftsarten);
      }
    }

    var coreData = decision.coreData();
    if (coreData != null) {
      // Abweichende Aktenzeichen
      if (!CollectionUtils.isEmpty(coreData.deviatingFileNumbers())) {
        // Aktenzeichenliste is already set in CommonTransformer
        var aktenzeichenListe = buildAbweichendeAktenzeichen(builder, coreData);
        builder.aktenzeichenListe(aktenzeichenListe);
      }

      // Fehlerhafte Gerichte
      if (!CollectionUtils.isEmpty(coreData.deviatingCourts())) {
        var fehlerhafteGerichte = buildFehlerhafteGerichte(coreData);
        builder.fehlerhafteGerichte(fehlerhafteGerichte);
      }
      // Daten der mündlichen Verhandlung
      if (!CollectionUtils.isEmpty(coreData.oralHearingDates())) {
        var datumDerMuendlichenVerhandlung = buildDatumDerMuendlichenVerhandlung(coreData);
        builder.datenDerMuendlichenVerhandlung(datumDerMuendlichenVerhandlung);
      }

      // Abweichende Daten
      if (!CollectionUtils.isEmpty(coreData.deviatingDecisionDates())) {
        var abweichendeDaten = buildAbweichendeDaten(coreData);
        builder.abweichendeDaten(abweichendeDaten);
      }
      // Abweichende Dokumentnummern
      if (!CollectionUtils.isEmpty(coreData.deviatingDocumentNumbers())) {
        var abweichendeDokumentnummern = buildAbweichendeDokumentnummern(coreData);
        builder.abweichendeDokumentnummern(abweichendeDokumentnummern);
      }
      // Abweichende ECLIs
      if (!CollectionUtils.isEmpty(coreData.deviatingEclis())) {
        var abweichendeEclis = buildAbweichendeEclis(coreData);
        builder.abweichendeEclis(abweichendeEclis);
      }

      // Rechtskraft (map from legalEffect yes/no)
      if (coreData.legalEffect() != null) {
        String legalEffect = coreData.legalEffect();
        builder.rechtskraft(Rechtskraft.builder().value(legalEffect).build());
      }

      // Vorgänge (previous procedures + current label)
      if (coreData.procedure() != null) {
        var vorgaenge = buildVorgaenge(coreData);
        builder.vorgaenge(vorgaenge);
      }

      // Eingangsarten
      if (!CollectionUtils.isEmpty(coreData.inputTypes())) {
        var eingangsarten = buildEingangsarten(coreData);
        builder.eingangsarten(eingangsarten);
      }

      // Quelle
      if (!CollectionUtils.isEmpty(coreData.sources())) {
        var quellen = buildQuellen(coreData);
        builder.quellen(quellen);
      }

      // Streitjahr
      if (!CollectionUtils.isEmpty(coreData.yearsOfDispute())) {
        var streitjahre = buildStreitjahre(coreData);
        builder.streitjahre(streitjahre);
      }

      // Gesetzgebungsauftrag
      if (isNotBlank(coreData.legalEffect())) {
        builder.gesetzgebungsauftrag(
            Gesetzgebungsauftrag.builder().value(coreData.legalEffect()).build());
      }
    }

    // Notiz
    if (isNotBlank(decision.note())) {
      builder.notiz(Notiz.builder().content(decision.note()).build());
    }

    // Berichtigung
    if (decision.longTexts() != null
        && !CollectionUtils.isEmpty(decision.longTexts().corrections())) {
      builder.berichtigungen(buildBerichtigungen(decision.longTexts().corrections()));
    }

    return builder.build();
  }

  @Nullable
  protected DokumentarischeKurztexte buildKurztexte(Decision decision) {
    var builder = getCommonKurztexteBuilder(decision);

    ShortTexts shortTexts = decision.shortTexts();
    if (shortTexts != null) {
      // Entscheidungsnamen
      if (shortTexts.decisionNames() != null && !shortTexts.decisionNames().isEmpty()) {
        Entscheidungsnamen entscheidungsnamen =
            Entscheidungsnamen.builder()
                .entscheidungsnamen(
                    shortTexts.decisionNames().stream()
                        .map(
                            entscheidungsname ->
                                Entscheidungsnamen.Entscheidungsname.builder()
                                    .value(entscheidungsname)
                                    .build())
                        .toList())
                .build();
        builder.entscheidungsnamen(entscheidungsnamen);
      }

      // Orientierungssatz
      if (isNotBlank(shortTexts.headnote())) {
        var orientierungssatz =
            JaxbHtml.build(htmlTransformer.htmlStringToObjectList(shortTexts.headnote()));
        orientierungssatz.setDomainTerm("Orientierungssatz");
        builder.orientierungssatz(orientierungssatz);
      }

      // Sonstiger Orientierungssatz
      if (isNotBlank(shortTexts.otherHeadnote())) {
        var sonstigerOrientierungssatz =
            JaxbHtml.build(htmlTransformer.htmlStringToObjectList(shortTexts.otherHeadnote()));
        sonstigerOrientierungssatz.setDomainTerm("Sonstiger Orientierungssatz");
        builder.sonstigerOrientierungssatz(sonstigerOrientierungssatz);
      }
    }
    DokumentarischeKurztexte kurztexte = builder.build();
    return kurztexte.isEmpty() ? null : kurztexte;
  }

  @Override
  protected Header buildHeader(Decision decision) {
    List<Paragraph> paragraphs = new ArrayList<>();

    paragraphs = buildCommonHeader(decision, paragraphs);
    var shortTexts = decision.shortTexts();
    var decisionNames = nullSafeGet(shortTexts, ShortTexts::decisionNames);
    var headline = nullSafeGet(shortTexts, ShortTexts::headline);
    var refersToTitelzeile = false;

    // fallback
    if (isBlank(headline)) {
      headline = buildFallbackHeadline(decision);
    } else {
      refersToTitelzeile = true;
    }

    // Entscheidungsname
    if (!CollectionUtils.isEmpty(decisionNames)) {
      Paragraph decisionNameParagraph = Paragraph.builder().content(new ArrayList<>()).build();
      decisionNameParagraph.getContent().add("Entscheidungsnamen: ");
      shortTexts
          .decisionNames()
          .forEach(
              decisionName ->
                  decisionNameParagraph
                      .getContent()
                      .add(
                          DocTitle.builder()
                              .refersTo("#entscheidungsname")
                              .content(decisionName)
                              .build()));
      paragraphs.add(decisionNameParagraph);
    }

    // Titelzeile
    if (isNotBlank(headline)) {
      buildHeadline(paragraphs, headline, htmlTransformer, refersToTitelzeile);
    }

    return Header.builder().paragraphs(paragraphs).build();
  }

  @Override
  public boolean isFullLDML() {
    return true;
  }

  private Sachgebiete buildSachgebiete(ContentRelatedIndexing contentRelatedIndexing) {
    return Sachgebiete.builder()
        .sachgebiete(
            contentRelatedIndexing.fieldsOfLaw().stream()
                .map(
                    sachgebiet ->
                        Sachgebiete.Sachgebiet.builder()
                            .value(sachgebiet.text())
                            .notation(sachgebiet.notation())
                            .sachgebietId(sachgebiet.identifier())
                            .build())
                .toList())
        .build();
  }

  private Definitionen buildDefinitionen(ContentRelatedIndexing contentRelatedIndexing) {
    return Definitionen.builder()
        .definitionen(
            contentRelatedIndexing.definitions().stream()
                .map(
                    definition ->
                        Definitionen.Definition.builder()
                            .definierterBegriff(
                                Definitionen.Definition.DefinierterBegriff.builder()
                                    .value(definition.definedTerm())
                                    .build())
                            .stelleImText(
                                definition.definingBorderNumber() == null
                                    ? null
                                    : new Definitionen.Definition.StelleImText(
                                        String.valueOf(definition.definingBorderNumber())))
                            .build())
                .toList())
        .build();
  }

  private Rechtsmittelzulassung buildRechtsmittelzulassung(
      ContentRelatedIndexing contentRelatedIndexing) {

    AtomicReference<Rechtsmittelzulassung> result = new AtomicReference<>();

    Optional.ofNullable(contentRelatedIndexing.appealAdmission())
        .ifPresent(
            value -> {
              var builder =
                  Rechtsmittelzulassung.builder()
                      .rechtsmittelZugelassen(
                          new Rechtsmittelzulassung.RechtsmittelZugelassen(value.admitted()));

              Optional.ofNullable(value.by())
                  .ifPresent(
                      appealAdmitter ->
                          builder.rechtsmittelZugelassenDurch(
                              new Rechtsmittelzulassung.RechtsmittelZugelassenDurch(
                                  appealAdmitter.name())));

              result.set(builder.build());
            });

    return result.get();
  }

  private Rechtsmittel buildRechtsmittel(Appeal appeal) {
    var builder = Rechtsmittel.builder();
    if (!CollectionUtils.isEmpty(appeal.appellants())) {
      builder.rechtsmittelfuehrer(
          appeal.appellants().stream()
              .map(appellant -> new Rechtsmittel.Rechtsmittelfuehrer(appellant.value()))
              .toList());
    }
    if (!CollectionUtils.isEmpty(appeal.revisionPlaintiffStatuses())) {
      builder.revisionKlaeger(
          appeal.revisionPlaintiffStatuses().stream()
              .map(status -> new Rechtsmittel.RevisionKlaeger(status.value()))
              .toList());
    }
    if (!CollectionUtils.isEmpty(appeal.revisionDefendantStatuses())) {
      builder.revisionBeklagte(
          appeal.revisionDefendantStatuses().stream()
              .map(status -> new Rechtsmittel.RevisionBeklagter(status.value()))
              .toList());
    }
    if (!CollectionUtils.isEmpty(appeal.jointRevisionPlaintiffStatuses())) {
      builder.anschlussRevisionKlaeger(
          appeal.jointRevisionPlaintiffStatuses().stream()
              .map(status -> new Rechtsmittel.AnschlussRevisionKlaeger(status.value()))
              .toList());
    }
    if (!CollectionUtils.isEmpty(appeal.jointRevisionDefendantStatuses())) {
      builder.anschlussRevisionBeklagte(
          appeal.jointRevisionDefendantStatuses().stream()
              .map(status -> new Rechtsmittel.AnschlussRevisionBeklagter(status.value()))
              .toList());
    }
    if (!CollectionUtils.isEmpty(appeal.nzbPlaintiffStatuses())) {
      builder.nzbKlaeger(
          appeal.nzbPlaintiffStatuses().stream()
              .map(status -> new Rechtsmittel.NzbKlaeger(status.value()))
              .toList());
    }
    if (!CollectionUtils.isEmpty(appeal.nzbDefendantStatuses())) {
      builder.nzbBeklagte(
          appeal.nzbDefendantStatuses().stream()
              .map(status -> new Rechtsmittel.NzbBeklagter(status.value()))
              .toList());
    }
    if (appeal.appealWithdrawal() != null) {
      builder.zuruecknahmeDerRevision(
          new Rechtsmittel.ZuruecknahmeDerRevision(appeal.appealWithdrawal().humanReadable));
    }
    if (appeal.pkhPlaintiff() != null) {
      builder.pkhAntragKlaeger(
          new Rechtsmittel.PkhAntragKlaeger(appeal.pkhPlaintiff().humanReadable));
    }
    return builder.build();
  }

  private Tarifvertraege buildTarifvertraege(ContentRelatedIndexing contentRelatedIndexing) {
    return Tarifvertraege.builder()
        .tarifvertraege(
            contentRelatedIndexing.collectiveAgreements().stream()
                .map(
                    collectiveAgreement -> {
                      var tarifvertragBuilder = Tarifvertraege.Tarifvertrag.builder();

                      if (collectiveAgreement.name() != null) {
                        tarifvertragBuilder.bezeichnung(
                            Tarifvertraege.Tarifvertrag.TarifvertragBezeichnung.builder()
                                .value(collectiveAgreement.name())
                                .build());
                      }

                      if (collectiveAgreement.date() != null) {
                        tarifvertragBuilder.datum(
                            Tarifvertraege.Tarifvertrag.TarifvertragDatum.builder()
                                .value(collectiveAgreement.date())
                                .build());
                      }

                      if (collectiveAgreement.norm() != null) {
                        tarifvertragBuilder.tarifnorm(
                            Tarifvertraege.Tarifvertrag.Tarifnorm.builder()
                                .value(collectiveAgreement.norm())
                                .build());
                      }

                      if (collectiveAgreement.industry() != null) {
                        tarifvertragBuilder.branche(
                            Tarifvertraege.Tarifvertrag.TarifvertragBranche.builder()
                                .value(collectiveAgreement.industry().label())
                                .build());
                      }

                      return tarifvertragBuilder.build();
                    })
                .toList())
        .build();
  }

  private Berufsbilder buildBerufsbilder(ContentRelatedIndexing contentRelatedIndexing) {
    return Berufsbilder.builder()
        .berufsbilder(
            contentRelatedIndexing.jobProfiles().stream()
                .map(jobProfile -> Berufsbilder.Berufsbild.builder().value(jobProfile).build())
                .toList())
        .build();
  }

  private Kuendigungsarten buildKuendigungsarten(ContentRelatedIndexing contentRelatedIndexing) {
    return Kuendigungsarten.builder()
        .kuendigungsarten(
            contentRelatedIndexing.dismissalTypes().stream()
                .map(
                    dismissalType ->
                        Kuendigungsarten.Kuendigungsart.builder().value(dismissalType).build())
                .toList())
        .build();
  }

  private Kuendigungsgruende buildKuendigungsgruende(
      ContentRelatedIndexing contentRelatedIndexing) {
    return Kuendigungsgruende.builder()
        .kuendigungsgruende(
            contentRelatedIndexing.dismissalGrounds().stream()
                .map(
                    dismissalGround ->
                        Kuendigungsgruende.Kuendigungsgrund.builder()
                            .value(dismissalGround)
                            .build())
                .toList())
        .build();
  }

  private FremdsprachigeFassungen buildFremdsprachigeFassung(
      ContentRelatedIndexing contentRelatedIndexing) {
    return FremdsprachigeFassungen.builder()
        .fremdsprachigeFassungen(
            contentRelatedIndexing.foreignLanguageVersions().stream()
                .map(
                    version ->
                        FremdsprachigeFassungen.FremdsprachigeFassung.builder()
                            .documentRef(
                                DocumentRef.builder()
                                    .href(version.link())
                                    .showAs(version.languageCode().label())
                                    .build())
                            .frbrLanguage(
                                new FrbrLanguage(version.languageCode().isoCode3Letters()))
                            .build())
                .toList())
        .build();
  }

  private HerkunftDerUebersetzungen buildHerkunftDerUebersetzungen(
      ContentRelatedIndexing contentRelatedIndexing) {

    var herkunftList =
        contentRelatedIndexing.originOfTranslations().stream()
            .filter(translation -> translation.languageCode() != null)
            .map(
                translation -> {
                  var language = new FrbrLanguage(translation.languageCode().isoCode3Letters());
                  language.setDomainTerm("Originalsprache");

                  var translators =
                      Optional.ofNullable(translation.translators()).orElseGet(List::of).stream()
                          .map(
                              translator ->
                                  HerkunftDerUebersetzungen.Uebersetzerin.builder()
                                      .value(translator)
                                      .build())
                          .toList();

                  List<HerkunftDerUebersetzungen.InterneVerlinkung> borderNumbers =
                      Optional.ofNullable(translation.borderNumbers()).orElseGet(List::of).stream()
                          .map(
                              borderNumber ->
                                  new HerkunftDerUebersetzungen.InterneVerlinkung(
                                      String.valueOf(borderNumber)))
                          .toList();

                  var urls =
                      Optional.ofNullable(translation.urls()).orElseGet(List::of).stream()
                          .map(
                              url ->
                                  HerkunftDerUebersetzungen.ExterneVerlinkung.builder()
                                      .documentRef(
                                          DocumentRef.builder()
                                              .href(url)
                                              .showAs(translation.languageCode().label())
                                              .build())
                                      .build())
                          .toList();

                  var uebersetzungsartBuilder =
                      HerkunftDerUebersetzungen.Uebersetzungsart.builder();
                  if (translation.translationType() != null) {
                    uebersetzungsartBuilder.value(translation.translationType().toString());
                  }

                  return HerkunftDerUebersetzungen.HerkunftDerUebersetzung.builder()
                      .frbrLanguage(language)
                      .uebersetzerinnen(
                          HerkunftDerUebersetzungen.Uebersetzerinnen.builder()
                              .uebersetzerinnen(translators)
                              .build())
                      .interneVerlinkungen(
                          HerkunftDerUebersetzungen.InterneVerlinkungen.builder()
                              .interneVerlinkungen(borderNumbers)
                              .build())
                      .externeVerlinkungen(
                          HerkunftDerUebersetzungen.ExterneVerlinkungen.builder()
                              .externeVerlinkungen(urls)
                              .build())
                      .uebersetzungsart(uebersetzungsartBuilder.build())
                      .build();
                })
            .toList();

    return HerkunftDerUebersetzungen.builder().herkunftDerUebersetzungen(herkunftList).build();
  }

  private Gegenstandswerte buildGegenstandswerte(ContentRelatedIndexing contentRelatedIndexing) {

    var gegenstandswerte =
        contentRelatedIndexing.objectValues().stream()
            .map(
                objectValue ->
                    Gegenstandswerte.Gegenstandswert.builder()
                        .betrag(
                            Gegenstandswerte.GegenstandswertBetrag.builder()
                                .value(String.valueOf(objectValue.amount()))
                                .build())
                        .waehrung(
                            Gegenstandswerte.GegenstandswertWaehrung.builder()
                                .value(objectValue.currencyCode().isoCode())
                                .build())
                        .verfahren(
                            Gegenstandswerte.Verfahren.builder()
                                .value(objectValue.proceedingType().toString())
                                .build())
                        .build())
            .toList();

    return Gegenstandswerte.builder().gegenstandswerte(gegenstandswerte).build();
  }

  private Gebuehren buildGebuehren(ContentRelatedIndexing contentRelatedIndexing) {

    var gebuehren =
        contentRelatedIndexing.abuseFees().stream()
            .map(
                abuseFee ->
                    Gebuehren.Gebuehr.builder()
                        .betrag(
                            Gebuehren.Betrag.builder()
                                .value(String.valueOf(abuseFee.amount()))
                                .build())
                        .waehrung(
                            Gebuehren.Waehrung.builder()
                                .value(abuseFee.currencyCode().isoCode())
                                .build())
                        .adressat(
                            Gebuehren.Adressat.builder()
                                .value(abuseFee.addressee().toString())
                                .build())
                        .build())
            .toList();

    return Gebuehren.builder().gebuehren(gebuehren).build();
  }

  private Herkunftslaender buildHerkunftslaender(ContentRelatedIndexing contentRelatedIndexing) {
    return Herkunftslaender.builder()
        .herkunftslaender(
            contentRelatedIndexing.countriesOfOrigin().stream()
                .map(
                    countryOfOrigin -> {
                      var builder = Herkunftslaender.Herkunftsland.builder();

                      if (!StringUtils.isNullOrBlank(countryOfOrigin.legacyValue())) {
                        builder.altwert(
                            Herkunftslaender.Altwert.builder()
                                .value(countryOfOrigin.legacyValue())
                                .build());
                      }

                      if (countryOfOrigin.country() != null) {
                        builder.landbezeichnung(
                            Herkunftslaender.Landbezeichnung.builder()
                                .value(countryOfOrigin.country().text())
                                .notation(countryOfOrigin.country().notation())
                                .sachgebietId(countryOfOrigin.country().identifier())
                                .build());
                      }
                      if (countryOfOrigin.fieldOfLaw() != null) {
                        builder.rechtlicherRahmen(
                            Herkunftslaender.RechtlicherRahmen.builder()
                                .value(countryOfOrigin.fieldOfLaw().text())
                                .notation(countryOfOrigin.fieldOfLaw().notation())
                                .sachgebietId(countryOfOrigin.fieldOfLaw().identifier())
                                .build());
                      }

                      return builder.build();
                    })
                .toList())
        .build();
  }

  private Einkunftsarten buildEinkunftsarten(ContentRelatedIndexing contentRelatedIndexing) {

    var einkunftsarten =
        contentRelatedIndexing.incomeTypes().stream()
            .map(DecisionFullLdmlTransformer::buildEinkunftsart)
            .toList();

    return Einkunftsarten.builder().values(einkunftsarten).build();
  }

  private static Einkunftsarten.Einkunftsart buildEinkunftsart(IncomeType incomeType) {
    var builder =
        Einkunftsarten.Einkunftsart.builder()
            .einkunftsartTyp(
                Einkunftsarten.EinkunftsartTyp.builder()
                    .value(incomeType.typeOfIncome().humanReadable)
                    .build());

    if (incomeType.terminology() != null) {
      builder.begrifflichkeit(
          Einkunftsarten.Begrifflichkeit.builder().value(incomeType.terminology()).build());
    }

    return builder.build();
  }

  private AktenzeichenListe buildAbweichendeAktenzeichen(
      RisMeta.RisMetaBuilder builder, CoreData coreData) {
    AktenzeichenListe aktenzeichenListe = builder.build().getAktenzeichenListe(); // NOSONAR
    List<AktenzeichenListe.Aktenzeichen> aktenzeichen = aktenzeichenListe.getAktenzeichen();
    if (aktenzeichen != null) {
      coreData
          .deviatingFileNumbers()
          .forEach(
              fileNumber ->
                  aktenzeichen.add(
                      AktenzeichenListe.Aktenzeichen.builder()
                          .domainTerm("Abweichendes Aktenzeichen")
                          .value(fileNumber)
                          .build()));
    }
    return aktenzeichenListe.toBuilder().aktenzeichen(aktenzeichen).build();
  }

  private FehlerhafteGerichte buildFehlerhafteGerichte(CoreData coreData) {
    return FehlerhafteGerichte.builder()
        .fehlerhafteGerichte(
            coreData.deviatingCourts().stream()
                .map(
                    court -> FehlerhafteGerichte.FehlerhaftesGericht.builder().value(court).build())
                .toList())
        .build();
  }

  private DatenDerMuendlichenVerhandlung buildDatumDerMuendlichenVerhandlung(CoreData coreData) {
    return DatenDerMuendlichenVerhandlung.builder()
        .daten(
            coreData.oralHearingDates().stream()
                .map(
                    date ->
                        DatenDerMuendlichenVerhandlung.DatumDerMuendlichenVerhandlung.builder()
                            .value(DateUtils.toDateString(date))
                            .build())
                .toList())
        .build();
  }

  private AbweichendeDaten buildAbweichendeDaten(CoreData coreData) {
    return AbweichendeDaten.builder()
        .daten(
            coreData.deviatingDecisionDates().stream()
                .map(
                    date ->
                        AbweichendeDaten.AbweichendesDatum.builder()
                            .value(DateUtils.toDateString(date))
                            .build())
                .toList())
        .build();
  }

  private AbweichendeDokumentnummern buildAbweichendeDokumentnummern(CoreData coreData) {
    return AbweichendeDokumentnummern.builder()
        .dokumentnummern(
            coreData.deviatingDocumentNumbers().stream()
                .map(
                    docNumber ->
                        AbweichendeDokumentnummern.AbweichendeDokumentnummer.builder()
                            .value(docNumber)
                            .build())
                .toList())
        .build();
  }

  private AbweichendeEclis buildAbweichendeEclis(CoreData coreData) {
    return AbweichendeEclis.builder()
        .eclis(
            coreData.deviatingEclis().stream()
                .map(ecli -> AbweichendeEclis.AbweichenderEcli.builder().value(ecli).build())
                .toList())
        .build();
  }

  private Vorgaenge buildVorgaenge(CoreData coreData) {
    List<String> all = new ArrayList<>();
    all.add(coreData.procedure().label());
    if (coreData.previousProcedures() != null) {
      all.addAll(coreData.previousProcedures());
    }
    return Vorgaenge.builder()
        .vorgaenge(all.stream().map(v -> Vorgaenge.Vorgang.builder().value(v).build()).toList())
        .build();
  }

  private Eingangsarten buildEingangsarten(CoreData coreData) {
    return Eingangsarten.builder()
        .eingangsarten(
            coreData.inputTypes().stream()
                .map(
                    eingangsart -> Eingangsarten.Eingangsart.builder().content(eingangsart).build())
                .toList())
        .build();
  }

  private Quellen buildQuellen(CoreData coreData) {
    return Quellen.builder()
        .quellen(
            coreData.sources().stream()
                .map(source -> Quellen.Quelle.builder().value(source.toString()).build())
                .toList())
        .build();
  }

  private Streitjahre buildStreitjahre(CoreData coreData) {
    return Streitjahre.builder()
        .streitjahre(
            coreData.yearsOfDispute().stream()
                .map(
                    yearOfDispute ->
                        Streitjahre.Streitjahr.builder().value(yearOfDispute.toString()).build())
                .toList())
        .build();
  }

  private Berichtigungen buildBerichtigungen(List<Correction> corrections) {
    return Berichtigungen.builder()
        .values(
            corrections.stream()
                .map(
                    correction -> {
                      var builder = Berichtigungen.Berichtigung.builder();

                      if (correction.type() != null) {
                        builder.artDerEintragung(
                            Berichtigungen.Berichtigung.ArtDerEintragung.builder()
                                .value(correction.type().getLabel())
                                .build());
                      }

                      if (correction.description() != null) {
                        builder.artDerAenderung(
                            Berichtigungen.Berichtigung.ArtDerAenderung.builder()
                                .value(correction.description())
                                .build());
                      }

                      if (correction.date() != null) {
                        builder.datumDerAenderung(
                            Berichtigungen.Berichtigung.DatumDerAenderung.builder()
                                .value(DateUtils.toDateString(correction.date()))
                                .build());
                      }

                      if (correction.borderNumbers() != null) {
                        var randnummernBuilder = Berichtigungen.Berichtigung.Randnummern.builder();

                        correction
                            .borderNumbers()
                            .forEach(
                                borderNumber ->
                                    randnummernBuilder.value(
                                        new Berichtigungen.Berichtigung.Randnummern.Randnummer(
                                            String.valueOf(borderNumber))));

                        builder.randnummern(randnummernBuilder.build());
                      }

                      if (correction.content() != null) {
                        var content = correction.content();

                        if (isNotBlank(content)) {
                          builder.inhaltDerAenderung(
                              Berichtigungen.Berichtigung.InhaltDerAenderung.builder()
                                  .content(htmlTransformer.htmlStringToObjectList(content))
                                  .build());
                        }
                      }

                      return builder.build();
                    })
                .toList())
        .build();
  }

  @Nonnull
  private List<ImplicitReference> buildAnhaengigeVerfahren(Decision documentationUnit) {
    List<ImplicitReference> anhaengigeVerfahren = new ArrayList<>();
    if (documentationUnit.contentRelatedIndexing() == null) {
      return anhaengigeVerfahren;
    }

    var relatedPendingProceedings =
        documentationUnit.contentRelatedIndexing().relatedPendingProceedings();
    if (relatedPendingProceedings == null) {
      return anhaengigeVerfahren;
    }

    for (RelatedPendingProceeding relatedPendingProceeding : relatedPendingProceedings) {
      if (relatedPendingProceeding == null) continue;

      var builder = AnhaengigesVerfahren.builder();
      buildCaselawReference(relatedPendingProceeding, builder);
      anhaengigeVerfahren.add(
          ImplicitReference.builder()
              .domainTerm("Anhängiges Verfahren")
              .anhaengigesVerfahren(builder.build())
              .build());
    }

    return anhaengigeVerfahren;
  }

  private List<ImplicitReference> buildNichtanwendungsgesetze(Decision documentationUnit) {
    List<ImplicitReference> nichtanwendungsgesetze = new ArrayList<>();
    if (documentationUnit.contentRelatedIndexing() == null) {
      return nichtanwendungsgesetze;
    }

    var nonApplicationNorms = documentationUnit.contentRelatedIndexing().nonApplicationNorms();
    if (nonApplicationNorms == null) {
      return nichtanwendungsgesetze;
    }

    for (NormReference nonApplicationNorm : nonApplicationNorms) {
      if (nonApplicationNorm == null) continue;

      var norm = buildNorm(nonApplicationNorm).domainTerm("Nichtanwendungsgesetz").build();

      nichtanwendungsgesetze.add(
          ImplicitReference.builder()
              .domainTerm("Nichtanwendungsgesetz")
              .nichtanwendungsgesetz(norm)
              .build());
    }

    return nichtanwendungsgesetze;
  }
}
