package de.bund.digitalservice.ris.caselaw.adapter.transformer.ldml.decision;

import static de.bund.digitalservice.ris.caselaw.adapter.MappingUtils.nullSafeGet;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import de.bund.digitalservice.ris.caselaw.adapter.DateUtils;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.JaxbHtml;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.header.DocTitle;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.header.Header;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.header.Paragraph;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.meta.Classification;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.meta.Keyword;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.meta.Meta;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.meta.analysis.DokumentarischeKurztexte;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.meta.analysis.Entscheidungsnamen;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.meta.analysis.ImplicitReference;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.meta.identification.FrbrLanguage;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.meta.proprietary.AbweichendeDaten;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.meta.proprietary.AbweichendeDokumentnummern;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.meta.proprietary.AbweichendeEclis;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.meta.proprietary.AktenzeichenListe;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.meta.proprietary.DatenDerMuendlichenVerhandlung;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.meta.proprietary.Definitionen;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.meta.proprietary.DocumentRef;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.meta.proprietary.Eingangsarten;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.meta.proprietary.Evsf;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.meta.proprietary.FehlerhafteGerichte;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.meta.proprietary.FremdsprachigeFassungen;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.meta.proprietary.Proprietary;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.meta.proprietary.Rechtskraft;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.meta.proprietary.Rechtsmittelzulassung;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.meta.proprietary.RisMeta;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.meta.proprietary.Sachgebiete;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.meta.proprietary.Vorgaenge;
import de.bund.digitalservice.ris.caselaw.domain.Decision;
import de.bund.digitalservice.ris.caselaw.domain.ShortTexts;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
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
    List<ImplicitReference> implicitReferences = super.buildImplicitReferences(decision);
    List<ImplicitReference> fundstellen = buildFundstellen(decision);
    if (fundstellen.isEmpty()) return implicitReferences;
    implicitReferences.addAll(fundstellen);
    return implicitReferences;
  }

  @SuppressWarnings({"java:S6541", "java:S3776"})
  private RisMeta buildRisMeta(Decision decision) {
    var builder = buildCommonRisMeta(decision);

    var contentRelatedIndexing = decision.contentRelatedIndexing();
    if (contentRelatedIndexing != null) {
      // Sachgebiete
      if (!CollectionUtils.isEmpty(contentRelatedIndexing.fieldsOfLaw())) {
        Sachgebiete sachgebiete =
            Sachgebiete.builder()
                .sachgebiete(
                    contentRelatedIndexing.fieldsOfLaw().stream()
                        .map(
                            sachgebiet ->
                                Sachgebiete.Sachgebiet.builder()
                                    .value(sachgebiet.text())
                                    .notation(sachgebiet.notation())
                                    .build())
                        .toList())
                .build();
        builder.sachgebiete(sachgebiete);
      }

      // Definitionen
      if (!CollectionUtils.isEmpty(contentRelatedIndexing.definitions())) {
        var defs =
            contentRelatedIndexing.definitions().stream()
                .map(
                    definition ->
                        Definitionen.Definition.builder()
                            .definierterBegriff(
                                Definitionen.Definition.DefinierterBegriff.builder()
                                    .value(definition.definedTerm())
                                    .build())
                            .definierendeRandnummer(
                                definition.definingBorderNumber() == null
                                    ? null
                                    : Definitionen.Definition.DefinierendeRandnummer.builder()
                                        .refersTo(
                                            "#randnummer-" + definition.definingBorderNumber())
                                        .value(String.valueOf(definition.definingBorderNumber()))
                                        .build())
                            .build())
                .toList();
        builder.definitionen(Definitionen.builder().definitionen(defs).build());
      }

      // EVSF
      Optional.ofNullable(contentRelatedIndexing.evsf())
          .ifPresent(value -> builder.evsf(Evsf.builder().value(value).build()));

      // Rechtsmittelzulassung
      Optional.ofNullable(contentRelatedIndexing.appealAdmission())
          .ifPresent(
              value -> {
                var rechtsmittelzulassungBuilder =
                    Rechtsmittelzulassung.builder()
                        .rechtsmittelZugelassen(
                            new Rechtsmittelzulassung.RechtsmittelZugelassen(value.admitted()));

                Optional.ofNullable(value.by())
                    .ifPresent(
                        appealAdmitter ->
                            rechtsmittelzulassungBuilder.rechtsmittelZugelassenDurch(
                                new Rechtsmittelzulassung.RechtsmittelZugelassenDurch(
                                    appealAdmitter.name())));

                builder.rechtsmittelzulassung(rechtsmittelzulassungBuilder.build());
              });

      // Fremdsprachige Fassungen
      if (!CollectionUtils.isEmpty(contentRelatedIndexing.foreignLanguageVersions())) {
        builder.fremdsprachigeFassungen(
            FremdsprachigeFassungen.builder()
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
                .build());
      }
    }

    var coreData = decision.coreData();
    if (coreData != null) {

      if (!CollectionUtils.isEmpty(coreData.deviatingFileNumbers())) {
        // Aktenzeichenliste is already set in CommonTransformer
        AktenzeichenListe aktenzeichenListe = builder.build().getAktenzeichenListe();
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
          builder.aktenzeichenListe(
              aktenzeichenListe.toBuilder().aktenzeichen(aktenzeichen).build());
        }
      }

      // Fehlerhafte Gerichte
      if (!CollectionUtils.isEmpty(coreData.deviatingCourts())) {
        builder.fehlerhafteGerichte(
            FehlerhafteGerichte.builder()
                .fehlerhafteGerichte(
                    coreData.deviatingCourts().stream()
                        .map(
                            court ->
                                FehlerhafteGerichte.FehlerhaftesGericht.builder()
                                    .value(court)
                                    .build())
                        .toList())
                .build());
      }
      // Daten der mündlichen Verhandlung
      if (!CollectionUtils.isEmpty(coreData.oralHearingDates())) {
        builder.datenDerMuendlichenVerhandlung(
            DatenDerMuendlichenVerhandlung.builder()
                .daten(
                    coreData.oralHearingDates().stream()
                        .map(
                            date ->
                                DatenDerMuendlichenVerhandlung.DatumDerMuendlichenVerhandlung
                                    .builder()
                                    .value(DateUtils.toDateString(date))
                                    .build())
                        .toList())
                .build());
      }

      // Abweichende Daten
      if (!CollectionUtils.isEmpty(coreData.deviatingDecisionDates())) {
        builder.abweichendeDaten(
            AbweichendeDaten.builder()
                .daten(
                    coreData.deviatingDecisionDates().stream()
                        .map(
                            date ->
                                AbweichendeDaten.AbweichendesDatum.builder()
                                    .value(DateUtils.toDateString(date))
                                    .build())
                        .toList())
                .build());
      }
      // Abweichende Dokumentnummern
      if (!CollectionUtils.isEmpty(coreData.deviatingDocumentNumbers())) {
        builder.abweichendeDokumentnummern(
            AbweichendeDokumentnummern.builder()
                .dokumentnummern(
                    coreData.deviatingDocumentNumbers().stream()
                        .map(
                            docNumber ->
                                AbweichendeDokumentnummern.AbweichendeDokumentnummer.builder()
                                    .value(docNumber)
                                    .build())
                        .toList())
                .build());
      }
      // Abweichende ECLIs
      if (!CollectionUtils.isEmpty(coreData.deviatingEclis())) {
        var abweichendeEclis =
            coreData.deviatingEclis().stream()
                .map(ecli -> AbweichendeEclis.AbweichenderEcli.builder().value(ecli).build())
                .toList();
        builder.abweichendeEclis(AbweichendeEclis.builder().eclis(abweichendeEclis).build());
      }

      // Rechtskraft (map from legalEffect yes/no)
      if (coreData.legalEffect() != null) {
        String legalEffect = coreData.legalEffect();
        builder.rechtskraft(Rechtskraft.builder().value(legalEffect).build());
      }

      // Vorgänge (previous procedures + current label)
      if (coreData.procedure() != null) {
        List<String> all = new ArrayList<>();
        all.add(coreData.procedure().label());
        if (coreData.previousProcedures() != null) {
          all.addAll(coreData.previousProcedures());
        }
        builder.vorgaenge(
            Vorgaenge.builder()
                .vorgaenge(
                    all.stream().map(v -> Vorgaenge.Vorgang.builder().value(v).build()).toList())
                .build());
      }

      // Eingangsarten
      if (!CollectionUtils.isEmpty(coreData.inputTypes())) {
        builder.eingangsarten(
            Eingangsarten.builder()
                .eingangsarten(
                    coreData.inputTypes().stream()
                        .map(
                            eingangsart ->
                                Eingangsarten.Eingangsart.builder().content(eingangsart).build())
                        .toList())
                .build());
      }
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
      buildHeadline(paragraphs, headline, htmlTransformer);
    }

    return Header.builder().paragraphs(paragraphs).build();
  }

  @Override
  public boolean isFullLDML() {
    return true;
  }
}
