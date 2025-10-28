package de.bund.digitalservice.ris.caselaw.adapter.transformer.ldml.decision;

import static de.bund.digitalservice.ris.caselaw.adapter.MappingUtils.nullSafeGet;
import static de.bund.digitalservice.ris.caselaw.adapter.MappingUtils.validateNotNull;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.CaseLawLdml;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.JaxbHtml;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.Judgment;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.header.Header;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.judgementbody.Block;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.judgementbody.Introduction;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.judgementbody.JudgmentBody;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.judgementbody.Motivation;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.judgementbody.Opinion;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.meta.Meta;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.meta.proprietary.AktenzeichenListe;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.meta.proprietary.DokumentTyp;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.meta.proprietary.Dokumentationsstelle;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.meta.proprietary.Gericht;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.meta.proprietary.Regionen;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.meta.proprietary.RisMeta;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.meta.proprietary.Spruchkoerper;
import de.bund.digitalservice.ris.caselaw.adapter.exception.LdmlTransformationException;
import de.bund.digitalservice.ris.caselaw.adapter.transformer.ldml.DocumentationUnitLdmlTransformer;
import de.bund.digitalservice.ris.caselaw.adapter.transformer.ldml.HtmlTransformer;
import de.bund.digitalservice.ris.caselaw.domain.Decision;
import de.bund.digitalservice.ris.caselaw.domain.LongTexts;
import de.bund.digitalservice.ris.caselaw.domain.ShortTexts;
import de.bund.digitalservice.ris.caselaw.domain.court.Court;
import jakarta.xml.bind.ValidationException;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.DocumentBuilderFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

/**
 * Abstract base class for transforming decisions into LDML case law format. Provides common
 * transformation logic and helper methods.
 */
@Slf4j
public abstract class DecisionCommonLdmlTransformer
    implements DocumentationUnitLdmlTransformer<Decision> {

  protected final HtmlTransformer htmlTransformer;

  protected DecisionCommonLdmlTransformer(DocumentBuilderFactory documentBuilderFactory) {
    this.htmlTransformer = new HtmlTransformer(documentBuilderFactory);
  }

  public CaseLawLdml transformToLdml(Decision decision) {
    try {
      validateCoreData(decision);
      validateNotNull(decision.documentNumber(), "Unique identifier missing");
      validateNotNull(decision.uuid(), "Caselaw UUID missing");

      return CaseLawLdml.builder().judgment(buildJudgment(decision)).build();
    } catch (ValidationException e) {
      if (e.getMessage().contains("Empty judgment body")) {
        throw new LdmlTransformationException("Missing judgment body.", e);
      }
      throw new LdmlTransformationException("LDML validation failed.", e);
    }
  }

  private Judgment buildJudgment(Decision decision) throws ValidationException {
    var judgmentBuilder =
        Judgment.builder()
            .header(buildHeader(decision))
            .meta(buildMeta(decision))
            .judgmentBody(buildJudgmentBody(decision));
    if (decision.coreData() != null && decision.coreData().documentType() != null) {
      judgmentBuilder.name(decision.coreData().documentType().label());
    }
    return judgmentBuilder.build();
  }

  protected abstract Header buildHeader(Decision decision);

  protected abstract Meta buildMeta(Decision decision);

  @SuppressWarnings("java:S3776")
  protected RisMeta.RisMetaBuilder buildCommonRisMeta(Decision decision) {
    RisMeta.RisMetaBuilder builder = RisMeta.builder();

    var coreData = decision.coreData();
    if (coreData != null) {
      // Dokumenttyp
      builder.dokumentTyp(
          DokumentTyp.builder().eId("dokumenttyp").value(coreData.documentType().label()).build());

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
      if (coreData.court() != null && coreData.court().regions() != null) {
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

      // Aktenzeichenliste (incl. abweichende Aktenzeichen)
      List<AktenzeichenListe.Aktenzeichen> aktenzeichenListe = new ArrayList<>();
      if (coreData.fileNumbers() != null && !coreData.fileNumbers().isEmpty()) {
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

      // Spruchkörper
      if (coreData.appraisalBody() != null) {
        builder.spruchkoerper(
            Spruchkoerper.builder()
                .refersTo("#spruchkoerper")
                .value(coreData.appraisalBody())
                .build());
      }
    }

    return builder;
  }

  private JudgmentBody buildJudgmentBody(Decision decision) throws ValidationException {
    JudgmentBody.JudgmentBodyBuilder builder = JudgmentBody.builder();

    builder
        .motivations(buildMotivations(decision))
        .introductions(buildIntroductions(decision))
        .background(buildBackground(decision))
        .decision(buildDecision(decision));

    var judgmentBody = builder.build();

    if (judgmentBody.getIntroductions().isEmpty()
        && judgmentBody.getBackground() == null
        && judgmentBody.getDecision() == null
        && judgmentBody.getMotivations().isEmpty()) {
      throw new ValidationException("Empty judgment body");
    }

    return judgmentBody;
  }

  protected List<Introduction> buildIntroductions(Decision decision) {
    List<Introduction> introductions = new ArrayList<>();

    var longTexts = decision.longTexts();
    var shortTexts = decision.shortTexts();
    var guidingPrinciple = nullSafeGet(shortTexts, ShortTexts::guidingPrinciple);
    var outline = nullSafeGet(longTexts, LongTexts::outline);

    // Leitsatz
    if (isNotBlank(guidingPrinciple)) {
      var introduction =
          Introduction.builder()
              .content(htmlTransformer.htmlStringToObjectList(shortTexts.guidingPrinciple()))
              .build();
      introduction.setDomainTerm("Leitsatz");
      introductions.add(introduction);
    }

    // Gliederung
    if (isNotBlank(outline)) {
      var introduction =
          Introduction.builder()
              .content(htmlTransformer.htmlStringToObjectList(longTexts.outline()))
              .build();
      introduction.setDomainTerm("Gliederung");
      introductions.add(introduction);
    }
    return introductions;
  }

  private List<Motivation> buildMotivations(Decision decision) {
    List<Motivation> motivations = new ArrayList<>();

    var longTexts = decision.longTexts();
    var reasons = nullSafeGet(longTexts, LongTexts::reasons);
    var decisionReasons = nullSafeGet(longTexts, LongTexts::decisionReasons);
    var otherLongTexts = nullSafeGet(longTexts, LongTexts::otherLongText);
    var dissentingOpinion = nullSafeGet(longTexts, LongTexts::dissentingOpinion);

    // Gründe
    if (isNotBlank(reasons)) {
      var motivation =
          Motivation.builder()
              .content(htmlTransformer.htmlStringToObjectList(longTexts.reasons()))
              .build();
      motivation.setDomainTerm("Gründe");
      motivations.add(motivation);
    }

    // Entscheidungsgründe
    if (isNotBlank(decisionReasons)) {
      var motivation =
          Motivation.builder()
              .content(htmlTransformer.htmlStringToObjectList(longTexts.decisionReasons()))
              .build();
      motivation.setDomainTerm("Entscheidungsgründe");
      motivations.add(motivation);
    }

    // Sonstiger Langtext
    if (isNotBlank(otherLongTexts)) {
      var motivation =
          Motivation.builder()
              .content(htmlTransformer.htmlStringToObjectList(longTexts.otherLongText()))
              .build();
      motivation.setDomainTerm("Sonstiger Langtext");
      motivations.add(motivation);
    }

    // Abweichende Meinung + Mitwirkende Richter
    if (isNotBlank(dissentingOpinion)) {
      var motivation = buildDissentingOpinion(decision);
      motivation.setDomainTerm("Abweichende Meinung");
      motivations.add(motivation);
    }
    return motivations;
  }

  private Motivation buildDissentingOpinion(Decision decision) {
    var longTexts = decision.longTexts();
    var dissentingOpinion = nullSafeGet(longTexts, LongTexts::dissentingOpinion);
    var participatingJudges = nullSafeGet(longTexts, LongTexts::participatingJudges);

    List<Opinion> opinions = new ArrayList<>();

    if (participatingJudges != null && !participatingJudges.isEmpty()) {
      for (var judge : participatingJudges) {
        String byAttribute = "#" + toKebabCase(judge.name());
        Opinion opinion;
        if (judge.referencedOpinions() != null) {
          opinion =
              Opinion.builder()
                  .type("dissenting")
                  .by(byAttribute)
                  .content(judge.referencedOpinions())
                  .build();
          opinion.setDomainTerm("Art der Mitwirkung");
        } else {
          opinion = Opinion.builder().by(byAttribute).build();
        }
        opinions.add(opinion);
      }
    }

    Block block = null;
    if (!opinions.isEmpty()) {
      block = new Block("Mitwirkende Richter", opinions);
    }

    List<Object> content =
        new ArrayList<>(htmlTransformer.htmlStringToObjectList(dissentingOpinion));

    if (block != null) {
      content.add(block);
    }

    Motivation motivation = new Motivation();
    motivation.setDomainTerm("Abweichende Meinung");
    motivation.setContent(content);

    return motivation;
  }

  private JaxbHtml buildBackground(Decision decision) {
    var longTexts = decision.longTexts();
    var caseFacts = nullSafeGet(longTexts, LongTexts::caseFacts);

    if (isNotBlank(caseFacts)) {
      JaxbHtml html = JaxbHtml.build(htmlTransformer.htmlStringToObjectList(caseFacts));
      html.setDomainTerm("Tatbestand");
      return html;
    }
    return null;
  }

  private JaxbHtml buildDecision(Decision decision) {
    var longTexts = decision.longTexts();
    var tenor = nullSafeGet(longTexts, LongTexts::tenor);

    if (isNotBlank(tenor)) {
      var tenorHtml = JaxbHtml.build(htmlTransformer.htmlStringToObjectList(tenor));
      tenorHtml.setDomainTerm("Tenor");
      return tenorHtml;
    }
    return null;
  }

  protected String nullIfEmpty(String input) {
    if (StringUtils.isEmpty(input)) {
      return null;
    }
    return input;
  }
}
