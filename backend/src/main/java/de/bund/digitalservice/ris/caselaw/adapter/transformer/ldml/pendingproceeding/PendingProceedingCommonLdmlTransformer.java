package de.bund.digitalservice.ris.caselaw.adapter.transformer.ldml.pendingproceeding;

import static de.bund.digitalservice.ris.caselaw.adapter.MappingUtils.applyIfNotEmpty;
import static de.bund.digitalservice.ris.caselaw.adapter.MappingUtils.nullSafeGet;
import static de.bund.digitalservice.ris.caselaw.adapter.MappingUtils.validateNotNull;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.CaseLawLdml;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.JaxbHtml;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.Judgment;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.header.Header;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.judgementbody.Introduction;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.judgementbody.JudgmentBody;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.judgementbody.Motivation;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.meta.Meta;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.meta.proprietary.AktenzeichenListe;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.meta.proprietary.DokumentTyp;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.meta.proprietary.Dokumentationsstelle;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.meta.proprietary.Gericht;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.meta.proprietary.Regionen;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.meta.proprietary.RisMeta;
import de.bund.digitalservice.ris.caselaw.adapter.exception.LdmlTransformationException;
import de.bund.digitalservice.ris.caselaw.adapter.transformer.ldml.DocumentationUnitLdmlTransformer;
import de.bund.digitalservice.ris.caselaw.adapter.transformer.ldml.HtmlTransformer;
import de.bund.digitalservice.ris.caselaw.domain.PendingProceeding;
import de.bund.digitalservice.ris.caselaw.domain.PendingProceedingShortTexts;
import de.bund.digitalservice.ris.caselaw.domain.court.Court;
import jakarta.xml.bind.ValidationException;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.DocumentBuilderFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;

/**
 * Abstract base class for transforming pending proceedings into LDML case law format. Provides
 * common transformation logic and helper methods.
 */
@Slf4j
public abstract class PendingProceedingCommonLdmlTransformer
    implements DocumentationUnitLdmlTransformer<PendingProceeding> {

  protected final HtmlTransformer htmlTransformer;

  protected PendingProceedingCommonLdmlTransformer(DocumentBuilderFactory documentBuilderFactory) {
    this.htmlTransformer = new HtmlTransformer(documentBuilderFactory);
  }

  public CaseLawLdml transformToLdml(PendingProceeding pendingProceeding) {
    try {
      validateCoreData(pendingProceeding);
      validateNotNull(pendingProceeding.documentNumber(), "Unique identifier missing");
      validateNotNull(pendingProceeding.uuid(), "Caselaw UUID missing");

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
        .name(pendingProceeding.coreData().documentType().label())
        .header(buildHeader(pendingProceeding))
        .meta(buildMeta(pendingProceeding))
        .judgmentBody(buildJudgmentBody(pendingProceeding))
        .build();
  }

  protected abstract Header buildHeader(PendingProceeding pendingProceeding);

  @SuppressWarnings("java:S3776")
  protected RisMeta.RisMetaBuilder buildCommonRisMeta(PendingProceeding pendingProceeding) {
    RisMeta.RisMetaBuilder builder = RisMeta.builder();

    if (!CollectionUtils.isEmpty(pendingProceeding.previousDecisions())) {
      applyIfNotEmpty(
          buildRelatedDecisions(pendingProceeding.previousDecisions()), builder::previousDecision);
    }

    var coreData = pendingProceeding.coreData();
    if (coreData != null) {
      // Dokumenttyp
      builder.dokumentTyp(DokumentTyp.builder().value(coreData.documentType().label()).build());

      // Gericht (Gerichtstyp + Ort)
      Court court = coreData.court();
      if (court != null) {
        builder.gericht(
            Gericht.builder()
                .refersTo("#gericht")
                .typ(Gericht.GerichtTyp.builder().value(court.type()).build())
                .ort(Gericht.GerichtOrt.builder().value(court.location()).build())
                .build());
      }

      // Regionen
      List<Regionen.Region> regionen = new ArrayList<>();
      if (coreData.court() != null && !CollectionUtils.isEmpty(coreData.court().regions())) {
        coreData
            .court()
            .regions()
            .forEach(region -> regionen.add(Regionen.Region.builder().value(region).build()));
      }
      if (!regionen.isEmpty()) {
        builder.regionen(Regionen.builder().regionen(regionen).build());
      }

      // Dokumentationsstelle
      if (coreData.documentationOffice() != null) {
        String docOffice = coreData.documentationOffice().abbreviation();
        builder.dokumentationsstelle(
            Dokumentationsstelle.builder()
                .refersTo("#" + docOffice.toLowerCase() + "-dokumentationsstelle")
                .value(docOffice)
                .build());
      }

      // Aktenzeichenliste
      List<AktenzeichenListe.Aktenzeichen> aktenzeichenListe = new ArrayList<>();
      if (!CollectionUtils.isEmpty(coreData.fileNumbers())) {
        coreData
            .fileNumbers()
            .forEach(
                fileNumber ->
                    aktenzeichenListe.add(
                        AktenzeichenListe.Aktenzeichen.builder()
                            .refersTo("#aktenzeichen")
                            .value(fileNumber)
                            .build()));
      }
      if (!aktenzeichenListe.isEmpty()) {
        builder.aktenzeichenListe(
            AktenzeichenListe.builder().aktenzeichen(aktenzeichenListe).build());
      }
    }

    return builder;
  }

  private JudgmentBody buildJudgmentBody(PendingProceeding pendingProceeding)
      throws ValidationException {
    JudgmentBody.JudgmentBodyBuilder builder = JudgmentBody.builder();

    builder
        .motivations(buildMotivations(pendingProceeding))
        .introductions(buildIntroductions(pendingProceeding))
        .background(null)
        .decision(buildDecision(pendingProceeding));

    var judgmentBody = builder.build();

    if (judgmentBody.getIntroductions().isEmpty()
        && judgmentBody.getBackground() == null
        && judgmentBody.getDecision() == null
        && judgmentBody.getMotivations() == null) {
      throw new ValidationException("Empty judgment body");
    }

    return judgmentBody;
  }

  protected List<Introduction> buildIntroductions(PendingProceeding pendingProceeding) {
    List<Introduction> introductions = new ArrayList<>();
    var shortTexts = pendingProceeding.shortTexts();
    var appellant = nullSafeGet(shortTexts, PendingProceedingShortTexts::appellant);
    var admissionOfAppeal = nullSafeGet(shortTexts, PendingProceedingShortTexts::admissionOfAppeal);

    // Rechtsmittelführer
    if (isNotBlank(appellant)) {
      var introduction =
          Introduction.builder()
              .content(htmlTransformer.htmlStringToObjectList(shortTexts.appellant()))
              .build();
      introduction.setDomainTerm("Rechtsmittelführer");
      introductions.add(introduction);
    }

    // Rechtsmittelzulassung
    if (isNotBlank(admissionOfAppeal)) {
      var introduction =
          Introduction.builder()
              .content(htmlTransformer.htmlStringToObjectList(shortTexts.admissionOfAppeal()))
              .build();
      introduction.setDomainTerm("Rechtsmittelzulassung");
      introductions.add(introduction);
    }
    return introductions;
  }

  private List<Motivation> buildMotivations(PendingProceeding pendingProceeding) {
    List<Motivation> motivations = new ArrayList<>();
    var shortTexts = pendingProceeding.shortTexts();
    var legalIssue = nullSafeGet(shortTexts, PendingProceedingShortTexts::legalIssue);

    // Rechtsfrage
    if (isNotBlank(legalIssue)) {
      var motivation =
          Motivation.builder()
              .content(htmlTransformer.htmlStringToObjectList(shortTexts.legalIssue()))
              .build();
      motivation.setDomainTerm("Rechtsfrage");
      motivations.add(motivation);
    }
    return motivations;
  }

  private JaxbHtml buildDecision(PendingProceeding pendingProceeding) {
    var shortTexts = pendingProceeding.shortTexts();
    var resolutionNote = nullSafeGet(shortTexts, PendingProceedingShortTexts::resolutionNote);

    if (isNotBlank(resolutionNote)) {
      var resolutionNoteHtml =
          JaxbHtml.build(htmlTransformer.htmlStringToObjectList(resolutionNote));
      resolutionNoteHtml.setDomainTerm("Erledigungsvermerk");
      return resolutionNoteHtml;
    }
    return null;
  }

  protected abstract Meta buildMeta(PendingProceeding pendingProceeding);
}
