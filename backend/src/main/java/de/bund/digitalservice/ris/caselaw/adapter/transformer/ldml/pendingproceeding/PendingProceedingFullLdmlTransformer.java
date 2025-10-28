package de.bund.digitalservice.ris.caselaw.adapter.transformer.ldml.pendingproceeding;

import static de.bund.digitalservice.ris.caselaw.adapter.MappingUtils.nullSafeGet;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import de.bund.digitalservice.ris.caselaw.adapter.DateUtils;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.header.Header;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.header.Paragraph;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.meta.Classification;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.meta.Keyword;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.meta.Meta;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.meta.proprietary.AbweichendeDaten;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.meta.proprietary.AbweichendeDokumentnummern;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.meta.proprietary.AktenzeichenListe;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.meta.proprietary.FehlerhafteGerichte;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.meta.proprietary.Proprietary;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.meta.proprietary.RisMeta;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.meta.proprietary.Sachgebiete;
import de.bund.digitalservice.ris.caselaw.domain.PendingProceeding;
import de.bund.digitalservice.ris.caselaw.domain.PendingProceedingShortTexts;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.xml.parsers.DocumentBuilderFactory;
import lombok.extern.slf4j.Slf4j;

/** Transformer for converting pending proceedings to full LDML format. */
@Slf4j
public class PendingProceedingFullLdmlTransformer extends PendingProceedingCommonLdmlTransformer {

  public PendingProceedingFullLdmlTransformer(DocumentBuilderFactory documentBuilderFactory) {
    super(documentBuilderFactory);
  }

  @Override
  protected Meta buildMeta(PendingProceeding pendingProceeding) {
    Meta.MetaBuilder builder = Meta.builder();

    List<Keyword> keywords =
        pendingProceeding.contentRelatedIndexing() == null
            ? Collections.emptyList()
            : pendingProceeding.contentRelatedIndexing().keywords().stream()
                .map(Keyword::new)
                .toList();

    if (!keywords.isEmpty()) {
      builder.classification(Classification.builder().keyword(keywords).build());
    }

    return builder
        .identification(buildIdentification(pendingProceeding))
        .references(buildReferences(pendingProceeding))
        .proprietary(Proprietary.builder().meta(buildRisMeta(pendingProceeding)).build())
        .build();
  }

  private RisMeta buildRisMeta(PendingProceeding pendingProceeding) {
    var builder = buildCommonRisMeta(pendingProceeding);

    var contentRelatedIndexing = pendingProceeding.contentRelatedIndexing();
    if (contentRelatedIndexing != null && contentRelatedIndexing.fieldsOfLaw() != null) {
      Sachgebiete sachgebiete =
          Sachgebiete.builder()
              .sachgebiete(
                  contentRelatedIndexing.fieldsOfLaw().stream()
                      .map(
                          sachgebiet ->
                              Sachgebiete.Sachgebiet.builder()
                                  .value(sachgebiet.text())
                                  // FIXME: Notation is not yet part of the domain object
                                  .notation("neu")
                                  .build())
                      .toList())
              .build();
      builder.sachgebiete(sachgebiete);
    }

    var coreData = pendingProceeding.coreData();
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
    }

    return builder.build();
  }

  @Override
  protected Header buildHeader(PendingProceeding pendingProceeding) {
    List<Paragraph> paragraphs = new ArrayList<>();

    paragraphs = buildCommonHeader(pendingProceeding, paragraphs);
    var shortTexts = pendingProceeding.shortTexts();
    var headline = nullSafeGet(shortTexts, PendingProceedingShortTexts::headline);

    if (isNotBlank(headline)) {
      buildHeadline(paragraphs, headline, htmlTransformer);
    }

    return Header.builder().paragraphs(paragraphs).build();
  }
}
