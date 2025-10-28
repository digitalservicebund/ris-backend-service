package de.bund.digitalservice.ris.caselaw.adapter.transformer.ldml.decision;

import static de.bund.digitalservice.ris.caselaw.adapter.MappingUtils.nullSafeGet;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import de.bund.digitalservice.ris.caselaw.adapter.DateUtils;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.header.DocTitle;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.header.Header;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.header.Paragraph;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.meta.Classification;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.meta.Keyword;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.meta.Meta;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.meta.identification.FrbrLanguage;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.meta.proprietary.AbweichendeDaten;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.meta.proprietary.AbweichendeDokumentnummern;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.meta.proprietary.AbweichendeEclis;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.meta.proprietary.AktenzeichenListe;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.meta.proprietary.Definition;
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
import javax.xml.parsers.DocumentBuilderFactory;
import lombok.extern.slf4j.Slf4j;

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
        .proprietary(Proprietary.builder().meta(buildRisMeta(decision)).build())
        .build();
  }

  @SuppressWarnings({"java:S6541", "java:S3776"})
  private RisMeta buildRisMeta(Decision decision) {
    var builder = buildCommonRisMeta(decision);

    var contentRelatedIndexing = decision.contentRelatedIndexing();
    if (contentRelatedIndexing != null) {
      // Sachgebiete
      if (contentRelatedIndexing.fieldsOfLaw() != null) {
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
      if (contentRelatedIndexing.definitions() != null) {
        var defs =
            contentRelatedIndexing.definitions().stream()
                .map(
                    definition ->
                        Definition.builder()
                            .definierterBegriff(
                                Definition.DefinierterBegriff.builder()
                                    .value(definition.definedTerm())
                                    .build())
                            .definierendeRandnummer(
                                definition.definingBorderNumber() == null
                                    ? null
                                    : Definition.DefinierendeRandnummer.builder()
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
      if (contentRelatedIndexing.foreignLanguageVersions() != null
          && !contentRelatedIndexing.foreignLanguageVersions().isEmpty()) {
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

      if (coreData.deviatingFileNumbers() != null) {
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
      if (coreData.deviatingCourts() != null) {
        builder.fehlerhafteGerichte(
            FehlerhafteGerichte.builder()
                .fehlerhafteGerichte(
                    coreData.deviatingCourts().stream()
                        .map(
                            v -> FehlerhafteGerichte.FehlerhaftesGericht.builder().value(v).build())
                        .toList())
                .build());
      }
      // Abweichende Daten
      if (coreData.deviatingDecisionDates() != null) {
        builder.abweichendeDaten(
            AbweichendeDaten.builder()
                .daten(
                    coreData.deviatingDecisionDates().stream()
                        .map(
                            d ->
                                AbweichendeDaten.AbweichendesDatum.builder()
                                    .value(DateUtils.toDateString(d))
                                    .build())
                        .toList())
                .build());
      }
      // Abweichende Dokumentnummern
      if (coreData.deviatingDocumentNumbers() != null) {
        builder.abweichendeDokumentnummern(
            AbweichendeDokumentnummern.builder()
                .dokumentnummern(
                    coreData.deviatingDocumentNumbers().stream()
                        .map(
                            v ->
                                AbweichendeDokumentnummern.AbweichendeDokumentnummer.builder()
                                    .value(v)
                                    .build())
                        .toList())
                .build());
      }
      // Abweichende ECLIs
      if (coreData.deviatingEclis() != null && !coreData.deviatingEclis().isEmpty()) {
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
      if (coreData.inputTypes() != null) {
        builder.eingangsarten(
            Eingangsarten.builder()
                .eingangsarten(
                    coreData.inputTypes().stream()
                        .map(v -> Eingangsarten.Eingangsart.builder().content(v).build())
                        .toList())
                .build());
      }
    }

    return builder.build();
  }

  @Override
  protected Header buildHeader(Decision decision) {
    List<Paragraph> paragraphs = new ArrayList<>();

    paragraphs = buildCommonHeader(decision, paragraphs);
    var shortTexts = decision.shortTexts();
    var decisionNames = nullSafeGet(shortTexts, ShortTexts::decisionNames);
    var headline = nullSafeGet(shortTexts, ShortTexts::headline);

    // Entscheidungsname
    if (decisionNames != null && !decisionNames.isEmpty()) {
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
}
